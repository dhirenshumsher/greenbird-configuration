package com.greenbird.configuration.jmx;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.greenbird.configuration.context.SpringContextLoader;
import com.greenbird.configuration.properties.ConfigurationPropertyPlaceholderConfigurer;
import com.greenbird.configuration.util.ResourceFinder;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.greenbird.configuration.util.SpringContextUtils.getBeanIfAvailable;
import static java.lang.String.format;
import static java.util.Arrays.asList;

@Service
@ManagedResource("greenbird.configuration:name=greenbirdConfiguration,type=GreenbirdConfiguration")
public class ConfigurationMBean implements ApplicationContextAware {
    private ResourceFinder resourceFinder = new ResourceFinder();
    private Environment environment;
    private ApplicationContext applicationContext;
    private ConfigurationPropertyPlaceholderConfigurer placeholderConfigurer = null;

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
        List<String> result;
        if (placeholderConfigurer != null) {
            result = Lists.transform(placeholderConfigurer.getLoadedPropertyFiles(), new Function<Resource, String>() {
                @Override
                public String apply(Resource resource) {
                    return resource.toString();
                }
            });
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    @ManagedAttribute
    public String getPropertiesReport() {
        String result;
        if (placeholderConfigurer != null) {
            result = placeholderConfigurer.createPropertyReport();
        } else {
            result = "";
        }
        return result;
    }

    @ManagedAttribute
    public List<String> getLoadedSpringDefinitionFiles() {
        List<String> result;
        if (getBeanIfAvailable(applicationContext, SpringContextLoader.class) != null) {
            result = Lists.transform(asList(resourceFinder.findContextDefinitions()), new Function<Resource, String>() {
                @Override
                public String apply(Resource resource) {
                    return resource.toString();
                }
            });
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    @ManagedAttribute
    public List<String> getBeansInContext() {
        List<String> beans = new ArrayList<String>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (BeanIsAbstractException e) {
                // skip abstract bean
                continue;
            }
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
        placeholderConfigurer = getBeanIfAvailable(applicationContext, ConfigurationPropertyPlaceholderConfigurer.class);
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    // for test
    public void setResourceFinder(ResourceFinder resourceFinder) {
        this.resourceFinder = resourceFinder;
    }
}
