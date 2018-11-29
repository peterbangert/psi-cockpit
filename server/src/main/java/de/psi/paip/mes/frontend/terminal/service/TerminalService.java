package de.psi.paip.mes.frontend.terminal.service;

import de.psi.paip.mes.frontend.terminal.model.Terminal;
import de.psi.paip.mes.frontend.terminal.model.repository.TerminalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TerminalService {
    private final Logger log = LoggerFactory.getLogger(TerminalService.class);

    private final TerminalRepository repository;

    @Autowired
    public TerminalService(TerminalRepository repository) {
        this.repository = repository;
    }

    public List<Terminal> getAll() {
        return repository.findAll(sortByBusinessKey());
    }

    public Optional<List<Terminal>> getAllOrdered() {
        return repository.findAllOrdered();
    }

    public Sort sortByBusinessKey() {
        return new Sort(Sort.Direction.ASC, "businessKey");
    }

    public boolean exists(String terminalKey) {
        return repository.findByBusinessKey(terminalKey).isPresent();
    }

    public Terminal getTerminal(String terminalKey) {
        return repository.getByBusinessKey(terminalKey);
    }

    /**
     * Method to get all neighbours
     *
     * @param terminalId id of the terminal
     * @return List of all terminals
     */
	/*public List<Terminal> getNeighbours(String terminalId){
		List<Terminal> allTerminals = getAll();
		List<Terminal> terminalNeighbours = new ArrayList<Terminal>();
		
		int index = allTerminals.indexOf(getTerminal(terminalId));
		
		for(int i= index-2; i <= index+2; i++) {
			
			if( (i >= 0) && (i < allTerminals.size())) {
				if(allTerminals.get(i)!= null) {
					terminalNeighbours.add(allTerminals.get(i));
				}
			}
		}
		// terminalNeighbours.stream().forEach(t -> log.trace(t.getName()));
		return terminalNeighbours;
	}*/
    public List<Terminal> getNeighbours(String terminalId) {
        Optional<List<Terminal>> allTerminals = getAllOrdered();
        List<Terminal> terminalNeighbours = new ArrayList<Terminal>();
        if (allTerminals.isPresent()) {
            int index = allTerminals.get().indexOf(getTerminal(terminalId));

            if(index != -1){
                for (int i = index - 2; i <= index + 2; i++) {

                    if ((i >= 0) && (i < allTerminals.get().size())) {
                        if (allTerminals.get().get(i) != null) {
                            terminalNeighbours.add(allTerminals.get().get(i));
                        }
                    }
                }
                return terminalNeighbours;
            }else{
                log.info("No neighbours found for terminal:"+terminalId);
                Terminal terminal = getTerminal(terminalId);
                if(terminal!= null){
                    log.info("Return terminal-state:"+terminalId);
                    terminalNeighbours.add(terminal);
                    return terminalNeighbours;
                }else{
                    log.info("No Terminal found for terminalId:"+terminalId);
                    return null;
                }
            }
        } else {
            log.info("No terminals with parameter 'terminalOrder' found.");
            Terminal terminal = getTerminal(terminalId);
            if(terminal!= null){
                log.info("Return terminal-state:"+terminalId);
                terminalNeighbours.add(terminal);
                return terminalNeighbours;
            }else{
                log.info("No Terminal found for terminalId:"+terminalId);
                return null;
            }
        }
    }

    public void deleteAll() {
        log.info("Deleting all terminals...");
        repository.deleteAll();
    }

    public void add(Terminal terminal) {
        log.info("Adding terminal {}...", terminal.getBusinessKey());
        repository.save(terminal);
    }

    public void delete(Terminal terminal) {
        log.info("Deleting terminal {}...", terminal.getBusinessKey());
        repository.delete(terminal);
    }

    public void updateTerminalOrder(Terminal terminal) {
        log.info("Update terminalOrder of terminal: {}...", terminal.getBusinessKey());
        repository.save(terminal);
    }
}
