package com.greenbird.configuration;

import com.greenbird.test.logging.TestLogAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationReporterTest extends ContextLoadingTestBase {
    private static final String LS = System.getProperty("line.separator");
    private TestLogAppender testLogAppender;

    @Before
    public void setUpAppender() {
        testLogAppender = new TestLogAppender(ConfigurationReporter.class);
        createContextManually("prod", "testprofile");
    }

    @After
    public void tearDownAppender() {
        testLogAppender.close();
    }

    @Test
    public void setApplicationContext_normal_reportIsLoggedAndContainsExpectedElements() {
        String normalizedMessage = getNormalizedMessageFromLogEvent();
        assertThat(normalizedMessage, containsString("GREENBIRD CONFIGURATION REPORT"));
        assertThat(normalizedMessage, containsString("SPRING PROFILES"));
        assertThat(normalizedMessage, containsString("Active profiles: prod, testprofile Default profiles: default"));
        assertThat(normalizedMessage, containsString("AUTO-LOADED CONFIGURATION FILES"));
        assertThat(normalizedMessage, containsString("/gb-conf/greenbird-default.properties"));
        assertThat(normalizedMessage, containsString("/gb-conf/greenbird-testprofile.properties"));
        assertThat(normalizedMessage, containsString("CONFIGURATION PROPERTIES"));
        assertThat(normalizedMessage, containsString("test.property = valueProd"));
        assertThat(normalizedMessage, containsString("AUTO-LOADED SPRING DEFINITION FILES"));
        assertThat(normalizedMessage, containsString("/gb-conf/sub_config_1/greenbird-context.xml"));
        assertThat(normalizedMessage, containsString("/gb-conf/sub_config_2/greenbird-context.xml"));
        assertThat(normalizedMessage, containsString("/gb-conf/greenbird-configuration-context.xml"));
        assertThat(normalizedMessage, containsString("BEANS IN CONTEXT"));
        assertThat(normalizedMessage, containsString("com.greenbird.configuration: ConfigPojoTestBean (pojoTestBean)"));
        assertThat(normalizedMessage, containsString("com.greenbird.configuration.sub: BeanToBeReported (beanToBeReported1, beanToBeReported2)"));
    }

    private String getNormalizedMessageFromLogEvent() {
        List<LoggingEvent> loggingEvents = testLogAppender.getLoggingEvents();
        assertThat(loggingEvents.size(), is(1));
        LoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel(), is(Level.INFO));
        return loggingEvent.getMessage().toString().replace(LS, " ").replaceAll(" +", " ");
    }

}
