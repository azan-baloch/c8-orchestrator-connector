package io.camunda.orchestrator.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record OrchestratorResult(
        boolean success,
        String operation,
        Object data,
        String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC") Instant timestamp) {

    public static OrchestratorResult success(String operation, Object data, String message) {
        return new OrchestratorResult(
                true,
                operation,
                data,
                message,
                Instant.now());
    }

    public static OrchestratorResult failure(String operation, String message) {
        return new OrchestratorResult(
                false,
                operation,
                null,
                message,
                Instant.now());
    }
}