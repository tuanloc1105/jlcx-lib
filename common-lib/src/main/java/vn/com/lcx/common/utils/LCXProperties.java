package vn.com.lcx.common.utils;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.Properties;
import java.util.function.Function;

public class LCXProperties {
    private YamlProperties yamlProperties;

    public LCXProperties() {
    }

    public LCXProperties(YamlProperties yamlProperties) {
        this.yamlProperties = yamlProperties;
    }

    public String getProperty(String key) {
        return yamlProperties.getProperty(key);
    }

    public <T> T getProperty_(String key) {
        return yamlProperties.getProperty_(key);
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
            return valueReadFromFile + CommonConstant.EMPTY_STRING;
        }
    }

    public <T> T getPropertyWithEnvironment(String key, Function<String, T> function) {
        return function.apply(getPropertyWithEnvironment(key));
    }

}
