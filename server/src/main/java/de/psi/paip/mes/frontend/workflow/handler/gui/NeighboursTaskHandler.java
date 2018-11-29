package de.psi.paip.mes.frontend.workflow.handler.gui;

import de.psi.paip.mes.frontend.WorkflowWebSocketHandler;
import de.psi.paip.mes.frontend.model.workflow.serviceTasks.RefreshAllNeighboursTask;
import de.psi.paip.mes.frontend.terminal.model.Terminal;
import de.psi.paip.mes.frontend.terminal.service.TerminalService;
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
public class NeighboursTaskHandler {
	private static final Logger log = LoggerFactory.getLogger(NeighboursTaskHandler.class);

	private static final String TOPIC_PREFIX = "psi.mes.pia.gui.neighbours.";
	private static final String TASK_NAME_PREFIX = "PSI-MES//PIA/GUI/Neighbours/";

	private final TerminalStateService stateService;
	private final TerminalService terminalService;
	private final WorkflowWebSocketHandler wsHandler;

	@Autowired
	public NeighboursTaskHandler(TerminalStateService stateService, WorkflowWebSocketHandler wsHandler, TerminalService terminalService) {
		this.stateService = stateService;
		this.wsHandler = wsHandler;
		this.terminalService = terminalService;
	}

	@ServiceTaskListener(topic = TOPIC_PREFIX + "RefreshAll")
	@ServiceTaskDescription(name = TASK_NAME_PREFIX + "Refresh All", properties = {
			@Property(type = Type.STRING, key = "terminalId", label = "Terminal", notEmpty = true),
			@Property(type = Type.STRING, key = "instanceNotRunningErrorCode", label = "Error Code (Terminal instance not running)", notEmpty = true) })
	@Transactional
	public ServiceTaskResponse handleRefreshAll(ServiceTaskRequest request,
			@Variable(value = "terminalId") String terminalId,
			@Variable(value = "instanceNotRunningErrorCode") String errorCode) {

		log.trace("Received refresh request for terminal {}", terminalId);

		/*if (!wsHandler.checkSessionIsOpen(terminalId)) {
			return ServiceTaskResponse.failed(errorCode).build();
		}*/
		
		if (!stateService.stateExists(terminalId)) {
			return ServiceTaskResponse.failed("Terminal State not found").build();
		}

		RefreshAllNeighboursTask task = new RefreshAllNeighboursTask(terminalId, stateService.getForTerminal(terminalId));

		log.trace(task.toString());
		wsHandler.pushServiceTask(task);
		
		terminalService.getNeighbours(terminalId).stream().forEach(t -> refreshNeighbours(t));
		
		return ServiceTaskResponse.completed().build();
	}
	
	// helper method to refresh to refresh neighbour terminals
	public void refreshNeighbours(Terminal t) {
		RefreshAllNeighboursTask task = new RefreshAllNeighboursTask(t.getBusinessKey(), stateService.getForTerminal(t.getBusinessKey()));

		if (wsHandler.checkSessionIsOpen(t.getBusinessKey())) {
			wsHandler.pushServiceTask(task);
			log.trace("refresh sent to"+t.toString());
		}
	}
}
