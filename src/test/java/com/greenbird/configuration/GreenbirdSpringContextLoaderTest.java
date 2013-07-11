package com.greenbird.configuration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class GreenbirdSpringContextLoaderTest extends ContextLoadingTestBase {
    @Autowired
    private ContextTestBean1 testBean1;
    @Autowired
    private ContextTestBean2 testBean2;

    @Test
    public void importResources_normal_beansInModuleConfigsAreLoadedAndHasAccessToConfigProperties() {
        assertThat(testBean1, notNullValue());
        assertThat(testBean2.getTestValue2(), is("value2"));
    }

}
