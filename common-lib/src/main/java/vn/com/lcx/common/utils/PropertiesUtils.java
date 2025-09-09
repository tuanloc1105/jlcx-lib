package vn.com.lcx.common.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesUtils {

    private PropertiesUtils() {
    }

    public static LCXProperties getProperties(final String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new LCXProperties();
        }
        if (file.isDirectory()) {
            return new LCXProperties();
        }
        final var fileExtension = FileUtils.getFileExtension(file);
        if (fileExtension.equals("yaml") || fileExtension.equals("yml")) {
            Yaml yaml = new Yaml();
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
                // Load YAML
                // noinspection RedundantTypeArguments
                return new LCXProperties(new YamlProperties(yaml.<Map<String, Object>>load(inputStream)));
            } catch (Exception e) {
                LogUtils.writeLog(e.getMessage(), e);
            }
        }
        return new LCXProperties();
    }

    public static LCXProperties getProperties(final ClassLoader classLoader, final String resourceFilePath) {
        final var fileExtension = FileUtils.getFileExtension(resourceFilePath);
        if (fileExtension.equals("yaml") || fileExtension.equals("yml")) {
            Yaml yaml = new Yaml();
            try (InputStream inputStream = classLoader.getResourceAsStream(resourceFilePath)) {
                // Load YAML
                if (inputStream == null) {
                    return new LCXProperties(new YamlProperties(new HashMap<>()));
                }
                return new LCXProperties(new YamlProperties(yaml.<Map<String, Object>>load(inputStream)));
            } catch (Exception e) {
                LogUtils.writeLog(e.getMessage(), e);
            }
        }
        return new LCXProperties();
    }

    public static LCXProperties emptyProperty() {
        return new LCXProperties(null);
    }

}
