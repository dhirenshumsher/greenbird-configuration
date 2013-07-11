package com.greenbird.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigTestBean {
    @Value("${test.property}")
    private String value;
    @Value("${default.test.property}")
    private String defaultValue;
    @Value("${test.uuid}")
    private String uuid;

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getUuid() {
        return uuid;
    }
}
