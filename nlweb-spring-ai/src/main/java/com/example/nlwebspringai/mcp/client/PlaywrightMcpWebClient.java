package com.example.nlwebspringai.mcp.client;

import com.example.nlwebspringai.mcp.model.McpBaseRequest;
import com.example.nlwebspringai.mcp.model.McpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PlaywrightMcpWebClient implements PlaywrightMcpClient {

    private static final Logger logger = LoggerFactory.getLogger(PlaywrightMcpWebClient.class);
    private final WebClient webClient;

    public PlaywrightMcpWebClient(WebClient.Builder webClientBuilder,
                                  @Value("${playwright.mcp.sse.url:http://localhost:8931/sse}") String mcpSseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(mcpSseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();
        logger.info("PlaywrightMcpWebClient initialized with base URL: {}", mcpSseUrl);
    }

    @Override
    public Flux<McpResponse> sendCommand(McpBaseRequest command) {
        logger.info("Sending command to Playwright-MCP: ID={}, Command={}", command.getId(), command.getCommand());

        // According to Playwright-MCP documentation, the /sse endpoint is opened with a GET request.
        // The command payload is typically sent as a query parameter or path variable if needed at connection time,
        // or more commonly, commands are sent over an established WebSocket connection for bi-directional communication.
        // However, the subtask prompt states: "Assume for now it's a POST that initiates the SSE stream for that command."
        // This is unusual for SSE. Standard SSE is GET. If it were a POST to *initiate* an SSE stream based on that POST body,
        // the server would have to somehow tie that POST to the subsequent GET that actually establishes the SSE.
        // A more common pattern for sending commands and receiving SSE is:
        // 1. Client sends command via POST to a standard REST endpoint (e.g., /command).
        // 2. Server acknowledges and provides a stream ID or URL.
        // 3. Client connects to an SSE endpoint (e.g., /events/{streamId}) via GET to receive updates.

        // Given the constraint "Assume for now it's a POST that initiates the SSE stream",
        // this implies the POST body itself is the command and the response is an SSE stream.
        // WebClient's `post().retrieve().bodyToFlux(McpResponse.class)` would expect a normal HTTP response body
        // that is a collection convertible to Flux, not a continuous SSE stream from a POST.
        //
        // For a true SSE stream initiated by a client request (typically GET), you'd use:
        // this.webClient.get().uri("/?command="+command.getCommand()+"&id="+command.getId())...retrieve().bodyToFlux(McpResponse.class)
        //
        // If the POST must *return* an SSE stream directly (non-standard but possible),
        // the server-side would need specific handling. The client side with WebClient
        // would still expect the Content-Type of the response to be text/event-stream.

        // Let's proceed with the assumption that the POST request's response *is* the event stream.
        return this.webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(command))
                .retrieve()
                .bodyToFlux(McpResponse.class)
                .doOnSubscribe(subscription -> logger.info("Subscribed to Playwright-MCP command stream for request ID: {}", command.getId()))
                .doOnNext(response -> logger.debug("Received MCP response: ID={}, Type={}", response.getId(), response.getType()))
                .doOnError(error -> logger.error("Error in Playwright-MCP command stream for request ID: {}", command.getId(), error))
                .doOnComplete(() -> logger.info("Playwright-MCP command stream completed for request ID: {}", command.getId()));
    }
}
