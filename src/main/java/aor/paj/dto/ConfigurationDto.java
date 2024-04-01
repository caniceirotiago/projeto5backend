package aor.paj.dto;

public class ConfigurationDto {
    private String configKey;
    private String configValue;
    public ConfigurationDto() {
    }

    public ConfigurationDto(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
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
}
