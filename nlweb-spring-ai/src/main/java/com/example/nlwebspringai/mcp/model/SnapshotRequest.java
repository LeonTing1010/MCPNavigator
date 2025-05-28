package com.example.nlwebspringai.mcp.model;

// For a basic snapshot, no specific parameters are needed beyond the base request.
// If specific snapshot parameters were required (e.g., viewport size, specific element to snapshot),
// they would be added here.
public class SnapshotRequest extends McpBaseRequest {

    public SnapshotRequest() {
        super("browser_snapshot");
        // No additional parameters for a generic snapshot request in this basic version
    }
}
