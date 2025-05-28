package com.example.nlwebspringai.model;

import java.util.Map;

public class McpCommand {
    private String action;
    private String target;
    private Map<String, Object> params;

    public McpCommand() {
    }

    public McpCommand(String action, String target, Map<String, Object> params) {
        this.action = action;
        this.target = target;
        this.params = params;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
