package com.greenbird.configuration.report;

import com.greenbird.configuration.ContextLoadingTestBase;
import com.greenbird.test.logging.TestLogAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationReporterTest extends ContextLoadingTestBase {
    private static final String LS = System.getProperty("line.separator");
    private TestLogAppender testLogAppender;

    @Before
    public void setUp() {
        testLogAppender = new TestLogAppender(ConfigurationReporter.class);
    }

    @After
    public void tearDownAppender() {
        testLogAppender.close();
    }

    @Test
    public void setApplicationContext_allSubsystemsActive_reportIsLoggedAndContainsExpectedElements() {
        createContextForProfiles("prod", "testprofile");
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
        assertThat(normalizedMessage, containsString("com.greenbird.configuration.sub: AnotherBeanToBeReported (externalBuilderBean) BeanToBeReported (beanToBeReported1, beanToBeReported2) BeanWithBeanBuilderPattern (beanBuilderPattern) BeanWithBeanBuilderPattern$MyBuilderBean (myBuilderBean)"));
    }

    @Test
    public void setApplicationContext_onlyReportAndPropertySubsystemsActive_reportIsLoggedAndContainsExpectedElements() {
        createContextForConfiguration("test-spring-context-report-only.xml", "test-spring-context-properties-only.xml");
        String normalizedMessage = getNormalizedMessageFromLogEvent();

        assertThat(normalizedMessage, containsString("GREENBIRD CONFIGURATION REPORT"));
        assertThat(normalizedMessage, containsString("SPRING PROFILES"));
        assertThat(normalizedMessage, containsString("Active profiles: <none>"));
        assertThat(normalizedMessage, containsString("Default profiles: default"));
        assertThat(normalizedMessage, containsString("AUTO-LOADED CONFIGURATION FILES"));
        assertThat(normalizedMessage, containsString("/gb-conf/greenbird-default.properties"));
        assertThat(normalizedMessage, containsString("CONFIGURATION PROPERTIES"));
        assertThat(normalizedMessage, containsString("test.property = value"));
        assertThat(normalizedMessage, containsString("AUTO-LOADED SPRING DEFINITION FILES"));
        assertThat(normalizedMessage, containsString("Spring definition sub-system has not been loaded"));
        assertThat(normalizedMessage, not(containsString("greenbird-context.xml")));
        assertThat(normalizedMessage, containsString("BEANS IN CONTEXT"));
        assertThat(normalizedMessage, containsString("ConfigurationReporter (configurationReporter)"));
        assertThat(normalizedMessage, not(containsString("ConfigPojoTestBean")));
    }

    @Test
    public void setApplicationContext_onlyReportAndContextSubsystemsActive_reportIsLoggedAndContainsExpectedElements() {
        createContextForConfiguration("test-spring-context-report-only.xml", "test-spring-context-contexts-only.xml");
        String normalizedMessage = getNormalizedMessageFromLogEvent();

        assertThat(normalizedMessage, containsString("GREENBIRD CONFIGURATION REPORT"));
        assertThat(normalizedMessage, containsString("SPRING PROFILES"));
        assertThat(normalizedMessage, containsString("Active profiles: <none>"));
        assertThat(normalizedMessage, containsString("Default profiles: default"));
        assertThat(normalizedMessage, containsString("AUTO-LOADED CONFIGURATION FILES"));
        assertThat(normalizedMessage, containsString("No files to report as configuration property sub-system has not been loaded"));
        assertThat(normalizedMessage, not(containsString("greenbird-default.properties")));
        assertThat(normalizedMessage, containsString("CONFIGURATION PROPERTIES"));
        assertThat(normalizedMessage, containsString("No properties to report"));
        assertThat(normalizedMessage, not(containsString("test.property = value")));
        assertThat(normalizedMessage, containsString("AUTO-LOADED SPRING DEFINITION FILES"));
        assertThat(normalizedMessage, containsString("/gb-conf/sub_config_1/greenbird-context.xml"));
        assertThat(normalizedMessage, containsString("/gb-conf/sub_config_2/greenbird-context.xml"));
        assertThat(normalizedMessage, containsString("/gb-conf/greenbird-configuration-context.xml"));
        assertThat(normalizedMessage, containsString("BEANS IN CONTEXT"));
        assertThat(normalizedMessage, containsString("com.greenbird.configuration: ConfigPojoTestBean (pojoTestBean)"));
        assertThat(normalizedMessage, containsString("com.greenbird.configuration.sub: BeanToBeReported (beanToBeReported1, beanToBeReported2)"));
    }

    @Test
    public void setApplicationContext_onlyReportSubsystemsActive_reportIsLoggedAndContainsExpectedElements() {
        createContextForConfiguration("test-spring-context-report-only.xml");
        String normalizedMessage = getNormalizedMessageFromLogEvent();

        assertThat(normalizedMessage, containsString("GREENBIRD CONFIGURATION REPORT"));
        assertThat(normalizedMessage, containsString("SPRING PROFILES"));
        assertThat(normalizedMessage, containsString("Active profiles: <none>"));
        assertThat(normalizedMessage, containsString("Default profiles: default"));
        assertThat(normalizedMessage, containsString("AUTO-LOADED CONFIGURATION FILES"));
        assertThat(normalizedMessage, containsString("No files to report as configuration property sub-system has not been loaded"));
        assertThat(normalizedMessage, not(containsString("greenbird-default.properties")));
        assertThat(normalizedMessage, containsString("CONFIGURATION PROPERTIES"));
        assertThat(normalizedMessage, containsString("No properties to report"));
        assertThat(normalizedMessage, not(containsString("test.property = value")));
        assertThat(normalizedMessage, containsString("AUTO-LOADED SPRING DEFINITION FILES"));
        assertThat(normalizedMessage, containsString("Spring definition sub-system has not been loaded"));
        assertThat(normalizedMessage, not(containsString("greenbird-context.xml")));
        assertThat(normalizedMessage, containsString("BEANS IN CONTEXT"));
        assertThat(normalizedMessage, containsString("ConfigurationReporter (configurationReporter)"));
        assertThat(normalizedMessage, not(containsString("ConfigPojoTestBean")));
    }

    @Test
    public void setApplicationContext_reportSubsystemNotActive_reportIsNotLogged() {
        createContextForConfiguration("test-spring-context-properties-only.xml",
                "test-spring-context-contexts-only.xml", "test-spring-context-jmx-only.xml");
        List<LoggingEvent> loggingEvents = testLogAppender.getLoggingEvents();
        assertThat(loggingEvents.size(), is(0));
    }

    @Test
    public void setApplicationContext_missingBeanClassName_loggedUnderUnknownPackage() {
        createContextForConfiguration("test-spring-context-bean-class-missing.xml");
        String normalizedMessage = getNormalizedMessageFromLogEvent();

        assertThat(normalizedMessage, containsString("<unknown package>"));
        assertThat(normalizedMessage, containsString("<unknown class>"));
        assertThat(normalizedMessage, containsString("(child.bean)"));
    }

    private String getNormalizedMessageFromLogEvent() {
        List<LoggingEvent> loggingEvents = testLogAppender.getLoggingEvents();
        assertThat(loggingEvents.size(), is(1));
        LoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel(), is(Level.INFO));
        return loggingEvent.getMessage().toString().replace(LS, " ").replaceAll(" +", " ");
    }
}
