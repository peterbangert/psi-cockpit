package de.psi.paip.mes.frontend;

import de.psi.paip.mes.frontend.model.workflow.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;


@Controller
public class PagesController {
    private static final Logger log = LoggerFactory.getLogger(PagesController.class);

    private final FrontendApplication application;
    private final String operationURL;
    private final long stationID = 5;// TODO dirty
    private String currentWorkflowProcessInstanceId = "-1"; // TODO dirty
    private Payload currentPayload;
    private String currentVin;
    private String currentProcessStepName;
    private String currentActivityName;
    private String currentStep;


    public PagesController(FrontendApplication application, @Value("${url.service.operation}") String operationURL) {
        this.application = application;
        this.operationURL = operationURL;
    }

    public String getCurrentWorkflowProcessInstanceId() {
        return currentWorkflowProcessInstanceId;
    }

    public void setCurrentWorkflowProcessInstanceId(String currentWorkflowProcessInstanceId) {
        this.currentWorkflowProcessInstanceId = currentWorkflowProcessInstanceId;
    }

    public void setCurrentPayload(Payload payload) {

        boolean finished = false;

        if(payload.workflowVariables.containsKey("activityType")) {
            finished = ((String)payload.workflowVariables.getOrDefault("activityType","")).endsWith("EndEvent");
        }

        if(finished) {
            log.info("Workflow finished: " + currentWorkflowProcessInstanceId);
            currentPayload = null;
            currentVin = "-2";
            currentProcessStepName = "-";
            currentActivityName = "-";
            currentStep = "-";
        } else {
            currentPayload = payload;
            currentVin = (String) currentPayload.variables.get("vin");
            currentProcessStepName = (String) currentPayload.variables.get("processStepName");
            currentActivityName = (String) currentPayload.workflowVariables.get("workflowActivityName");
            currentStep = currentProcessStepName + " / " + currentActivityName;
        }
    }

    /*
    @GetMapping(path = "/")
    public RedirectView index() {
        return new RedirectView("/index.html");
    }
    */

    public static class WorkStep {
        public String id;
        public String description;
        public String businessId;
        public String productionState;
        public String workState;
        public boolean requiresProofOfCompetence;
        public String vehicleIdentNumber;
        public String currentStation;
    }

}
