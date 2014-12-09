package org.ow2.proactive.rm.util.process;

import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * Utility class to use the ProcessTreeKiller with Environment cookies
 */
public class EnvironmentCookieBasedChildProcessKiller {

    private static final Logger logger = Logger.getLogger(EnvironmentCookieBasedChildProcessKiller.class);

    private final String cookieName;
    private final String cookieValue;

    public EnvironmentCookieBasedChildProcessKiller(String cookieNameSuffix) {
        cookieValue = ProcessTreeKiller.createCookie();
        cookieName = "PROCESS_KILLER_COOKIE_" + cookieNameSuffix;

        logger.debug("Setting environment cookie " + cookieName + " to: " + cookieValue);
        Environment.setenv(cookieName, cookieValue, true);
    }

    public void killChildProcesses() {
        logger.debug("Node terminating");
        Map<String, String> environmentMap = Collections.singletonMap(cookieName, cookieValue);
        logger.debug("Killing all processes with environment: " + environmentMap);
        try {
            ProcessTreeKiller.get().kill(environmentMap);
        } catch (Throwable e) {
            logger.warn("Unable to kill children processes", e);
        }
    }

    /**
     * Register a shutdown hook to kill children processes
     */
    public static void registerKillChildProcessesOnShutdown(String cookieNameSuffix) {
        final EnvironmentCookieBasedChildProcessKiller processKiller = new EnvironmentCookieBasedChildProcessKiller(
            cookieNameSuffix);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                processKiller.killChildProcesses();
            }
        });
    }

}
