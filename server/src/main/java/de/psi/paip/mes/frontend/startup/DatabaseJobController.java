package de.psi.paip.mes.frontend.startup;

import java.io.IOException;

import de.psi.paip.mes.frontend.config.service.DefaultButtonConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import de.psi.paip.mes.frontend.config.service.ButtonConfigService;
import de.psi.paip.mes.frontend.config.service.ConfigService;
import de.psi.paip.mes.frontend.terminal.service.TerminalService;

@Controller
public class DatabaseJobController {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseJobController.class);

	private static final ApplicationArguments ApplicationArguments = null;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private ButtonConfigService buttonConfigService;

	@Autowired
	private DefaultButtonConfigService defaultButtonConfigService;
	
	@Autowired
	private TerminalService terminalService;

	/**
	 * check if database is currently updating data
	 * @return Status of Database
	 */
	@CrossOrigin(origins = "*")
    @GetMapping(path="/isDatabaseWorking")
    public ResponseEntity<String> getIsDatabaseWorking(){
		LOG.debug("Check database state...");
		if(DatabaseState.getConfigImporter() || DatabaseState.getTerminalImporter()) {
			return ResponseEntity.badRequest().body("Database is currently working");
		} else {
			return ResponseEntity.ok("Database is idle");
		}
	}
	
	/**
	 * start database importers
	 * @return Status of DataBaseImorter
	 */
	@CrossOrigin(origins = "*")
    @GetMapping(path="/startDatabaseUpdate")
	public ResponseEntity<String> startDatabaseImporters() {
		LOG.debug("Start database update...");
		ConfigImporter configImporter = new ConfigImporter(configService, buttonConfigService, defaultButtonConfigService);
		TerminalImporter terminalImporter = new TerminalImporter(terminalService);
		
		if(DatabaseState.getConfigImporter() || DatabaseState.getTerminalImporter()) {
			return ResponseEntity.badRequest().body("Database is currently working");
		}
		
		Thread configImporterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					configImporter.run(ApplicationArguments);
				} catch (IOException e) {
					LOG.warn("Error on executing database importer");
				}
			}
			
		});
		
		Thread terminalImporterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					terminalImporter.run(ApplicationArguments);
				} catch (IOException e) {
					LOG.warn("Error on executing database importer");
				}
			}
			
		});
		
		configImporterThread.start();
		terminalImporterThread.start();
		
		return ResponseEntity.ok("Database update started");
	}
}
