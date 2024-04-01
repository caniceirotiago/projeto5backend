package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="system_configuration")
@NamedQueries({
        @NamedQuery(name="ConfigurationEntity.findConfigValueByKey", query="SELECT c.configValue FROM ConfigurationEntity c WHERE c.configKey = :configKey"),
        @NamedQuery(name="ConfigurationEntity.configExists", query="SELECT count(c) FROM ConfigurationEntity c WHERE c.configKey = :configKey"),
})
public class ConfigurationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="config_key", nullable=false, unique=true, updatable=false)
    private String configKey;

    @Column(name="config_value", nullable=false, unique=false, updatable=true)
    private String configValue;

    @Column(name="description", nullable=true, unique=false, updatable=true)
    private String description;

    public ConfigurationEntity() {}

    public ConfigurationEntity(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // toString
    @Override
    public String toString() {
        return "ConfigurationEntity{" +
                "configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
