package aor.paj.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

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
    public void init() {
        userBean.createDefaultUsersIfNotExistent();
        categoryBean.createDefaultCategoryIfNotExistent();
        initializeDefaultConfigurations();
    }
    private void initializeDefaultConfigurations() {
        configurationBean.initializeDefaultConfigurations();
    }
}
