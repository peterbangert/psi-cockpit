package de.psi.paip.mes.frontend.model.workflow;

import lombok.Data;

@Data
public class ProcessInstance {
    private String instanceId;
    private String definitionId;
    private String businessKey;
    private Activity[] activities;

    public String toString() {
        return "ProcessInstance: " + instanceId;
    }

    public Activity getFirstActivity() {
        if (activities.length > 0) {
            return activities[0];
        } else {
            return null;
        }
    }
}