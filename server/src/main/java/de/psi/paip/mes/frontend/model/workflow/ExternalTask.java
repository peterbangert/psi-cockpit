package de.psi.paip.mes.frontend.model.workflow;

import lombok.Data;

@Data
public class ExternalTask extends Activity {
    public static final String TYPE = "externalTask";

    private String taskTopic;
    private String taskVersion;
    private String taskId;

    @Override
    public String toString(){
        return TYPE+" with BusinessKey:" +getProcessBusinessKey();
    }
}
