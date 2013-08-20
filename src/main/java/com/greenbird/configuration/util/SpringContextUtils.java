package com.greenbird.configuration.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

public class SpringContextUtils {
    private static final SpringContextUtils INSTANCE = new SpringContextUtils();

    private SpringContextUtils() {
        // NOP
    }

    public static <T> T getBeanIfAvailable(ApplicationContext applicationContext, Class<T> beanClass) {
        return INSTANCE.doGetBeanIfAvailable(applicationContext, beanClass);
    }

    private <T> T doGetBeanIfAvailable(ApplicationContext applicationContext, Class<T> beanClass) {
        T bean = null;
        try {
            bean = applicationContext.getBean(beanClass);
        } catch (NoSuchBeanDefinitionException e) {
            // NOP
        }
        return bean;
    }
}
