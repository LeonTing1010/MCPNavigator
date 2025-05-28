package com.example.nlwebspringai.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;

public class McpResponse {
    private String id; // Corresponds to the request ID
    private String type; // e.g., "snapshot", "ack", "error", "stream_chunk", "stream_end"
    private JsonNode data; // Flexible data field
    private String error; // Error message if type is "error"

    public McpResponse() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "McpResponse{" +
               "id='" + id + '\'' +
               ", type='" + type + '\'' +
               ", data=" + (data != null ? data.toString() : "null") +
               ", error='" + error + '\'' +
               '}';
    }
}
