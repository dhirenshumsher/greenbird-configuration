package com.greenbird.configuration.report;

import com.greenbird.configuration.ConfigurationException;
import com.greenbird.configuration.ContextLoadingTestBase;
import com.greenbird.test.logging.TestLogAppender;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;
import java.util.Map;

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
        System.out.println(normalizedMessage);
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

    private String getNormalizedMessageFromLogEvent() {
        List<LoggingEvent> loggingEvents = testLogAppender.getLoggingEvents();
        assertThat(loggingEvents.size(), is(1));
        LoggingEvent loggingEvent = loggingEvents.get(0);
        assertThat(loggingEvent.getLevel(), is(Level.INFO));
        return loggingEvent.getMessage().toString().replace(LS, " ").replaceAll(" +", " ");
    }
    
    @Test
    public void testBeanDefinitionClass_onFactory_noBeanName() throws ClassNotFoundException {
    	BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
    	
    	Mockito.when(beanDefinition.getFactoryBeanName()).thenReturn(null);
    	Mockito.when(beanDefinition.getFactoryMethodName()).thenReturn("myMethod");

    	BeanDefinition factoryBeanDefinition = Mockito.mock(BeanDefinition.class);
    	Mockito.when(factoryBeanDefinition.getBeanClassName()).thenReturn(Object.class.getName());

    	try {
    		ConfigurationReporter.lookupBeanClassNameFromFactory(beanDefinition, factoryBeanDefinition);
    		
    		Assert.fail();
    	} catch(ConfigurationException e) {
    		// pass
    	}
    }
    
    @Test
    public void testBeanDefinitionClass_onFactory_multipleFactoryMethodsSameReturnType() throws ClassNotFoundException {
    	BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
    	
    	Mockito.when(beanDefinition.getFactoryBeanName()).thenReturn(null);
    	Mockito.when(beanDefinition.getFactoryMethodName()).thenReturn("contentEquals");

    	BeanDefinition factoryBeanDefinition = Mockito.mock(BeanDefinition.class);
    	Mockito.when(factoryBeanDefinition.getBeanClassName()).thenReturn(String.class.getName());

   		ConfigurationReporter.lookupBeanClassNameFromFactory(beanDefinition, factoryBeanDefinition);
    }

    @Test
    public void testBeanDefinitionClass_onFactory_multipleFactoryMethodsDifferentReturnType() throws ClassNotFoundException {
    	BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
    	
    	Mockito.when(beanDefinition.getFactoryBeanName()).thenReturn(null);
    	Mockito.when(beanDefinition.getFactoryMethodName()).thenReturn("getEnhancedBaseClass");

    	BeanDefinition factoryBeanDefinition = Mockito.mock(BeanDefinition.class);
    	Mockito.when(factoryBeanDefinition.getBeanClassName()).thenReturn(ConfigurationReporter.class.getName());

    	try {
    		ConfigurationReporter.lookupBeanClassNameFromFactory(beanDefinition, factoryBeanDefinition);
    		
    		Assert.fail();
    	} catch(ConfigurationException e) {
    		// pass
    	}
   		
    }

    @Test
    public void testBeanDefinitionClass_onFactory_noProperFactoryBean() {
    	try {
    		ConfigurationReporter.createPackageMap(createMockBeanFactory(null, Object.class.getName(), null));
    		
    		Assert.fail();
    	} catch(ConfigurationException e) {
    		// pass
    	}
    	try {
    		ConfigurationReporter.createPackageMap(createMockBeanFactory(null, null, "toString"));
    		
    		Assert.fail();
    	} catch(ConfigurationException e) {
    		// pass
    	}
    }
    
    @Test
    public void testBeanDefinitionClass_onFactory_unknownClassesBean() {
    	try {
    		ConfigurationReporter.createPackageMap(createMockBeanFactory("no.known.class$$CGLIB", null, null));
    		
    		Assert.fail();
    	} catch(ConfigurationException e) {
    		// pass
    	}
    	try {
    		ConfigurationReporter.createPackageMap(createMockBeanFactory(null, "no.known.class", "toString"));
    		
    		Assert.fail();
    	} catch(ConfigurationException e) {
    		// pass
    	}
    }
    
    @Test
    public void testBeanDefinitionClass_onFactory_springClassesBean() {
    	try {
    		Map<String, ?> map = ConfigurationReporter.createPackageMap(createMockBeanFactory(org.springframework.beans.BeanInfoFactory.class.getName(), null, null));
    		
    		Assert.assertTrue(map.isEmpty());
    	} catch(ConfigurationException e) {
    		// pass
    	}
    	try {
    		Map<String, ?> map = ConfigurationReporter.createPackageMap(createMockBeanFactory(null, org.springframework.beans.BeanInfoFactory.class.getName(), "toString"));
    		
    		Assert.assertTrue(map.isEmpty());
    	} catch(ConfigurationException e) {
    		// pass
    	}
    }

    @Test
    public void testBeanDefinitions_emptyFactory() {
    	BeanFactory factory = Mockito.mock(BeanFactory.class);
    	
    	Map<String, BeanDefinition> beanDefinitonsForTypeIncludingAncestors = ConfigurationReporter.beanDefinitonsForTypeIncludingAncestors(factory, Object.class);
    	Assert.assertEquals(0, beanDefinitonsForTypeIncludingAncestors.size());
    }
    
    @Test
    public void testBeanDefinitions_factory() {
    	BeanFactory factory = createMockBeanFactory(null, org.springframework.beans.BeanInfoFactory.class.getName(), "toString");
    	
    	Map<String, BeanDefinition> beanDefinitonsForTypeIncludingAncestors = ConfigurationReporter.beanDefinitonsForTypeIncludingAncestors(factory, Object.class);
    	Assert.assertEquals(2, beanDefinitonsForTypeIncludingAncestors.size());
    }

    @Test
    public void testBeanDefinitions_simpleParentFactory() {
    	HierarchicalBeanFactory factory = Mockito.mock(HierarchicalBeanFactory.class);
    	BeanFactory parentFactory = Mockito.mock(BeanFactory.class);
    	
    	Mockito.when(factory.getParentBeanFactory()).thenReturn(parentFactory);
    	
    	Map<String, BeanDefinition> beanDefinitonsForTypeIncludingAncestors = ConfigurationReporter.beanDefinitonsForTypeIncludingAncestors(factory, Object.class);
    	Assert.assertEquals(0, beanDefinitonsForTypeIncludingAncestors.size());

    	Mockito.verify(factory, Mockito.atLeastOnce()).getParentBeanFactory();
    }
    
    private BeanFactory createMockBeanFactory(String beanClassName, String factoryBeanClassName, String factoryMethodName) {
    	BeanDefinition beanDefinition = Mockito.mock(BeanDefinition.class);
    	Mockito.when(beanDefinition.getBeanClassName()).thenReturn(beanClassName);
    	Mockito.when(beanDefinition.getFactoryBeanName()).thenReturn("myFactoryBean");
    	Mockito.when(beanDefinition.getFactoryMethodName()).thenReturn(factoryMethodName);
    	
    	BeanDefinition factoryBeanDefinition = Mockito.mock(BeanDefinition.class);
    	Mockito.when(factoryBeanDefinition.getBeanClassName()).thenReturn(factoryBeanClassName);
    	
    	ConfigurableListableBeanFactory factory = Mockito.mock(ConfigurableListableBeanFactory.class);
    	BeanFactory parentFactory = Mockito.mock(BeanFactory.class);
    	
    	Mockito.when(factory.getParentBeanFactory()).thenReturn(parentFactory);
    	Mockito.when(factory.getBeanNamesForType(Object.class)).thenReturn(new String[]{"myBean", "myFactoryBean"});
    	
    	Mockito.when(factory.getBeanDefinition("myFactoryBean")).thenReturn(factoryBeanDefinition);
    	Mockito.when(factory.getBeanDefinition("myBean")).thenReturn(beanDefinition);
    	Mockito.when(factory.containsBeanDefinition(Mockito.anyString())).thenReturn(Boolean.TRUE);
    	
    	return factory;
    }

}
