package com.example.nlwebspringai.service;

import com.example.nlwebspringai.client.NlWebClient;
import com.example.nlwebspringai.model.McpCommand;
import com.example.nlwebspringai.model.NlWebQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NlWebService {

    private static final Logger logger = LoggerFactory.getLogger(NlWebService.class);

    private final NlWebClient nlWebClient;

    public NlWebService(NlWebClient nlWebClient) {
        this.nlWebClient = nlWebClient;
    }

    public McpCommand processQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Received null or empty query.");
            return createErrorCommand("Query cannot be null or empty.");
        }

        NlWebQueryRequest request = new NlWebQueryRequest(query);
        try {
            logger.info("Processing query: {}", query);
            McpCommand command = nlWebClient.translateNaturalLanguageToMcp(request);
            if (command == null) {
                logger.warn("NLWebClient returned null command for query: {}", query);
                return createErrorCommand("Failed to translate query to MCP command: client returned null.");
            }
            logger.info("Successfully processed query and received command: {} for target {}", command.getAction(), command.getTarget());
            return command;
        } catch (Exception e) {
            logger.error("Exception during query processing for query: {}", query, e);
            return createErrorCommand("Error processing query: " + e.getMessage());
        }
    }

    private McpCommand createErrorCommand(String errorMessage) {
        McpCommand errorCommand = new McpCommand();
        errorCommand.setAction("error");
        errorCommand.setTarget("nlweb_service");
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("errorMessage", errorMessage);
        errorCommand.setParams(params);
        return errorCommand;
    }
}
