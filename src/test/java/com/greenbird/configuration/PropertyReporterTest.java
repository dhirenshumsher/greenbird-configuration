package com.greenbird.configuration;

import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;
import org.constretto.internal.DefaultConstrettoConfiguration;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static com.greenbird.configuration.PropertyReporter.buildPropertyReport;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PropertyReporterTest {
    private static final String LS = System.getProperty("line.separator");

    @Test
    public void buildPropertyReport_unsortedInput_propertiesAreFormattedAndSorted() {
        ConstrettoConfiguration configuration = config(asList(
                prop("one", "valueOne"),
                prop("two", "valueTwo"),
                prop("three", "valueThree")
        ));
        String report = buildPropertyReport(configuration);
        assertThat(report, is(String.format("one   = valueOne%sthree = valueThree%stwo   = valueTwo%s", LS, LS, LS)));
    }

    @Test
    public void buildPropertyReport_passwordInProperties_valueNotReported() {
        ConstrettoConfiguration configuration = config(asList(
                prop("test.pw", "secret"),
                prop("test.password", "secret"),
                prop("test.passwd", "secret"),
                prop("test.pwd", "secret")
        ));
        String report = buildPropertyReport(configuration);
        assertThat(report, not(containsString("secret")));
    }

    private ConstrettoConfiguration config(final List<Property> properties) {
        return new DefaultConstrettoConfiguration(null, null) {
            @Override
            public Iterator<Property> iterator() {
                return properties.iterator();
            }
        };
    }

    private Property prop(String key, String value) {
        return new Property(key, value);
    }
}
