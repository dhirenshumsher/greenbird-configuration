package com.greenbird.configuration;

import com.greenbird.test.logging.TestLogAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Properties;

import static com.greenbird.configuration.GreenbirdPropertyPlaceholderConfigurer.GREENBIRD_CONFIG_UUID_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class GreenbirdPropertyPlaceholderConfigurerTest extends ContextLoadingTestBase {
    @Autowired
    private ConfigTestBean testBean;

    @Autowired
    private ConfigPojoTestBean pojoTestBean;

    @Autowired
    private GreenbirdPropertyPlaceholderConfigurer propertyConfigurer;

    private TestLogAppender testAppender;

    @Before
    public void setUp() {
        testAppender = new TestLogAppender(GreenbirdPropertyPlaceholderConfigurer.class);
    }

    @After
    public void tearDown() {
        testAppender.close();
    }

    @Test
    public void configure_valueAnnotationUsed_propertyIsSet() {
        assertThat(testBean.getValue(), is("value"));
    }

    @Test
    public void configure_springEnvironmentUsed_propertyIsSet() {
        assertThat(pojoTestBean.getValue(), is("pojoValue"));
    }

    @Test
    public void configure_springXmlPropertyExpansionUsed_propertyIsSet() {
        assertThat(testBean.getEnvironmentValue(), is("envValue"));
    }

    @Test
    public void configure_springProfilesActive_profilesAreConsidered() {
        System.setProperty("spring.profiles.active", "prod,other");
        GenericXmlApplicationContext context = createContextManually();
        ConfigTestBean bean = context.getBean("configTestBean", ConfigTestBean.class);
        assertThat(bean.getValue(), is("valueProd"));
        assertThat(bean.getValue2(), is("value2Other"));
        assertThat(bean.getEnvironmentValue(), is("envValueProd"));
        System.setProperty("spring.profiles.active", "");
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

    @Test
    public void getProperty_valueReferencesOtherProperty_referencedPropertyExpandedIntoValue() {
        Properties properties = propertyConfigurer.getProperties();
        assertThat(properties.size(), greaterThan(0));
        assertThat(properties.getProperty("test.reference"), is("value-reference"));
    }

}
