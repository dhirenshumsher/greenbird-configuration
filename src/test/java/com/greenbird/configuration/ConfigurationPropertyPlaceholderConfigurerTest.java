package com.greenbird.configuration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.util.List;

import static com.greenbird.configuration.ConfigurationPropertyPlaceholderConfigurer.GREENBIRD_CONFIG_UUID_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ConfigurationPropertyPlaceholderConfigurerTest extends ContextLoadingTestBase {
    @Autowired
    private ConfigTestBean testBean;

    @Autowired
    private ConfigPojoTestBean pojoTestBean;

    @Autowired
    private ConfigurationPropertyPlaceholderConfigurer propertyConfigurer;

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
        GenericXmlApplicationContext context = createContextManually("prod", "other", "testprofile");
        ConfigTestBean bean = context.getBean("configTestBean", ConfigTestBean.class);
        assertThat(bean.getValue(), is("valueProd"));
        assertThat(bean.getValue2(), is("value2Other"));
        assertThat(bean.getEnvironmentValue(), is("envValueProd"));
        assertThat(bean.getDefaultValue(), is("testProfileValue"));
    }

    @Test
    public void configure_defaultValueNotOverridden_defaultValueIsUsed() {
        assertThat(testBean.getDefaultValue(), is("defaultValue"));
    }

    @Test
    public void configure_presetValueNotOverridden_presetValueIsUsed() {
        assertThat(testBean.getPresetValue(), is("presetValue"));
    }

    @Test
    public void createPropertyReport_normal_propertiesReportedAsExpected() {
        String message = propertyConfigurer.createPropertyReport();
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
    public void getLoadedPropertyFiles_normal_expectedFilesLoadedInExpectedOrder() {
        GenericXmlApplicationContext context = createContextManually("testprofile");
        ConfigurationPropertyPlaceholderConfigurer configurer = context.getBean(ConfigurationPropertyPlaceholderConfigurer.class);
        List<Resource> loadedFiles = configurer.getLoadedPropertyFiles();
        assertThat(loadedFiles.size(), is(4));
        assertThat(loadedFiles.get(0).toString(), containsString("/gb-conf/greenbird-preset.properties"));
        assertThat(loadedFiles.get(1).toString(), containsString("/gb-conf/greenbird-configuration-preset.properties"));
        assertThat(loadedFiles.get(2).toString(), containsString("/gb-conf/greenbird-default.properties"));
        assertThat(loadedFiles.get(3).toString(), containsString("/gb-conf/greenbird-testprofile.properties"));
    }

}