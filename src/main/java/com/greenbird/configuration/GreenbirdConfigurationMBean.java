package com.greenbird.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
@ManagedResource("greenbird.configuration:name=greenbirdConfiguration,type=GreenbirdConfiguration")
public class GreenbirdConfigurationMBean {
    private GreenbirdPropertyPlaceholderConfigurer propertyConfigurer;
    private GreenbirdResourceFinder resourceFinder;

    @ManagedAttribute
    public Properties getProperties() {
        return propertyConfigurer.getProperties();
    }

    @ManagedAttribute
    public List<Resource> getLoadedContexts() {
        return Arrays.asList(resourceFinder.findContextDefinitions());
    }

    @Autowired
    public void setPropertyConfigurer(GreenbirdPropertyPlaceholderConfigurer propertyConfigurer) {
        this.propertyConfigurer = propertyConfigurer;
    }

    @Autowired
    public void setResourceFinder(GreenbirdResourceFinder resourceFinder) {
        this.resourceFinder = resourceFinder;
    }
}
