package com.greenbird.configuration;

import org.springframework.beans.factory.annotation.Value;

public class ContextTestBean3 {
    @Value("${test.property.3}")
    private String testValue3;

    public String getTestValue3() {
        return testValue3;
    }

    public void setTestValue3(String testValue3) {
        this.testValue3 = testValue3;
    }
}
