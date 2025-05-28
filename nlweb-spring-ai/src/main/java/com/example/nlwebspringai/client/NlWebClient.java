package com.example.nlwebspringai.client;

import com.example.nlwebspringai.model.McpCommand;
import com.example.nlwebspringai.model.NlWebQueryRequest;

public interface NlWebClient {
    McpCommand translateNaturalLanguageToMcp(NlWebQueryRequest request);
}
