package io.camunda.orchestrator.operation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.service.C8ApiClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ResolveIncidentHandlerTest {

    @Mock
    private C8ApiClient api;

    private ResolveIncidentHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ResolveIncidentHandler(api);
    }

    @Test
    void getType_shouldReturnResolveIncident() {
        assertEquals(OperationType.RESOLVE_INCIDENT, handler.getType());
    }

    @Test
    void handle_shouldThrowException_whenObjectKeyIsMissing() {
        // Arrange: objectKey is null
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster-123", "bru-2", "client-id", "secret",
                OperationType.RESOLVE_INCIDENT, null, null, null, null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(request, "valid-token"));

        assertEquals("Incident Key (objectKey) is required to resolve an incident", exception.getMessage());
        
        verify(api, never()).resolveIncident(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void handle_shouldCallApiClient_whenRequestIsValid() {
        // Arrange
        String clusterId = "my-cluster";
        String region = "us-east-1";
        String token = "Bearer token-123";
        String incidentKey = "987654321";

        OrchestratorRequest request = new OrchestratorRequest(
                clusterId, region, "id", "secret",
                OperationType.RESOLVE_INCIDENT, null, incidentKey, null, null);

        Map<String, String> expectedResponse = Map.of("status", "resolved");
        when(api.resolveIncident(clusterId, region, token, incidentKey))
                .thenReturn(expectedResponse);

        // Act
        Object result = handler.handle(request, token);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(api, times(1)).resolveIncident(clusterId, region, token, incidentKey);
    }
}