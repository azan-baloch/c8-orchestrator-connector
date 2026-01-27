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

class CancelInstanceHandlerTest {

    @Mock
    private C8ApiClient api;

    private CancelInstanceHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CancelInstanceHandler(api);
    }

    @Test
    void getType_shouldReturnCancelInstance() {
        assertEquals(OperationType.CANCEL_INSTANCE, handler.getType());
    }

    @Test
    void handle_shouldThrowException_whenObjectKeyIsMissing() {
        // Arrange: objectKey (Process Instance Key) is null
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster-abc", "bru-2", "client-id", "secret",
                OperationType.CANCEL_INSTANCE, null, null, null, null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(request, "test-token"));

        assertEquals("Process Instance Key (objectKey) is required to cancel an instance", exception.getMessage());

        verify(api, never()).cancelProcessInstance(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void handle_shouldCallApiClient_whenRequestIsValid() {
        // Arrange
        String clusterId = "test-cluster";
        String region = "bru-2";
        String token = "token-xyz";
        String processInstanceKey = "2251799813685248";

        OrchestratorRequest request = new OrchestratorRequest(
                clusterId, region, "id", "secret",
                OperationType.CANCEL_INSTANCE, null, processInstanceKey, null, null);

        Map<String, String> expectedResponse = Map.of("status", "success");
        when(api.cancelProcessInstance(clusterId, region, token, processInstanceKey))
                .thenReturn(expectedResponse);

        // Act
        Object result = handler.handle(request, token);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);

        verify(api, times(1)).cancelProcessInstance(clusterId, region, token, processInstanceKey);
    }
}