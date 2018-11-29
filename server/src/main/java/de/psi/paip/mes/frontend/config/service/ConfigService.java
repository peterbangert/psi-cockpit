package de.psi.paip.mes.frontend.config.service;

import de.psi.paip.mes.frontend.config.model.Config;
import de.psi.paip.mes.frontend.config.model.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {
	private final Logger log = LoggerFactory.getLogger(ConfigService.class);

	private final ConfigRepository repository;

	@Autowired
	public ConfigService(ConfigRepository repository) {
		this.repository = repository;
	}

	public List<Config> getAll() {
		return repository.findAll();
	}

	public boolean exists(String configKey) {
		return repository.findByBusinessKey(configKey).isPresent();
	}

	public Config getConfig(String configKey) {
		return repository.getByBusinessKey(configKey);
	}

	public void deleteAll() {
		log.info("Deleting all configs...");
		repository.deleteAll();
	}

	public Config add(Config config) {
		log.info("Adding config {}...", config.getBusinessKey());
		return repository.save(config);
	}

	public void delete(Config config) {
		log.info("Deleting config {}...", config.getBusinessKey());
		repository.delete(config);
	}
}
