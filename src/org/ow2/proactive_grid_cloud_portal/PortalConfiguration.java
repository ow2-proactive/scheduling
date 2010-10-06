package org.ow2.proactive_grid_cloud_portal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PortalConfiguration {

    public static String scheduler_url = "scheduler.url";
    public static String scheduler_cache_login = "scheduler.cache.login";
    public static String scheduler_cache_password = "scheduler.cache.password";
    public static String rm_url = "rm.url";


    private static Properties properties;

    public static void load(File f) throws FileNotFoundException, IOException {
        properties = new Properties();
        properties.load(new FileInputStream(f));
    }

    public static Properties getProperties() {
       return properties;
    }
}
