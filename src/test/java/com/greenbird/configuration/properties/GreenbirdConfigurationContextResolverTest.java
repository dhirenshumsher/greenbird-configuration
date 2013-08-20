package com.greenbird.configuration.properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GreenbirdConfigurationContextResolverTest {
    private GreenbirdConfigurationContextResolver resolver;

    @Mock
    private Environment mockEnvironment;

    @Before
    public void setUp() {
        when(mockEnvironment.getActiveProfiles()).thenReturn(new String[]{"a", "b"});
        resolver = new GreenbirdConfigurationContextResolver(mockEnvironment);
    }

    @Test
    public void getTags_normal_tagsRetrievedFromEnvironment() {
        assertThat(resolver.getTags(), is(Arrays.asList("a", "b")));
    }


}
