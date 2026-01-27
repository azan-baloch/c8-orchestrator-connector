package io.camunda.orchestrator.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.error.ConnectorException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthClient.class);
    private static final String AUTH_URL = "https://login.cloud.camunda.io/oauth/token";
    private static final String AUDIENCE = "zeebe.camunda.io";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AuthClient(ObjectMapper objectMapper, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public TokenResult fetchToken(String clientId, String clientSecret) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ConnectorException(
                    "AUTH_ERROR",
                    "Client ID or Client Secret is missing. Please check your connection configuration.");
        }

        try {
            Map<String, String> payload = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "audience", AUDIENCE,
                    "grant_type", "client_credentials");

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            LOGGER.debug("Requesting new access token from Camunda 8 Auth...");

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("OAuth failed with status {}: {}", response.statusCode(), response.body());
                throw new ConnectorException(
                        "AUTH_ERROR",
                        "Authentication failed (" + response.statusCode()
                                + "). Ensure your Client ID and Secret are correct.");
            }

            JsonNode json = objectMapper.readTree(response.body());

            if (!json.hasNonNull("access_token") || !json.hasNonNull("expires_in")) {
                throw new ConnectorException(
                        "AUTH_ERROR",
                        "The authentication server returned an unexpected response format.");
            }

            return new TokenResult(
                    json.get("access_token").asText(),
                    json.get("expires_in").asLong());

        } catch (ConnectorException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Technical error during authentication", e);
            throw new ConnectorException("AUTH_ERROR", "Failed to connect to Camunda Auth: " + e.getMessage(), e);
        }
    }

    public record TokenResult(String token, long expiresIn) {
    }
}