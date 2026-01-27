package io.camunda.orchestrator.operation;

import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.service.C8ApiClient;
import java.util.Map;

public class CompleteTaskHandler implements OperationHandler {

    private final C8ApiClient api;

    public CompleteTaskHandler(C8ApiClient api) {
        this.api = api;
    }

    @Override
    public OperationType getType() {
        return OperationType.COMPLETE_TASK;
    }

    @Override
    public Object handle(OrchestratorRequest request, String token) {
        if (request.userTaskKey() == null || request.userTaskKey().isBlank()) {
            throw new IllegalArgumentException(
                    "userTaskKey is required to complete a user task");
        }

        Object vars = request.variables() == null ? Map.of() : request.variables();

        return api.completeUserTask(
                request.clusterId(),
                request.region(),
                token,
                request.userTaskKey(),
                vars);
    }
}