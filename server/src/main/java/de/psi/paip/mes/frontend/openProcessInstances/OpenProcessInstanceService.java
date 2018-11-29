package de.psi.paip.mes.frontend.openProcessInstances;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenProcessInstanceService {


    private final Logger log = LoggerFactory.getLogger(OpenProcessInstanceService.class);

    private final OpenProcessInstanceRepository repository;

    @Autowired
    public OpenProcessInstanceService(OpenProcessInstanceRepository repository) {
        this.repository = repository;
    }

    /**
     * get a List of all open instances
     * @return List of all OpenProcessInstances
     */
    public List<OpenProcessInstance> getAll() {
        return repository.findAll(sortByProcessInstanceId());
    }

    public Sort sortByProcessInstanceId() {
        return new Sort(Sort.Direction.ASC, "processInstanceId");
    }

    /**
     * @param workStation id of the Terminal
     * @return List of all open instances for a workStation
     */
    public Optional<List<OpenProcessInstance>> getAllForWorkstation(String workStation) {
        return repository.findAllForStation(workStation);
    }

    /**
     * @param workStation id of the Terminal
     * @param workOperation id of a specific operation of this terminal
     * @return List of all open instances for a workStation
     */
    public Optional<List<OpenProcessInstance>> getAllForWorkoperation(String workStation, String workOperation) {
        return repository.findAllForOperation(workStation, workOperation);
    }

    /**
     * check if instance with process-instance-id exists
     * @param instance OpenProcessInstance
     * @return true if instance exists in db
     */
    public boolean exists(OpenProcessInstance instance) {
        return repository.findByProcessInstanceId(instance.getProcessInstanceId()).isPresent();
    }

    /**
     * check if instance with process-instance-id exists
     * @param instanceId id of the OpenProcessInstance
     * @return true if instance exists in db
     */
    public boolean exists(String instanceId) {
        return repository.findByProcessInstanceId(instanceId).isPresent();
    }

    /**
     * not in use for now
     */
    public void deleteAll() {
        log.info("Deleting all open instances...");
        repository.deleteAll();
    }

    public void add(OpenProcessInstance instance) {
        log.info("Adding open-instance with processInstanceId: {}...", instance.getProcessInstanceId() + " to Repository");
        repository.save(instance);
    }

    /**
     * delete using the DTO
     * @param instance OpenProcessInstance
     */
    @Transactional
    public void delete(OpenProcessInstance instance) {
        log.info("Deleting OpenProcessInstance with Instance Id: {}...", instance.getProcessInstanceId() + " from Repository");
        repository.delete(instance);
    }

    /**
     * delete using the id of the instance
     * @param openProcessInstanceId id of the OpenProcessInstance
     */
    @Transactional
    public void delete(String openProcessInstanceId) {
        log.info("Deleting OpenProcessInstance with Instance Id: {}..." + openProcessInstanceId + " from Repository");
        repository.deleteByProcessInstanceId(openProcessInstanceId);
    }


}
