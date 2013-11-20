package com.greenbird.configuration.properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationDirectoryLoader.class})
public class ConfigurationDirectoryLoaderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ConfigurationDirectoryLoader loader = new ConfigurationDirectoryLoader();

    @Mock
    private ConstrettoConfiguration mockConfiguration;

    @Before
    public void setUp() {
        mockStatic(System.class);
        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>emptyIterator());
        when(System.getProperties()).thenReturn(new Properties());
        when(System.getenv()).thenReturn(Collections.<String, String>emptyMap());
    }

    @Test
    public void getConfigurationDirectories_noDirectoriesConfigured_noDirectoriesReturned() {
        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        assertThat(configurationDirectories.size(), is(0));
    }

    @Test
    public void getConfigurationDirectories_configurationSystemAndEnvPropertiesConfigured_orderReturnedIsPropertiesSystemAndEnv() throws IOException {
        File testFolder1 = temporaryFolder.newFolder("test1");
        final File testFolder2 = temporaryFolder.newFolder("test2");
        File testFolder3 = temporaryFolder.newFolder("test3");

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property("1.greenbird.config.dir", testFolder1.getPath())
        ));

        when(System.getProperties()).thenReturn(new Properties() {{
            setProperty("2.greenbird.config.dir", testFolder2.getPath());
        }});

        when(System.getenv()).thenReturn(Collections.singletonMap("3.greenbird.config.dir", testFolder3.getPath()));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder1, testFolder2, testFolder3);
    }

    @Test
    public void getConfigurationDirectories_nixFriendlyPropertyNameUsed_nixFriendlyPropertyUsed() throws IOException {
        File testFolder1 = temporaryFolder.newFolder("test1");
        File testFolder2 = temporaryFolder.newFolder("test2");

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property(ConfigurationDirectoryLoader.CONFIG_DIR_PROPERTY, testFolder1.getPath()),
                new Property(ConfigurationDirectoryLoader.NIX_FRIENDLY_CONFIG_DIR_PROPERTY, testFolder2.getPath())
        ));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder1, testFolder2);
    }

    @Test
    public void getConfigurationDirectories_multiplePropertiesResolvesToTheSameDirectory_noDuplicatesLoaded() throws IOException {
        File testFolder1 = temporaryFolder.newFolder("test1");
        File testFolder2 = new File(testFolder1.getPath() + "/.");
        assertThat(testFolder2.exists(), is(true));
        assertThat(testFolder1.getPath(), not(is(testFolder2.getPath())));

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property(ConfigurationDirectoryLoader.CONFIG_DIR_PROPERTY, testFolder1.getPath()),
                new Property(ConfigurationDirectoryLoader.NIX_FRIENDLY_CONFIG_DIR_PROPERTY, testFolder2.getPath())
        ));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder1);
    }

    @Test
    public void getConfigurationDirectories_nonExistingDirectoryConfigured_nonExistingDirectoryIgnored() throws IOException {
        File testFolder1 = temporaryFolder.newFolder("test1");

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property("1.greenbird.config.dir", testFolder1.getPath()),
                new Property("2.greenbird.config.dir", "nonexisting")
        ));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder1);
    }

    @Test
    public void getConfigurationDirectories_fileConfiguredAsDirectory_fileIgnored() throws IOException {
        File testFolder = temporaryFolder.newFolder("testFolder");
        File testFile = temporaryFolder.newFile("testFile");

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property("1.greenbird.config.dir", testFolder.getPath()),
                new Property("2.greenbird.config.dir", testFile.getPath())
        ));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder);
    }

    @Test
    public void getConfigurationDirectories_unreadableDirectoryConfigured_unreadableDirectoryIgnored() throws IOException {
        File testFolder1 = temporaryFolder.newFolder("testFolder1");
        assertThat(testFolder1.setReadable(false), is(true));
        File testFolder2 = temporaryFolder.newFolder("testFolder2");

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property("1.greenbird.config.dir", testFolder1.getPath()),
                new Property("2.greenbird.config.dir", testFolder2.getPath())
        ));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder2);
    }

    @Test
    public void getConfigurationDirectories_unrelatedPropertiesAvailable_unrelatedPropertiesIgnored() throws IOException {
        File testFolder1 = temporaryFolder.newFolder("test1");
        final File testFolder2 = temporaryFolder.newFolder("test2");
        File testFolder3 = temporaryFolder.newFolder("test3");

        File testFolderToBeIgnored1 = temporaryFolder.newFolder("testToBeIgnored1");
        final File testFolderToBeIgnored2 = temporaryFolder.newFolder("testToBeIgnored2");
        File testFolderToBeIgnored3 = temporaryFolder.newFolder("testToBeIgnored3");

        when(mockConfiguration.iterator()).thenReturn(Iterators.<Property>forArray(
                new Property("greenbird.config.dir.toBeIgnored", testFolderToBeIgnored1.getPath()),
                new Property("greenbird.config.dir", testFolder1.getPath())
        ));

        when(System.getProperties()).thenReturn(new Properties() {{
            setProperty("toBeIgnored2", testFolderToBeIgnored2.getPath());
            setProperty("2.greenbird.config.dir", testFolder2.getPath());
        }});

        when(System.getenv()).thenReturn(ImmutableMap.of(
                "3.greenbird.config.dir", testFolder3.getPath(),
                "toBeIgnored3", testFolderToBeIgnored3.getPath()
        ));

        Set<File> configurationDirectories = loader.getConfigurationDirectories(mockConfiguration);
        verifyDirectories(configurationDirectories, testFolder1, testFolder2, testFolder3);
    }

    private void verifyDirectories(Set<File> actualDirectories, File... expectedDirectories) throws IOException {
        assertThat(actualDirectories.size(), is(expectedDirectories.length));
        Iterator<File> directoryIterator = actualDirectories.iterator();
        for (File expectedDirectory : expectedDirectories) {
            assertThat(directoryIterator.next().getCanonicalPath(), is(expectedDirectory.getCanonicalPath()));
        }
    }
}
