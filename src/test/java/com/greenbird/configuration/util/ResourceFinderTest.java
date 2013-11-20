package com.greenbird.configuration.util;

import com.google.common.base.Predicate;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ResourceFinderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void findContextDefinitions_normal_relevantModulesFound() throws Exception {
        List<Resource> greenbirdModules = asList(new ResourceFinder().findContextDefinitions());
        Iterable<Resource> relevantResources = filter(greenbirdModules, new Predicate<Resource>() {
            @Override
            public boolean apply(Resource resource) {
                return resourceContains(resource, "gb-conf/sub_config_2");
            }
        });
        assertThat(relevantResources.iterator().hasNext(), is(true));
    }

    @Test
    public void findClasspathConfigurationFilesForProfile_normal_relevantResourcesFound() {
        Resource[] greenbirdModules = new ResourceFinder().findClasspathConfigurationFilesForProfile("testprofile");
        assertThat(greenbirdModules.length, is(1));
        assertThat(resourceContains(greenbirdModules[0], "gb-conf/greenbird-testprofile.properties"), is(true));
    }

    @Test
    public void findFileSystemConfigurationFilesForProfile_normal_relevantResourcesFound() throws IOException {
        File testFolder = temporaryFolder.newFolder("testconfig");
        FileUtils.writeStringToFile(new File(testFolder, "greenbird-fileprofile.properties"), "");
        
        Resource[] greenbirdModules = new ResourceFinder()
                .findFileSystemConfigurationFilesForProfile(temporaryFolder.getRoot(), "fileprofile");
        assertThat(greenbirdModules.length, is(1));
        assertThat(resourceContains(greenbirdModules[0], "testconfig/greenbird-fileprofile.properties"), is(true));
    }

    private boolean resourceContains(Resource input, String pathFragment) {
        return input.toString().replace("\\", "/").contains(pathFragment);
    }
}
