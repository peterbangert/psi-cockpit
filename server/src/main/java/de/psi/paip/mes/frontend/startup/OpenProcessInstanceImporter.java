package de.psi.paip.mes.frontend.startup;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.psi.paip.mes.frontend.model.workflow.Activity;
import de.psi.paip.mes.frontend.model.workflow.ProcessInstance;
import de.psi.paip.mes.frontend.openProcessInstances.OpenProcessInstance;
import de.psi.paip.mes.frontend.openProcessInstances.OpenProcessInstanceService;
import de.psi.paip.mes.frontend.terminal.model.Terminal;
import de.psi.paip.mes.frontend.terminal.service.TerminalService;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Order(1)
@Component
public class OpenProcessInstanceImporter implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(OpenProcessInstance.class);

    @Value("${startup.import.file-name}")
    private String fileName;

    @Value("${startup.import.update}")
    private UpdateStrategy updateStrategy;

    @Value("${url.service.workflow}")
    private String server;

    private final OpenProcessInstanceService service;

    private RestTemplate rest;
    private HttpHeaders headers;
    private ObjectMapper objectMapper;
    private ResponseEntity<String> exchange;

    @Autowired
    public OpenProcessInstanceImporter(OpenProcessInstanceService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;

        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    @Override
    public void run(ApplicationArguments args) throws JsonParseException, JsonMappingException, IOException {
        if(!checkWorkflowService()){
            // thread to try to
            log.trace("Service-workflow not reachable.");
        }
        log.trace("Service-workflow reachable.");
        DatabaseState.setDatabaseWorkingOpenProcessInstance(true);
        if (UpdateStrategy.NONE == updateStrategy) {
            DatabaseState.setDatabaseWorkingOpenProcessInstance(false);
            return;
        }
        if (UpdateStrategy.RESET == updateStrategy) {
            service.deleteAll();
        }

        if (server != null) {
            DatabaseState.setServer(server);
        } else {
            server = DatabaseState.getServer();
        }

        updateOpenInstances();
        DatabaseState.setDatabaseWorkingTerminal(false);
    }

    private List<OpenProcessInstance> instanceMapper(ProcessInstance[] allOpenInstances) {

        List<OpenProcessInstance> openProcessInstanceList = new ArrayList<OpenProcessInstance>();

        Arrays.stream(allOpenInstances).forEach(i ->
                Arrays.stream(i.getActivities()).filter(a -> checkVariables(a)).forEach(a ->
                        openProcessInstanceList.add(
                                new OpenProcessInstance(
                                        i.getInstanceId(),
                                        a.getWorkStation(),
                                        a.getWorkOperation(),
                                        a.getWorkStep()
                                )
                        )
                )
        );
        return openProcessInstanceList;
    }

    /**
     * check if variables fulfill requirement to save open-instance to db
     */
    private boolean checkVariables(Activity a) {
        if (a.getVariables().containsKey("workStation") && a.getVariables().containsKey("workOperation") && a.getVariables().containsKey("workStep")
                && a.getActivityType().equals("userTask") && a.getProcessBusinessKey() != null) {
            return true;
        } else {
            return false;
        }
    }

    private void add(OpenProcessInstance instance) {
        if (service.exists(instance)) {
            return;
        }

        log.info("Importing open-process-instance {}...", instance.getProcessInstanceId());
        service.add(instance);
    }

    private void removeDead(List<OpenProcessInstance> instances) {
        for (OpenProcessInstance persistedInstance : service.getAll()) {
            if (instances.stream()
                    .noneMatch(instance -> persistedInstance.getProcessInstanceId().equals(instance.getProcessInstanceId()))
            ) {
                service.delete(persistedInstance);
            }
        }
    }

    public void updateOpenInstances(){
        try {
            HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
            ResponseEntity<String> responseEntity = rest.exchange(server + "/processes", HttpMethod.GET,
                    requestEntity, String.class);

            ProcessInstance[] allOpenInstances = objectMapper.readValue(responseEntity.getBody(), ProcessInstance[].class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                Optional<List<OpenProcessInstance>> instances = Optional.of(instanceMapper(allOpenInstances));

                if (instances.isPresent()) {
                    instances.get().forEach(a -> add(a));
                }

                if (UpdateStrategy.SYNC == updateStrategy) {
                    removeDead(instances.get());
                }
            }
        } catch (Exception e) {
            log.trace(e.toString());
        }
    }

    public boolean checkWorkflowService(){
        try {
            HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
            ResponseEntity<String> responseEntity = rest.exchange(server + "/actuator/health", HttpMethod.GET,requestEntity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.trace(responseEntity.toString());
            }else{
                log.trace("Statuscode:"+responseEntity.getStatusCode());
            }
            return true;
        } catch (Exception e) {
            log.trace(e.toString());
            return false;
        }
    }
}
