package com.greenbird.configuration.properties;

import com.google.common.collect.Ordering;
import org.constretto.ConstrettoConfiguration;
import org.constretto.Property;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.hasText;

class PropertyReportCreator {
    public static final String MASK_PATTERN_PROPERTY = "greenbird.config.mask.pattern";
    private static final Pattern DEFAULT_MASKING_PATTERN =
            Pattern.compile(".*(\\.pw|password|passwd|pwd).*", Pattern.CASE_INSENSITIVE);
    private static final String LS = System.getProperty("line.separator");
    private Pattern additionalMaskingPattern = null;

    PropertyReportCreator(String maskPattern) {
        if (hasText(maskPattern)) {
            additionalMaskingPattern = Pattern.compile(maskPattern, Pattern.CASE_INSENSITIVE);
        }
    }

    String createPropertyReport(ConstrettoConfiguration configuration) {
        int maxNameLength = getMaxPropertyLength(configuration);
        List<Property> properties = sortProperties(configuration);
        StringBuilder reportBuilder = new StringBuilder();
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
        if (valueShouldBeMasked(key)) {
            value = "*****";
        }
        return value;
    }

    private boolean valueShouldBeMasked(String key) {
        return DEFAULT_MASKING_PATTERN.matcher(key).matches() ||
                additionalMaskingPattern != null && additionalMaskingPattern.matcher(key).matches();
    }
}
