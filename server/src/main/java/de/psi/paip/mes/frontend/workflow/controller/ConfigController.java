package de.psi.paip.mes.frontend.workflow.controller;

import de.psi.paip.mes.frontend.config.model.ButtonConfig;
import de.psi.paip.mes.frontend.config.model.Config;
import de.psi.paip.mes.frontend.config.model.DefaultButtonConfig;
import de.psi.paip.mes.frontend.config.service.ButtonConfigService;
import de.psi.paip.mes.frontend.config.service.ConfigService;

import java.util.List;
import java.util.stream.Collectors;

import de.psi.paip.mes.frontend.config.service.DefaultButtonConfigService;
import de.psi.paip.mes.frontend.startup.OpenProcessInstanceImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/configuration")
public class ConfigController {
	private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

	@Autowired
	private ConfigService configService;

	@Autowired
	private ButtonConfigService buttonConfigService;

	@Autowired
	private DefaultButtonConfigService defaultButtonConfigService;

	@Autowired
	private OpenProcessInstanceImporter openProcessInstanceImporter;

	@Autowired
	public ConfigController() {

	}

	/**
	 * fetch config for terminal
	 * @param configId id of the requested config
	 * @return configuration of a terminal
	 */
	@RequestMapping(path = "/{configId}", method = RequestMethod.GET)
	public ResponseEntity<Config> get(@PathVariable("configId") String configId) {
		log.trace("Getting configuration {}...", configId);
		openProcessInstanceImporter.updateOpenInstances();
		Config config = configService.getConfig(configId);

		if (config != null) {
			return ResponseEntity.ok().body(config);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	/**
	 * fetch all buttonConfigs for terminal
	 * @param configId id of the requested button-config
	 * @return list of buttonconfigs or null
	 */
	@RequestMapping(path = "/buttonConfig/{configId}", method = RequestMethod.GET)
	public ResponseEntity<List<ButtonConfig>> getButtonConfigForTerminal(@PathVariable("configId") String configId) {
		log.trace("Getting button config for config {}...", configId);

		Config config = this.configService.getConfig(configId);

		if (config != null) {
			// filter buttonConfigs
			List<ButtonConfig> buttonConfigs = this.buttonConfigService.getAll().stream()
					.filter(buttonConfig -> buttonConfig.getConfig().getId() == config.getId())
					.sorted()
					.collect(Collectors.toList());

			return ResponseEntity.ok().body(buttonConfigs);
		} else {
			return ResponseEntity.badRequest().body(null);
		}
	}
	
	/**
	 * fetch all buttonConfigs for terminals
	 * @return list of buttonconfigs or null
	 */
	@RequestMapping(path = "/buttonConfig/getAll", method = RequestMethod.GET)
	public ResponseEntity<List<ButtonConfig>> getAllButtonConfigs() {
		log.trace("Fetch all buttonConfigs for terminals ...");
		
		List<ButtonConfig> buttonConfigs = this.buttonConfigService.getAll();

		return ResponseEntity.ok().body(buttonConfigs);
	}
	
	/**
	 * insert or update existing buttonConfig in database
	 * @param buttonConfig id of the requested button-config
	 * @return message
	 */
	@RequestMapping(path = "/buttonConfig/save", method = RequestMethod.POST)
	public ResponseEntity<String> saveButtonConfig(@RequestBody ButtonConfig buttonConfig) {
		log.trace("Insert new buttConfig for Terminal {} ...", buttonConfig.getConfig().getBusinessKey());
		ButtonConfig persistedButtonConfig = null;
		
		if(buttonConfig.getId() != 0) {
			// get buttonConfig from database
			persistedButtonConfig = this.buttonConfigService.getButtonConfig(buttonConfig.getId());
		}
		
		
		if(persistedButtonConfig == null) { // there is not a buttonConfig in database
			Config config = this.configService.getConfig(buttonConfig.getConfig().getBusinessKey());
			
			if(config != null) {
				persistedButtonConfig = new ButtonConfig();
				persistedButtonConfig.setButtonName(buttonConfig.getButtonName());
				persistedButtonConfig.setWorkflow(buttonConfig.getWorkflow());
				persistedButtonConfig.setButtonType(buttonConfig.getButtonType());
				persistedButtonConfig.setButtonOrder(buttonConfig.getButtonOrder());
				persistedButtonConfig.setConfig(config);
			} else {
				return ResponseEntity.badRequest().body("Terminal configuration not found!");
			}
		} else {
			persistedButtonConfig.setButtonName(buttonConfig.getButtonName());
			persistedButtonConfig.setWorkflow(buttonConfig.getWorkflow());
			persistedButtonConfig.setButtonType(buttonConfig.getButtonType());
			persistedButtonConfig.setButtonOrder(buttonConfig.getButtonOrder());
		}
		
		this.buttonConfigService.save(persistedButtonConfig);
		
		return ResponseEntity.ok().body("ButtonConfig inserted/updated successfully");
	}
	
	/**
	 * remove buttonConfig from database
	 * @param buttonConfigId id of the requested button-config
	 * @return message
	 */
	@RequestMapping(path = "/buttonConfig/delete/{buttonConfigId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> removeButtonConfig(@PathVariable("buttonConfigId") int buttonConfigId) {
		log.trace("Delete buttConfig with id {} ...", buttonConfigId);
		ButtonConfig persistedButtonConfig = null;
		
		if(buttonConfigId != 0) {
			// get buttonConfig from database
			persistedButtonConfig = this.buttonConfigService.getButtonConfig(buttonConfigId);
			
			if(persistedButtonConfig != null) {
				this.buttonConfigService.delete(persistedButtonConfig);
				return ResponseEntity.ok().body("Buttonconfig deleted successfully");
			}
		}
		
		return ResponseEntity.notFound().build();
	}

	/**
	 * fetch all defaultButtonConfigs for new terminals
	 * @return list of defaultbuttonconfigs or null
	 */
	@RequestMapping(path = "/defaultButtonConfig/getAll", method = RequestMethod.GET)
	public ResponseEntity<List<DefaultButtonConfig>> getAllDefaultButtonConfigs() {
		log.trace("Fetch all defaultbuttonConfigs for terminals ...");

		List<DefaultButtonConfig> defaultButtonConfigs = this.defaultButtonConfigService.findAll();

		return ResponseEntity.ok().body(defaultButtonConfigs);
	}

	/**
	 * insert or update existing defaultButtonConfig in database
	 * @param defaultButtonConfig Default Button Configuration to be added
	 * @return message
	 */
	@RequestMapping(path = "/defaultButtonConfig/save", method = RequestMethod.POST)
	public ResponseEntity<String> saveDefaultButtonConfig(@RequestBody DefaultButtonConfig defaultButtonConfig) {
		log.trace("Insert new defaultButtConfig");
		DefaultButtonConfig persistedDefaultButtonConfig = null;

		if(defaultButtonConfig.getId() != 0) {
			// get buttonConfig from database
			persistedDefaultButtonConfig = this.defaultButtonConfigService.getDefaultButtonConfig(defaultButtonConfig.getId());
		}


		if(persistedDefaultButtonConfig == null) { // there is not a defaultbuttonConfig in database
			persistedDefaultButtonConfig = new DefaultButtonConfig();
			persistedDefaultButtonConfig.setButtonName(defaultButtonConfig.getButtonName());
			persistedDefaultButtonConfig.setWorkflow(defaultButtonConfig.getWorkflow());
			persistedDefaultButtonConfig.setButtonType(defaultButtonConfig.getButtonType());
			persistedDefaultButtonConfig.setButtonOrder(defaultButtonConfig.getButtonOrder());
		} else {
			persistedDefaultButtonConfig.setButtonName(defaultButtonConfig.getButtonName());
			persistedDefaultButtonConfig.setWorkflow(defaultButtonConfig.getWorkflow());
			persistedDefaultButtonConfig.setButtonType(defaultButtonConfig.getButtonType());
			persistedDefaultButtonConfig.setButtonOrder(defaultButtonConfig.getButtonOrder());
		}

		this.defaultButtonConfigService.save(persistedDefaultButtonConfig);

		return ResponseEntity.ok().body("DefaultButtonConfig inserted/updated successfully");
	}

	/**
	 * remove defaultButtonConfig from database
	 * @param defaultButtonConfigId Default Button Configuration to be removed
	 * @return message
	 */
	@RequestMapping(path = "/defaultButtonConfig/delete/{defaultButtonConfigId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> removeDefaultButtonConfig(@PathVariable("defaultButtonConfigId") int defaultButtonConfigId) {
		log.trace("Delete defaultButtConfig with id {} ...", defaultButtonConfigId);
		DefaultButtonConfig persistedDefaultButtonConfig = null;

		if(defaultButtonConfigId != 0) {
			// get buttonConfig from database
			persistedDefaultButtonConfig = this.defaultButtonConfigService.getDefaultButtonConfig(defaultButtonConfigId);

			if(persistedDefaultButtonConfig != null) {
				this.defaultButtonConfigService.delete(persistedDefaultButtonConfig);
				return ResponseEntity.ok().body("DefaultButtonconfig deleted successfully");
			}
		}

		return ResponseEntity.notFound().build();
	}
}
