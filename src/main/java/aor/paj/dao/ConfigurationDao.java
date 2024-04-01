package aor.paj.dao;

import aor.paj.entity.ConfigurationEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Stateless
public class ConfigurationDao extends AbstractDao<ConfigurationEntity> {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager entityManager;

    public ConfigurationDao() {
        super(ConfigurationEntity.class);
    }

    public String findConfigValueByKey(String configKey) {
        try {
            Query query = entityManager.createQuery("SELECT c.configValue FROM ConfigurationEntity c WHERE c.configKey = :configKey");
            query.setParameter("configKey", configKey);
            return (String) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean updateConfigValue(String configKey, String configValue) {
        try {
            Query query = entityManager.createQuery("UPDATE ConfigurationEntity c SET c.configValue = :configValue WHERE c.configKey = :configKey");
            query.setParameter("configValue", configValue);
            query.setParameter("configKey", configKey);
            return query.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean configExists(String configKey) {
        try {
            Query query = entityManager.createQuery("SELECT count(c) FROM ConfigurationEntity c WHERE c.configKey = :configKey");
            query.setParameter("configKey", configKey);
            Long count = (Long) query.getSingleResult();
            return count > 0;
        } catch (NoResultException e) {
            return false;
        }
    }
}
