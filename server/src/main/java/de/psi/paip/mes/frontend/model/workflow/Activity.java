package de.psi.paip.mes.frontend.model.workflow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.ServiceTask;
import lombok.Data;

import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, property = "activityType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExternalTask.class, name = "externalTask"),
        @JsonSubTypes.Type(value = UserTask.class, name = "userTask"),
        @JsonSubTypes.Type(value = ServiceTask.class, name = "serviceTask"),
        @JsonSubTypes.Type(value = Activity.class, name = "startEvent"),
        @JsonSubTypes.Type(value = Activity.class, name = "endEvent"),
        @JsonSubTypes.Type(value = Activity.class, name = "exclusiveGateway")
})
@Data
public class Activity extends Typed {
    public static final String TYPE = "Activity";

    private String processInstanceId;
    private String processDefinitionId;
    private String processBusinessKey;
    private String activityId;
    private String activityName;
    private Map<String, Object> variables;
    private Map<String, String> properties;

    @Override
    public String toString() {
        return TYPE + " with BusinessKey:" + getProcessBusinessKey();
    }

    public String getWorkStation() {
        if (!variables.containsKey("workStation")) {
            return null;
        } else {
            if(variables.get("workStation") == null){
                return null;
            }
            return variables.get("workStation").toString();
        }
    }

    public String getWorkOperation() {
        if (!variables.containsKey("workOperation")) {
            return null;
        } else {
            if(variables.get("workOperation") == null){
                return null;
            }
            return variables.get("workOperation").toString();
        }
    }

    public String getWorkStep() {
        if (!variables.containsKey("workStep")) {
            return null;
        } else {
            if(variables.get("workStep") == null){
                return null;
            }
            return variables.get("workStep").toString();
        }
    }

    /** check if activity is a userTask, Start or EndEvent, return false if not
     * @return check if the ActivityType is valid*/
    public boolean checkIsValidType(){
        switch(this.getActivityType()){
            case "startEvent": return true;
            case "userTask": return true;
            case "endEvent": return true;
            default: return false;
        }
    }

}
