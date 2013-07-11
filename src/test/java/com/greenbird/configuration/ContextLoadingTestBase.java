package com.greenbird.configuration;

import org.junit.runner.RunWith;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-context.xml")
public abstract class ContextLoadingTestBase {
    protected GenericXmlApplicationContext createContextManually() {
        return new GenericXmlApplicationContext("/test-spring-context.xml");
    }
}
