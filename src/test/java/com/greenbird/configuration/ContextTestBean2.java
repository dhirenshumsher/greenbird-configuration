package com.greenbird.configuration;

import org.springframework.beans.factory.annotation.Value;

public class ContextTestBean2 {
    @Value("${test.property.2}")
    private String testValue2;

    public String getTestValue2() {
        return testValue2;
    }

    public void setTestValue2(String testValue2) {
        this.testValue2 = testValue2;
    }
}
