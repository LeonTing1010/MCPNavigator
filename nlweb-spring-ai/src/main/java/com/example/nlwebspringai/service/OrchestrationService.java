package com.example.nlwebspringai.service;

import com.example.nlwebspringai.mcp.model.McpResponse;
import com.example.nlwebspringai.mcp.service.PlaywrightMcpService;
import com.example.nlwebspringai.model.McpCommand; // This is our NlWebMcpCommand equivalent
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationService.class);

    private final NlWebService nlWebService;
    private final PlaywrightMcpService playwrightMcpService;

    public OrchestrationService(NlWebService nlWebService, PlaywrightMcpService playwrightMcpService) {
        this.nlWebService = nlWebService;
        this.playwrightMcpService = playwrightMcpService;
    }

    public Flux<McpResponse> processNaturalLanguageCommand(String nlQuery) {
        logger.info("OrchestrationService: Processing natural language query: {}", nlQuery);

        McpCommand nlWebMcpCommand = nlWebService.processQuery(nlQuery);

        if (nlWebMcpCommand == null) {
            logger.error("NlWebService returned null for query: {}", nlQuery);
            return Flux.error(new IllegalStateException("NLWeb service failed to process the query."));
        }
        
        // Handle if NlWebService itself returned an error command
        if ("error".equalsIgnoreCase(nlWebMcpCommand.getAction())) {
            logger.warn("NlWebService returned an error command: {}", nlWebMcpCommand.getParams());
            // Convert this error into a Flux<McpResponse> that signals an error.
            // Or, create a specific McpResponse error object if your client expects that.
             String errorMessage = (String) nlWebMcpCommand.getParams().getOrDefault("errorMessage", "Error from NLWebService");
             McpResponse errorResponse = new McpResponse();
             errorResponse.setId("error-nlweb-" + System.currentTimeMillis()); // Generate some ID
             errorResponse.setType("error");
             errorResponse.setError(errorMessage);
            return Flux.just(errorResponse); 
        }


        String action = nlWebMcpCommand.getAction();
        String target = nlWebMcpCommand.getTarget();
        Map<String, Object> params = nlWebMcpCommand.getParams();

        logger.info("NLWeb MCP Command: Action={}, Target={}, Params={}", action, target, params);

        switch (action.toLowerCase()) {
            case "navigate":
                if (target == null || target.trim().isEmpty()) {
                    logger.error("Navigate action called with null or empty target (URL).");
                    return Flux.error(new IllegalArgumentException("URL for navigate action cannot be null or empty."));
                }
                logger.info("Orchestrating NAVIGATE to URL: {}", target);
                return playwrightMcpService.navigate(target);
            case "snapshot":
                logger.info("Orchestrating SNAPSHOT.");
                return playwrightMcpService.takeSnapshot();
            case "click":
                if (target == null || target.trim().isEmpty()) {
                     logger.error("Click action called with null or empty target (ref).");
                    return Flux.error(new IllegalArgumentException("Ref for click action cannot be null or empty."));
                }
                String clickElementDesc = params != null ? (String) params.get("elementDescription") : "Unknown element";
                logger.info("Orchestrating CLICK on element with ref: {}, Description: {}", target, clickElementDesc);
                return playwrightMcpService.clickElement(target, clickElementDesc);
            case "type":
                if (target == null || target.trim().isEmpty()) {
                    logger.error("Type action called with null or empty target (ref).");
                    return Flux.error(new IllegalArgumentException("Ref for type action cannot be null or empty."));
                }
                if (params == null) {
                    logger.error("Type action called with null params.");
                    return Flux.error(new IllegalArgumentException("Parameters for type action cannot be null."));
                }
                String typeElementDesc = (String) params.get("elementDescription");
                String textToType = (String) params.get("text");
                Object submitObj = params.get("submit");
                boolean submit = false; // Default to false
                if (submitObj instanceof Boolean) {
                    submit = (Boolean) submitObj;
                } else if (submitObj instanceof String) {
                    submit = Boolean.parseBoolean((String) submitObj);
                }
                
                if (textToType == null) {
                     logger.error("Type action called with null text.");
                    return Flux.error(new IllegalArgumentException("Text for type action cannot be null."));
                }

                logger.info("Orchestrating TYPE in element with ref: {}, Description: {}, Text: '{}', Submit: {}", target, typeElementDesc, textToType, submit);
                return playwrightMcpService.typeInElement(target, typeElementDesc, textToType, submit);
            default:
                logger.warn("Unknown action received from NLWeb service: {}", action);
                return Flux.error(new IllegalArgumentException("Unknown action: " + action));
        }
    }
}
