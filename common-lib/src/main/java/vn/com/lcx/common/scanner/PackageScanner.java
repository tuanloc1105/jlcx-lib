package vn.com.lcx.common.scanner;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageScanner {

    public static List<Class<?>> findClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            LoggerFactory.getLogger("APP").warn(e.getMessage(), e);
            return classes;
        }
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                try {
                    classes.addAll(findClassesInDirectory(resource.getPath(), packageName));
                } catch (ClassNotFoundException ignore) {
                }
            } else if (resource.getProtocol().equals("jar")) {
                try {
                    JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
                    classes.addAll(findClassesInJar(jarConnection.getJarFile(), path));
                } catch (IOException | ClassNotFoundException ignore) {
                }
            }
        }
        return classes;
    }

    private static List<Class<?>> findClassesInDirectory(String directoryPath, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClassesInDirectory(file.getAbsolutePath(), packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }

    private static List<Class<?>> findClassesInJar(JarFile jarFile, String packagePath) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }
    // public static void main(String[] args) throws ClassNotFoundException, IOException {
    //     String packageName = "com.example.myapp";
    //     List<Class<?>> classes = findClasses(packageName);
    //     for (Class<?> clazz : classes) {
    //         System.out.println(clazz.getName());
    //     }
    // }
}
