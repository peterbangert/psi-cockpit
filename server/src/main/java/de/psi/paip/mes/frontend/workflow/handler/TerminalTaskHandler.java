package de.psi.paip.mes.frontend.workflow.handler;

import de.psi.paip.mes.frontend.terminalState.service.TerminalStateService;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription.Property;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskDescription.Property.Type;
import de.psi.paip.mes.starter.workflow.annotation.ServiceTaskListener;
import de.psi.paip.mes.starter.workflow.annotation.Variable;
import de.psi.paip.mes.starter.workflow.model.ServiceTaskRequest;
import de.psi.paip.mes.starter.workflow.model.ServiceTaskResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Order(0)
@Controller
public class TerminalTaskHandler {

	private static final String TOPIC_PREFIX = "psi.mes.pia.terminal.";
	private static final String TASK_NAME_PREFIX = "PSI-MES//PIA/Terminal/";

	private final TerminalStateService stateService;

	public TerminalTaskHandler(TerminalStateService stateService) {
		this.stateService = stateService;
	}

	@ServiceTaskListener(topic = TOPIC_PREFIX + "AddUrl")
	@ServiceTaskDescription(name = TASK_NAME_PREFIX + "Add Terminal URL to context", properties = {
			@Property(type = Type.STRING, key = "terminalId", label = "Terminal", notEmpty = true),
			@Property(type = Type.STRING, key = "terminalUrlVariable", label = "Output Variable Name", notEmpty = true),
			@Property(type = Type.STRING, key = "terminalNotConnectedErrorCode", label = "Error Code (Terminal not connected)", notEmpty = true) })
	@Transactional
	public ServiceTaskResponse handleAddUrl(ServiceTaskRequest request,
			@Variable(value = "terminalId") String terminalId,
			@Variable(value = "terminalUrlVariable") String variableName,
			@Variable(value = "terminalNotConnectedErrorCode") String errorCode) {

		if (!stateService.stateExists(terminalId)) {
			return ServiceTaskResponse.failed(errorCode).build();
		}

		// 2018/05/17 jjaenike: TODO lese die URL des (mit dem WebSocket verbundenen)
		// Rechners, der PIA geöffnet hat
		String url = "TODO";

		return ServiceTaskResponse.completed().variable(variableName, url).build();
	}
}
