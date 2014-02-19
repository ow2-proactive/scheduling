package org.ow2.proactive.rm.util.process;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class EnvironmentCookieBasedChildProcessKiller {

    private static final Logger logger = Logger.getLogger(EnvironmentCookieBasedChildProcessKiller.class);

    private static final String COOKIE_NAME = "PROCESS_KILLER_COOKIE";

    public static void setCookie(String prefix) {
        String cookieValue = prefix + ProcessTreeKiller.createCookie();
        logger.debug("Setting environment cookie to: " + cookieValue);
        Environment.setenv(COOKIE_NAME, cookieValue, true);
    }

    private static void unsetCookie() {
        try {
            Environment.unsetenv(COOKIE_NAME);
        } catch (Exception e) {
            logger.warn("Exception when unsetting the cookie", e);
        }
    }

    private static String getCookie() {
        return Environment.getenv().get(COOKIE_NAME);
    }

    public static void killChildProcesses() {
        String cookieValue = getCookie();
        if (cookieValue != null && !cookieValue.isEmpty()) {
            unsetCookie();
            Map<String, String> environmentMap = Collections.singletonMap(COOKIE_NAME, cookieValue);
            logger.debug("Killing all processes with environment: " + environmentMap);
            try {
                ProcessTreeKiller.get().kill(environmentMap);
            } catch (Throwable e) {
                logger.warn("Unable to kill children processes", e);
            }

        }
    }

    public static void registerKillChildProcessesOnShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.debug("Node terminating");
                EnvironmentCookieBasedChildProcessKiller.killChildProcesses();
            }
        });
    }

}
