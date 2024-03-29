package com.greenbird.configuration.util;

import com.greenbird.configuration.ConfigurationException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;

public class ResourceFinder {
    public static final String CONFIG_ROOT_PATH = "classpath*:/gb-conf/**/";
    public static final String CONTEXT_PATTERN = CONFIG_ROOT_PATH + "*-context.xml";
    private static final String CONFIGURATION_FILE_TEMPLATE = "*-%s.properties";
    private static final String CONFIGURATION_FILE_PATTERN = CONFIG_ROOT_PATH + CONFIGURATION_FILE_TEMPLATE;

    private final ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(
            new DefaultResourceLoader(Thread.currentThread().getContextClassLoader()));

    public Resource[] findContextDefinitions() {
        return findResources(CONTEXT_PATTERN);
    }

    public Resource[] findClasspathConfigurationFilesForProfile(String profileName) {
        return findResources(format(CONFIGURATION_FILE_PATTERN, profileName));
    }

    public Resource[] findFileSystemConfigurationFilesForProfile(File configDir, String profileName) {
        StringBuilder filePatternBuilder = new StringBuilder(configDir.toURI().toString());
        if (!filePatternBuilder.toString().endsWith("/")) {
            filePatternBuilder.append("/");
        }
        filePatternBuilder.append("**/").append(format(CONFIGURATION_FILE_TEMPLATE, profileName));
        return findResources(filePatternBuilder.toString());
    }

    private Resource[] findResources(String locationPattern) {
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(locationPattern);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to search for Greenbird resources.", e);
        }
        return resources;
    }

}
