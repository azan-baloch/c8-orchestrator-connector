package io.camunda.orchestrator.operation;

import io.camunda.orchestrator.dto.OrchestratorRequest;

public interface OperationHandler {

    OperationType getType();

    Object handle(OrchestratorRequest request, String accessToken);
}
