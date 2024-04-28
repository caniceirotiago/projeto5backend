package aor.paj.bean;

import aor.paj.exception.DatabaseOperationException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import aor.paj.dao.ConfigurationDao;
import aor.paj.entity.ConfigurationEntity;

@Stateless
public class ConfigurationBean {

    @EJB
    private ConfigurationDao configurationDao;

    public void initializeDefaultConfigurations() throws DatabaseOperationException {
        String sessionTimeoutKey = "sessionTimeout";
        if (!configurationDao.configExists(sessionTimeoutKey)) {
            ConfigurationEntity sessionTimeout = new ConfigurationEntity(sessionTimeoutKey, "1800", "Timeout da sessão em segundos");
            configurationDao.persist(sessionTimeout);
        }
    }
    public String findConfigValueByKey(String configKey) {
       return configurationDao.findConfigValueByKey(configKey);
    }
    public void updateConfigValue(String configKey, String configValue) {
        configurationDao.updateConfigValue(configKey, configValue);
    }
}
