package com.greenbird.configuration.sub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("beanBuilderPattern")
public class BeanWithBeanBuilderPattern {
	
	@Bean(name = "myBuilderBean")
	public MyBuilderBean myBuilderBean() throws Exception {
		return new MyBuilderBean();
	}
	
	public static class MyBuilderBean {
	}
	
	@Bean(name = "myLazyBuilderBean")
	@Lazy
	public MyLazyBuilderBean myLazyBuilderBean() throws Exception {
		return new MyLazyBuilderBean();
	}
	
	public static class MyLazyBuilderBean {
	}
	
	@Bean
	public AnotherBeanToBeReported externalBuilderBean() throws Exception {
		return new AnotherBeanToBeReported();
	}
}
