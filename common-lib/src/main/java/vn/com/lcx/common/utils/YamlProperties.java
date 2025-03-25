package vn.com.lcx.common.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlProperties {
    private final Map<String, Object> properties;

    public YamlProperties(String resourceFilePath, final ClassLoader classLoader) {
        properties = loadYaml(resourceFilePath);
    }

    public YamlProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    private Map<String, Object> loadYaml(String resourceFilePath) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceFilePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("YAML file not found: " + resourceFilePath);
            }
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + resourceFilePath, e);
        }
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        Object value = getNestedValue(properties, key);
        return value != null ? value.toString() : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> map, String key) {
        String[] keys = key.split("\\.");
        Object value = map;

        for (String k : keys) {
            if (value instanceof Map) {
                value = ((Map<String, Object>) value).get(k);
            } else {
                return null;
            }
        }
        return value;
    }
}
