package de.psi.paip.mes.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.psi.paip.mes.frontend.model.workflow.*;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshTask;
import de.psi.paip.mes.frontend.openProcessInstances.OpenProcessInstance;
import de.psi.paip.mes.frontend.openProcessInstances.OpenProcessInstanceService;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import de.psi.paip.mes.frontend.terminalState.service.TerminalStateService;
import de.psi.paip.mes.frontend.workflow.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Controller
public class RestAPIController {

    private static final Logger LOG = LoggerFactory.getLogger(RestAPIController.class);

    private EventListener listener;

    private ObjectMapper objectMapper;

    private final String stationID = "5";

    @Value("${url.service.operation}")
    private String operationUrl = "";

    @Value("${url.service.appworkflow}")
    private String appWorkflow = "";

    @Value("${url.service.workflow}")
    private String workflowURL = "";

    @Autowired
    private OpenProcessInstanceService openProcessInstanceService;

    @Autowired
    private PagesController pagesController;

    @Autowired
    private TerminalStateService stateService;

    @Autowired
    private WorkflowWebSocketHandler wsHandler;


    public RestAPIController(EventListener listener, ObjectMapper objectMapper) {
        this.listener = listener;
        this.objectMapper = objectMapper;
    }

    /**
     * retrieve webapp configuration from application.yml
     *
     * @return JSON object with webapp configuration
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/configuration")
    public ResponseEntity<String> getApplicationConfiguration() {
        String result = "{\"workflowUrl\": \"" + operationUrl + "\", \"camundaUrl\": \"" + workflowURL + "\", \"websocketUrl\": \"" + appWorkflow + "\"}";

        return ResponseEntity.ok(result);
    }

    /**
     * endpoint to complete user tasks
     *
     * @param taskId       of user-task
     * @param completeTask payload of user-task with required variables
     * @return response from workflow service
     */
    @CrossOrigin(origins = "*")
    @PutMapping(path = "/user-tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable String taskId, @RequestBody CompleteTask completeTask) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.put(workflowURL + "/user-tasks/" + taskId + "/complete", completeTask);

            return ResponseEntity.ok("Task " + taskId + " received!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Task " + taskId + " already canceled!");

        }
    }

    /**
     * endpoint to cancel a user tasks
     *
     * @param taskId of user-task
     * @return response from workflow service
     */
    @CrossOrigin(origins = "*")
    @PutMapping(path = "/user-tasks/{taskId}/cancel")
    public ResponseEntity<String> completeTask(@PathVariable String taskId) {
        RestTemplate restTemplate = new RestTemplate();
        LOG.trace("cancel user-task");
        try {
            restTemplate.put(workflowURL + "/user-tasks/" + taskId + "/cancel", Void.class);

            return ResponseEntity.ok("Task " + taskId + " received!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Task " + taskId + " already canceled!");

        }
    }

    @PostMapping(path = "/api/processStep/start/{processStepId}")
    public ResponseEntity<String> startProcessStep(@PathVariable String processStepId) {
        LOG.info("startProcessStep: " + processStepId);

        // TODO check the processstep state and call workflow service
        // TODO wee need the VIN and precess step name here - how to get it > maybe from the first worfklow event

        CreateProcess createProcess = new CreateProcess();
        createProcess.processDefinitionKey = "ego_demo_1";
        createProcess.businessKey = processStepId;
        createProcess.variables.put("vin", "ABC4711");
        createProcess.variables.put("processStepName", "Fenster links");
        createProcess.variables.put("stationID", stationID);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> post = restTemplate.postForEntity(workflowURL + "/processes", createProcess, Void.class);

        URI uri = post.getHeaders().getLocation();
        String instanceId = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);

        pagesController.setCurrentWorkflowProcessInstanceId(instanceId);

        if (post.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("{}");
        } else {
            return ResponseEntity.badRequest().body("{ \"message\": \"ProcessStep already finished\"}");
        }
    }

    @PostMapping(path = "/api/workflow/finishUserTask/{workflowTaskId}")
    public ResponseEntity<String> finishUserTask(@PathVariable String workflowTaskId, @RequestBody List<FormData> formDatas) {
        LOG.info("finishUserTask: " + workflowTaskId);

        CompleteTask completeTask = new CompleteTask();
        for (FormData formData : formDatas) {
            completeTask.variables.put(formData.name, formData.value);
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> exchange = restTemplate.exchange(workflowURL + "/tasks/" + workflowTaskId + "/complete", HttpMethod.PUT, new HttpEntity<CompleteTask>(completeTask), Void.class);

        if (exchange.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok("{}");
        } else {
            return ResponseEntity.badRequest().body("{ \"message\": \"No all fields filled\"}");
        }
    }

    /**
     * method is used to start a Camunda Workflow
     *
     * @param processDefinitionKey key of Camunda workflow
     * @param businessKey          property which can be used to transport additional information
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/processes/{processDefinitionKey}/{businessKey}")
    public ResponseEntity<String> startProcess(@PathVariable String processDefinitionKey, @PathVariable String businessKey) {
        LOG.info("Start workflow with BusinessKey:" + businessKey + " with ProcessDefinitionKey:" + processDefinitionKey);
        CreateProcess createProcess = new CreateProcess();
        createProcess.processDefinitionKey = processDefinitionKey;
        createProcess.businessKey = businessKey;

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Void> response = restTemplate.exchange(workflowURL + "/processes", HttpMethod.POST, new HttpEntity<CreateProcess>(createProcess), Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("{}");
            } else {
                LOG.trace("Start workflow with ProcessDefinitionKey" + processDefinitionKey + "failed");
                return ResponseEntity.badRequest().body("{ \"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            LOG.trace(e.toString());
            return ResponseEntity.badRequest().body("{ \"message\": \"An error occured\"}");
        }
    }

    /**
     * fetch open User-Tasks for a station
     *
     * @param payload   RequestBody
     * @param stationId of the station to fetch open User-tasks
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/processes/{stationId}")
    public ResponseEntity<String> fetchOpenUserTasks(@PathVariable String stationId, @RequestBody OpenTaskPayload payload) {
        LOG.info("Fetch open user-tasks for a station: " + stationId);
        try {
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.getForEntity(workflowURL + "/processes?businessKey=" + stationId, String.class);

            LOG.trace(response.getBody());
            ProcessInstance[] allOpenInstances = objectMapper.readValue(response.getBody(), ProcessInstance[].class);

            LOG.trace(payload.toString());
            if (payload.getWorkOperation() != null && payload.getWorkStep() != null) {
                LOG.trace("Find User-Task for Right-Screen");
                findUserTaskForRightSide(allOpenInstances, payload);
            } else {
                LOG.trace("Find User-Task for Left-Screen");
                findUserTaskForLeftSide(allOpenInstances);
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok().body("{\"message\": \"Request successfull\"}");
            } else {
                if (response.getBody().isEmpty()) {
                    LOG.trace("No open instances for this station found.");
                    return ResponseEntity.ok().body("{\"message\": \"No open instances found\"}");
                } else {
                    LOG.trace("Not able to map open process instance to DTO (ProcessInstance.class).");
                    return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
                }
            }
        } catch (Exception e) {
            LOG.trace(e.toString());
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured while mapping.\"}");
        }
    }

    /**
     * helper method to find a user-task in an array
     */
    private void findUserTaskForLeftSide(ProcessInstance[] allOpenInstances) {
        LOG.trace("search for user-task");
        Optional<ProcessInstance> instance = Arrays.stream(allOpenInstances).filter(
                pInstance -> pInstance != null && pInstance.getActivities().length > 0 && Arrays.stream(pInstance.getActivities()).allMatch(
                        activity -> activity.getActivityType() != null && activity.getActivityType().equals("userTask") && checkLeftSide(activity)
                                && checkValidWorkStation(activity)
                )
        ).findFirst();

        if (instance.isPresent()) {
            if (instance.get().getFirstActivity() != null) {
                try {
                    UserTask userTask = (UserTask) instance.get().getFirstActivity();
                    LOG.trace(userTask.toString() + " will be sent to LeftScreen");
                    listener.handle(objectMapper.writeValueAsString(userTask));
                } catch (Exception e) {
                    LOG.trace(e.toString());
                }
            }
        } else {
            LOG.trace("no user-task found");
        }
    }

    /**
     * helper method to find a user-task in an array
     */
    private void findUserTaskForRightSide(ProcessInstance[] allOpenInstances, OpenTaskPayload payload) {
        LOG.trace("search for user-task");
        Optional<ProcessInstance> instance = Arrays.stream(allOpenInstances).filter(
                pInstance -> pInstance != null && pInstance.getActivities().length > 0 && Arrays.stream(pInstance.getActivities()).allMatch(
                        activity -> activity.getActivityType() != null && activity.getActivityType().equals("userTask") && checkRightSide(activity, payload)
                                && checkValidWorkStation(activity)
                )
        ).findFirst();

        if (instance.isPresent()) {
            LOG.trace(instance.toString());
            if (instance.get().getFirstActivity() != null) {
                try {
                    UserTask userTask = (UserTask) instance.get().getFirstActivity();
                    listener.handle(objectMapper.writeValueAsString(userTask));
                    LOG.trace(userTask.toString() + " will be sent to RightScreen");
                } catch (Exception e) {
                    LOG.trace(e.toString());
                }
            }
        } else {
            LOG.trace("no user-task found");
        }
    }

    /**
     * helper method to check if workStation is valid
     */
    private boolean checkValidWorkStation(Activity activity) {
        if (activity.getVariables() != null && activity.getVariables().containsKey("workStation")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * helper method to get workOperation
     */
    private String getWorkOperation(Activity activity) {

        if (activity.getVariables() != null && activity.getVariables().containsKey("workOperation")) {
            if (activity.getVariables().get("workOperation") != null) {
                return activity.getVariables().get("workOperation").toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * helper method to get workStepId
     */
    private String getWorkStep(Activity activity) {

        if (activity.getVariables() != null && activity.getVariables().containsKey("workStep")) {
            if (activity.getVariables().get("workStep") != null) {
                return activity.getVariables().get("workStep").toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * helper method to check if a user-task is supposed to be sent to left screen
     */
    private boolean checkLeftSide(Activity activity) {
        String workOperation = getWorkOperation(activity);
        String workStep = getWorkStep(activity);
        LOG.trace(workStep);
        /** is supposed to be sent to left.screen if ... */
        if ((workOperation == null && workStep == null) || (workOperation != null && workStep == null)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * helper method to check if a user-task is supposed to be sent to right screen
     */
    private boolean checkRightSide(Activity activity, OpenTaskPayload payload) {
        String workOperation = getWorkOperation(activity);
        String workStep = getWorkStep(activity);

        /** is supposed to be sent to right.screen if ... */
        if (workOperation != null && workStep != null) {
            if (workOperation.equals(payload.getWorkOperation()) && workStep.equals(payload.getWorkStep())) {
                return true;
            } else {
                // LOG.trace("workOperation or workStep of user-task doesn't match station's operation and stepworkOperation ("+workOperation+") or workStep ("+workStep+")");
                return false;
            }
        } else {
            // LOG.trace(activity.getProcessInstanceId()+" Either workOperation null ( "+workOperation+") or workStep ("+workStep+") null");
            return false;
        }
    }


    /**
     * method is used to start a Camunda Workflow with payload
     *
     * @param process for camunda rest call
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @PostMapping(path = "/processes")
    public ResponseEntity<String> startProcessWithPayload(@RequestBody CreateProcess process) {
        LOG.info("Call workflow with key: " + process.processDefinitionKey);

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Void> response = restTemplate.exchange(workflowURL + "/processes", HttpMethod.POST, new HttpEntity<CreateProcess>(process), Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                HttpHeaders headers = response.getHeaders();

                if (checkValidBody(process)) {
                    String location = response.getHeaders().getLocation().toString();
                    String processInstanceId = location.substring(location.lastIndexOf("/") + 1);

                    OpenProcessInstance instance = new OpenProcessInstance(
                            processInstanceId,
                            process.variables.get("workStation").toString(),
                            process.variables.get("workOperation").toString(),
                            process.variables.get("workStep").toString()
                    );
                    openProcessInstanceService.add(instance);

                    TerminalState state = stateService.getForTerminal(process.variables.get("workStation").toString());

                    RefreshTask task = new RefreshTask(process.variables.get("workStation").toString(), process.variables.get("workOperation").toString(), "refresh terminal", "~*~", true, state);
                    LOG.trace(task.toString());

                    wsHandler.pushServiceTask(task);

                }
                LOG.trace("Workflow started successfully");
                return ResponseEntity.ok("{\"message\": \"Workflow started successfully\"}");
            } else {
                LOG.trace("Response Status-Code not successful start workflow:" + process.processDefinitionKey + " failed");
                return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            LOG.trace("Start workflow with ProcessDefinitionKey" + process.processDefinitionKey + " failed");
            LOG.trace(e.toString());
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
        }
    }

    /**
     * Helper Method to check if RequestBody is valid
     *
     * @param process Process that will be startet if valid
     * @return true if RequestBody is valid
     */
    public boolean checkValidBody(CreateProcess process) {
        if (process.variables.containsKey("workOperation") && process.variables.containsKey("workStep") && process.variables.containsKey("workStation")) {
            if (process.variables.get("workOperation") != null && process.variables.get("workStep") != null && process.variables.get("workStation") != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * fetch station information from MES
     *
     * @param stationId of requested terminal
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}")
    public ResponseEntity<String> fetchStation(@PathVariable String stationId) {
        LOG.info("Fetch station information for station: " + stationId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/" + stationId, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
        }
    }

    /**
     * fetch operations for station
     *
     * @param stationId of requested terminal
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}/operations")
    public ResponseEntity<String> fetchOperations(@PathVariable String stationId) {
        LOG.info("Fetch operations for station: " + stationId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/" + stationId + "/operations", String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
        }
    }

    /**
     * fetch one Operation operations for station
     *
     * @param stationId   of requested terminal
     * @param operationId of requested operation
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}/operations/{operationId}")
    public ResponseEntity<String> fetchOperation(@PathVariable String stationId, @PathVariable String operationId) {
        LOG.info("Fetch single operation for station: " + stationId + ": " + operationId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/" + stationId + "/operations/" + operationId, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
        }
    }

    /**
     * fetch worksteps for operation
     *
     * @param stationId   of requested terminal
     * @param operationId of requested operation
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}/operations/{operationId}/steps")
    public ResponseEntity<Optional<List<Map<String, Object>>>> fetchWorkstepsForOperation(@PathVariable String stationId, @PathVariable String operationId) {
        LOG.info("Fetch worksteps for station " + stationId + " with operation " + operationId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/"
                    + stationId + "/operations/" + operationId + "/steps", String.class);

            if (response.getStatusCode().is2xxSuccessful()) {

                // LOG.trace(response.getBody());
                Optional<List<Map<String, Object>>> steps = Optional.of(objectMapper.readValue(response.getBody(), List.class));
                Optional<List<OpenProcessInstance>> instances = openProcessInstanceService.getAllForWorkoperation(stationId, operationId);

                // check if any step and any instance is present
                if (instances.isPresent() && steps.isPresent()) {
                    instances.get().forEach(openInstance ->
                            steps.get().forEach(step -> {

                                // check if stepId is set
                                if (step.containsKey("stepId")) {
                                    // LOG.trace(step.toString());
                                    if (openInstance.getWorkStep().equals(step.get("stepId"))) {
                                        step.put("isRunning", "RUNNING");
                                    }
                                }
                            })
                    );
                }
                return ResponseEntity.ok(steps);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * fetch one workstep for operation
     *
     * @param stationId   of requested terminal
     * @param operationId of requested operation
     * @param stepId      of requested workstep
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}/operations/{operationId}/steps/{stepId}")
    public ResponseEntity<Optional<Map<String, Object>>> fetchWorkstepForOperation(@PathVariable String stationId, @PathVariable String operationId, @PathVariable String stepId) {
        LOG.info("Fetch single workstep for station " + stationId + " with operation " + operationId + ": " + stepId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/"
                    + stationId + "/operations/" + operationId + "/steps/" + stepId, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {

                Optional<Map<String, Object>> step = Optional.of(objectMapper.readValue(response.getBody(), Map.class));
                Optional<List<OpenProcessInstance>> instances = openProcessInstanceService.getAllForWorkoperation(stationId, operationId);
                // check if any step and any instance is present
                if (instances.isPresent() && step.isPresent()) {
                    instances.get().forEach(openInstance -> {
                        if (openInstance.getWorkStep().equals(step.get().get("stepId"))) {
                            step.get().put("isRunning", "RUNNING");
                            // LOG.trace(step.get().toString());
                        }
                    });
                }

                return ResponseEntity.ok(step);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * fetch instructions for operation
     *
     * @param stationId   of requested terminal
     * @param operationId of requested operation
     * @param stepId      of requested workstep
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}/operations/{operationId}/steps/{stepId}/instructions")
    public ResponseEntity<String> fetchInstructionsForOperation(
            @PathVariable String stationId,
            @PathVariable String operationId,
            @PathVariable String stepId) {
        LOG.info("Fetch instructions for station " + stationId + " with operation " + operationId + " with step " + stepId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/"
                    + stationId + "/operations/" + operationId + "/steps/" + stepId + "/instructions/", String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
        }
    }

    /**
     * fetch materials for operation
     *
     * @param stationId   of requested terminal
     * @param operationId of requested operation
     * @param stepId      of requested workstep
     * @return response http entity
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "/frontend/stations/{stationId}/operations/{operationId}/steps/{stepId}/materials")
    public ResponseEntity<String> fetchMaterialsForOperation(
            @PathVariable String stationId,
            @PathVariable String operationId,
            @PathVariable String stepId) {
        LOG.info("Fetch materials for station " + stationId + " with operation " + operationId + " with step " + stepId);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(operationUrl + "/frontend/stations/"
                    + stationId + "/operations/" + operationId + "/steps/" + stepId + "/materials/", String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"An error occured\"}");
        }
    }


    private static class CreateProcess {
        public String processDefinitionKey;
        public String businessKey;
        public Map<String, Object> variables = new HashMap<>();

        public String toString() {
            return "processDefinitionKey: " + processDefinitionKey + " businessKey: " + businessKey + " variables:" + variables.toString();
        }
    }

    private static class CompleteTask {
        public Map<String, Object> variables = new HashMap<>();
    }

    public static class FormData {
        public String name;
        public String value;
    }
}
