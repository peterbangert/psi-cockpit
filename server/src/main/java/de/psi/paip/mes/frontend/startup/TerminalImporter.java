package de.psi.paip.mes.frontend.startup;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.psi.paip.mes.frontend.terminal.model.Terminal;
import de.psi.paip.mes.frontend.terminal.service.TerminalService;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;

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
public class TerminalImporter implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(TerminalImporter.class);

	@Value("${startup.import.file-name}")
	private String fileName;

	@Value("${startup.import.update}")
	private UpdateStrategy updateStrategy;

	@Value("${url.service.operation}")
	private String server;

	private final TerminalService service;

	private RestTemplate rest;
	private HttpHeaders headers;

	@Autowired
	public TerminalImporter(TerminalService service) {
		this.service = service;
		this.rest = new RestTemplate();
		this.headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		headers.add("Accept", "*/*");
	}

	@Override
	public void run(ApplicationArguments args) throws JsonParseException, JsonMappingException, IOException {
		DatabaseState.setDatabaseWorkingTerminal(true);
		if (UpdateStrategy.NONE == updateStrategy) {
			DatabaseState.setDatabaseWorkingTerminal(false);
			return;
		}
		if (UpdateStrategy.RESET == updateStrategy) {
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

			Terminal[] terminals = terminalMapper(responseEntity.getBody().toString());

			for (Terminal terminal : terminals) {
				add(terminal);
			}

			if (UpdateStrategy.SYNC == updateStrategy) {
				removeDead(terminals);
			}
		}
		DatabaseState.setDatabaseWorkingTerminal(false);
	}

	private Terminal[] terminalMapper(String json) {
		JsonParser jsonParser = new JsonParser();
		JsonArray array = jsonParser.parse(json).getAsJsonArray();

		List<Terminal> terminalList = new ArrayList<Terminal>();

		for (JsonElement elem : array) {

			JsonObject tempObj = elem.getAsJsonObject();

			Terminal terminal = new Terminal();
			terminal.setBusinessKey(tempObj.get("stationId").toString().replace("\"", ""));
			terminal.setName(tempObj.get("name").toString().replace("\"", ""));
			terminal.setDescription(tempObj.get("description").toString().replace("\"", ""));
			TerminalState ts = new TerminalState();
			ts.setTerminal(terminal);
			terminal.setState(ts);
			
			terminalList.add(terminal);
		}


		Terminal [] terminals = new Terminal[terminalList.size()];
		
		for (int i = 0; i < terminals.length; i++) {
			terminals[i] = terminalList.get(i);
		}
		
		return terminals;
	}

	private void add(Terminal terminal) {
		if (service.exists(terminal.getBusinessKey())) {
			return;
		}

		log.info("Importing terminal {}...", terminal.getBusinessKey());
		service.add(terminal);
	}

	private void removeDead(Terminal[] terminals) {
		for (Terminal persistedTerminal : service.getAll()) {
			if (Arrays.stream(terminals)
					.noneMatch(terminal -> persistedTerminal.getBusinessKey().equals(terminal.getBusinessKey()))) {
				service.delete(persistedTerminal);
			}
		}
	}
}
