package de.psi.paip.mes.frontend.startup;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.psi.paip.mes.frontend.config.model.ButtonConfig;
import de.psi.paip.mes.frontend.config.model.Config;
import de.psi.paip.mes.frontend.config.model.DefaultButtonConfig;
import de.psi.paip.mes.frontend.config.service.ButtonConfigService;
import de.psi.paip.mes.frontend.config.service.ConfigService;

import de.psi.paip.mes.frontend.config.service.DefaultButtonConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Order(1)
@Component
public class ConfigImporter implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(ConfigImporter.class);

	@Value("${startup.import.update}")
	private UpdateStrategy updateStrategy;

	@Value("${url.service.operation}")
	private String server;

	private final ConfigService service;

	private final ButtonConfigService buttonConfigService;

	private final DefaultButtonConfigService defaultButtonConfigService;

	private RestTemplate rest;
	private HttpHeaders headers;

	@Autowired
	public ConfigImporter(ConfigService configService, ButtonConfigService buttonConfigService, DefaultButtonConfigService defaultButtonConfigService) {
		this.service = configService;
		this.buttonConfigService = buttonConfigService;
		this.defaultButtonConfigService = defaultButtonConfigService;
		this.rest = new RestTemplate();
		this.headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		headers.add("Accept", "*/*");
	}

	@Override
	public void run(ApplicationArguments args) throws JsonParseException, JsonMappingException, IOException {
		DatabaseState.setDatabaseWorkingConfiguration(true);
		if (UpdateStrategy.NONE == updateStrategy) {
			DatabaseState.setDatabaseWorkingConfiguration(false);
			return;
		}
		if (UpdateStrategy.RESET == updateStrategy) {
			buttonConfigService.deleteAll();
			service.deleteAll();
		}
		
		if(server != null) {
			DatabaseState.setServer(server);
		} else {
			server = DatabaseState.getServer();
		}

		HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
		ResponseEntity<String> responseEntity = rest.exchange(server + "/frontend/stations", HttpMethod.GET,
				requestEntity, String.class);
		responseEntity.getStatusCode();

		if (responseEntity.getStatusCode().toString().equals("200")) {

			Config[] configs = configMapper(responseEntity.getBody().toString());

			for (Config config : configs) {
				add(config);
			}

			if (UpdateStrategy.SYNC == updateStrategy) {
				removeDead(configs);
			}
		}
		DatabaseState.setDatabaseWorkingConfiguration(false);
	}
	
	private Config[] configMapper(String json) {
		JsonParser jsonParser = new JsonParser();
		JsonArray array = jsonParser.parse(json).getAsJsonArray();

		List<Config> configList = new ArrayList<Config>();
		

		for (JsonElement elem : array) {

			JsonObject tempObj = elem.getAsJsonObject();

			Config config = new Config();
			config.setBusinessKey(tempObj.get("stationId").toString().replace("\"", ""));
			config.setDisplayNumber("2");
						
			configList.add(config);
		}


		Config [] configs = new Config[configList.size()];
		
		for (int i = 0; i < configs.length; i++) {
			configs[i] = configList.get(i);
		}
		
		return configs;
	}
	
	/**
	 * create button configuration for configuration
	 * @param config station configuration
	 */
	private void createButtonConfigForConfig(Config config) {
		// get configuration from database
		List<DefaultButtonConfig> defaultButtonConfigs = this.defaultButtonConfigService.findAll();

		for(DefaultButtonConfig defaultButtonConfig: defaultButtonConfigs) {
			// create button configs from defaulbuttonConfigs
			ButtonConfig buttonConfig = new ButtonConfig();
			buttonConfig.setButtonName(defaultButtonConfig.getButtonName());
			buttonConfig.setWorkflow(defaultButtonConfig.getWorkflow());
			buttonConfig.setConfig(config);
			buttonConfig.setButtonOrder(defaultButtonConfig.getButtonOrder());
			buttonConfig.setButtonType(defaultButtonConfig.getButtonType());
			this.buttonConfigService.save(buttonConfig);
		}
	}


	private void add(Config config) {
		if (service.exists(config.getBusinessKey())) {
			return;
		}

		log.info("Importing config {}...", config.getBusinessKey());
		Config databaseConfig = service.add(config);
		
		this.createButtonConfigForConfig(databaseConfig);
	}

	private void removeDead(Config[] configs) {
		for (Config persistedConfig : service.getAll()) {
			if (Arrays.stream(configs)
					.noneMatch(config -> persistedConfig.getBusinessKey().equals(config.getBusinessKey()))) {
				
				this.removeButtonConfigsForConfig(persistedConfig);
				service.delete(persistedConfig);
			}
		}
		
		this.removeDeadButtonConfigs();
	}
	
	/**
	 * delete buttonConfigs of Station Configuration
	 * @param config
	 */
	private void removeButtonConfigsForConfig(Config config) {
		List<ButtonConfig> buttonConfigs = this.buttonConfigService.getAll();
		
		for(ButtonConfig bConfig: buttonConfigs) {
			if(bConfig.getConfig().getId() == config.getId()) {
				this.buttonConfigService.delete(bConfig);
			}
		}
	}
	
	/**
	 * removes dead button configurations from database
	 */
	private void removeDeadButtonConfigs() {
		List<ButtonConfig> buttonConfigs = this.buttonConfigService.getAll();
		
		for(ButtonConfig bConfig: buttonConfigs) {
			if(!service.exists(bConfig.getConfig().getBusinessKey())) {
				this.buttonConfigService.delete(bConfig);
			}
		}
	}
}
