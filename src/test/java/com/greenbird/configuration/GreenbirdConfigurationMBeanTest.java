package com.greenbird.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GreenbirdConfigurationMBeanTest {
    @Mock
    private GreenbirdConstrettoPropertyPlaceholderConfigurer mockPropertyConfigurer;
    @Mock
    private GreenbirdResourceFinder mockResourceFinder;

    private GreenbirdConfigurationMBean configurationMBean = new GreenbirdConfigurationMBean();

    @Before
    public void setUp() {
        configurationMBean.setPropertyConfigurer(mockPropertyConfigurer);
        configurationMBean.setResourceFinder(mockResourceFinder);
    }

    @Test
    public void getProperties_normal_propertiesReturned() {
        Properties properties = new Properties();
        when(mockPropertyConfigurer.getProperties()).thenReturn(properties);
        assertThat(configurationMBean.getGreenbirdProperties(), is(properties));
    }

    @Test
    public void getLoadedGreenbirdModules_normal_modulesReturned() throws MalformedURLException {
        Resource[] resources = new Resource[]{new UrlResource("http://test")};
        when(mockResourceFinder.findGreenbirdModules()).thenReturn(resources);
        assertThat(configurationMBean.getLoadedGreenbirdModules(), is(asList(resources)));
    }
}
