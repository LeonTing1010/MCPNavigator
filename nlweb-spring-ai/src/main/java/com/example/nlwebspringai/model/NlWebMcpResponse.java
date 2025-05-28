package com.example.nlwebspringai.model;

public class NlWebMcpResponse {
    private McpCommand mcpCommand;

    public NlWebMcpResponse() {
    }

    public NlWebMcpResponse(McpCommand mcpCommand) {
        this.mcpCommand = mcpCommand;
    }

    public McpCommand getMcpCommand() {
        return mcpCommand;
    }

    public void setMcpCommand(McpCommand mcpCommand) {
        this.mcpCommand = mcpCommand;
    }
}
