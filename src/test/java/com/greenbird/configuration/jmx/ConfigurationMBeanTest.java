package com.greenbird.configuration.jmx;

import com.greenbird.configuration.ContextTestBean1;
import com.greenbird.configuration.ContextTestBean2;
import com.greenbird.configuration.context.SpringContextLoader;
import com.greenbird.configuration.properties.ConfigurationPropertyPlaceholderConfigurer;
import com.greenbird.configuration.util.ResourceFinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;

import java.net.MalformedURLException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
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
    @Mock
    private SpringContextLoader mockContextLoader;

    private ConfigurationMBean configurationMBean = new ConfigurationMBean();

    @Before
    public void setUp() {
        configurationMBean.setResourceFinder(mockResourceFinder);
        configurationMBean.setEnvironment(mockEnvironment);
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
        setUpBeanRetrieval(ConfigurationPropertyPlaceholderConfigurer.class, mockPropertyConfigurer);
        configurationMBean.setApplicationContext(mockApplicationContext);

        Resource resourceA = new DescriptiveResource("a");
        Resource resourceB = new DescriptiveResource("b");
        when(mockPropertyConfigurer.getLoadedPropertyFiles()).thenReturn(asList(resourceA, resourceB));
        List<String> loadedPropertyFiles = configurationMBean.getLoadedConfigurationFiles();
        assertThat(loadedPropertyFiles, contains("a", "b"));
    }

    @Test
    public void getLoadedConfigurationFiles_propertySubSystemNotLoaded_emptyListReturned() {
        setUpMissingBean(ConfigurationPropertyPlaceholderConfigurer.class);
        configurationMBean.setApplicationContext(mockApplicationContext);

        List<String> loadedPropertyFiles = configurationMBean.getLoadedConfigurationFiles();
        assertThat(loadedPropertyFiles, is(empty()));
    }

    @Test
    public void getPropertiesReport_normal_propertiesReturned() {
        setUpBeanRetrieval(ConfigurationPropertyPlaceholderConfigurer.class, mockPropertyConfigurer);
        configurationMBean.setApplicationContext(mockApplicationContext);

        when(mockPropertyConfigurer.createPropertyReport()).thenReturn("x");
        assertThat(configurationMBean.getPropertiesReport(), is("x"));
    }

    @Test
    public void getPropertiesReport_propertySubSystemNotLoaded_emptyStringReturned() {
        setUpMissingBean(ConfigurationPropertyPlaceholderConfigurer.class);
        configurationMBean.setApplicationContext(mockApplicationContext);

        assertThat(configurationMBean.getPropertiesReport(), is(""));
    }

    @Test
    public void getLoadedSpringDefinitionFiles_normal_modulesReturned() throws MalformedURLException {
        setUpBeanRetrieval(SpringContextLoader.class, mockContextLoader);
        configurationMBean.setApplicationContext(mockApplicationContext);

        Resource[] resources = new Resource[]{new DescriptiveResource("a"), new DescriptiveResource("b")};
        when(mockResourceFinder.findContextDefinitions()).thenReturn(resources);
        assertThat(configurationMBean.getLoadedSpringDefinitionFiles(), contains("a", "b"));
    }

    @Test
    public void getLoadedSpringDefinitionFiles_contextLoadingSubSystemNotLoaded_emptyListReturned() throws MalformedURLException {
        setUpMissingBean(SpringContextLoader.class);
        configurationMBean.setApplicationContext(mockApplicationContext);

        Resource[] resources = new Resource[]{new DescriptiveResource("a"), new DescriptiveResource("b")};
        when(mockResourceFinder.findContextDefinitions()).thenReturn(resources);

        assertThat(configurationMBean.getLoadedSpringDefinitionFiles(), is(empty()));
    }

    @Test
    public void getBeansInContext_normal_sortedAndFilteredBeanListReturned() {
        when(mockApplicationContext.getBeanDefinitionNames()).thenReturn(new String[]{"b", "a", "c"});
        when(mockApplicationContext.getBean("a")).thenReturn(new ContextTestBean1());
        when(mockApplicationContext.getBean("b")).thenReturn(new ContextTestBean2());
        when(mockApplicationContext.getBean("c")).thenReturn(new DescriptiveResource("c")); // expected to be filtered
        configurationMBean.setApplicationContext(mockApplicationContext);

        assertThat(configurationMBean.getBeansInContext(), contains(
                format("a (%s)", ContextTestBean1.class.getName()),
                format("b (%s)", ContextTestBean2.class.getName())
        ));
    }

    private <T> void setUpBeanRetrieval(Class<T> type, T instance) {
        when(mockApplicationContext.getBean(type)).thenReturn(instance);
    }

    @SuppressWarnings("unchecked")
    private void setUpMissingBean(Class<?> type) {
        when(mockApplicationContext.getBean(type)).thenThrow(NoSuchBeanDefinitionException.class);
    }
}
