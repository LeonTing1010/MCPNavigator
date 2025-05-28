package com.example.nlwebspringai.service;

import com.example.nlwebspringai.client.NlWebClient;
import com.example.nlwebspringai.model.McpCommand;
import com.example.nlwebspringai.model.NlWebQueryRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NlWebServiceTest {

    @Mock
    private NlWebClient mockNlWebClient;

    @InjectMocks
    private NlWebService nlWebService;

    @Test
    void processQuery_validQuery_shouldCallClientAndReturnResult() {
        String query = "Test query";
        McpCommand expectedCommand = new McpCommand("action", "target", Collections.emptyMap());
        when(mockNlWebClient.translateNaturalLanguageToMcp(any(NlWebQueryRequest.class)))
                .thenReturn(expectedCommand);

        McpCommand actualCommand = nlWebService.processQuery(query);

        assertNotNull(actualCommand);
        assertEquals(expectedCommand, actualCommand);
        verify(mockNlWebClient).translateNaturalLanguageToMcp(any(NlWebQueryRequest.class));
    }

    @Test
    void processQuery_nullQuery_shouldReturnErrorCommand() {
        McpCommand errorCommand = nlWebService.processQuery(null);

        assertNotNull(errorCommand);
        assertEquals("error", errorCommand.getAction());
        assertEquals("nlweb_service", errorCommand.getTarget());
        assertTrue(errorCommand.getParams().containsKey("errorMessage"));
        assertEquals("Query cannot be null or empty.", errorCommand.getParams().get("errorMessage"));
    }

    @Test
    void processQuery_emptyQuery_shouldReturnErrorCommand() {
        McpCommand errorCommand = nlWebService.processQuery("   ");

        assertNotNull(errorCommand);
        assertEquals("error", errorCommand.getAction());
        assertEquals("nlweb_service", errorCommand.getTarget());
        assertTrue(errorCommand.getParams().containsKey("errorMessage"));
        assertEquals("Query cannot be null or empty.", errorCommand.getParams().get("errorMessage"));
    }
    
    @Test
    void processQuery_clientReturnsNull_shouldReturnErrorCommand() {
        String query = "Test query";
        when(mockNlWebClient.translateNaturalLanguageToMcp(any(NlWebQueryRequest.class)))
                .thenReturn(null);

        McpCommand errorCommand = nlWebService.processQuery(query);

        assertNotNull(errorCommand);
        assertEquals("error", errorCommand.getAction());
        assertEquals("nlweb_service", errorCommand.getTarget());
        assertTrue(errorCommand.getParams().containsKey("errorMessage"));
        assertEquals("Failed to translate query to MCP command: client returned null.", errorCommand.getParams().get("errorMessage"));
    }

    @Test
    void processQuery_clientThrowsException_shouldReturnErrorCommand() {
        String query = "Test query";
        String exceptionMessage = "Client communication error";
        when(mockNlWebClient.translateNaturalLanguageToMcp(any(NlWebQueryRequest.class)))
                .thenThrow(new RuntimeException(exceptionMessage));

        McpCommand errorCommand = nlWebService.processQuery(query);

        assertNotNull(errorCommand);
        assertEquals("error", errorCommand.getAction());
        assertEquals("nlweb_service", errorCommand.getTarget());
        assertTrue(errorCommand.getParams().containsKey("errorMessage"));
        assertEquals("Error processing query: " + exceptionMessage, errorCommand.getParams().get("errorMessage"));
    }
}
