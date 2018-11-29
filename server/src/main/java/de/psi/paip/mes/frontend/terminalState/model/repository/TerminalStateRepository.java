package de.psi.paip.mes.frontend.terminalState.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.psi.paip.mes.frontend.terminalState.model.TerminalState;

import java.util.Optional;

@Repository
public interface TerminalStateRepository extends JpaRepository<TerminalState, String> {

   @Query("select state from TerminalState state where state.terminal.businessKey = :terminalKey")
   TerminalState getByTerminalKey(@Param("terminalKey") String terminalKey);

   @Query("select state from TerminalState state where state.terminal.businessKey = :terminalKey")
   Optional<TerminalState> findByTerminalKey(@Param("terminalKey") String terminalKey);
}
