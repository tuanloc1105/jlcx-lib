package vn.com.lcx.common.utils;

import lombok.val;
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
        val fileExtension = FileUtils.getFileExtension(file);
        if (fileExtension.equals("yaml") || fileExtension.equals("yml")) {
            Yaml yaml = new Yaml();
            try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
                // Load YAML
                // noinspection RedundantTypeArguments
                return new LCXProperties(new YamlProperties(yaml.<Map<String, Object>>load(inputStream)), null);
            } catch (Exception e) {
                LogUtils.writeLog(e.getMessage(), e);
            }
        } else {
            val properties = new Properties();
            try (InputStream input = Files.newInputStream(Paths.get(filePath))) {
                properties.load(input);
            } catch (IOException ex) {
                LogUtils.writeLog(ex.getMessage(), ex);
            }
            return new LCXProperties(null, properties);
        }
        return new LCXProperties();
    }

    public static LCXProperties getProperties(final ClassLoader classLoader, final String resourceFilePath) {
        val fileExtension = FileUtils.getFileExtension(resourceFilePath);
        if (fileExtension.equals("yaml") || fileExtension.equals("yml")) {
            Yaml yaml = new Yaml();
            try (InputStream inputStream = classLoader.getResourceAsStream(resourceFilePath)) {
                // Load YAML
                if (inputStream == null) {
                    return new LCXProperties(new YamlProperties(new HashMap<>()), null);
                }
                return new LCXProperties(new YamlProperties(yaml.<Map<String, Object>>load(inputStream)), null);
            } catch (Exception e) {
                LogUtils.writeLog(e.getMessage(), e);
            }
        } else {
            val properties = new Properties();
            try (InputStream input = classLoader.getResourceAsStream(resourceFilePath)) {
                if (input != null) {
                    properties.load(input);
                }
            } catch (IOException ex) {
                LogUtils.writeLog(ex.getMessage(), ex);
            }
            return new LCXProperties(null, properties);
        }
        return new LCXProperties();
    }

}
