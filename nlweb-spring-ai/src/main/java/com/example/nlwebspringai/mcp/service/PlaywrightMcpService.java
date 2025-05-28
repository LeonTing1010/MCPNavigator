package com.example.nlwebspringai.mcp.service;

import com.example.nlwebspringai.mcp.client.PlaywrightMcpClient;
import com.example.nlwebspringai.mcp.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PlaywrightMcpService {

    private static final Logger logger = LoggerFactory.getLogger(PlaywrightMcpService.class);

    private final PlaywrightMcpClient playwrightMcpClient;

    public PlaywrightMcpService(PlaywrightMcpClient playwrightMcpClient) {
        this.playwrightMcpClient = playwrightMcpClient;
    }

    public Flux<McpResponse> navigate(String url) {
        logger.info("Service: Navigating to URL: {}", url);
        NavigateRequest request = new NavigateRequest(url);
        return playwrightMcpClient.sendCommand(request)
                .doOnError(e -> logger.error("Service: Error during navigate to {}", url, e))
                .doOnComplete(() -> logger.info("Service: Navigate command stream completed for URL: {}", url));
    }

    public Flux<McpResponse> takeSnapshot() {
        logger.info("Service: Taking snapshot");
        SnapshotRequest request = new SnapshotRequest();
        return playwrightMcpClient.sendCommand(request)
                .doOnError(e -> logger.error("Service: Error during takeSnapshot", e))
                .doOnComplete(() -> logger.info("Service: TakeSnapshot command stream completed"));
    }

    public Flux<McpResponse> clickElement(String ref, String elementDescription) {
        logger.info("Service: Clicking element with ref: {} (Description: {})", ref, elementDescription);
        ClickRequest request = new ClickRequest(ref, elementDescription);
        return playwrightMcpClient.sendCommand(request)
                .doOnError(e -> logger.error("Service: Error during clickElement for ref: {}", ref, e))
                .doOnComplete(() -> logger.info("Service: ClickElement command stream completed for ref: {}", ref));
    }

    public Flux<McpResponse> typeInElement(String ref, String elementDescription, String text, boolean submit) {
        logger.info("Service: Typing in element with ref: {} (Description: {}, Text: {}, Submit: {})", ref, elementDescription, text, submit);
        TypeRequest request = new TypeRequest(ref, elementDescription, text, submit);
        return playwrightMcpClient.sendCommand(request)
                .doOnError(e -> logger.error("Service: Error during typeInElement for ref: {}", ref, e))
                .doOnComplete(() -> logger.info("Service: TypeInElement command stream completed for ref: {}", ref));
    }
}
