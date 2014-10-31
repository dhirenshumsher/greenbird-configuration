package com.greenbird.configuration.util;

import com.greenbird.configuration.ConfigTestBean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpringContextUtilsTest {
    @Rule
    public ExpectedException exceptionExpectation = ExpectedException.none();

    @Mock
    private ApplicationContext context;

    @Test
    public void getBeanIfAvailable_beanAvailable_beanReturned() {
        ConfigTestBean expectedBean = new ConfigTestBean();
        when(context.getBean(ConfigTestBean.class)).thenReturn(expectedBean);
        ConfigTestBean actualBean = SpringContextUtils.getBeanIfAvailable(context, ConfigTestBean.class);
        assertThat(actualBean, is(expectedBean));
    }

    @Test
    public void getBeanIfAvailable_beanNotAvailable_nullReturned() {
        when(context.getBean(ConfigTestBean.class)).thenThrow(new NoSuchBeanDefinitionException(ConfigTestBean.class));
        ConfigTestBean actualBean = SpringContextUtils.getBeanIfAvailable(context, ConfigTestBean.class);
        assertThat(actualBean, is(nullValue()));
    }

    @Test
    public void getBeanIfAvailable_applicationThrowsUnrelatedException_exceptionPropagatedAsIs() {
        RuntimeException expectedException = new RuntimeException();
        when(context.getBean(ConfigTestBean.class)).thenThrow(expectedException);
        exceptionExpectation.expect(is(expectedException));
        SpringContextUtils.getBeanIfAvailable(context, ConfigTestBean.class);
    }
}
