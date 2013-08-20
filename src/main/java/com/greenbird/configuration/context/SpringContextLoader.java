package com.greenbird.configuration.context;

import com.greenbird.configuration.util.ResourceFinder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(ResourceFinder.CONTEXT_PATTERN)
public class SpringContextLoader {
}
