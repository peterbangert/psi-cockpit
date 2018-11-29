package de.psi.paip.mes.frontend.workflow.handler;

import com.fasterxml.jackson.databind.ObjectMapper;


import de.psi.paip.mes.frontend.WorkflowWebSocketHandler;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.BroadcastStateTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshAllNeighboursTask;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshTask;
import de.psi.paip.mes.frontend.terminal.model.Terminal;
import de.psi.paip.mes.frontend.terminal.service.TerminalService;
import de.psi.paip.mes.frontend.terminalState.controller.TerminalStateController;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import de.psi.paip.mes.frontend.terminalState.service.TerminalStateService;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription.Property;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription.Property.Type;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskListener;
import de.psi.paip.mes.starter.workflow.annotation.Variable;
import de.psi.paip.mes.starter.workflow.model.ServiceTaskRequest;
import de.psi.paip.mes.starter.workflow.model.ServiceTaskResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Order(0)
@Controller
public class MainTaskHandler {
	private static final Logger log = LoggerFactory.getLogger(MainTaskHandler.class);

	private static final String TOPIC_PREFIX = "psi.mes.pia.";
	private static final String TASK_NAME_PREFIX = "PSI-MES//PIA/";

	private final TerminalStateService stateService;
	private final TerminalService terminalService;
	private final TerminalStateController stateController;

	private final WorkflowWebSocketHandler wsHandler;

	@Autowired
	public MainTaskHandler(TerminalStateService stateService, TerminalService terminalService,
						   WorkflowWebSocketHandler wsHandler, ObjectMapper mapper, TerminalStateController stateController) {
		this.stateService = stateService;
		this.wsHandler = wsHandler;
		this.terminalService = terminalService;
		this.stateController = stateController;
	}

	@ServiceTaskListener(topic = TOPIC_PREFIX + "RefreshGui")
	@ServiceTaskDescription(name = TASK_NAME_PREFIX + "Refresh GUI", properties = {
			@Property(type = Type.STRING, key = "terminalId", label = "Terminal", notEmpty = true),
			@Property(type = Type.STRING, key = "operationId", label = "OperationID", notEmpty = true),
			@Property(type = Type.STRING, key = "infoText", label = "Infotext", notEmpty = true),
			@Property(type = Type.STRING, key = "separator", label = "Separator", notEmpty = true),
			@Property(type = Type.STRING, key = "instanceNotRunningErrorCode", label = "Error Code (Terminal instance not running)", notEmpty = true) })
	@Transactional
	public ServiceTaskResponse handleRefreshGui(ServiceTaskRequest request,
			@Variable(value = "separator") String separator, @Variable(value = "infoText") String infoText,
			@Variable(value = "operationId") String operationId, @Variable(value = "terminalId") String terminalId,
			@Variable(value = "instanceNotRunningErrorCode") String notRunningErrorCode) {

		log.trace("Received refresh request for terminal {}", terminalId);

		if(operationId.equals("null")){
			operationId="";
		}

		this.stateController.updateOperationId(terminalId, operationId);

		// check if terminal session exists
		if (!wsHandler.checkSessionIsOpen(terminalId)) {
			log.trace("TerminalSession not found for Terminal: {}", terminalId);
			return ServiceTaskResponse.failed(notRunningErrorCode).build();
		}

		TerminalState state = stateService.getForTerminal(terminalId);

		RefreshTask task = new RefreshTask(terminalId, operationId, infoText, separator,true, state);
		log.trace(task.toString());

		wsHandler.pushServiceTask(task);	

		return ServiceTaskResponse.completed().build();
	}

	@ServiceTaskListener(topic = TOPIC_PREFIX + "BroadcastState")
	@ServiceTaskDescription(name = TASK_NAME_PREFIX + "Broadcast State", properties = {
			@Property(type = Type.STRING, key = "terminalId", label = "Terminal", notEmpty = true),
			@Property(type = Type.STRING, key = "stateNotFoundErrorCode", label = "Error Code (State object not found)", notEmpty = true),
			@Property(type = Type.STRING, key = "instanceNotRunningErrorCode", label = "Error Code (Terminal instance not running)", notEmpty = true) })
	@Transactional
	public ServiceTaskResponse handleBroadcastState(ServiceTaskRequest request,
			@Variable(value = "terminalId") String terminalId,
			@Variable(value = "stateNotFoundErrorCode") String notFoundErrorCode,
			@Variable(value = "instanceNotRunningErrorCode") String notRunningErrorCode) {

		log.trace("Received broadcast state request for terminal {}", terminalId);
		if (!stateService.stateExists(terminalId)) {
			return ServiceTaskResponse.failed(notFoundErrorCode).build();
		}

		// check if terminal session exists
		if (!wsHandler.checkSessionIsOpen(terminalId)) {
			log.trace("TerminalSession not found for Terminal: {}", terminalId);
			return ServiceTaskResponse.failed(notRunningErrorCode).build();
		}

		TerminalState state = stateService.getForTerminal(terminalId);
		List<Terminal> terminals = terminalService.getAll();

		BroadcastStateTask task = new BroadcastStateTask(terminalId, true, state);
		log.trace(task.toString());
		wsHandler.pushServiceTask(task);

		// broadcast refresh all neighbours terminal states
		terminals.stream().forEach(t -> publishBroadcast(t));

		return ServiceTaskResponse.completed().build();
	}

	// helper method to send broadcast to all terminals
	public void publishBroadcast(Terminal t) {

		RefreshAllNeighboursTask task = new RefreshAllNeighboursTask(t.getBusinessKey(), stateService.getForTerminal(t.getBusinessKey()));
		
		if (wsHandler.checkSessionIsOpen(t.getBusinessKey())) {
			wsHandler.pushServiceTask(task);
		}
	}

}
