package de.psi.paip.mes.frontend.config.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.psi.paip.mes.frontend.config.model.ButtonConfig;
import de.psi.paip.mes.frontend.config.model.repository.ButtonConfigRepository;

/**
 * 
 * service is used as database access object
 * @author Benjamin Hüttenberger
 * @since 01.06.2018
 *
 */
@Service
public class ButtonConfigService {
	
	private final Logger log = LoggerFactory.getLogger(ButtonConfigService.class);

	@Autowired
	private ButtonConfigRepository buttonConfigRepository;
	
	/**
	 * retrieves ButtonConfig by Id
	 * @param id id of the button configuration
	 * @return null if ButtonConfig isn't found
	 */
	public ButtonConfig getButtonConfig(int id) {
		Optional<ButtonConfig> optional = this.buttonConfigRepository.findById(id);
		
		if(optional.isPresent()) {
			return optional.get();
		} else {
			log.warn("ButtonConfig with id " + id + " could not be found");
			return null;
		}
	}
	
	/**
	 * retrieves all ButtonConfigs in database
	 * @return list of all ButtonConfigs in Database
	 */
	public List<ButtonConfig> getAll() {
		return this.buttonConfigRepository.findAll();
	}
	
	/**
	 * check if buttonConfig exists by it's id
	 * @param id key of buttonConfig
	 * @return true if buttonConfig is present in database
	 */
	public boolean exists(int id) {
		return this.buttonConfigRepository.existsById(id);
	}
	
	/**
	 * saves buttonConfig into database
	 * @param buttonConfig id of the button configuration
	 */
	public void save(ButtonConfig buttonConfig) {
		this.buttonConfigRepository.save(buttonConfig);
	}
	
	/**
	 * delete buttonConfig from database
	 * @param buttonConfig id of the button configuration
	 */
	public void delete(ButtonConfig buttonConfig) {
		this.buttonConfigRepository.delete(buttonConfig);
	}
	
	/**
	 * deletes all buttonConfigs in database
	 */
	public void deleteAll() {
		this.buttonConfigRepository.deleteAll();
	}
}
