package org.objectweb.proactive.extensions.scheduler.common.job;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * This class provides a ClassLoader which can dynamically load classes from the classpath defines in PAS_CLASSPATH environment variable.
 * 
 * @author FRADJ Johann
 */
public class SchedulerClassLoader {

    public static ClassLoader getClassLoader(ClassLoader defaultLoader) {
        ClassLoader loader = defaultLoader;

        String classpath = System.getenv("PAS_CLASSPATH");

        if ((classpath != null) && (!classpath.trim().equals("")))
            try {
                String[] cp = classpath.split(System.getProperty("path.separator"));
                URL[] urls = new URL[cp.length];
                for (int i = 0; i < cp.length; i++) {
                    urls[i] = new File(cp[i]).toURL();
                }
                loader = new URLClassLoader(urls, defaultLoader);
            } catch (MalformedURLException e) {
                // nothing to do because the defaultloader will be returned so
                // the class will not be found and a ClassNotFoundException will be thrown
            }

        return loader;
    }
}
