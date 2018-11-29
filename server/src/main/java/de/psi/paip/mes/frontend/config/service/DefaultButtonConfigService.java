package de.psi.paip.mes.frontend.config.service;

import de.psi.paip.mes.frontend.config.model.DefaultButtonConfig;
import de.psi.paip.mes.frontend.config.model.repository.DefaultButtonConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DefaultButtonConfigService {
    private final Logger log = LoggerFactory.getLogger(DefaultButtonConfigService.class);

    @Autowired
    private DefaultButtonConfigRepository defaultButtonConfigRepository;

    /**
     * fetch all entries from table
     * @return List of all default button configurations
     */
    public List<DefaultButtonConfig> findAll() {
        return this.defaultButtonConfigRepository.findAll();
    }

    /**
     * fetch DefaultButtonConfig by its id
     * @param id if of defaultButtonConfig
     * @return single entry of a default button configuration filtered by id
     */
    public DefaultButtonConfig getDefaultButtonConfig(int id) {
        Optional<DefaultButtonConfig> optional = this.defaultButtonConfigRepository.findById(id);

        if(optional.isPresent()) {
            return optional.get();
        } else {
            log.warn("DefaultButtonConfig with id " + id + " could not be found");
            return null;
        }
    }

    /**
     * check if defaultbuttonConfig exists by it's id
     * @param id key of buttonConfig
     * @return true if defaultbuttonConfig is present in database
     */
    public boolean exists(int id) {
        return this.defaultButtonConfigRepository.existsById(id);
    }

    /**
     * saves defaultbuttonConfig into database
     * @param defaultbuttonConfig DTO default button configuration
     */
    public void save(DefaultButtonConfig defaultbuttonConfig) {
        this.defaultButtonConfigRepository.save(defaultbuttonConfig);
    }

    /**
     * delete defaultbuttonConfig from database
     * @param defaultbuttonConfig DTO default button configuration
     */
    public void delete(DefaultButtonConfig defaultbuttonConfig) {
        this.defaultButtonConfigRepository.delete(defaultbuttonConfig);
    }
}
