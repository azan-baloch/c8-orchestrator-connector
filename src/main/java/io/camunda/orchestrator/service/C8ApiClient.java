package io.camunda.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.error.ConnectorException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class C8ApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public C8ApiClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build(), new ObjectMapper());
    }

    public C8ApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public ObjectMapper mapper() {
        return objectMapper;
    }

    private String buildUrl(String region, String clusterId, String path) {
        return String.format("https://%s.zeebe.camunda.io/%s/v2/%s", region, clusterId, path);
    }

    public Object startProcessInstance(String clusterId, String region, String token, String bpmnProcessId,
            Object variables) {
        return post(buildUrl(region, clusterId, "process-instances"),
                Map.of("processDefinitionId", bpmnProcessId, "variables", variables), token);
    }

    public Object completeUserTask(String clusterId, String region, String token, String userTaskKey,
            Object variables) {
        return post(buildUrl(region, clusterId, "user-tasks/" + userTaskKey + "/completion"),
                Map.of("action", "complete", "variables", variables), token);
    }

    public Object cancelProcessInstance(String clusterId, String region, String token, String processInstanceKey) {
        return post(buildUrl(region, clusterId, "process-instances/" + processInstanceKey + "/cancellation"),
                Map.of(), token);
    }

    public Object resolveIncident(String clusterId, String region, String token, String incidentKey) {
        return post(buildUrl(region, clusterId, "incidents/" + incidentKey + "/resolution"),
                Map.of(), token);
    }

    private Object post(String url, Object body, String token) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw mapError(response);
            }

            if (response.body() == null || response.body().isBlank()) {
                return Map.of("status", "success");
            }

            return objectMapper.readValue(response.body(), Object.class);

        } catch (ConnectorException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectorException("HTTP_ERROR", e.getMessage());
        }
    }

    private ConnectorException mapError(HttpResponse<String> response) {
        return new ConnectorException(String.valueOf(response.statusCode()),
                "Camunda API error: " + response.body());
    }
}