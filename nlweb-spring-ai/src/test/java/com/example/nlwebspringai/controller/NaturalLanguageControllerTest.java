package com.example.nlwebspringai.controller;

import com.example.nlwebspringai.mcp.model.McpResponse;
import com.example.nlwebspringai.model.NlWebQueryRequest;
import com.example.nlwebspringai.service.OrchestrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec; // Import ResponseSpec
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier; // Import StepVerifier

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@WebFluxTest(NaturalLanguageController.class) // Using WebFluxTest as we are testing a reactive controller
class NaturalLanguageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrchestrationService mockOrchestrationService;

    @Autowired
    private ObjectMapper objectMapper; // For serializing request body

    @Test
    void processNaturalLanguageQuery_success_shouldReturnMcpResponseStream() throws JsonProcessingException {
        String query = "navigate to example.com";
        NlWebQueryRequest request = new NlWebQueryRequest(query);
        String commandId = UUID.randomUUID().toString();

        McpResponse response1 = new McpResponse();
        response1.setId(commandId);
        response1.setType("ack");

        McpResponse response2 = new McpResponse();
        response2.setId(commandId); // Should be same ID for stream related to one command
        response2.setType("snapshot");
        // response2.setData(...); // Optionally set data

        when(mockOrchestrationService.processNaturalLanguageCommand(query))
                .thenReturn(Flux.just(response1, response2));

        Flux<McpResponse> responseBodyFlux = webTestClient.post().uri("/api/v1/process-nl")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(request))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .returnResult(McpResponse.class).getResponseBody();

        StepVerifier.create(responseBodyFlux)
                .consumeNextWith(r1 -> {
                    assert r1.getId().equals(commandId);
                    assert r1.getType().equals("ack");
                })
                .consumeNextWith(r2 -> {
                    assert r2.getId().equals(commandId);
                    assert r2.getType().equals("snapshot");
                })
                .verifyComplete();
    }
    
    @Test
    void processNaturalLanguageQuery_emptyQuery_shouldReturnError() throws JsonProcessingException {
        NlWebQueryRequest request = new NlWebQueryRequest(""); // Empty query

        webTestClient.post().uri("/api/v1/process-nl")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(request))
                .accept(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_JSON) // Accept JSON for error response
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON) // Adjusted to Spring Boot's default for WebFlux
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request");
                // Removed assertion for jsonPath("$.message") as it's not reliably present by default
    }
    
    @Test
    void processNaturalLanguageQuery_orchestrationServiceError_shouldReturnErrorInStream() throws JsonProcessingException {
        String query = "test query that causes error";
        NlWebQueryRequest request = new NlWebQueryRequest(query);
        String errorMessage = "Service error";

        when(mockOrchestrationService.processNaturalLanguageCommand(query))
                .thenReturn(Flux.error(new RuntimeException(errorMessage)));

        webTestClient.post().uri("/api/v1/process-nl")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(request))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk() // The stream itself is OK, but it contains an error McpResponse
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(McpResponse.class)
                .hasSize(1)
                .value(responses -> {
                    McpResponse errResponse = responses.get(0);
                    assert errResponse.getType().equals("error");
                    assert errResponse.getError().contains("Failed to process query: " + errorMessage);
                });
    }
}
