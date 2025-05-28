package com.example.nlwebspringai.mcp.service;

import com.example.nlwebspringai.mcp.client.PlaywrightMcpClient;
import com.example.nlwebspringai.mcp.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaywrightMcpServiceTest {

    @Mock
    private PlaywrightMcpClient mockPlaywrightMcpClient;

    @InjectMocks
    private PlaywrightMcpService playwrightMcpService;

    @Test
    void navigate_shouldCallClientWithNavigateRequest() {
        String testUrl = "http://example.com";
        McpResponse mockResponse = new McpResponse();
        mockResponse.setType("ack");
        when(mockPlaywrightMcpClient.sendCommand(any(NavigateRequest.class))).thenReturn(Flux.just(mockResponse));

        Flux<McpResponse> resultFlux = playwrightMcpService.navigate(testUrl);

        StepVerifier.create(resultFlux)
                .expectNext(mockResponse)
                .verifyComplete();

        verify(mockPlaywrightMcpClient).sendCommand(any(NavigateRequest.class));
    }

    @Test
    void takeSnapshot_shouldCallClientWithSnapshotRequest() {
        McpResponse mockResponse = new McpResponse();
        mockResponse.setType("snapshot");
        when(mockPlaywrightMcpClient.sendCommand(any(SnapshotRequest.class))).thenReturn(Flux.just(mockResponse));

        Flux<McpResponse> resultFlux = playwrightMcpService.takeSnapshot();

        StepVerifier.create(resultFlux)
                .expectNext(mockResponse)
                .verifyComplete();

        verify(mockPlaywrightMcpClient).sendCommand(any(SnapshotRequest.class));
    }

    @Test
    void clickElement_shouldCallClientWithClickRequest() {
        String ref = "element123";
        String description = "Login button";
        McpResponse mockResponse = new McpResponse();
        mockResponse.setType("ack");
        when(mockPlaywrightMcpClient.sendCommand(any(ClickRequest.class))).thenReturn(Flux.just(mockResponse));

        Flux<McpResponse> resultFlux = playwrightMcpService.clickElement(ref, description);

        StepVerifier.create(resultFlux)
                .expectNext(mockResponse)
                .verifyComplete();

        verify(mockPlaywrightMcpClient).sendCommand(any(ClickRequest.class));
    }

    @Test
    void typeInElement_shouldCallClientWithTypeRequest() {
        String ref = "inputField456";
        String description = "Username input";
        String text = "testuser";
        boolean submit = false;
        McpResponse mockResponse = new McpResponse();
        mockResponse.setType("ack");
        when(mockPlaywrightMcpClient.sendCommand(any(TypeRequest.class))).thenReturn(Flux.just(mockResponse));

        Flux<McpResponse> resultFlux = playwrightMcpService.typeInElement(ref, description, text, submit);

        StepVerifier.create(resultFlux)
                .expectNext(mockResponse)
                .verifyComplete();

        verify(mockPlaywrightMcpClient).sendCommand(any(TypeRequest.class));
    }
}
