package io.camunda.orchestrator.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.error.ConnectorException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthClientTest {

    private AuthClient authClient;
    private ObjectMapper objectMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        authClient = new AuthClient(objectMapper, httpClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchToken_shouldReturnToken_onSuccess() throws Exception {
        // Arrange
        String jsonResponse = "{\"access_token\":\"secret-token\",\"expires_in\":3600}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act
        AuthClient.TokenResult result = authClient.fetchToken("myId", "mySecret");

        // Assert
        assertEquals("secret-token", result.token());
        assertEquals(3600, result.expiresIn());
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchToken_shouldThrowException_onAuthError() throws Exception {
        // Arrange
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpResponse.body()).thenReturn("Unauthorized");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act & Assert
        assertThrows(ConnectorException.class, () -> authClient.fetchToken("wrongId", "wrongSecret"));
    }
}