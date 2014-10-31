package com.greenbird.configuration.properties;

import com.greenbird.GreenbirdException;
import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.lang.String.format;

public class ConfigurationDirectoryLoader {
    public static final String CONFIG_DIR_PROPERTY = "greenbird.config.dir";
    public static final String NIX_FRIENDLY_CONFIG_DIR_PROPERTY = CONFIG_DIR_PROPERTY.replace(".", "_");
    private Logger logger = LoggerFactory.getLogger(getClass());

    public Set<File> getConfigurationDirectories(ConstrettoConfiguration classpathConfiguration) {
        Set<String> uniquePaths = new LinkedHashSet<String>();
        Set<File> uniqueDirectories = new LinkedHashSet<File>();

        getDirectoriesFromConfiguration(classpathConfiguration, uniquePaths, uniqueDirectories);
        getDirectoriesFromSystemProperties(uniquePaths, uniqueDirectories);
        getDirectoriesFromEnvironment(uniquePaths, uniqueDirectories);

        return uniqueDirectories;
    }

    private void getDirectoriesFromConfiguration(ConstrettoConfiguration configuration, Set<String> uniquePaths, Set<File> uniqueDirectories) {
        for (Property property : configuration) {
            processProperty(property.getKey(), property.getValue(), uniquePaths, uniqueDirectories);
        }
    }

    private void getDirectoriesFromSystemProperties(Set<String> uniquePaths, Set<File> uniqueDirectories) {
        Properties properties = System.getProperties();
        for (String name : properties.stringPropertyNames()) {
            processProperty(name, properties.getProperty(name), uniquePaths, uniqueDirectories);
        }
    }

    private void getDirectoriesFromEnvironment(Set<String> uniquePaths, Set<File> uniqueDirectories) {
        for (Map.Entry<String, String> envEntry : System.getenv().entrySet()) {
            processProperty(envEntry.getKey(), envEntry.getValue(), uniquePaths, uniqueDirectories);
        }
    }

    private void processProperty(String name, String value, Set<String> uniquePaths, Set<File> uniqueDirectories) {
        if (isConfigurationDirectoryProperty(name)) {
            File configDir = new File(value);
            boolean isUsable = isUsableDirectory(configDir, name, value);
            try {
                if (isUsable && uniquePaths.add(configDir.getCanonicalPath())) {
                    uniqueDirectories.add(configDir);
                }
            } catch (IOException e) {
                throw new GreenbirdException(e);
            }

        }
    }

    private boolean isConfigurationDirectoryProperty(String name) {
        return name.endsWith(CONFIG_DIR_PROPERTY) || name.equals(NIX_FRIENDLY_CONFIG_DIR_PROPERTY);
    }

    private boolean isUsableDirectory(File configDir, String propertyName, String propertyValue) {
        boolean usable = false;
        if (!configDir.exists()) {
            logger.warn(format("Configuration directory %s defined in %s does not exists.",
                    propertyValue, propertyName));
        } else if (!configDir.isDirectory()) {
            logger.warn(format("Configuration directory %s defined in %s is not a directory.",
                    propertyValue, propertyName));
        } else if (!configDir.canRead()) {
            logger.warn(format("Configuration directory %s defined in %s is not readable.",
                    propertyValue, propertyName));
        } else {
            usable = true;
        }
        return usable;
    }
}
