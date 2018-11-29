package de.psi.paip.mes.frontend.workflow.handler.state;

import de.psi.paip.mes.frontend.terminalState.model.Status;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
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
public class StatusTaskHandler {
   private static final Logger log = LoggerFactory.getLogger(StatusTaskHandler.class);

   private static final String TOPIC_PREFIX = "psi.mes.pia.state.status.";
   private static final String TASK_NAME_PREFIX = "PSI-MES//PIA/State/Status/";

   private final TerminalStateService stateService;

   @Autowired
   public StatusTaskHandler(TerminalStateService stateService) {
      this.stateService = stateService;
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "Reset")
   @ServiceTaskDescription(
           name = TASK_NAME_PREFIX + "Reset",
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
   public ServiceTaskResponse handleReset(ServiceTaskRequest request,
                                          @Variable(value = "terminalId") String terminalId,
                                          @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Received reset status request for terminal {}", terminalId);

      if (!stateService.stateExists(terminalId)) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      stateService.updateStatus(terminalId, Status.DEFAULT);

      return ServiceTaskResponse.completed().build();
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "SetAbnormal")
   @ServiceTaskDescription(
           name = TASK_NAME_PREFIX + "Set Abnormal",
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
   public ServiceTaskResponse handleSetAbnormal(ServiceTaskRequest request,
                                                @Variable(value = "terminalId") String terminalId,
                                                @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Received abnormal status request for terminal {}", terminalId);

      if (!stateService.stateExists(terminalId)) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      stateService.updateStatus(terminalId, Status.ABNORMAL);

      return ServiceTaskResponse.completed().build();
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "SetWaiting")
   @ServiceTaskDescription(
           name = TASK_NAME_PREFIX + "Set Waiting",
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
   public ServiceTaskResponse handleSetWaiting(ServiceTaskRequest request,
                                               @Variable(value = "terminalId") String terminalId,
                                               @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Received waiting status request for terminal {}", terminalId);

      if (!stateService.stateExists(terminalId)) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      stateService.updateStatus(terminalId, Status.WAITING);

      return ServiceTaskResponse.completed().build();
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "SetInProcess")
   @ServiceTaskDescription(
           name = TASK_NAME_PREFIX + "Set In-Process",
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
   public ServiceTaskResponse handleSetInProcess(ServiceTaskRequest request,
                                                 @Variable(value = "terminalId") String terminalId,
                                                 @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Received in-process status request for terminal {}", terminalId);

      if (!stateService.stateExists(terminalId)) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      stateService.updateStatus(terminalId, Status.IN_PROCESS);

      return ServiceTaskResponse.completed().build();
   }

   @ServiceTaskListener(topic = TOPIC_PREFIX + "GetTerminalProcessState")
   @ServiceTaskDescription(
           name = TASK_NAME_PREFIX + "Get Process-State",
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
   public ServiceTaskResponse getTerminalState(ServiceTaskRequest request,
                                                 @Variable(value = "terminalId") String terminalId,
                                                 @Variable(value = "stateNotFoundErrorCode") String errorCode) {

      log.trace("Getting TerminalState for Terminal: {}", terminalId);

      if (!stateService.stateExists(terminalId)) {
         return ServiceTaskResponse.failed(errorCode).build();
      }

      TerminalState state = stateService.getForTerminal(terminalId);
      log.trace(state.toString());
      return ServiceTaskResponse.completed().variable("terminalState",state.getStatus()).build();
   }
}
