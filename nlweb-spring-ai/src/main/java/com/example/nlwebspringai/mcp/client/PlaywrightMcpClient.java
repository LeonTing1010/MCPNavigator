package com.example.nlwebspringai.mcp.client;

import com.example.nlwebspringai.mcp.model.McpBaseRequest;
import com.example.nlwebspringai.mcp.model.McpResponse;
import reactor.core.publisher.Flux;

public interface PlaywrightMcpClient {
    Flux<McpResponse> sendCommand(McpBaseRequest command);
}
