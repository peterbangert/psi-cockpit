package de.psi.paip.mes.frontend.model.workflow;


import lombok.Data;

import java.util.List;

@Data
public class FormField {
    private String id;
    private String type;
    private String label;
    private String defaultValue;
    private List<FormFieldValue> values;

    public static enum FieldType {
        STRING, LONG, DATE, BOOLEAN, ENUM;

        public String getName(){
            return name().toLowerCase();
        }
    };

    public static FormField build(String id, FieldType type, String label) {
        FormField f = new FormField();
        f.id = id;
        f.type = type.getName();
        f.label = label;

        return f;
    }

}