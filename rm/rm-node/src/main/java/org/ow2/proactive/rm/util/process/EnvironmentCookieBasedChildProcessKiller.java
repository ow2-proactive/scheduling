package org.ow2.proactive.rm.util.process;

import org.apache.log4j.Logger;

import org.ow2.proactive.process_tree_killer.ProcessTree;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;


/**
 * Utility class to use the ProcessTreeKiller with Environment cookies
 */
public class EnvironmentCookieBasedChildProcessKiller {

    private static final Logger logger = Logger.getLogger(EnvironmentCookieBasedChildProcessKiller.class);

    private final String cookieName;
    private final String cookieValue;

    public EnvironmentCookieBasedChildProcessKiller(String cookieNameSuffix) {
        cookieName = "PROCESS_KILLER_COOKIE_" + cookieNameSuffix;
        cookieValue = UUID.randomUUID().toString();

        logger.debug("Setting environment cookie " + cookieName + " to: " + cookieValue);
        Environment.setenv(cookieName, cookieValue, true);
    }

    public void killChildProcesses() {
        Environment.unsetenv(cookieName); // do not kill current JVM
        Map<String, String> environmentMap = Collections.singletonMap(cookieName, cookieValue);

        logger.debug("Killing all processes with environment: " + environmentMap);

        try {
            ProcessTree.get().killAll(environmentMap);
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
