package com.greenbird.configuration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.jmx.support.JmxUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class GreenbirdConfigurationMBeanJMXTest extends ContextLoadingTestBase {
    private MBeanServer mBeanServer = JmxUtils.locateMBeanServer();
    private ObjectName objectName;

    @Before
    public void setUp() throws Exception {
        objectName = ObjectName.getInstance("greenbird.configuration:name=greenbirdConfiguration,type=GreenbirdConfiguration");
    }

    @Test
    public void getGreenbirdProperties_normal_propertiesReturned() throws Exception {
        Properties properties = (Properties) mBeanServer.getAttribute(objectName, "GreenbirdProperties");
        assertThat(properties.size(), greaterThan(0));
    }

    @Test
    public void getLoadedGreenbirdModules_normal_modulesReturned() throws Exception {
        @SuppressWarnings("unchecked")
        List<Resource> loadedGreenbirdModules = (List<Resource>) mBeanServer.getAttribute(objectName, "LoadedGreenbirdModules");
        assertThat(loadedGreenbirdModules.size(), greaterThan(0));
    }
}
