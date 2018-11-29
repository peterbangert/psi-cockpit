package de.psi.paip.mes.frontend.terminalState.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.psi.paip.mes.frontend.terminalState.model.Status;
import de.psi.paip.mes.frontend.terminalState.model.TerminalMode;
import de.psi.paip.mes.frontend.terminalState.model.TerminalState;
import de.psi.paip.mes.frontend.terminalState.model.repository.TerminalStateRepository;

import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Service
public class TerminalStateService {
   private static final Logger log = LoggerFactory.getLogger(TerminalStateService.class);

   private final TerminalStateRepository repository;

   @Autowired
   public TerminalStateService(TerminalStateRepository repository) {
      this.repository = repository;
   }

   /**
    * @return List of all TerminalStates
    * */
   public List<TerminalState> getAll() {
	  return repository.findAll();
   }
   
   public boolean stateExists(final String terminalId) {
      return repository.findByTerminalKey(terminalId).isPresent();
   }

   public TerminalState getForTerminal(String terminalKey) {
      return repository.getByTerminalKey(terminalKey);
   }

   public Optional<TerminalState> findForTerminal(String terminalKey) {
      return repository.findByTerminalKey(terminalKey);
   }

   public void updateLocked(final String terminalKey, final boolean locked) {
      final TerminalState state = repository.getByTerminalKey(terminalKey);

      state.setLocked(locked);

      repository.save(state);

      log.info("Set terminal {} to {} locked", terminalKey, locked ? EMPTY : "not");
   }

   public void updateStatus(final String terminalKey, final Status status) {
      final TerminalState state = repository.getByTerminalKey(terminalKey);

      state.setStatus(status);

      repository.save(state);

      log.info("Status of terminal '{}' changed to '{}'", terminalKey, status);
   }

   public void updateMessage(final String terminalKey, final String message) {
      final TerminalState state = repository.getByTerminalKey(terminalKey);

      state.setMessage(message);

      repository.save(state);

      log.info("Message of terminal '{}' changed to '{}'", terminalKey, message);
   }

   public void updateOperationId(final String terminalKey, final String operationId) {
      final TerminalState state = repository.getByTerminalKey(terminalKey);

      state.setOperationId(operationId);

      try{
         repository.save(state);
         log.info("OperationId of terminal '{}' changed to '{}'", terminalKey, operationId);
      }catch(Exception e){
         log.trace(e.toString());
      }
   }
   public void updateTerminalMode(final String terminalKey, final TerminalMode terminalMode) {
      final TerminalState state = repository.getByTerminalKey(terminalKey);

      state.setTerminalMode(terminalMode);

      repository.save(state);

      log.info("TerminalMode of terminal '{}' changed to '{}'", terminalKey, terminalMode);
   }
}
