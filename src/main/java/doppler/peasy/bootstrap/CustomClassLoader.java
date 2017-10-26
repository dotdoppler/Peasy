package doppler.peasy.bootstrap;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author doppler
 * @date 2017/10/26
 */
public class CustomClassLoader {
    private final static Logger log = LoggerFactory.getLogger(CustomClassLoader.class);


    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * @param className
     * @param isInitialized
     * @return
     */
    public static Class<?> load(String className, boolean isInitialized) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            log.error("Load class  : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return clazz;
    }

    /**
     * @param packageName
     * @return
     */
    public static Set<Class<?>> getClasses(String packageName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(packageName));
        String packageDir = packageName.replace(".", File.separator);
        Set<Class<?>> set = new HashSet<>();
        try {
            Enumeration<URL> resources = getClassLoader().getResources(packageDir);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();
                if ("file".equals(protocol)) {
                    addClass(set, resource.getPath().replace("%5c", File.separator), packageName);
                } else if ("jar".equals(protocol)) {
                    JarURLConnection connection = (JarURLConnection) resource.openConnection();
                    if (connection != null) {
                        JarFile jarFile = connection.getJarFile();
                        if (jarFile != null) {
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                if (entry.getName().endsWith(".class")) {
                                    String className = entry.getName().substring(0, entry.getName().lastIndexOf(".")).replaceAll(File.separator, ".");
                                    addClass(set, className);
                                }
                            }
                        }
                    }
                }

            }

        } catch (IOException e) {
            log.error("Get Class Error : {}", e.getMessage());
        }
        return set;
    }

    public static void addClass(Set<Class<?>> set, String packageDir, String packageName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(packageName));
        List<File> files = Arrays.asList(new File(packageDir).listFiles(file -> file.isFile() && file.getName().endsWith(".class") || file.isDirectory()));
        files.forEach(file -> {
            if (file.isFile()) {
                String className = packageName + "." + file.getName().substring(0, file.getName().lastIndexOf("."));
                addClass(set, className);
            } else {
                String subPackageDir = packageDir + File.separator + file.getName();
                String subPackageName = packageName + "." + file.getName();
                addClass(set, subPackageDir, subPackageName);
            }
        });
    }

    private static void addClass(Set<Class<?>> set, String className) {
        log.debug("Load class {}", className);
        Class<?> clazz = load(className, false);
        set.add(clazz);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        getClasses("doppler.peasy");
    }
}
