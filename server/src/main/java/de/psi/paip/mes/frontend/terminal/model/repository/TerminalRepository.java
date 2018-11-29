package de.psi.paip.mes.frontend.terminal.model.repository;

import de.psi.paip.mes.frontend.terminal.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, String> {
   Terminal getByBusinessKey(String key);

   Optional<Terminal> findByBusinessKey(String key);


   /**select all open instances for a specific work_station
    *
    * @return List of terminals with parameter terminalOrder
    * */
   @Query("SELECT t FROM Terminal t WHERE t.terminalOrder IS NOT NULL ORDER BY t.terminalOrder")
   Optional<List<Terminal>> findAllOrdered();
}
