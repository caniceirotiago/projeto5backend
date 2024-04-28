package aor.paj.bean;

import aor.paj.exception.DatabaseOperationException;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@Singleton
@Startup
public class StartupBean {
    @EJB
    UserBean userBean;
    @EJB
    CategoryBean categoryBean;
    @EJB
    ConfigurationBean configurationBean;

    @PostConstruct
    public void init() throws DatabaseOperationException {
        userBean.createDefaultUsersIfNotExistent();
        categoryBean.createDefaultCategoryIfNotExistent();
        initializeDefaultConfigurations();
    }
    private void initializeDefaultConfigurations() throws DatabaseOperationException {
        configurationBean.initializeDefaultConfigurations();
    }
}
