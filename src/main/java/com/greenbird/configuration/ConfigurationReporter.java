package com.greenbird.configuration;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ch.lambdaj.Lambda.join;
import static java.util.Arrays.asList;

@Service
public class ConfigurationReporter implements ApplicationContextAware {
    private static final String LS = System.getProperty("line.separator");
    private static final String RULER = "***********************************************************************";
    private static Joiner commaJoiner = Joiner.on(", ");

    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConfigurationPropertyPlaceholderConfigurer placeholderConfigurer;
    private ResourceFinder resourceFinder;
    private Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logConfigurationReport(applicationContext);
    }

    private void logConfigurationReport(ApplicationContext applicationContext) {
        StringBuilder reportBuilder = new StringBuilder();
        buildHeader(reportBuilder);
        buildProfileReport(reportBuilder);
        buildPropertyFilesReport(reportBuilder);
        buildPropertyReport(reportBuilder);
        buildSpringDefinitionsReport(reportBuilder);
        buildBeanReport(applicationContext, reportBuilder);
        buildFooter(reportBuilder);
        logger.info(reportBuilder.toString());
    }

    private void buildHeader(StringBuilder reportBuilder) {
        reportBuilder
                .append(LS).append(LS).append(RULER).append(LS)
                .append("GREENBIRD CONFIGURATION REPORT").append(LS)
                .append(RULER).append(LS);
    }

    private void buildProfileReport(StringBuilder reportBuilder) {
        String activeProfiles = buildProfileList(environment.getActiveProfiles());
        String defaultProfiles = buildProfileList(environment.getDefaultProfiles());
        reportBuilder
                .append(LS)
                .append("SPRING PROFILES")
                .append(LS)
                .append("---------------")
                .append(LS)
                .append("Active profiles:  ").append(activeProfiles).append(LS)
                .append("Default profiles: ").append(defaultProfiles)
                .append(LS);
    }

    private String buildProfileList(String[] profiles) {
        return profiles.length > 0 ?
                commaJoiner.join(profiles) : "<none>";
    }

    private void buildPropertyFilesReport(StringBuilder reportBuilder) {
        reportBuilder
                .append(LS)
                .append("AUTO-LOADED CONFIGURATION FILES")
                .append(LS)
                .append("-------------------------------")
                .append(LS);
        Joiner.on(LS).appendTo(reportBuilder, placeholderConfigurer.getLoadedPropertyFiles());
        reportBuilder.append(LS);
    }

    private void buildPropertyReport(StringBuilder reportBuilder) {
        reportBuilder
                .append(LS)
                .append("CONFIGURATION PROPERTIES")
                .append(LS)
                .append("------------------------------------")
                .append(LS)
                .append(placeholderConfigurer.createPropertyReport())
                .append(LS);
    }

    private void buildSpringDefinitionsReport(StringBuilder reportBuilder) {
        List<Resource> moduleResource = asList(resourceFinder.findContextDefinitions());
        String result = join(moduleResource, LS);
        reportBuilder
                .append("AUTO-LOADED SPRING DEFINITION FILES")
                .append(LS)
                .append("-----------------------------------")
                .append(LS)
                .append(result)
                .append(LS);
    }

    private void buildBeanReport(ApplicationContext applicationContext, StringBuilder reportBuilder) {
        reportBuilder
                .append(LS)
                .append("BEANS IN CONTEXT")
                .append(LS)
                .append("----------------");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Map<String, BeanPackage> packageMap = new TreeMap<String, BeanPackage>();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            String packageName = bean.getClass().getPackage().getName();
            if (packageName.startsWith("org.springframework")) {
                continue;
            }
            if (!packageMap.containsKey(packageName)) {
                packageMap.put(packageName, new BeanPackage(packageName));
            }
            packageMap.get(packageName).addBean(beanName, bean);
        }
        for (String packageName : packageMap.keySet()) {
            packageMap.get(packageName).report(reportBuilder);
        }
        reportBuilder.append(LS);
    }

    private void buildFooter(StringBuilder reportBuilder) {
        reportBuilder.append(RULER).append(LS).append(LS);
    }

    @Autowired
    public void setResourceFinder(ResourceFinder resourceFinder) {
        this.resourceFinder = resourceFinder;
    }

    @Autowired
    public void setPlaceholderConfigurer(ConfigurationPropertyPlaceholderConfigurer placeholderConfigurer) {
        this.placeholderConfigurer = placeholderConfigurer;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private static class BeanPackage {
        private String packageName;
        private ListMultimap<String, String> beanClassAndNameMap = ArrayListMultimap.create();

        private BeanPackage(String packageName) {
            this.packageName = packageName;
        }

        private void addBean(String beanName, Object bean) {
            String className = bean.getClass().getSimpleName();
            beanClassAndNameMap.put(className, beanName);
        }

        private void report(StringBuilder reportBuilder) {
            reportBuilder.append(LS).append(packageName).append(":").append(LS);
            List<String> sortedClassNames = Ordering.natural().sortedCopy(beanClassAndNameMap.keySet());
            for (String className : sortedClassNames) {
                List<String> sortedBeanNames = Ordering.natural().sortedCopy(beanClassAndNameMap.get(className));
                reportBuilder
                        .append("  ")
                        .append(className)
                        .append(" (")
                        .append(commaJoiner.join(sortedBeanNames))
                        .append(")")
                        .append(LS);
            }
        }
    }
}
