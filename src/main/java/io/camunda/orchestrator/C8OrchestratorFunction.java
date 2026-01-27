package io.camunda.orchestrator;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.orchestrator.auth.TokenProvider;
import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.dto.OrchestratorResult;
import io.camunda.orchestrator.operation.*;
import io.camunda.orchestrator.service.C8ApiClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(name = "C8_ORCHESTRATOR_COMMUNITY", type = "io.camunda:c8-orchestrator-community:1")
@ElementTemplate(id = "io.camunda.community.c8.orchestrator.v1", name = "Camunda 8 Orchestrator (Community)", version = 1, inputDataClass = OrchestratorRequest.class, propertyGroups = {
        @ElementTemplate.PropertyGroup(id = "connection", label = "Cluster Connection"),
        @ElementTemplate.PropertyGroup(id = "operation", label = "Operation"),
        @ElementTemplate.PropertyGroup(id = "details", label = "Operation Details")
}, icon = "icon.svg")
public class C8OrchestratorFunction implements OutboundConnectorFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(C8OrchestratorFunction.class);
    private final TokenProvider tokenProvider;
    private final Map<OperationType, OperationHandler> handlers;

    public C8OrchestratorFunction() {
        C8ApiClient apiClient = new C8ApiClient();
        this.tokenProvider = new TokenProvider(apiClient.mapper());

        this.handlers = Map.of(
                OperationType.START_PROCESS, new StartProcessHandler(apiClient),
                OperationType.CANCEL_INSTANCE, new CancelInstanceHandler(apiClient),
                OperationType.COMPLETE_TASK, new CompleteTaskHandler(apiClient),
                OperationType.RESOLVE_INCIDENT, new ResolveIncidentHandler(apiClient));
    }

    public C8OrchestratorFunction(TokenProvider tokenProvider, Map<OperationType, OperationHandler> handlers) {
        this.tokenProvider = tokenProvider;
        this.handlers = handlers;
    }

    @Override
    public Object execute(OutboundConnectorContext context) {
        try {
            OrchestratorRequest request = context.bindVariables(OrchestratorRequest.class);
            LOGGER.info("Executing operation: {}", request.operation());

            OperationHandler handler = handlers.get(request.operation());
            if (handler == null) {
                throw new ConnectorException("INVALID_INPUT", "Unsupported operation: " + request.operation());
            }

            String token = tokenProvider.getToken(request.clientId(), request.clientSecret());
            Object data = handler.handle(request, token);

            return OrchestratorResult.success(
                    request.operation().name(),
                    data,
                    "Operation " + request.operation() + " executed successfully");

        } catch (ConnectorException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error in connector", e);
            throw new ConnectorException("EXECUTION_ERROR", e.getMessage());
        }
    }
}