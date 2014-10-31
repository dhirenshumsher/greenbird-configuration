package com.greenbird.configuration.context;

import com.greenbird.configuration.ContextLoadingTestBase;
import com.greenbird.configuration.ContextTestBean1;
import com.greenbird.configuration.ContextTestBean2;
import com.greenbird.configuration.ContextTestBean3;
import com.greenbird.configuration.ContextTestBean4;
import com.greenbird.configuration.properties.ConfigurationDirectoryLoader;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SpringContextLoaderTest extends ContextLoadingTestBase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder temporaryFolder2 = new TemporaryFolder();

    @Autowired
    private ContextTestBean1 testBean1;
    @Autowired
    private ContextTestBean2 testBean2;

    @Test
    public void importResources_normal_beansInModuleConfigsAreLoadedAndHasAccessToConfigProperties() {
        assertThat(testBean1, notNullValue());
        assertThat(testBean2.getTestValue2(), is("value2"));
    }

    @Test
    public void importResourceFromFileSystem_normal_beansInModuleConfigsAreLoadedAndHasAccessToConfigProperties() throws IOException {
        File subFolder = temporaryFolder.newFolder("gb-conf");
        File subFolder2 = temporaryFolder2.newFolder("gb-conf");
        System.setProperty(ConfigurationDirectoryLoader.CONFIG_DIR_PROPERTY, temporaryFolder.getRoot().getPath());
        FileUtils.writeStringToFile(new File(subFolder, "greenbird-default.properties"), "some.greenbird.config.dir=" + subFolder.getPath());
        FileUtils.writeStringToFile(new File(subFolder, "my-context.xml"), getSpringConfig("contextTestBean3", "com.greenbird.configuration.ContextTestBean3"));
        FileUtils.writeStringToFile(new File(subFolder, "external-default.properties"), "external.config.greenbird.config.dir="+subFolder2.getPath());
        FileUtils.writeStringToFile(new File(subFolder2, "another-context.xml"), getSpringConfig("contextTestBean4", "com.greenbird.configuration.ContextTestBean4"));
        GenericXmlApplicationContext context = createContextForProfiles("default");
        ContextTestBean3 testBean3 = context.getBean("contextTestBean3", ContextTestBean3.class);
        assertThat(testBean3, notNullValue());
        assertThat(testBean3.getTestValue3(), is("value3"));
        ContextTestBean4 testBean4 = context.getBean("contextTestBean4", ContextTestBean4.class);
        assertThat(testBean4, notNullValue());
        assertThat(testBean4.getTestValue4(), is("value4"));
    }

    private String getSpringConfig(String beanName, String clazz) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<beans\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                "        xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
                "\n" +
                "    <bean name=\"" + beanName + "\" class=\"" + clazz + "\"/>" +
                "</beans>";
    }

}
