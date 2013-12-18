package org.ow2.proactive.scheduler.ext.mapreduce.logging;

import org.objectweb.proactive.annotation.PublicAPI;

@PublicAPI
public class DefaultLogger implements Logger {

    public static Logger instance = null;
    protected boolean debugLogLevel = false;
    protected boolean profileLogLevel = false;

    public static Logger getInstance() {
        if (instance == null) {
            instance = new DefaultLogger();
            load();
        }
        return instance;
    }

    protected static void load() {
    }

    protected static Logger getLogger() {
        return DefaultLogger.getInstance();
    }

    public void info(String message) {
        // DefaultLogger.getLogger().info(message);
        System.out.println("[INFO] " + message);
    }

    public void profile(String message) {
        if (profileLogLevel) {
            System.out.println("[PROFILE] " + message);
        }
    }

    public void debug(String message) {
        // DefaultLogger.getLogger().info(message);
        if (debugLogLevel) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public void debug(String message, Exception e) {
        // DefaultLogger.getLogger().info(message);
        if (debugLogLevel) {
            System.out.println("[DEBUG] " + message + "\n" + e.getMessage());
        }
    }

    public void error(String message) {
        // DefaultLogger.getLogger().error(message);
        System.out.println("[ERROR] " + message);
    }

    public void error(String message, Exception e) {
        // DefaultLogger.getLogger().error(message, e);
        System.out.println("[ERROR] " + message + "\n" + e.getMessage());
    }

    public void warning(String message) {
        // DefaultLogger.getLogger().warning(message);
        System.out.println("[WARN] " + message);
    }

    public void warning(String message, Exception e) {
        // DefaultLogger.getLogger().warning(message, e);
        System.out.println("[WARN] " + message + "\n" + e.getMessage());
    }

    @Override
    public void setDebugLogLevel(boolean debugLogLevel) {
        this.debugLogLevel = debugLogLevel;
    }

    @Override
    public void setProfileLogLevel(boolean profileLogLevel) {
        this.profileLogLevel = profileLogLevel;
    }
}
