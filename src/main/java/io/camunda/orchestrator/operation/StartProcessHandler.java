package io.camunda.orchestrator.operation;

import io.camunda.orchestrator.dto.OrchestratorRequest;
import io.camunda.orchestrator.service.C8ApiClient;
import java.util.Map;

public class StartProcessHandler implements OperationHandler {

    private final C8ApiClient api;

    public StartProcessHandler(C8ApiClient api) {
        this.api = api;
    }

    @Override
    public OperationType getType() {
        return OperationType.START_PROCESS;
    }

    @Override
    public Object handle(OrchestratorRequest request, String token) {
        if (request.bpmnProcessId() == null || request.bpmnProcessId().isBlank()) {
            throw new IllegalArgumentException("BPMN Process ID is required to start an instance");
        }

        Object variables = request.variables() == null ? Map.of() : request.variables();

        return api.startProcessInstance(
                request.clusterId(),
                request.region(),
                token,
                request.bpmnProcessId(),
                variables);
    }
}