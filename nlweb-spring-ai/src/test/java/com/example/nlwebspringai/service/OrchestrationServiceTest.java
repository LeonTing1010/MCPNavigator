package com.example.nlwebspringai.service;

import com.example.nlwebspringai.mcp.model.McpResponse;
import com.example.nlwebspringai.mcp.service.PlaywrightMcpService;
import com.example.nlwebspringai.model.McpCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class OrchestrationServiceTest {

    @Mock
    private NlWebService mockNlWebService;

    @Mock
    private PlaywrightMcpService mockPlaywrightMcpService;

    @InjectMocks
    private OrchestrationService orchestrationService;

    @Test
    void processNaturalLanguageCommand_navigateAction_shouldCallPlaywrightNavigate() {
        String query = "go to example.com";
        String url = "http://example.com";
        McpCommand mcpCommand = new McpCommand("navigate", url, null);
        McpResponse mockMcpResponse = new McpResponse();
        mockMcpResponse.setType("ack");

        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);
        when(mockPlaywrightMcpService.navigate(url)).thenReturn(Flux.just(mockMcpResponse));

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectNext(mockMcpResponse)
                .verifyComplete();

        verify(mockNlWebService).processQuery(query);
        verify(mockPlaywrightMcpService).navigate(url);
    }

    @Test
    void processNaturalLanguageCommand_snapshotAction_shouldCallPlaywrightSnapshot() {
        String query = "take a snapshot";
        McpCommand mcpCommand = new McpCommand("snapshot", null, null);
        McpResponse mockMcpResponse = new McpResponse();
        mockMcpResponse.setType("snapshot");


        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);
        when(mockPlaywrightMcpService.takeSnapshot()).thenReturn(Flux.just(mockMcpResponse));

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectNext(mockMcpResponse)
                .verifyComplete();
        
        verify(mockNlWebService).processQuery(query);
        verify(mockPlaywrightMcpService).takeSnapshot();
    }

    @Test
    void processNaturalLanguageCommand_clickAction_shouldCallPlaywrightClick() {
        String query = "click the login button";
        String ref = "button-ref-123";
        String elementDesc = "Login Button";
        McpCommand mcpCommand = new McpCommand("click", ref, Map.of("elementDescription", elementDesc));
        McpResponse mockMcpResponse = new McpResponse();
        mockMcpResponse.setType("ack");

        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);
        when(mockPlaywrightMcpService.clickElement(ref, elementDesc)).thenReturn(Flux.just(mockMcpResponse));

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectNext(mockMcpResponse)
                .verifyComplete();
        
        verify(mockNlWebService).processQuery(query);
        verify(mockPlaywrightMcpService).clickElement(ref, elementDesc);
    }

    @Test
    void processNaturalLanguageCommand_typeAction_shouldCallPlaywrightType() {
        String query = "type 'hello' into the search bar";
        String ref = "input-ref-456";
        String elementDesc = "Search Bar";
        String text = "hello";
        boolean submit = false;
        Map<String, Object> params = Map.of("elementDescription", elementDesc, "text", text, "submit", submit);
        McpCommand mcpCommand = new McpCommand("type", ref, params);
        McpResponse mockMcpResponse = new McpResponse();
        mockMcpResponse.setType("ack");

        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);
        when(mockPlaywrightMcpService.typeInElement(ref, elementDesc, text, submit)).thenReturn(Flux.just(mockMcpResponse));

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectNext(mockMcpResponse)
                .verifyComplete();
        
        verify(mockNlWebService).processQuery(query);
        verify(mockPlaywrightMcpService).typeInElement(ref, elementDesc, text, submit);
    }
    
    @Test
    void processNaturalLanguageCommand_typeActionWithSubmitAsString_shouldCallPlaywrightType() {
        String query = "type 'hello' and submit";
        String ref = "input-ref-789";
        String elementDesc = "Search Form";
        String text = "hello";
        // NLWeb might send boolean as string
        Map<String, Object> params = Map.of("elementDescription", elementDesc, "text", text, "submit", "true");
        McpCommand mcpCommand = new McpCommand("type", ref, params);
        McpResponse mockMcpResponse = new McpResponse();
        mockMcpResponse.setType("ack");

        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);
        when(mockPlaywrightMcpService.typeInElement(ref, elementDesc, text, true)).thenReturn(Flux.just(mockMcpResponse));

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectNext(mockMcpResponse)
                .verifyComplete();
        
        verify(mockNlWebService).processQuery(query);
        verify(mockPlaywrightMcpService).typeInElement(ref, elementDesc, text, true);
    }


    @Test
    void processNaturalLanguageCommand_unknownAction_shouldReturnFluxError() {
        String query = "do something strange";
        McpCommand mcpCommand = new McpCommand("unknown_action", "target", null);

        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
        
        verify(mockNlWebService).processQuery(query);
        verify(mockPlaywrightMcpService, never()).navigate(anyString());
    }

    @Test
    void processNaturalLanguageCommand_nlWebServiceReturnsNull_shouldReturnFluxError() {
        String query = "a query that results in null from NlWebService";
        when(mockNlWebService.processQuery(query)).thenReturn(null);

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();
        verify(mockNlWebService).processQuery(query);
    }
    
    @Test
    void processNaturalLanguageCommand_nlWebServiceReturnsErrorCommand_shouldReturnFluxWithErrorResponse() {
        String query = "a query that results in error from NlWebService";
        McpCommand errorMcpCommand = new McpCommand("error", "nlweb_service", Map.of("errorMessage", "NL Service Error"));
        
        when(mockNlWebService.processQuery(query)).thenReturn(errorMcpCommand);

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
            .consumeNextWith(response -> {
                assertEquals("error", response.getType());
                assertTrue(response.getError().contains("NL Service Error"));
            })
            .verifyComplete();
        verify(mockNlWebService).processQuery(query);
    }

    @Test
    void processNaturalLanguageCommand_navigateWithNullTarget_shouldReturnFluxError() {
        String query = "navigate to nowhere";
        McpCommand mcpCommand = new McpCommand("navigate", null, null); // Null target
        when(mockNlWebService.processQuery(query)).thenReturn(mcpCommand);

        Flux<McpResponse> result = orchestrationService.processNaturalLanguageCommand(query);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
        verify(mockNlWebService).processQuery(query);
    }
}
