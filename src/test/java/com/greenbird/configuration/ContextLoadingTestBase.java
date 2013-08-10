package com.greenbird.configuration;

import com.google.common.base.Joiner;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-context.xml")
public abstract class ContextLoadingTestBase {

    @After
    public void unsetProfiles() {
        System.setProperty("spring.profiles.active", "");
    }

    protected GenericXmlApplicationContext createContextManually(String... activeProfiles) {
        System.setProperty("spring.profiles.active", Joiner.on(",").join(activeProfiles));
        return new GenericXmlApplicationContext("/test-spring-context.xml");
    }
}
