package com.greenbird.configuration;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.constretto.resolver.ConfigurationContextResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GreenbirdConfigurationContextResolver implements ConfigurationContextResolver {
    public static final String GREENBIRD_CONFIG_PROPERTY = "GREENBIRD_CONFIG_TAGS";

    private static final Splitter TAG_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    public List<String> getTags() {
        return getTagsFromSystemPropertyOrSystemEnv();
    }

    private static List<String> getTagsFromSystemPropertyOrSystemEnv() {
        String tagsString = System.getProperty(GREENBIRD_CONFIG_PROPERTY);
        if (tagsString == null) {
            tagsString = System.getenv(GREENBIRD_CONFIG_PROPERTY);
        }
        List<String> tagList = Collections.emptyList();
        if (tagsString != null) {
            tagList = ImmutableList.copyOf(TAG_SPLITTER.split(tagsString));
        }
        return tagList;
    }

    public static boolean isAnyActive(List<?> tags) {
        List<String> activeTags = getTagsFromSystemPropertyOrSystemEnv();
        boolean result = false;
        for (Object tag : tags) {
            if (activeTags.contains(tag.toString())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean isAnyActive(String... tags) {
        return isAnyActive(Arrays.asList(tags));
    }

    public static boolean isAnyActive(Enum... tags) {
        return isAnyActive(Arrays.asList(tags));
    }
}
