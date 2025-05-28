package com.example.nlwebspringai.model;

public class NlWebQueryRequest {
    private String query;

    public NlWebQueryRequest() {
    }

    public NlWebQueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
