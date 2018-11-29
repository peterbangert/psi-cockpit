package de.psi.paip.mes.frontend.startup;

public class DatabaseState {

	private static Boolean databaseWorkingConfiguration = null;
	
	private static Boolean databaseWorkingTerminal = null;

	private static Boolean databaseWorkingOpenProcessInstance = null;
	
	private static String server = null;

	private DatabaseState() {}

	public static Boolean getConfigImporter() {
		if (databaseWorkingConfiguration == null) {
			databaseWorkingConfiguration = new Boolean(false);
		}
		return databaseWorkingConfiguration;
	}
	
	public static Boolean getTerminalImporter() {
		if (databaseWorkingTerminal == null) {
			databaseWorkingTerminal = new Boolean(false);
		}
		return databaseWorkingTerminal;
	}
	
	public static String getServer() {
		return server;
	}
	
	public static void setDatabaseWorkingConfiguration(Boolean value) {
		DatabaseState.databaseWorkingConfiguration = value;
	}
	
	public static void setDatabaseWorkingTerminal(Boolean value) {
		DatabaseState.databaseWorkingTerminal = value;
	}

	public static void setDatabaseWorkingOpenProcessInstance(Boolean value) {
		DatabaseState.databaseWorkingOpenProcessInstance = value;
	}
	
	public static void setServer(String value) {
		DatabaseState.server = value;
	}
}
