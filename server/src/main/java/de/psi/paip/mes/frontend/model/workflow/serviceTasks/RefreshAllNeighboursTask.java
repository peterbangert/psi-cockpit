package de.psi.paip.mes.frontend.model.workflow.serviceTasks;

import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import lombok.Data;

@Data
public class RefreshAllNeighboursTask extends ServiceTask{
	
	private boolean refreshAllNeighbours;
	private String name;
	
	public RefreshAllNeighboursTask(String terminalId,TerminalState state) {
		super(terminalId, true, state);
		refreshAllNeighbours=true;
		this.name = "Refresh All neighbours";
	}
	@Override
	public String toString() {
		return "name: " + name + "ActivityType: " + getActivityType() + " TerminalID: " + getTerminalId() + " refresh: " + isRefresh()
				+ " state: " + getState() + "RefreshAllNeighbours: "+ refreshAllNeighbours;
	}
}
