package io.camunda.orchestrator.operation;

import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.service.C8ApiClient;

public class CancelInstanceHandler implements OperationHandler {

    private final C8ApiClient api;

    public CancelInstanceHandler(C8ApiClient api) {
        this.api = api;
    }

    @Override
    public OperationType getType() {
        return OperationType.CANCEL_INSTANCE;
    }

    @Override
    public Object handle(OrchestratorRequest request, String token) {
        if (request.objectKey() == null || request.objectKey().isBlank()) {
            throw new IllegalArgumentException("Process Instance Key (objectKey) is required to cancel an instance");
        }

        return api.cancelProcessInstance(
                request.clusterId(),
                request.region(),
                token,
                request.objectKey());
    }
}