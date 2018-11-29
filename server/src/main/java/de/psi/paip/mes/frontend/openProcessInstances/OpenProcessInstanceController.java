package de.psi.paip.mes.frontend.openProcessInstances;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/openProcessInstances")
public class OpenProcessInstanceController {

        private static final Logger log = LoggerFactory.getLogger(OpenProcessInstance.class);

        private final OpenProcessInstanceService openProcessInstanceService;

        @Autowired
        public OpenProcessInstanceController( OpenProcessInstanceService openProcessInstanceService) {
            this.openProcessInstanceService = openProcessInstanceService;
        }

        @RequestMapping(path = "/", method = RequestMethod.GET)
        public ResponseEntity<Optional<List<OpenProcessInstance>>> get() {
            log.trace("Getting all open instances {}...");

            Optional<List<OpenProcessInstance>> instances = Optional.of(openProcessInstanceService.getAll());

            if(instances.isPresent()) {
                return ResponseEntity.ok().body(instances);
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }

        @RequestMapping(path = "/{workStation}", method = RequestMethod.GET)
        public ResponseEntity<Optional<List<OpenProcessInstance>>> getAllForWorkstation(@PathVariable("workStation") String workStation) {
            log.trace("Getting all open-process-instances for workStation {}...", workStation);

            Optional<List<OpenProcessInstance>> instances  = openProcessInstanceService.getAllForWorkstation(workStation);

            if (instances.isPresent()) {
                return ResponseEntity.ok().body(instances);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

        }

        @RequestMapping(path = "/{workStation}/workOperation/{workOperation}", method = RequestMethod.GET)
        public ResponseEntity<Optional<List<OpenProcessInstance>>> getAllForWorkOperation(@PathVariable("workStation") String workStation,
                                                                                        @PathVariable("workOperation") String workOperation) {
            log.trace("Getting all open-process-instances for station: "+workStation+" and workOperation {}...", workOperation);

            Optional<List<OpenProcessInstance>> instances = openProcessInstanceService.getAllForWorkoperation(workStation, workOperation);

            if (instances.isPresent()) {
                return ResponseEntity.ok().body(instances);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

        }

}
