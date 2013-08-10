package com.greenbird.configuration;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

@Service
@ManagedResource("greenbird.configuration:name=greenbirdConfiguration,type=GreenbirdConfiguration")
public class ConfigurationMBean implements ApplicationContextAware {
    private ConfigurationPropertyPlaceholderConfigurer propertyConfigurer;
    private ResourceFinder resourceFinder;
    private Environment environment;
    private ApplicationContext applicationContext;

    @ManagedAttribute
    public String getActiveSpringProfiles() {
        return getProfilesString(environment.getActiveProfiles());
    }

    @ManagedAttribute
    public String getDefaultSpringProfiles() {
        return getProfilesString(environment.getDefaultProfiles());
    }

    @ManagedAttribute
    public List<String> getLoadedConfigurationFiles() {
        return Lists.transform(propertyConfigurer.getLoadedPropertyFiles(), new Function<Resource, String>() {
            @Override
            public String apply(Resource resource) {
                return resource.toString();
            }
        });
    }

    @ManagedAttribute
    public String getPropertiesReport() {
        return propertyConfigurer.createPropertyReport();
    }

    @ManagedAttribute
    public List<String> getLoadedSpringDefinitionFiles() {
        return Lists.transform(asList(resourceFinder.findContextDefinitions()), new Function<Resource, String>() {
            @Override
            public String apply(Resource resource) {
                return resource.toString();
            }
        });
    }

    @ManagedAttribute
    public List<String> getBeansInContext() {
        List<String> beans = new ArrayList<String>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            String beanClass = bean.getClass().getName();
            if (!beanClass.startsWith("org.springframework")) {
                beans.add(format("%s (%s)", beanName, beanClass));
            }
        }
        return Ordering.natural().sortedCopy(beans);
    }

    private String getProfilesString(String[] profiles) {
        String profilesString;
        if (profiles.length > 0) {
            profilesString = Joiner.on(", ").join(profiles);
        } else {
            profilesString = "<none>";
        }
        return profilesString;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setPropertyConfigurer(ConfigurationPropertyPlaceholderConfigurer propertyConfigurer) {
        this.propertyConfigurer = propertyConfigurer;
    }

    @Autowired
    public void setResourceFinder(ResourceFinder resourceFinder) {
        this.resourceFinder = resourceFinder;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
