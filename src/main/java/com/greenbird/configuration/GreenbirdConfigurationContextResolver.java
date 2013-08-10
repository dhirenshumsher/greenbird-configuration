package com.greenbird.configuration;

import org.constretto.resolver.ConfigurationContextResolver;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

public class GreenbirdConfigurationContextResolver implements ConfigurationContextResolver {
    private Environment environment;

    public GreenbirdConfigurationContextResolver(Environment environment) {
        this.environment = environment;
    }

    public List<String> getTags() {
        return Arrays.asList(environment.getActiveProfiles());
    }
}
