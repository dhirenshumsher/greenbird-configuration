package com.greenbird.configuration;

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

import static com.greenbird.configuration.PropertyReporter.buildPropertyReport;


@Component
public class ConfigurationPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {
    public static final String GREENBIRD_CONFIG_UUID_KEY = "GREENBIRD_CONFIG_UUID";
    private static final Pattern GREENBIRD_CONFIG_UUID_PATTERN = Pattern.compile(GREENBIRD_CONFIG_UUID_KEY);

    private ResourceFinder resourceFinder = new ResourceFinder();
    private ConstrettoConfiguration configuration;
    private List<Resource> loadedPropertyFiles = new ArrayList<Resource>();

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

        // load default properties first so they can be overridden
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
        return buildPropertyReport(configuration);
    }

    public List<Resource> getLoadedPropertyFiles() {
        return loadedPropertyFiles;
    }
}
