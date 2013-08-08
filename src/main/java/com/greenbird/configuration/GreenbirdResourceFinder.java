package com.greenbird.configuration;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.lang.String.format;

@Service
public class GreenbirdResourceFinder {
    public static final String CONFIG_ROOT_PATH = "classpath*:/gb-conf/**/";
    public static final String CONTEXT_PATTERN = CONFIG_ROOT_PATH + "*-context.xml";
    private static final String CONFIGURATION_FILE_PATTERN = CONFIG_ROOT_PATH + "*-%s.properties";

    private final ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(
            new DefaultResourceLoader(Thread.currentThread().getContextClassLoader()));

    public Resource[] findContextDefinitions() {
        return findResources(CONTEXT_PATTERN);
    }

    public Resource[] findConfigurationFilesForProfile(String profileName) {
        return findResources(format(CONFIGURATION_FILE_PATTERN, profileName));
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
