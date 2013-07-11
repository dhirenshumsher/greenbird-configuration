package com.greenbird.configuration;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static com.greenbird.configuration.GreenbirdConfigurationContextResolver.isAnyActive;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GreenbirdConfigurationContextResolver.class)
public class GreenbirdConfigurationContextResolverTest {

    private final GreenbirdConfigurationContextResolver greenbirdConfigurationContextResolver = new GreenbirdConfigurationContextResolver();

    @After
    public void tearDown() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "");
    }

    @Test
    public void getTags_noTagsConfigured_emptyListReturned() {
        mockStatic(System.class);
        when(System.getenv(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY)).thenReturn(null);
        List<String> tagList = greenbirdConfigurationContextResolver.getTags();
        assertThat(tagList.isEmpty(), is(true));
    }

    @Test
    public void getTags_tagsConfiguredInSystemProperty_listWithTagsReturned() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "one, two");
        List<String> tagList = greenbirdConfigurationContextResolver.getTags();
        assertThat(tagList, is(asList("one", "two")));
    }

    @Test
    public void isActive_aMatchingTagConfiguredInSystemProperty_returnsTrue() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "one, two");
        assertThat(isAnyActive("one", "three"), is(true));
    }

    @Test
    public void isActive_tagsAsStringArrayAndNoMatchingTagsConfigured_returnsFalse() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "one, two");
        assertThat(isAnyActive("three"), is(false));
    }

    @Test
    public void isActive_tagsAsStringsAndNoMatchingTagsConfigured_returnsFalse() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "one, two");
        assertThat(isAnyActive(Arrays.asList("three")), is(false));
    }

    @Test
    public void isActive_tagAsEnumAndTagConfigured_returnsTrue() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "OTHER, RUNTIME");
        assertThat(isAnyActive(RetentionPolicy.RUNTIME), is(true));
    }

    @Test
    public void isActive_tagAsEnumAndTagNotConfigured_returnsFalse() {
        System.setProperty(GreenbirdConfigurationContextResolver.GREENBIRD_CONFIG_PROPERTY, "one, two");
        assertThat(isAnyActive(RetentionPolicy.RUNTIME), is(false));
    }
}
