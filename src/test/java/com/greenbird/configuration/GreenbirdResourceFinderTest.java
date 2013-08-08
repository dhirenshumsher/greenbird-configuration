package com.greenbird.configuration;

import com.google.common.base.Predicate;
import org.junit.Test;
import org.springframework.core.io.Resource;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GreenbirdResourceFinderTest {
    @Test
    public void findContextDefinitions_normal_relevantModulesFound() throws Exception {
        List<Resource> greenbirdModules = asList(new GreenbirdResourceFinder().findContextDefinitions());
        Iterable<Resource> relevantResources = filter(greenbirdModules, new Predicate<Resource>() {
            @Override
            public boolean apply(Resource resource) {
                return resourceContains(resource, "gb-conf/sub_config_2");
            }
        });
        assertThat(relevantResources.iterator().hasNext(), is(true));
    }

    @Test
    public void findConfigurationFilesForProfile_normal_relevantResourcesFound() {
        Resource[] greenbirdModules = new GreenbirdResourceFinder().findConfigurationFilesForProfile("testprofile");
        assertThat(greenbirdModules.length, is(1));
        assertThat(resourceContains(greenbirdModules[0], "gb-conf/greenbird-testprofile.properties"), is(true));
    }

    private boolean resourceContains(Resource input, String pathFragment) {
        return input.toString().replace("\\", "/").contains(pathFragment);
    }
}
