package com.example.nlwebspringai.client;

import com.example.nlwebspringai.model.McpCommand;
import com.example.nlwebspringai.model.NlWebMcpResponse;
import com.example.nlwebspringai.model.NlWebQueryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NlWebRestClient implements NlWebClient {

    private static final Logger logger = LoggerFactory.getLogger(NlWebRestClient.class);

    private final RestTemplate restTemplate;
    private final String nlWebUrl;

    public NlWebRestClient(RestTemplate restTemplate,
                           @Value("${nlweb.service.url:http://localhost:8000/ask}") String nlWebUrl) {
        this.restTemplate = restTemplate;
        this.nlWebUrl = nlWebUrl;
    }

    @Override
    public McpCommand translateNaturalLanguageToMcp(NlWebQueryRequest request) {
        try {
            logger.info("Sending query to NLWeb service at {}: {}", nlWebUrl, request.getQuery());
            // Assuming NLWeb service expects NlWebQueryRequest and returns NlWebMcpResponse
            // which contains the McpCommand.
            NlWebMcpResponse response = restTemplate.postForObject(nlWebUrl, request, NlWebMcpResponse.class);
            if (response != null && response.getMcpCommand() != null) {
                logger.info("Received MCP command from NLWeb service: {}", response.getMcpCommand().getAction());
                return response.getMcpCommand();
            } else {
                logger.warn("Received null or empty response from NLWeb service for query: {}", request.getQuery());
                // Return a default/error command or throw an exception
                return createErrorCommand("No command received from NLWeb service");
            }
        } catch (Exception e) {
            logger.error("Error calling NLWeb service for query: {}", request.getQuery(), e);
            // Return a default/error command or throw a custom exception
            return createErrorCommand("Error communicating with NLWeb service: " + e.getMessage());
        }
    }

    private McpCommand createErrorCommand(String errorMessage) {
        McpCommand errorCommand = new McpCommand();
        errorCommand.setAction("error");
        errorCommand.setTarget("nlweb_client");
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("errorMessage", errorMessage);
        errorCommand.setParams(params);
        return errorCommand;
    }
}
