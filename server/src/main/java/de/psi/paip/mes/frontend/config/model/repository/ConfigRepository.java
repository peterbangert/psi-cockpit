package de.psi.paip.mes.frontend.config.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.psi.paip.mes.frontend.config.model.Config;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config, String> {
	Config getByBusinessKey(String key);

   Optional<Config> findByBusinessKey(String key);
}
