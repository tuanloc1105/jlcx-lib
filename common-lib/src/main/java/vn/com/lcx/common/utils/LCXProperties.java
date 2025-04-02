package vn.com.lcx.common.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.Properties;
import java.util.function.Function;

@AllArgsConstructor
@NoArgsConstructor
public class LCXProperties {
    private YamlProperties yamlProperties;
    private Properties properties;

    public String getProperty(String key) {
        if (properties != null) {
            return properties.getProperty(key);
        } else if (yamlProperties != null) {
            return yamlProperties.getProperty(key);
        }
        return CommonConstant.EMPTY_STRING;
    }

    public <T> T getProperty(String key, Function<String, T> function) {
        if (properties != null) {
            return function.apply(properties.getProperty(key));
        } else if (yamlProperties != null) {
            return function.apply(yamlProperties.getProperty(key));
        }
        return null;
    }

}
