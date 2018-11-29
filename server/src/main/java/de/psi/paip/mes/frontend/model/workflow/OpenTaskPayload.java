package de.psi.paip.mes.frontend.model.workflow;

import lombok.Data;

@Data
public class OpenTaskPayload {
    private String workOperation;
    private String workStep;

    public String toString(){
        return "Payload-> workOperation:"+workOperation+" workStep:"+workStep;
    }
}
