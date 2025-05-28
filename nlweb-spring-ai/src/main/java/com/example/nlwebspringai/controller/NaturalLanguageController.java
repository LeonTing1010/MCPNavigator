package com.example.nlwebspringai.controller;

import com.example.nlwebspringai.mcp.model.McpResponse;
import com.example.nlwebspringai.model.NlWebQueryRequest; // Using existing DTO
import com.example.nlwebspringai.service.OrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1") // Base path for the API
public class NaturalLanguageController {

    private static final Logger logger = LoggerFactory.getLogger(NaturalLanguageController.class);

    private final OrchestrationService orchestrationService;

    public NaturalLanguageController(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping(value = "/process-nl", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<McpResponse> processNaturalLanguageQuery(@RequestBody NlWebQueryRequest request) {
        if (request == null || request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            logger.warn("Received empty or null query in request.");
            // Return a Flux that signals an error, which can be mapped to HTTP 400
            return Flux.error(new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Query cannot be null or empty in the request body."));
        }

        try {
            logger.info("Controller: Received natural language query: {}", request.getQuery());
            return orchestrationService.processNaturalLanguageCommand(request.getQuery())
                    .doOnError(e -> logger.error("Controller: Error processing command for query '{}': {}", request.getQuery(), e.getMessage()))
                    .onErrorResume(e -> {
                        McpResponse errorResponse = new McpResponse();
                        errorResponse.setId("error-controller-stream-" + System.currentTimeMillis());
                        errorResponse.setType("error");
                        errorResponse.setError("Failed to process query: " + e.getMessage());
                        return Flux.just(errorResponse);
                    });
        } catch (Exception e) {
            // This catch block might be redundant if all exceptions are handled by onErrorResume in the Flux chain.
            // However, it can catch synchronous errors from the initial part of processNaturalLanguageCommand if any.
            logger.error("Controller: Unexpected synchronous error for query: {}", request.getQuery(), e);
            McpResponse errorResponse = new McpResponse();
            errorResponse.setId("error-controller-sync-" + System.currentTimeMillis());
            errorResponse.setType("error");
            errorResponse.setError("Unexpected error processing your request: " + e.getMessage());
            return Flux.just(errorResponse);
        }
    }
}
