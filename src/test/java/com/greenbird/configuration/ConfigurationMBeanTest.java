package com.greenbird.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;

import java.net.MalformedURLException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationMBeanTest {
    @Mock
    private ConfigurationPropertyPlaceholderConfigurer mockPropertyConfigurer;
    @Mock
    private ResourceFinder mockResourceFinder;
    @Mock
    private Environment mockEnvironment;
    @Mock
    private ApplicationContext mockApplicationContext;

    private ConfigurationMBean configurationMBean = new ConfigurationMBean();

    @Before
    public void setUp() {
        configurationMBean.setPropertyConfigurer(mockPropertyConfigurer);
        configurationMBean.setResourceFinder(mockResourceFinder);
        configurationMBean.setEnvironment(mockEnvironment);
        configurationMBean.setApplicationContext(mockApplicationContext);
    }

    @Test
    public void getActiveSpringProfiles_noProfileActive_noProfilesReturned() {
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[0]);
        assertThat(configurationMBean.getActiveSpringProfiles(), is("<none>"));
    }

    @Test
    public void getActiveSpringProfiles_profilesActive_profilesReturned() {
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{"a", "b"});
        assertThat(configurationMBean.getActiveSpringProfiles(), is("a, b"));
    }

    @Test
    public void getDefaultSpringProfiles_noProfileDefault_noProfilesReturned() {
        when(mockEnvironment.getDefaultProfiles()).thenReturn(new String[0]);
        assertThat(configurationMBean.getDefaultSpringProfiles(), is("<none>"));
    }

    @Test
    public void getDefaultSpringProfiles_profilesDefault_profilesReturned() {
        when(mockEnvironment.getDefaultProfiles()).thenReturn(new String[]{"a", "b"});
        assertThat(configurationMBean.getDefaultSpringProfiles(), is("a, b"));
    }

    @Test
    public void getLoadedConfigurationFiles_normal_propertyFilesReturned() {
        Resource resourceA = new DescriptiveResource("a");
        Resource resourceB = new DescriptiveResource("b");
        when(mockPropertyConfigurer.getLoadedPropertyFiles()).thenReturn(asList(resourceA, resourceB));
        List<String> loadedPropertyFiles = configurationMBean.getLoadedConfigurationFiles();
        assertThat(loadedPropertyFiles, contains("a", "b"));
    }

    @Test
    public void getPropertiesReport_normal_propertiesReturned() {
        when(mockPropertyConfigurer.createPropertyReport()).thenReturn("x");
        assertThat(configurationMBean.getPropertiesReport(), is("x"));
    }

    @Test
    public void getLoadedSpringDefinitionFiles_normal_modulesReturned() throws MalformedURLException {
        Resource[] resources = new Resource[]{new DescriptiveResource("a"), new DescriptiveResource("b")};
        when(mockResourceFinder.findContextDefinitions()).thenReturn(resources);
        assertThat(configurationMBean.getLoadedSpringDefinitionFiles(), contains("a", "b"));
    }

    @Test
    public void getBeansInContext_normal_sortedAndFilteredBeanListReturned() {
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"b", "a", "c"});
        when(mockApplicationContext.getBean("a")).thenReturn(new ContextTestBean1());
        when(mockApplicationContext.getBean("b")).thenReturn(new ContextTestBean2());
        when(mockApplicationContext.getBean("c")).thenReturn(new DescriptiveResource("c")); // expected to be filtered

        assertThat(configurationMBean.getBeansInContext(), contains(
                format("a (%s)", ContextTestBean1.class.getName()),
                format("b (%s)", ContextTestBean2.class.getName())
        ));
    }
}
