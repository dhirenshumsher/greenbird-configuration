package com.greenbird.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigTestBean {
    @Value("${test.property}")
    private String value;
    @Value("${test.property.2}")
    private String value2;
    @Value("${default.test.property}")
    private String defaultValue;
    @Value("${preset.test.property}")
    private String presetValue;
    @Value("${test.uuid}")
    private String uuid;

    @Autowired
    private Environment environment;

    public String getValue() {
        return value;
    }

    public String getValue2() {
        return value2;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEnvironmentValue() {
        return environment.getProperty("environment.test.property");
    }

    public String getPresetValue() {
        return presetValue;
    }
}
