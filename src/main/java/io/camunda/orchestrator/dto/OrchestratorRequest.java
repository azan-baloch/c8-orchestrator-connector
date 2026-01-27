package io.camunda.orchestrator.dto;

import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateProperty.*;
import io.camunda.orchestrator.operation.OperationType;

public record OrchestratorRequest(

        @TemplateProperty(group = "connection", label = "Cluster ID") String clusterId,

        @TemplateProperty(group = "connection", label = "Region", defaultValue = "bru-2") String region,

        @TemplateProperty(group = "connection", label = "Client ID") String clientId,

        @TemplateProperty(group = "connection", label = "Client Secret", type = PropertyType.Text) String clientSecret,

        @TemplateProperty(group = "operation", label = "Operation", type = PropertyType.Dropdown, defaultValue = "START_PROCESS", choices = {
                @DropdownPropertyChoice(value = "START_PROCESS", label = "Start Process Instance"),
                @DropdownPropertyChoice(value = "CANCEL_INSTANCE", label = "Cancel Process Instance"),
                @DropdownPropertyChoice(value = "COMPLETE_TASK", label = "Complete User Task"),
                @DropdownPropertyChoice(value = "RESOLVE_INCIDENT", label = "Resolve Incident")
        }) OperationType operation,

        @TemplateProperty(group = "details", label = "BPMN Process ID", condition = @PropertyCondition(property = "operation", equals = "START_PROCESS")) String bpmnProcessId,

        @TemplateProperty(group = "details", label = "Object Key (Instance or Incident)", description = "The numeric ID from Operate", condition = @PropertyCondition(property = "operation", oneOf = {
                "CANCEL_INSTANCE", "RESOLVE_INCIDENT" })) String objectKey,

        @TemplateProperty(group = "details", label = "User Task Key", condition = @PropertyCondition(property = "operation", equals = "COMPLETE_TASK")) String userTaskKey,

        @TemplateProperty(group = "details", label = "Variables", type = PropertyType.Text, defaultValue = "={}", condition = @PropertyCondition(property = "operation", oneOf = {
                "START_PROCESS", "COMPLETE_TASK" })) Object variables) {
}