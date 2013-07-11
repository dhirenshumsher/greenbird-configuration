package com.greenbird.configuration;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.greenbird.configuration.PropertyReporter.buildPropertyReport;


@Component
public class GreenbirdConstrettoPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    public static final String GREENBIRD_CONFIG_UUID_KEY = "GREENBIRD_CONFIG_UUID";
    private static final Pattern GREENBIRD_CONFIG_UUID_PATTERN = Pattern.compile(GREENBIRD_CONFIG_UUID_KEY);

    private Logger logger = LoggerFactory.getLogger(getClass());
    private GreenbirdResourceFinder resourceFinder = new GreenbirdResourceFinder();
    private ConstrettoConfiguration configuration;

    public GreenbirdConstrettoPropertyPlaceholderConfigurer() {
        loadProperties();
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        for (Property property : configuration) {
            properties.setProperty(property.getKey(), property.getValue());
        }
        return properties;
    }

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
        String value = configuration.evaluateTo(String.class, placeholder);
        if (value.contains(GREENBIRD_CONFIG_UUID_KEY)) {
            value = buildRandomPropertyValue(value);
        }
        return value;
    }

    private void loadProperties() {
        ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder =
                new ConstrettoBuilder(new GreenbirdConfigurationContextResolver()).createPropertiesStore();
        // load default properties first so they can be overridden
        addPropertyResources(propertiesBuilder, resourceFinder.findGreenbirdModuleDefaultConfigurationFiles());
        addPropertyResources(propertiesBuilder, resourceFinder.findGreenbirdModuleConfigurationFiles());
        configuration = propertiesBuilder.done().getConfiguration();
        logger.info(buildPropertyReport(configuration));
    }

    private void addPropertyResources(ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder, Resource[] resources) {
        for (Resource propertyResource : resources) {
            propertiesBuilder.addResource(propertyResource);
        }
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

}
