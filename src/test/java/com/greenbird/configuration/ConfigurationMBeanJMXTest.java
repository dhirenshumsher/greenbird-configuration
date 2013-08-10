package com.greenbird.configuration;

import com.google.common.base.Joiner;
import com.greenbird.test.GreenbirdTestException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jmx.support.JmxUtils;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationMBeanJMXTest extends ContextLoadingTestBase {
    private MBeanServer mBeanServer = JmxUtils.locateMBeanServer();
    private ObjectName objectName;

    @Before
    public void setUp() throws Exception {
        objectName = ObjectName.getInstance("greenbird.configuration:name=greenbirdConfiguration,type=GreenbirdConfiguration");
    }

    @Test
    public void getActiveSpringProfiles_normal_noneReturned() throws Exception {
        String activeSpringProfiles = attribute("ActiveSpringProfiles");
        assertThat(activeSpringProfiles, is("<none>"));
    }

    @Test
    public void getDefaultSpringProfiles_normal_defaultReturned() throws Exception {
        String defaultSpringProfiles = attribute("DefaultSpringProfiles");
        assertThat(defaultSpringProfiles, is("default"));
    }

    @Test
    public void getLoadedConfigurationFiles_normal_expectedFilesReturned() throws Exception {
        List<String> configurationFiles = attribute("LoadedConfigurationFiles");
        assertThat(configurationFiles.size(), is(1));
        assertThat(configurationFiles.get(0), containsString("/gb-conf/greenbird-default.properties"));
    }

    @Test
    public void getPropertiesReport_normal_expectedContentReturned() throws Exception {
        String propertiesReport = attribute("PropertiesReport");
        String normalizedPropertiesReport = propertiesReport.replaceAll(" +", " ");
        assertThat(normalizedPropertiesReport, containsString("test.property = value"));
    }

    @Test
    public void getLoadedSpringDefinitionFiles_normal_expectedContentReturned() throws Exception {
        List<String> configurationFiles = attribute("LoadedSpringDefinitionFiles");
        assertThat(configurationFiles.size(), is(3));
        String mergedFileList = Joiner.on(" ").join(configurationFiles);
        assertThat(mergedFileList, containsString("/gb-conf/greenbird-configuration-context.xml"));
        assertThat(mergedFileList, containsString("/gb-conf/sub_config_1/greenbird-context.xml"));
        assertThat(mergedFileList, containsString("/gb-conf/sub_config_2/greenbird-context.xml"));
    }

    @Test
    public void getBeansInContext_normal_expectedContentReturned() {
        List<String> beansInContext = attribute("BeansInContext");
        assertThat(beansInContext.size(), greaterThan(0));
        String mergedBeansList = Joiner.on(" ").join(beansInContext);
        assertThat(mergedBeansList, containsString("beanToBeReported1 (com.greenbird.configuration.sub.BeanToBeReported)"));
    }

    @SuppressWarnings("unchecked")
    private <T> T attribute(String attributeName) {
        try {
            return (T) mBeanServer.getAttribute(objectName, attributeName);
        } catch (Exception e) {
            throw new GreenbirdTestException(e);
        }
    }
}
