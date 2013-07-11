package com.greenbird.configuration;

import com.google.common.collect.Ordering;
import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class PropertyReporter {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(".*(\\.pw|password|passwd|pwd).*", Pattern.CASE_INSENSITIVE);
    private static final String LS = System.getProperty("line.separator");
    private static final PropertyReporter INSTANCE = new PropertyReporter();

    private PropertyReporter() {
        //NOP
    }

    public static String buildPropertyReport(ConstrettoConfiguration configuration) {
        return INSTANCE.doBuildPropertyReport(configuration);
    }

    private String doBuildPropertyReport(ConstrettoConfiguration configuration) {
        int maxNameLength = getMaxPropertyLength(configuration);
        List<Property> properties = sortProperties(configuration);
        StringBuilder reportBuilder = new StringBuilder("using properties :").append(LS);
        for (Property property : properties) {
            reportBuilder
                    .append(String.format("%-" + maxNameLength + "s = %s", property.getKey(), formatValue(property)))
                    .append(LS);
        }
        return reportBuilder.toString();
    }

    private int getMaxPropertyLength(ConstrettoConfiguration configuration) {
        int maxNameLength = 0;
        for (Property property : configuration) {
            maxNameLength = Math.max(property.getKey().length(), maxNameLength);
        }
        return maxNameLength;
    }

    private List<Property> sortProperties(Iterable<Property> properties) {
        Comparator<Property> propertyComparator = new Comparator<Property>() {
            @Override
            public int compare(Property property1, Property property2) {
                return property1.getKey().compareTo(property2.getKey());

            }
        };
        return Ordering.from(propertyComparator).sortedCopy(properties);
    }

    private String formatValue(Property property) {
        String key = property.getKey();
        String value = property.getValue();
        if (PASSWORD_PATTERN.matcher(key).matches()) {
            value = "*****";
        }
        return value;
    }
}
