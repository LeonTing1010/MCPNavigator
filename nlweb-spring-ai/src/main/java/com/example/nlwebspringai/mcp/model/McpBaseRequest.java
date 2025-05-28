package com.example.nlwebspringai.mcp.model;

import java.util.UUID;

public class McpBaseRequest {
    private String id;
    private String command; // e.g., "browser_navigate", "browser_snapshot"

    public McpBaseRequest(String command) {
        this.id = UUID.randomUUID().toString();
        this.command = command;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
