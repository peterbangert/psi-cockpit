package de.psi.paip.mes.frontend.model.workflow;

import lombok.Data;

import java.util.List;


@Data
public class UserTask extends Activity {
    public static final String TYPE = "userTask";

    private String taskId;
    private List<FormField> fields;
    private boolean cancelable;

    @Override
    public String toString(){
        return TYPE+" with BusinessKey:" +getProcessBusinessKey();
    }
}
