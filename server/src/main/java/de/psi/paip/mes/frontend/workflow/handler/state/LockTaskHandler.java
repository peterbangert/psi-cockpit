package de.psi.paip.mes.frontend.workflow.handler.state;

import de.psi.paip.mes.frontend.terminalState.service.TerminalStateService;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription.Property;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription.Property.Type;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskListener;
import de.psi.paip.mes.starter.workflow.annotation.Variable;
import de.psi.paip.mes.starter.workflow.model.ServiceTaskRequest;
import de.psi.paip.mes.starter.workflow.model.ServiceTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Order(0)
@Controller
public class LockTaskHandler {
   private static final Logger log = LoggerFactory.getLogger(LockTaskHandler.class);

   private static final String TOPIC_PREFIX = "psi.mes.pia.state.lock.";
   private static final String TASK_NAME_PREFIX = "PSI-MES//PIA/State/Lock/";

   private final TerminalStateService stateService;

   @Autowired
   public LockTaskHandler(TerminalStateService stateService) {
      this.stateService = stateService;
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "Lock")
   @ServiceTaskDescription(name = TASK_NAME_PREFIX + "Lock Terminal",
         properties = {
               @Property(
                     type = Type.STRING,
                     key = "terminalId",
                     label = "Terminal",
                     notEmpty = true),
               @Property(
                     type = Type.STRING,
                     key = "lockMessage",
                     label = "Message"),
               @Property(
                     type = Type.STRING,
                     key = "stateNotFoundErrorCode",
                     label = "Error Code (State object not found)",
                     notEmpty = true)})
   @Transactional
   public ServiceTaskResponse handleLock(ServiceTaskRequest request,
                                         @Variable(value = "terminalId") String terminalId,
                                         @Variable(value = "lockMessage", required = false) String message,
                                         @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Received lock request for terminal {}", terminalId);

      if (!stateService.stateExists(terminalId.toString())) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      stateService.updateLocked(terminalId.toString(), true);
      stateService.updateMessage(terminalId.toString(), message);

      return ServiceTaskResponse.completed().build();
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "Unlock")
   @ServiceTaskDescription(
         name = TASK_NAME_PREFIX + "Unlock Terminal",
         properties = {
               @Property(
                     type = Type.STRING,
                     key = "terminalId",
                     label = "Terminal",
                     notEmpty = true),
               @Property(
                     type = Type.STRING,
                     key = "stateNotFoundErrorCode",
                     label = "Error Code (State object not found)",
                     notEmpty = true)})
   @Transactional
   public ServiceTaskResponse handleUnlock(ServiceTaskRequest request,
                                           @Variable(value = "terminalId") String terminalId,
                                           @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Received unlock request for terminal {}", terminalId);

      if (!stateService.stateExists(terminalId)) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      stateService.updateLocked(terminalId, false);
      stateService.updateMessage(terminalId, null);

      return ServiceTaskResponse.completed().build();
   }
}
