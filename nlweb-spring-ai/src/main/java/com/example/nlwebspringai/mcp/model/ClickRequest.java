package com.example.nlwebspringai.mcp.model;

import java.util.HashMap;
import java.util.Map;

public class ClickRequest extends McpBaseRequest {
    private Map<String, String> params;

    public ClickRequest(String ref, String elementDescription) {
        super("browser_click");
        this.params = new HashMap<>();
        this.params.put("ref", ref);
        this.params.put("element", elementDescription);
    }

    // Getters and setters
    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
