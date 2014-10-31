package com.greenbird.configuration.properties;

import com.google.common.collect.Iterators;
import com.greenbird.configuration.util.ResourceFinder;
import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;
import org.constretto.exception.ConstrettoExpressionException;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class ConfigurationPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {
    private static final String PRESET_PROFILE = "preset";
    public static final String GREENBIRD_CONFIG_UUID_KEY = "GREENBIRD_CONFIG_UUID";
    private static final Pattern GREENBIRD_CONFIG_UUID_PATTERN = Pattern.compile(GREENBIRD_CONFIG_UUID_KEY);

    private ResourceFinder resourceFinder = new ResourceFinder();
    private List<Resource> loadedPropertyFiles = new ArrayList<Resource>();
    private ConstrettoConfiguration classpathConfiguration;
    private ConstrettoConfiguration fileSystemConfiguration;

    @Override
    public void setEnvironment(Environment environment) {
        loadClasspathProperties(environment);
        loadFileSystemProperties(environment);
        ((ConfigurableEnvironment) environment).getPropertySources().addLast(new PropertySource<Object>("gbConfSource") {
            @Override
            public Object getProperty(String name) {
                return ConfigurationPropertyPlaceholderConfigurer.this.getProperty(name);
            }
        });
        super.setEnvironment(environment);
    }

    private void loadClasspathProperties(Environment environment) {
        ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder =
                new ConstrettoBuilder(new GreenbirdConfigurationContextResolver(environment)).createPropertiesStore();

        // load preset properties first of all
        addClasspathPropertyResources(propertiesBuilder, PRESET_PROFILE);
        // load default properties before the active ones so they can be overridden
        addClasspathPropertyResources(propertiesBuilder, environment.getDefaultProfiles());
        addClasspathPropertyResources(propertiesBuilder, environment.getActiveProfiles());

        classpathConfiguration = propertiesBuilder.done().getConfiguration();
    }

    private void loadFileSystemProperties(Environment environment) {
        ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder =
                new ConstrettoBuilder(new GreenbirdConfigurationContextResolver(environment)).createPropertiesStore();

        Set<File> configurationDirectories = new ConfigurationDirectoryLoader().getConfigurationDirectories(classpathConfiguration);

        // load preset properties first of all
        addFileSystemPropertyResources(propertiesBuilder, configurationDirectories, PRESET_PROFILE);
        // load default properties before the active ones so they can be overridden
        addFileSystemPropertyResources(propertiesBuilder, configurationDirectories, environment.getDefaultProfiles());
        addFileSystemPropertyResources(propertiesBuilder, configurationDirectories, environment.getActiveProfiles());

        fileSystemConfiguration = propertiesBuilder.done().getConfiguration();
    }

    private void addClasspathPropertyResources(ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder, String... profiles) {
        for (String profile : profiles) {
            Resource[] resources = resourceFinder.findClasspathConfigurationFilesForProfile(profile);
            addResources(propertiesBuilder, resources);
        }
    }

    private void addFileSystemPropertyResources(ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder, Set<File> configurationDirectories, String... profiles) {
        for (File configurationDirectory : configurationDirectories) {
            for (String profile : profiles) {
                Resource[] resources = resourceFinder.findFileSystemConfigurationFilesForProfile(configurationDirectory, profile);
                addResources(propertiesBuilder, resources);
            }
        }
    }

    private void addResources(ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder, Resource[] resources) {
        for (Resource propertyResource : resources) {
            loadedPropertyFiles.add(propertyResource);
            propertiesBuilder.addResource(propertyResource);
        }
    }

    private String getProperty(String name) {
        // prefer properties from file system over properties from classpath
        String value = getValue(name, fileSystemConfiguration);
        if (value == null) {
            value = getValue(name, classpathConfiguration);
        }
        if (value != null && value.contains(GREENBIRD_CONFIG_UUID_KEY)) {
            value = buildRandomPropertyValue(value);
        }
        return value;
    }

    private String getValue(String name, ConstrettoConfiguration configuration) {
        String value = null;
        try {
            value = configuration.evaluateTo(String.class, name);
        } catch (ConstrettoExpressionException e) {
            // NOP - value not found is OK
        }
        return value;
    }

    private String buildRandomPropertyValue(String value) {
        Matcher uuidMatcher = GREENBIRD_CONFIG_UUID_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();
        while (uuidMatcher.find()) {
            uuidMatcher.appendReplacement(result, UUID.randomUUID().toString());
        }
        uuidMatcher.appendTail(result);
        return result.toString();
    }

    public String createPropertyReport() {
        String maskPattern = getProperty(PropertyReportCreator.MASK_PATTERN_PROPERTY);
        Set<Property> uniqueProperties = getPropertySet();

        return new PropertyReportCreator(maskPattern).createPropertyReport(uniqueProperties);
    }

    private Set<Property> getPropertySet() {
        Set<String> fileSystemPropertyNames = new HashSet<String>();
        Set<Property> uniqueProperties = new HashSet<Property>();
        Iterators.addAll(uniqueProperties, fileSystemConfiguration.iterator());

        for (Property property : fileSystemConfiguration) {
            fileSystemPropertyNames.add(property.getKey());
        }

        for (Property property : classpathConfiguration) {
            if (!fileSystemPropertyNames.contains(property.getKey())) {
                uniqueProperties.add(property);
            }
        }
        return uniqueProperties;
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        for (Property property : getPropertySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }
        return properties;
    }

    public List<Resource> getLoadedPropertyFiles() {
        return loadedPropertyFiles;
    }
    
    public ConstrettoConfiguration getClasspathConfiguration() {
        return classpathConfiguration;
    }
    
    public ConstrettoConfiguration getFileSystemConfiguration() {
        return fileSystemConfiguration;
    }
}
