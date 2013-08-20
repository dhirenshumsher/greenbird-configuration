package com.greenbird.configuration;

import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;
import org.constretto.internal.DefaultConstrettoConfiguration;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PropertyReportCreatorTest {
    private static final String LS = System.getProperty("line.separator");
    private PropertyReportCreator propertyReportCreator = new PropertyReportCreator(null);

    @Test
    public void createPropertyReport_unsortedInput_propertiesAreFormattedAndSorted() {
        ConstrettoConfiguration configuration = config(asList(
                prop("one", "valueOne"),
                prop("two", "valueTwo"),
                prop("three", "valueThree")
        ));
        String report = propertyReportCreator.createPropertyReport(configuration);
        assertThat(report, is(String.format("one   = valueOne%sthree = valueThree%stwo   = valueTwo%s", LS, LS, LS)));
    }

    @Test
    public void createPropertyReport_noAdditionalMaskAndMaskableValuesInProperties_maskableValuesNotReported() {
        ConstrettoConfiguration configuration = propertiesToMask();
        String report = propertyReportCreator.createPropertyReport(configuration);
        assertThat(report, not(containsString("secret")));
        assertThat(report, containsString("otherValue"));
    }

    @Test
    public void createPropertyReport_additionalMaskAndMaskableValuesInProperties_maskableValuesNotReported() {
        PropertyReportCreator reporter = new PropertyReportCreator(".*other.*");
        ConstrettoConfiguration configuration = propertiesToMask();
        String report = reporter.createPropertyReport(configuration);
        assertThat(report, not(containsString("secret")));
        assertThat(report, not(containsString("otherValue")));
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

    private ConstrettoConfiguration propertiesToMask() {
        return config(asList(
                prop("test.pw", "secret"),
                prop("test.password", "secret"),
                prop("test.passwd", "secret"),
                prop("test.pwd", "secret"),
                prop("test.other", "otherValue")
        ));
    }
}
