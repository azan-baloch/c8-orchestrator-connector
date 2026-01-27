package io.camunda.orchestrator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.runtime.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.orchestrator.auth.TokenProvider;
import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.dto.OrchestratorResult;
import io.camunda.orchestrator.operation.OperationHandler;
import io.camunda.orchestrator.operation.OperationType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class C8OrchestratorFunctionTest {

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private OperationHandler mockHandler;

    private C8OrchestratorFunction function;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Map<OperationType, OperationHandler> handlers = Map.of(
                OperationType.START_PROCESS, mockHandler);
        function = new C8OrchestratorFunction(tokenProvider, handlers);
    }

    @Test
    void execute_shouldReturnSuccessResult_onValidRequest() throws Exception {
        // Arrange
        OrchestratorRequest request = new OrchestratorRequest(
                "cluster-1", "bru-2", "clientId", "secret",
                OperationType.START_PROCESS, "process-abc", null, null, Map.of());

        var context = OutboundConnectorContextBuilder.create()
                .variables(request)
                .build();

        when(tokenProvider.getToken("clientId", "secret")).thenReturn("valid-token");
        when(mockHandler.handle(eq(request), eq("valid-token"))).thenReturn(Map.of("instanceKey", "123"));

        // Act
        Object result = function.execute(context);

        // Assert
        assertTrue(result instanceof OrchestratorResult);
        OrchestratorResult castResult = (OrchestratorResult) result;

        assertTrue(castResult.success());
        assertEquals("START_PROCESS", castResult.operation());
        assertNotNull(castResult.data());
        verify(mockHandler).handle(any(), anyString());
    }

    @Test
    void execute_shouldThrowInvalidInput_whenHandlerIsMissing() {
        // Arrange: Function initialized with NO handlers for CANCEL_INSTANCE
        function = new C8OrchestratorFunction(tokenProvider, Map.of());

        OrchestratorRequest request = new OrchestratorRequest(
                "c", "r", "id", "s", OperationType.CANCEL_INSTANCE, null, "key", null, null);

        var context = OutboundConnectorContextBuilder.create().variables(request).build();

        // Act & Assert
        ConnectorException ex = assertThrows(ConnectorException.class, () -> function.execute(context));
        assertEquals("INVALID_INPUT", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Unsupported operation"));
    }

    @Test
    void execute_shouldPassThroughAuthErrors() {
        // Arrange
        OrchestratorRequest request = new OrchestratorRequest(
                "c", "r", "wrong-id", "s", OperationType.START_PROCESS, "p", null, null, null);

        var context = OutboundConnectorContextBuilder.create().variables(request).build();

        when(tokenProvider.getToken(anyString(), anyString()))
                .thenThrow(new ConnectorException("AUTH_ERROR", "Invalid credentials"));

        // Act & Assert
        ConnectorException ex = assertThrows(ConnectorException.class, () -> function.execute(context));
        assertEquals("AUTH_ERROR", ex.getErrorCode());
    }

    @Test
    void execute_shouldWrapUnexpectedExceptions() {
        // Arrange
        OrchestratorRequest request = new OrchestratorRequest(
                "c", "r", "id", "s", OperationType.START_PROCESS, "p", null, null, null);

        var context = OutboundConnectorContextBuilder.create().variables(request).build();

        when(tokenProvider.getToken(anyString(), anyString())).thenReturn("token");
        
        when(mockHandler.handle(any(), anyString())).thenThrow(new RuntimeException("Boom!"));

        // Act & Assert
        ConnectorException ex = assertThrows(ConnectorException.class, () -> function.execute(context));
        assertEquals("EXECUTION_ERROR", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Boom!"));
    }
}