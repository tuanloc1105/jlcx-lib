package vn.com.lcx.common.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        return function.apply(getProperty(key));
    }

    public String getPropertyWithEnvironment(String key) {
        String valueReadFromFile = getProperty(key);
        if (
                StringUtils.isNotBlank(valueReadFromFile) &&
                        valueReadFromFile.startsWith("${") &&
                        valueReadFromFile.endsWith("}")
        ) {
            final String valueRemovedPrefixAndSuffix = valueReadFromFile
                    .replace("${", CommonConstant.EMPTY_STRING)
                    .replace("}", CommonConstant.EMPTY_STRING);
            if (valueRemovedPrefixAndSuffix.isEmpty()) {
                return CommonConstant.EMPTY_STRING;
            }
            final String[] valueArraySplitColon = valueRemovedPrefixAndSuffix.split(":");
            final String envVariableName = valueArraySplitColon[0];
            final String envDefaultValue = valueArraySplitColon.length >= 2 ? valueArraySplitColon[1] : null;
            final String valueFromEnvVariableName = System.getenv(envVariableName);
            return StringUtils.isNotBlank(valueFromEnvVariableName) ? valueFromEnvVariableName : envDefaultValue;
        } else {
            return CommonConstant.EMPTY_STRING;
        }
    }

    public <T> T getPropertyWithEnvironment(String key, Function<String, T> function) {
        return function.apply(getPropertyWithEnvironment(key));
    }

}
