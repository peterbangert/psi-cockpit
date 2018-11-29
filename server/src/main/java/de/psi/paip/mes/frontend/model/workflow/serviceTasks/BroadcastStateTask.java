package de.psi.paip.mes.frontend.model.workflow.serviceTasks;

import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import lombok.Data;

@Data
public class BroadcastStateTask extends ServiceTask{

	private String name;
	
	public BroadcastStateTask(String terminalId, boolean refresh, TerminalState state) {
		super(terminalId, refresh, state);
		this.name = "Broadcast";
	}
}
