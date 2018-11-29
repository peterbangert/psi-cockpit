package de.psi.paip.mes.frontend.model.workflow.serviceTasks;

import de.psi.paip.mes.frontend.terminalState.model.TerminalState;


public class RefreshTask extends ServiceTask{
	public String operationId;
	public String infoText;
	public String separator;
	public String name;

	public RefreshTask(String terminalId, String operationId, String infoTest, String separator,
			boolean refresh, TerminalState state) {
		super(terminalId, refresh, state);
		this.operationId = operationId;
		this.infoText = infoTest;
		this.separator = separator;
		this.name = "Refresh Terminal";
	}

	@Override
	public String toString() {
		return "name: " + name + "ActivityType: " + getActivityType() + " TerminalID: " + getTerminalId() + " operationId: " + operationId + " infoText: "
				+ infoText + " separator: " + separator + " refresh: " + isRefresh() + " state: " + getState();
	}
}
