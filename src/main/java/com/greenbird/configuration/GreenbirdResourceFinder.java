package com.greenbird.configuration;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GreenbirdResourceFinder {
    public static final String GREENBIRD_CONFIG_ROOT_PATH = "classpath*:/gb-conf/**/";
    public static final String GREENBIRD_MODULE_PATTERN = GREENBIRD_CONFIG_ROOT_PATH + "*-context.xml";
    private static final String GREENBIRD_CONFIGURATION_FILE_PATTERN = GREENBIRD_CONFIG_ROOT_PATH + "greenbird.properties";
    private static final String GREENBIRD_DEFAULT_CONFIGURATION_FILE_PATTERN = GREENBIRD_CONFIG_ROOT_PATH + "greenbird-default.properties";

    private final ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(
            new DefaultResourceLoader(Thread.currentThread().getContextClassLoader()));

    public Resource[] findGreenbirdModules() {
        return findResources(GREENBIRD_MODULE_PATTERN);
    }

    public Resource[] findGreenbirdModuleConfigurationFiles() {
        return findResources(GREENBIRD_CONFIGURATION_FILE_PATTERN);
    }

    public Resource[] findGreenbirdModuleDefaultConfigurationFiles() {
        return findResources(GREENBIRD_DEFAULT_CONFIGURATION_FILE_PATTERN);
    }

    private Resource[] findResources(String locationPattern) {
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new GreenbirdConfigurationException("Failed to search for Greenbird resources.", e);
        }
        return resources;
    }
}
