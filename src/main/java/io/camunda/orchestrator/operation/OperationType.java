package io.camunda.orchestrator.operation;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OperationType {
    @JsonProperty("START_PROCESS")    START_PROCESS,
    @JsonProperty("CANCEL_INSTANCE")  CANCEL_INSTANCE,
    @JsonProperty("COMPLETE_TASK")    COMPLETE_TASK,
    @JsonProperty("RESOLVE_INCIDENT") RESOLVE_INCIDENT
}