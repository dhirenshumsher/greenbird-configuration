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
    @Autowired
    private GreenbirdConstrettoPropertyPlaceholderConfigurer propertyConfigurer;
    @Autowired
    private GreenbirdResourceFinder resourceFinder;

    @ManagedAttribute
    public Properties getGreenbirdProperties() {
        return propertyConfigurer.getProperties();
    }

    @ManagedAttribute
    public List<Resource> getLoadedGreenbirdModules() {
        return Arrays.asList(resourceFinder.findGreenbirdModules());
    }

    public void setPropertyConfigurer(GreenbirdConstrettoPropertyPlaceholderConfigurer propertyConfigurer) {
        this.propertyConfigurer = propertyConfigurer;
    }

    public void setResourceFinder(GreenbirdResourceFinder resourceFinder) {
        this.resourceFinder = resourceFinder;
    }
}
