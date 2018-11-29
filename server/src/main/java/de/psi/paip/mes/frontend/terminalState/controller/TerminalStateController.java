package de.psi.paip.mes.frontend.terminalState.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.psi.paip.mes.frontend.RestAPIController;
import de.psi.paip.mes.frontend.terminalState.model.TerminalMode;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import de.psi.paip.mes.frontend.terminalState.service.TerminalStateService;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/state")
public class TerminalStateController {
	private static final Logger log = LoggerFactory.getLogger(TerminalStateController.class);

	private final TerminalStateService service;

	private final RestAPIController restApiController;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	public TerminalStateController(TerminalStateService service, RestAPIController restAPIController, ObjectMapper objectMapper) {
		this.service = service;
		this.restApiController = restAPIController;
		this.objectMapper = objectMapper;
	}

	@RequestMapping(path = "/{terminalId}", method = RequestMethod.GET)
   public ResponseEntity<Optional<TerminalState>> get(@PathVariable("terminalId") String terminalId) {
      log.trace("Getting state for terminal {}...", terminalId);
      
      Optional<TerminalState> terminalState = service.findForTerminal(terminalId);
      
      if (terminalState.isPresent()) {
    	  return ResponseEntity.ok().body(terminalState);
      }else {
    	  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

   }
	@RequestMapping(path = "/{terminalId}/updateOperationId/{operationId}", method = RequestMethod.POST)
	public ResponseEntity<String> updateOperationId(@PathVariable("terminalId") String terminalId, @PathVariable("operationId") String operationId) {
		log.trace("Update OperationId for Terminal {} ...", terminalId);

		Optional<TerminalState> terminalState = service.findForTerminal(terminalId);

		if (terminalState.isPresent()) {

			// set Leertakt-Screen
			if(operationId.equals("")){
				service.updateOperationId(terminalId, operationId);
				return ResponseEntity.ok().body("OperationId successfully updated to"+ operationId);
			}else{
				// try to find operationId and check if completionState is INITIAL
				try{
					Map<String, Object> operation = objectMapper.readValue(this.restApiController.fetchOperation(terminalId,operationId).getBody(),Map.class);

					if(operation.get("completionState") != null && operation.get("completionState").equals("INITIAL")){
						service.updateOperationId(terminalId, operationId);
						return ResponseEntity.ok().body("OperationId successfully updated to"+ operationId);
					}else{
						log.trace("correct completion state not found:"+operation.get("completionState"));
					}
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
				}catch(Exception e){
					log.trace(e.toString());
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
				}
			}
		}else {
			log.trace("terminal state not found for terminal:"+terminalId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	@RequestMapping(path = "/{terminalId}/updateTerminalMode/{terminalMode}", method = RequestMethod.POST)
	public ResponseEntity<String> updateTerminalMode(@PathVariable("terminalId") String terminalId, @PathVariable("terminalMode") String terminalMode) {
		log.trace("Update TerminalMode for Terminal {} ...", terminalId);

		Optional<TerminalState> terminalState = service.findForTerminal(terminalId);

		if (terminalState.isPresent()) {

			TerminalMode mode =null;

			switch (terminalMode)
			{
				case "firstOperation":
					mode = TerminalMode.firstOperation; break;
				case "chosenOperation": mode = TerminalMode.chosenOperation; break;
				case "givenOperation": mode = TerminalMode.givenOperation; break;
				default:mode = null; break;
			}

			if(mode != null){
				service.updateTerminalMode(terminalId, mode);
				return ResponseEntity.ok().body("OperationId successfully updated to" + mode);
			}else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}

		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
}
