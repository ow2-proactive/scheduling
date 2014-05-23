package org.ow2.proactive.rm.util.process;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Utility class to use the ProcessTreeKiller with Environment cookies
 */
public class EnvironmentCookieBasedChildProcessKiller {

    private static final Logger logger = Logger.getLogger(EnvironmentCookieBasedChildProcessKiller.class);

    private static final String COOKIE_DEFAULT_NAME = "PROCESS_KILLER_COOKIE";

    private static String cookieName = COOKIE_DEFAULT_NAME;

    /**
     * Sets an environment variable cookie used by the ProcessTreeKiller
     * @param valuePrefix prefix to prepend to the cookie value only
     */
    public static void setCookie(String valuePrefix) {
        setCookie("", valuePrefix);
    }

    /**
     * Sets an environment variable cookie used by the ProcessTreeKiller
     * @param namePrefix prefix to prepend to the cookie name (it can be used to separate cookies set
     *               from different cascading JVM, for example in tests)
     * @param valuePrefix prefix to prepend to the  value
     */
    public static void setCookie(String namePrefix, String valuePrefix) {
        cookieName = namePrefix + COOKIE_DEFAULT_NAME;
        String cookieValue = valuePrefix + ProcessTreeKiller.createCookie();

        logger.debug("Setting environment cookie " + cookieName + " to: " + cookieValue);
        Environment.setenv(cookieName, cookieValue, true);
    }

    private static void unsetCookie() {
        try {
            Environment.unsetenv(cookieName);
        } catch (Exception e) {
            logger.warn("Exception when unsetting the cookie", e);
        }
    }

    /**
     * Returns the name of the cookie previously set
     * @return cookie name
     */
    public static String getCookieName() {
        return cookieName;
    }

    /**
     * Returns the value of the cookie previously set
     * @return cookie vale
     */
    public static String getCookieValue() {
        return Environment.getenv().get(cookieName);
    }

    /**
     * Tries to Kill children processes of this JVM. It will kill
     * processes containing in their environment the cookie. It will not kill the current JVM.
     * No killing will be performed if the cookie was not set.
     */
    public static void killChildProcesses() {
        String cookieValue = getCookieValue();
        if (cookieValue != null && !cookieValue.isEmpty()) {
            unsetCookie();
            Map<String, String> environmentMap = Collections.singletonMap(cookieName, cookieValue);
            logger.debug("Killing all processes with environment: " + environmentMap);
            try {
                ProcessTreeKiller.get().kill(environmentMap);
            } catch (Throwable e) {
                logger.warn("Unable to kill children processes", e);
            }

        }
    }

    /**
     * Register a shutdown hook to kill children processes
     */
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
