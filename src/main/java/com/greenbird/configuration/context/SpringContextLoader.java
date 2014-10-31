package com.greenbird.configuration.context;

import com.greenbird.configuration.properties.ConfigurationDirectoryLoader;
import com.greenbird.configuration.properties.ConfigurationPropertyPlaceholderConfigurer;
import com.greenbird.configuration.util.ResourceFinder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.greenbird.configuration.util.SpringContextUtils.getBeanIfAvailable;

@Configuration
@ImportResource(ResourceFinder.CONTEXT_PATTERN)
public class SpringContextLoader implements ApplicationContextAware {

    private ResourceFinder resourceFinder = new ResourceFinder();
    private ApplicationContext applicationContext;

    private List<Resource> externalResources = new ArrayList<Resource>();

    @PostConstruct
    public void loadFileSystemSpringContextConfigurationFiles() throws IOException {
        Set<Resource> contextConfigurationFiles = findFileSystemContextConfigurationFiles();
        if (!contextConfigurationFiles.isEmpty()) {
            BeanDefinitionReader reader = new XmlBeanDefinitionReader((GenericApplicationContext) applicationContext);
            for (Resource resource : contextConfigurationFiles) {
                reader.loadBeanDefinitions(resource);
                externalResources.add(resource);
            }
        }
    }

    private Set<Resource> findFileSystemContextConfigurationFiles() throws IOException {
        Set<Resource> contextConfigurationFiles = new HashSet<Resource>();
        ConfigurationPropertyPlaceholderConfigurer placeholderConfigurer =
                getBeanIfAvailable(applicationContext, ConfigurationPropertyPlaceholderConfigurer.class);
        if (placeholderConfigurer != null) {
            ConfigurationDirectoryLoader loader = new ConfigurationDirectoryLoader();
            Set<File> configurationDirectories = loader.getConfigurationDirectories(placeholderConfigurer.getFileSystemConfiguration());
            contextConfigurationFiles.addAll(resourceFinder.findFileSystemContextFiles(configurationDirectories));
        }
        return contextConfigurationFiles;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<Resource> getExternalResources() {
        return externalResources;
    }
}
