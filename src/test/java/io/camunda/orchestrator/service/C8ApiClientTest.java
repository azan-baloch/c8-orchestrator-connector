package io.camunda.orchestrator.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.error.ConnectorException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class C8ApiClientTest {

    private C8ApiClient c8ApiClient;
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        c8ApiClient = new C8ApiClient(httpClient, objectMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void startProcessInstance_shouldReturnMap_onSuccess() throws Exception {
        // Arrange
        String jsonResponse = "{\"processInstanceKey\":\"12345\"}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act
        Object result = c8ApiClient.startProcessInstance(
                "cluster-id", "region", "token", "my-process", Map.of());

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals("12345", ((Map<?, ?>) result).get("processInstanceKey"));

        verify(httpClient).send(argThat(request -> {
            boolean matchesUrl = request.uri().toString()
                    .equals("https://region.zeebe.camunda.io/cluster-id/v2/process-instances");

            boolean hasAuthHeader = request.headers()
                    .firstValue("Authorization")
                    .map(v -> v.equals("Bearer token"))
                    .orElse(false);

            boolean isPost = request.method().equals("POST");

            return matchesUrl && hasAuthHeader && isPost;
        }), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void post_shouldThrowConnectorException_onApiError() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpResponse.body()).thenReturn("Process not found");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act & Assert
        ConnectorException exception = assertThrows(ConnectorException.class,
                () -> c8ApiClient.startProcessInstance("cluster", "region", "token", "invalid", Map.of()));

        assertEquals("404", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Camunda API error"));
    }
}