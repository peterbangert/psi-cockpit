package de.psi.paip.mes.frontend.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.psi.paip.mes.frontend.WorkflowWebSocketHandler;
import de.psi.paip.mes.frontend.model.workflow.Activity;
import de.psi.paip.mes.frontend.model.workflow.Typed;
import de.psi.paip.mes.frontend.model.workflow.UserTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshTask;
import de.psi.paip.mes.frontend.openProcessInstances.OpenProcessInstanceService;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import de.psi.paip.mes.frontend.terminalState.service.TerminalStateService;
import de.psi.paip.mes.starter.artemis.annotation.MessageListener;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final ObjectMapper mapper;
    private final WorkflowWebSocketHandler wsHandler;
    private final OpenProcessInstanceService openProcessInstanceService;
    private final TerminalStateService stateService;

    private static final String DESCRIPTION_KEY = "description";

    @Autowired
    public EventListener(ObjectMapper mapper, WorkflowWebSocketHandler wsHandler,OpenProcessInstanceService openProcessInstanceService, TerminalStateService stateService) {
        this.mapper = mapper;
        this.wsHandler = wsHandler;
        this.openProcessInstanceService= openProcessInstanceService;
        this.stateService = stateService;
    }

    @MessageListener(destination = "workflow")
    public void handle(Message<String> jsonMessage) {
        String messageType = determineTaskType(jsonMessage);

        // log.trace(jsonMessage.getPayload());

        if(messageType == null){
            messageType= Typed.UNKNOW_TYPE;
        }

        switch (messageType) {
            case UserTask.TYPE:
                log.trace("Received a user task. Constructing object...");
                handleUserTask(parseAsUserTask(jsonMessage.getPayload()));
                return;
            case Typed.UNKNOW_TYPE:
                log.warn("Unknown event type. Ignoring...");
                return;
            default:
                handle(jsonMessage.getPayload());
        }
    }

    private UserTask parseAsUserTask(String jsonString) {
        try {
            final UserTask task = mapper.readValue(jsonString, UserTask.class);

            if (!task.getProperties().containsKey(DESCRIPTION_KEY)) {
                return task;
            }
            // log.trace(task.toString());
            final String renderedDescription = renderDescription(task.getProperties().get(DESCRIPTION_KEY), task.getVariables());

            task.getProperties().put(DESCRIPTION_KEY, renderedDescription);

            return task;
        } catch (IOException e) {
            throw new RuntimeException("Invalid JSON format!");
        }
    }

    private String renderDescription(String descriptionFormatString, Map<String, Object> processVariables) {
        final String rendered = new StrSubstitutor(processVariables, "${", "}").replace(descriptionFormatString);

        if (descriptionFormatString.equals(rendered)) {
            return rendered;
        }

        return renderDescription(rendered, processVariables);
    }

    private void handleUserTask(UserTask userTask) {
        try {
            final String jsonString = mapper.writeValueAsString(userTask);

            JsonElement jsonTree = new JsonParser().parse(jsonString);

            wsHandler.pushWorkflowChange(jsonTree.getAsJsonObject());
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize user task: {}", e.getMessage());
        }
    }

    public void handle(String message) {
        log.trace("Message payload {}", message);
        JsonParser jsonParser = new JsonParser();

        try{
            Activity activity = mapper.readValue(message, Activity.class);

            if(activity.checkIsValidType()){

                /**if endEvent check if open instance exists in db, if so delete it*/
                if(activity.getActivityType().equals("endEvent") && openProcessInstanceService.exists(activity.getProcessInstanceId())){
                    log.trace("Found instance with ProcessInstanceId:"+activity.getProcessInstanceId());
                    openProcessInstanceService.delete(activity.getProcessInstanceId());

                    TerminalState state = stateService.getForTerminal(activity.getWorkStation());
                    RefreshTask task = new RefreshTask(activity.getWorkStation(), activity.getWorkOperation(), "workStepCompleted~*~"+activity.getProcessInstanceId(), "~*~",true, state);
                    log.trace(task.toString());

                    wsHandler.pushServiceTask(task);
                }else{
                    log.trace("Found activity:"+activity.getProcessInstanceId()+" Type:"+activity.getActivityType());
                    JsonElement jsonTree = jsonParser.parse(mapper.writeValueAsString(activity));
                    JsonObject activityObject = jsonTree.getAsJsonObject();
                    wsHandler.pushWorkflowChange(activityObject);
                }
            }else{
                log.warn("Unknown event type. Ignoring...");
            }
        }catch(Exception e){
            log.warn("Message could not be parsed, because it isn't a known type of Activity.");
            // log.error(e.toString());
        }
    }

    private String determineTaskType(Message<String> jsonMessage) {
        try {
            return mapper.readValue(jsonMessage.getPayload(), Typed.class).getActivityType();
        } catch (IOException e) {
            log.error("Error while parsing message as typed activity: {}", e.getMessage());
            return Typed.UNKNOW_TYPE;
        }
    }

}
