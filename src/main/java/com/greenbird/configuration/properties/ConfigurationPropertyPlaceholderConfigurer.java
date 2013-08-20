package com.greenbird.configuration.properties;

import com.greenbird.configuration.util.ResourceFinder;
import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.exception.ConstrettoExpressionException;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    private ConstrettoConfiguration configuration;

    @Override
    public void setEnvironment(Environment environment) {
        loadProperties(environment);
        ((ConfigurableEnvironment) environment).getPropertySources().addLast(new PropertySource<Object>("gbConfSource") {
            @Override
            public Object getProperty(String name) {
                String value = null;
                try {
                    value = configuration.evaluateTo(String.class, name);
                } catch (ConstrettoExpressionException e) {
                    // NOP - value not found is OK
                }
                if (value != null && value.contains(GREENBIRD_CONFIG_UUID_KEY)) {
                    value = buildRandomPropertyValue(value);
                }
                return value;
            }
        });
        super.setEnvironment(environment);
    }

    private void loadProperties(Environment environment) {
        ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder =
                new ConstrettoBuilder(new GreenbirdConfigurationContextResolver(environment)).createPropertiesStore();

        // load preset properties first of all
        addPropertyResources(propertiesBuilder, PRESET_PROFILE);
        // load default properties before the active ones so they can be overridden
        addPropertyResources(propertiesBuilder, environment.getDefaultProfiles());
        addPropertyResources(propertiesBuilder, environment.getActiveProfiles());

        configuration = propertiesBuilder.done().getConfiguration();
    }

    private void addPropertyResources(ConstrettoBuilder.PropertiesStoreBuilder propertiesBuilder, String... profiles) {
        for (String profile : profiles) {
            Resource[] resources = resourceFinder.findConfigurationFilesForProfile(profile);
            for (Resource propertyResource : resources) {
                loadedPropertyFiles.add(propertyResource);
                propertiesBuilder.addResource(propertyResource);
            }
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

    public String createPropertyReport() {
        String maskPattern = configuration.evaluateToString(PropertyReportCreator.MASK_PATTERN_PROPERTY);
        return new PropertyReportCreator(maskPattern).createPropertyReport(configuration);
    }

    public List<Resource> getLoadedPropertyFiles() {
        return loadedPropertyFiles;
    }
}
