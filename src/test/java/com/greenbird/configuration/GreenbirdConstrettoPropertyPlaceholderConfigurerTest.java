package com.greenbird.configuration;

import com.greenbird.test.logging.TestLogAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Properties;

import static com.greenbird.configuration.GreenbirdConstrettoPropertyPlaceholderConfigurer.GREENBIRD_CONFIG_UUID_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class GreenbirdConstrettoPropertyPlaceholderConfigurerTest extends ContextLoadingTestBase {

    @Autowired
    private ConfigTestBean testBean;

    @Autowired
    private GreenbirdConstrettoPropertyPlaceholderConfigurer propertyConfigurer;

    private TestLogAppender testAppender;

    @Before
    public void setUp() {
        testAppender = new TestLogAppender(GreenbirdConstrettoPropertyPlaceholderConfigurer.class);
    }

    @After
    public void tearDown() {
        testAppender.close();
    }

    @Test
    public void configure_normal_propertiesAreLoadedIntoSpring() {
        assertThat(testBean.getValue(), is("value"));
    }

    @Test
    public void configure_constrettoTagActive_tagsAreConsidered() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "prod");
        GenericXmlApplicationContext context = createContextManually();
        ConfigTestBean bean = context.getBean("configTestBean", ConfigTestBean.class);
        assertThat(bean.getValue(), is("valueProd"));
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "");
    }

    @Test
    public void configure_defaultValueNotOverridden_defaultValueIsUsed() {
        assertThat(testBean.getDefaultValue(), is("defaultValue"));
    }

    @Test
    public void configure_loggerConfiguredToLog_propertiesAreReported() {
        createContextManually();
        LoggingEvent propertyReportEvent = testAppender.getLoggingEvents().get(testAppender.getLoggingEvents().size() - 1);
        String message = propertyReportEvent.getMessage().toString();
        assertThat(message, containsString("default.test.property"));
        assertThat(message, containsString("test.property.2"));
    }

    @Test
    public void configure_uuidTagUsed_valueContainsUuid() {
        String uuid1 = testBean.getUuid().replaceFirst("random\\.(.*)\\.test.*", "$1");
        assertThat(uuid1.length(), greaterThan(0));
        assertThat(uuid1, not(containsString(GREENBIRD_CONFIG_UUID_KEY)));
        String uuid2 = testBean.getUuid().replaceFirst("random\\..*\\.test\\.(.*)", "$1");
        assertThat(uuid2.length(), greaterThan(0));
        assertThat(uuid2, not(containsString(GREENBIRD_CONFIG_UUID_KEY)));
    }

    @Test
    public void getProperties_normal_propertiesReturned() {
        Properties properties = propertyConfigurer.getProperties();
        assertThat(properties.size(), greaterThan(0));
        assertThat(properties.getProperty("test.property"), is("value"));
    }

}
