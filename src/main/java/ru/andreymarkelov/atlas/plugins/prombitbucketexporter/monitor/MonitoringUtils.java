package ru.andreymarkelov.atlas.plugins.prombitbucketexporter.monitor;

import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MonitoringUtils {
    private MonitoringUtils() {
    }

    public static Map<String, String> initializeMap(String[] attributes, Set<String> without) {
        Map<String, String> result = new HashMap<>();
        for (String attribute : attributes) {
            if (without.contains(attribute)) {
                continue;
            }
            result.put(attribute, "0");
        }
        return result;
    }

    public static Map<String, String> convertAttributeListToMap(AttributeList attributeList) {
        Map<String, String> result = new HashMap<>();
        for (Object attrObj : attributeList) {
            Attribute attribute = (Attribute) attrObj;
            String attributeName = attribute.getName();
            Object attributeValue = attribute.getValue();
            result.put(attributeName, attributeValue == null ? "null" : attributeValue.toString());
        }
        return result;
    }

    public static Map<String, String> combineMaps(Map<String, String> superSet, Map<String, String> subSet) {
        for (Map.Entry<String, String> entry : subSet.entrySet()) {
            String key = entry.getKey();
            if (key.contains("Name")) {
                continue;
            }
            if (superSet.containsKey(entry.getKey())) {
                Double newValue = Double.parseDouble(entry.getValue()) + Double.parseDouble(superSet.get(entry.getKey()));
                superSet.put(entry.getKey(), newValue.toString());
                continue;
            }
            superSet.put(entry.getKey(), entry.getValue());
        }
        return superSet;
    }
}
