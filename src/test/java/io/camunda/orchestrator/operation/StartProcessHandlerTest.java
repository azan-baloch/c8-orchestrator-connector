package io.camunda.orchestrator.operation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.service.C8ApiClient;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StartProcessHandlerTest {

    @Mock
    private C8ApiClient api;
    private StartProcessHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new StartProcessHandler(api);
    }

    @Test
    void handle_shouldThrowException_whenBpmnProcessIdIsMissing() {
        // Arrange: missing bpmnProcessId
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster", "region", "clientId", "secret",
                OperationType.START_PROCESS, null, null, null, Map.of());

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(request, "token"));

        assertEquals("BPMN Process ID is required to start an instance", ex.getMessage());
    }

    @Test
    void handle_shouldCallApiClient_withCorrectData() {
        // Arrange
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster", "region", "clientId", "secret",
                OperationType.START_PROCESS, "process-v1", null, null, Map.of("key", "val"));

        when(api.startProcessInstance(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Map.of("instanceKey", "123"));

        // Act
        Object result = handler.handle(request, "token");

        // Assert
        assertNotNull(result);
        verify(api).startProcessInstance(
                eq("cluster"), eq("region"), eq("token"), eq("process-v1"), any());
    }
}