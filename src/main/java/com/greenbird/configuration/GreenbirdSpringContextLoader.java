package com.greenbird.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static ch.lambdaj.Lambda.join;
import static java.lang.String.format;
import static java.util.Arrays.asList;

@Configuration
@ImportResource(GreenbirdResourceFinder.CONTEXT_PATTERN)
public class GreenbirdSpringContextLoader implements ApplicationContextAware {
    private static final String LS = System.getProperty("line.separator");

    private Logger logger = LoggerFactory.getLogger(getClass());
    private GreenbirdResourceFinder resourceFinder = new GreenbirdResourceFinder();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<Resource> moduleResource = asList(resourceFinder.findContextDefinitions());
        String result = join(moduleResource, LS);
        logger.info(format("Loaded Greenbird modules:%s%s%s", LS, result, LS));

    }
}
