package com.greenbird.configuration.report;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.greenbird.configuration.context.SpringContextLoader;
import com.greenbird.configuration.properties.ConfigurationPropertyPlaceholderConfigurer;
import com.greenbird.configuration.util.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ch.lambdaj.Lambda.join;
import static com.greenbird.configuration.util.SpringContextUtils.getBeanIfAvailable;
import static java.util.Arrays.asList;

@Service
public class ConfigurationReporter implements ApplicationContextAware {
    public static final String UNKNOWN_PACKAGE = "<unknown package>";
    public static final String UNKNOWN_CLASS = "<unknown class>";
    private static final String LS = System.getProperty("line.separator");
    private static final String RULER = "***********************************************************************";
    private static final Joiner commaJoiner = Joiner.on(", ");

    private Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext = null;
    private ResourceFinder resourceFinder = new ResourceFinder();
    private Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
        ConfigurationPropertyPlaceholderConfigurer placeholderConfigurer =
                getBeanIfAvailable(applicationContext, ConfigurationPropertyPlaceholderConfigurer.class);
        reportBuilder
                .append(LS)
                .append("AUTO-LOADED CONFIGURATION FILES")
                .append(LS)
                .append("-------------------------------")
                .append(LS);

        if (placeholderConfigurer != null) {
            Joiner.on(LS).appendTo(reportBuilder, placeholderConfigurer.getLoadedPropertyFiles());
        } else {
            reportBuilder.append("No files to report as configuration property sub-system has not been loaded.");
        }

        reportBuilder.append(LS);
    }

    private void buildPropertyReport(StringBuilder reportBuilder) {
        ConfigurationPropertyPlaceholderConfigurer placeholderConfigurer =
                getBeanIfAvailable(applicationContext, ConfigurationPropertyPlaceholderConfigurer.class);
        String report;
        if (placeholderConfigurer != null) {
            report = placeholderConfigurer.createPropertyReport();
        } else {
            report = "No properties to report as configuration property sub-system has not been loaded." + LS;
        }

        reportBuilder
                .append(LS)
                .append("CONFIGURATION PROPERTIES")
                .append(LS)
                .append("------------------------------------")
                .append(LS)
                .append(report)
                .append(LS);
    }

    private void buildSpringDefinitionsReport(StringBuilder reportBuilder) {
        String result;
        if (getBeanIfAvailable(applicationContext, SpringContextLoader.class) != null) {
            List<Resource> moduleResource = asList(resourceFinder.findContextDefinitions());
            result = join(moduleResource, LS);
        } else {
            result = "No files to report as Spring definition sub-system has not been loaded.";
        }


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

        Map<String, BeanPackage> packageMap = createPackageMap(((AbstractApplicationContext) applicationContext).getBeanFactory());
        for (String packageName : packageMap.keySet()) {
            packageMap.get(packageName).report(reportBuilder);
        }
        reportBuilder.append(LS);
    }

    private static Map<String, BeanPackage> createPackageMap(ConfigurableListableBeanFactory beanFactory) {
        Map<String, BeanPackage> packageMap = new TreeMap<String, BeanPackage>();

        Map<String, BeanDefinition> beanNamesForTypeIncludingAncestors = beanDefinitionsForTypeIncludingAncestors(beanFactory);

        for (Map.Entry<String, BeanDefinition> entry : beanNamesForTypeIncludingAncestors.entrySet()) {
            BeanDefinition beanDefinition = entry.getValue();
            String beanName = entry.getKey();
            String simpleName;
            String packageName;
            try {
                String className = beanDefinition.getBeanClassName();
                if (className == null) {
                    String factoryBeanName = beanDefinition.getFactoryBeanName();
                    BeanDefinition factoryBeanDefinition = beanNamesForTypeIncludingAncestors.get(factoryBeanName);
                    className = lookupBeanClassNameFromFactory(beanDefinition, factoryBeanDefinition);
                }
                if (filterClassName(className)) {
                    continue;
                }
                simpleName = className.substring(className.lastIndexOf('.') + 1);
                packageName = className.substring(0, className.length() - simpleName.length() - 1);
            } catch (Exception e) {
                simpleName = UNKNOWN_CLASS;
                packageName = UNKNOWN_PACKAGE;
            }

            if (!packageMap.containsKey(packageName)) {
                packageMap.put(packageName, new BeanPackage(packageName));
            }
            packageMap.get(packageName).addBeanClass(beanName, simpleName);
        }
        return packageMap;
    }

    private static String lookupBeanClassNameFromFactory(BeanDefinition beanDefinition, BeanDefinition factoryBeanDefinition) throws ClassNotFoundException, NoSuchMethodException {
        Class<?> factoryDefinitionBeanClass = Class.forName(factoryBeanDefinition.getBeanClassName());
        return factoryDefinitionBeanClass.getMethod(beanDefinition.getFactoryMethodName()).getReturnType().getName();
    }

    private static boolean filterClassName(String className) {
        return className.startsWith("org.springframework");
    }

    private static Map<String, BeanDefinition> beanDefinitionsForTypeIncludingAncestors(ConfigurableListableBeanFactory lbf) {
        Map<String, BeanDefinition> map = new HashMap<String, BeanDefinition>();
        String[] beanNames = lbf.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (!map.containsKey(beanName)) {
                if (lbf.containsBeanDefinition(beanName)) {
                    map.put(beanName, lbf.getBeanDefinition(beanName));
                }
            }
        }
        return map;
    }

    private void buildFooter(StringBuilder reportBuilder) {
        reportBuilder.append(RULER).append(LS).append(LS);
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

        private void addBeanClass(String beanName, String className) {
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
