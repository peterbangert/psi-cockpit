package de.psi.paip.mes.frontend.terminal.controller;

import de.psi.paip.mes.frontend.terminal.model.Terminal;
import de.psi.paip.mes.frontend.terminal.service.TerminalService;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/terminals")
public class TerminalController {
	private static final Logger log = LoggerFactory.getLogger(TerminalController.class);

	private final TerminalService terminalService;

	@Autowired
	public TerminalController( TerminalService terminalService) {
		this.terminalService = terminalService;
	}

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public ResponseEntity<Optional<List<Terminal>>> get() {
		log.trace("Getting all terminals {}...");

		Optional<List<Terminal>> terminals = Optional.of(terminalService.getAll());

		if(terminals.isPresent()) {
			return ResponseEntity.ok().body(terminals);
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	
	@RequestMapping(path = "/{terminalId}", method = RequestMethod.GET)
	public ResponseEntity<Optional<Terminal>> getTerminal(@PathVariable("terminalId") String terminalId) {
		log.trace("Getting terminal with terminalId {}...", terminalId);

		Optional<Terminal> terminal = Optional.of(terminalService.getTerminal(terminalId));
		
		if (terminal.isPresent()) {
			return ResponseEntity.ok().body(terminal);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

	}
	@RequestMapping(path = "/neighbours/{terminalId}", method = RequestMethod.GET)
	public ResponseEntity<List<Terminal>> getTerminalNeighbours(@PathVariable("terminalId") String terminalId) {
		log.trace("Getting terminal neighbours for terminal with terminalId {}...", terminalId);

		List<Terminal> terminalNeighbours = terminalService.getNeighbours(terminalId);

		if (terminalNeighbours != null) {
			return ResponseEntity.ok().body(terminalNeighbours);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	@RequestMapping(path = "/updateOrder/", method = RequestMethod.POST)
	public ResponseEntity<String> updateTerminalOrder(@RequestBody List<Terminal> terminals) {
		log.trace("Update Order for Terminals...");

		Optional<List<Terminal>> allTerminals = Optional.of(terminalService.getAll());

		if (allTerminals.isPresent() && !terminals.isEmpty()) {
			allTerminals.get().forEach(
					t-> updateTerminalTable(t,terminals)
			);
			return ResponseEntity.ok().body("worked");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	private void updateTerminalTable(Terminal t, List<Terminal> terminals){

		Optional<Terminal> terminal =terminals.stream().filter(ordered ->
				ordered.getBusinessKey().equals(t.getBusinessKey())).findFirst();

		if(terminal.isPresent()){
			if(t.getTerminalOrder() != terminal.get().getTerminalOrder()) {
				t.setTerminalOrder(terminal.get().getTerminalOrder());
				updateTable(t);
			}
		}else{
			if(t.getTerminalOrder() != null) {
				t.setTerminalOrder(null);
				updateTable(t);
			}
		}
	}

	private void updateTable(Terminal t){
		log.trace("try update:"+t.getBusinessKey()+" "+ t.getTerminalOrder());
		try {
			terminalService.updateTerminalOrder(t);
		}catch(Exception e){
			log.trace(e.toString());
			log.trace("something went wrong updating:"+t.getBusinessKey());
		}
	}
}
