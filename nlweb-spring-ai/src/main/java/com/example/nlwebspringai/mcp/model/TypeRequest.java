package com.example.nlwebspringai.mcp.model;

import java.util.HashMap;
import java.util.Map;

public class TypeRequest extends McpBaseRequest {
    private Map<String, Object> params; // Using Object for 'submit' to handle boolean

    public TypeRequest(String ref, String elementDescription, String text, boolean submit) {
        super("browser_type");
        this.params = new HashMap<>();
        this.params.put("ref", ref);
        this.params.put("element", elementDescription);
        this.params.put("text", text);
        this.params.put("submit", submit); // Jackson will serialize boolean correctly
    }

    // Getters and setters
    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
