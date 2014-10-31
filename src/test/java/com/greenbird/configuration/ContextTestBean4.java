package com.greenbird.configuration;

import org.springframework.beans.factory.annotation.Value;

public class ContextTestBean4 {
    @Value("${test.property.4}")
    private String testValue4;

    public String getTestValue4() {
        return testValue4;
    }

    public void setTestValue4(String testValue4) {
        this.testValue4 = testValue4;
    }
}
