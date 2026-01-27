package io.camunda.orchestrator.operation;

import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.service.C8ApiClient;

public class ResolveIncidentHandler implements OperationHandler {

    private final C8ApiClient api;

    public ResolveIncidentHandler(C8ApiClient api) {
        this.api = api;
    }

    @Override
    public OperationType getType() {
        return OperationType.RESOLVE_INCIDENT;
    }

    @Override
    public Object handle(OrchestratorRequest request, String token) {
        if (request.objectKey() == null || request.objectKey().isBlank()) {
            throw new IllegalArgumentException("Incident Key (objectKey) is required to resolve an incident");
        }

        return api.resolveIncident(
                request.clusterId(),
                request.region(),
                token,
                request.objectKey());
    }
}