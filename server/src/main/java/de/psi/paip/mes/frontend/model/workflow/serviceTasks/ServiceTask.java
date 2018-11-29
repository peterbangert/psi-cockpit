package de.psi.paip.mes.frontend.model.workflow.serviceTasks;

import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import lombok.Data;

@Data
public class ServiceTask {
	private String activityType;
	private String terminalId;
	private boolean refresh;
	private TerminalState state;
	private String name;

	public ServiceTask(String terminalId, boolean refresh, TerminalState state) {
		this.activityType = "serviceTask";
		this.terminalId = terminalId;
		this.refresh = refresh;
		this.state = state;
		this.name = "plane service-task";
	}

	public String getTerminalId() {
		return terminalId;
	}

	@Override
	public String toString() {
		return "name: " + name + "ActivityType: " + activityType + " TerminalID: " + terminalId + " refresh: " + refresh
				+ " state: " + state;
	}
}