package de.psi.paip.mes.frontend.model.workflow;

import java.util.List;
import java.util.Map;

public class Payload {
    public Map<String, Object> variables;
    public Map<String, Object> workflowVariables;
    public List<FormField> fields;
    public Map<String, String> properties;
}