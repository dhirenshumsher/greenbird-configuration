package com.greenbird.configuration.context;

import com.greenbird.configuration.ContextLoadingTestBase;
import com.greenbird.configuration.ContextTestBean1;
import com.greenbird.configuration.ContextTestBean2;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SpringContextLoaderTest extends ContextLoadingTestBase {
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
