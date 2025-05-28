package com.example.nlwebspringai.mcp.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;


public class NavigateRequest extends McpBaseRequest {
    private Map<String, String> params;

    public NavigateRequest(String url) {
        super("browser_navigate");
        this.params = Map.of("url", url);
    }

    // Getter and setter
    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
