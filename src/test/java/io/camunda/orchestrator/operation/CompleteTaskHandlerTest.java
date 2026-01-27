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

class CompleteTaskHandlerTest {

    @Mock
    private C8ApiClient api;

    private CompleteTaskHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new CompleteTaskHandler(api);
    }

    @Test
    void getType_shouldReturnCompleteTask() {
        assertEquals(OperationType.COMPLETE_TASK, handler.getType());
    }

    @Test
    void handle_shouldThrowException_whenUserTaskKeyIsMissing() {
        // Arrange: userTaskKey is null
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster-1", "region-1", "id", "secret",
                OperationType.COMPLETE_TASK, null, null, null, Map.of());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.handle(request, "token"));

        assertEquals("userTaskKey is required to complete a user task", exception.getMessage());
        verify(api, never()).completeUserTask(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void handle_shouldPassEmptyMap_whenVariablesAreNull() {
        // Arrange
        String taskKey = "55555";
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster", "region", "id", "secret",
                OperationType.COMPLETE_TASK, null, null, taskKey, null // Variables are null
        );

        // Act
        handler.handle(request, "token");

        // Assert
        verify(api).completeUserTask(anyString(), anyString(), anyString(), eq(taskKey), eq(Map.of()));
    }

    @Test
    void handle_shouldCallApiClient_withProvidedVariables() {
        // Arrange
        Map<String, Object> myVars = Map.of("approved", true, "score", 95);
        String taskKey = "12345";

        OrchestratorRequest request = new OrchestratorRequest(
                "cluster-id", "bru-2", "client", "secret",
                OperationType.COMPLETE_TASK, null, null, taskKey, myVars);

        when(api.completeUserTask(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(Map.of("status", "success"));

        // Act
        Object result = handler.handle(request, "token");

        // Assert
        assertNotNull(result);
        verify(api).completeUserTask(
                eq("cluster-id"),
                eq("bru-2"),
                eq("token"),
                eq(taskKey),
                eq(myVars));
    }
}