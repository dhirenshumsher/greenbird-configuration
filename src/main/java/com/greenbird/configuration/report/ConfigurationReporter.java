package com.greenbird.configuration.report;

import static ch.lambdaj.Lambda.join;
import static com.greenbird.configuration.util.SpringContextUtils.getBeanIfAvailable;
import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.greenbird.configuration.ConfigurationException;
import com.greenbird.configuration.context.SpringContextLoader;
import com.greenbird.configuration.properties.ConfigurationPropertyPlaceholderConfigurer;
import com.greenbird.configuration.util.ResourceFinder;

@Service
public class ConfigurationReporter implements ApplicationContextAware {
    private static final String LS = System.getProperty("line.separator");
    private static final String RULER = "***********************************************************************";
    private static Joiner commaJoiner = Joiner.on(", ");

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

	protected static Map<String, BeanPackage> createPackageMap(BeanFactory beanFactory) { 
		Map<String, BeanPackage> packageMap = new TreeMap<String, BeanPackage>();
        
        Map<String, BeanDefinition> beanNamesForTypeIncludingAncestors = beanDefinitonsForTypeIncludingAncestors(beanFactory, Object.class);
        
        for (Map.Entry<String, BeanDefinition> entry : beanNamesForTypeIncludingAncestors.entrySet()) {
        	BeanDefinition beanDefinition = entry.getValue();

        	String beanName = entry.getKey();        

        	String className = beanDefinition.getBeanClassName();
        	try {
	        	if(className == null) {
	        		String factoryBeanName = beanDefinition.getFactoryBeanName();
	        		String factoryMethodName = beanDefinition.getFactoryMethodName();
	
	        		if(factoryBeanName != null && factoryMethodName != null) {
	        			
	        			BeanDefinition factoryBeanDefinition = beanNamesForTypeIncludingAncestors.get(factoryBeanName);
	                	if(filterClassName(factoryBeanDefinition.getBeanClassName())) {
	                        continue;
	                    }
	            		className = lookupBeanClassNameFromFactory(beanDefinition, factoryBeanDefinition);
	        		} else {
	    				throw new ConfigurationException("Unable to report on bean '" + beanName + "': " + beanDefinition);
	        		}
	        	}

        		className = getEnhancedBaseClass(className);
			} catch(ClassNotFoundException e) {
				throw new ConfigurationException("Unable to report on bean '" + beanName + "': " + beanDefinition, e);
			}
        	if(filterClassName(className)) {
                continue;
            }

        	String simpleName = className.substring(className.lastIndexOf('.') + 1);
        	String packageName = className.substring(0, className.length() - simpleName.length() - 1);
        	        	
            if (!packageMap.containsKey(packageName)) {
                packageMap.put(packageName, new BeanPackage(packageName));
            }
            packageMap.get(packageName).addBeanClass(beanName, simpleName);
        }
		return packageMap;
	}

	protected static String lookupBeanClassNameFromFactory(BeanDefinition beanDefinition, BeanDefinition factoryBeanDefinition) throws ClassNotFoundException {

		// get the class
		Class<?> factoryDefinitionBeanClass = Class.forName(getEnhancedBaseClass(factoryBeanDefinition.getBeanClassName()));
		
		// get the method return type
		Method method = null;
		try {
			// attempt getting factroy method with no arguments
			method = factoryDefinitionBeanClass.getMethod(beanDefinition.getFactoryMethodName());
		} catch(NoSuchMethodException e) {
			// determine which method matches, and ensure there is only one class with that name
			Method[] methods = factoryDefinitionBeanClass.getDeclaredMethods();
			
			for(Method candiateMethod : methods) {
				if(candiateMethod.getName().equals(beanDefinition.getFactoryMethodName())) {
					if(method == null) {
						method = candiateMethod;
					} else if(!method.getReturnType().getName().equals(candiateMethod.getReturnType().getName())) {
						throw new ConfigurationException("Unable to determine bean class for bean: " + beanDefinition, e);
					}
				}
			}
		}
		if(method == null) {
			throw new ConfigurationException("Unable to report locate class " + factoryBeanDefinition.getBeanClassName() + " method " + beanDefinition.getFactoryMethodName());
		}
		return getEnhancedBaseClass(method.getReturnType().getName());
			
	}

	private static String getEnhancedBaseClass(String className) throws ClassNotFoundException {
		if(!className.contains("CGLIB")) {
			return className;
		}
		
   		return getEnhancedBaseClass(Class.forName(className)).getName();
	}

	private static Class<?> getEnhancedBaseClass(Class<?> beanClass) {
		if(Enhancer.isEnhanced(beanClass)) {
			beanClass = beanClass.getSuperclass();
		}

		return beanClass;
	}

	
	private static boolean filterClassName(String className) {
		return className.startsWith("org.springframework");
	}

	
    /**
     * 
     * Based of the {@linkplain BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)} method
     * 
     */

	protected static Map<String, BeanDefinition> beanDefinitonsForTypeIncludingAncestors(BeanFactory factory, Class<Object> type) {

    	Map<String, BeanDefinition> map = new HashMap<String, BeanDefinition>();
		while (true) {
			
			if(factory instanceof ConfigurableListableBeanFactory) {
				ConfigurableListableBeanFactory lbf = (ConfigurableListableBeanFactory)factory;
				
				String[] beanNames = lbf.getBeanNamesForType(type);
				
				for(String beanName : beanNames) {
					if(!map.containsKey(beanName)) {
						if(lbf.containsBeanDefinition(beanName)) {
							map.put(beanName, lbf.getBeanDefinition(beanName));
						}
					}
				}
			}

			if(factory instanceof HierarchicalBeanFactory) {
				factory = ((HierarchicalBeanFactory)factory).getParentBeanFactory();
				
				if(factory instanceof ListableBeanFactory) {
					continue;
				}
			}
			break;
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
