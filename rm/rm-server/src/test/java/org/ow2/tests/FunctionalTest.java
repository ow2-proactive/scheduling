/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.tests;

import java.io.PrintStream;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.utils.SafeTimerTask;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;


@Ignore
public class FunctionalTest extends ProActiveTest {
    static {
        configureLogging();
        configurePAHome();
        ProActiveConfiguration.load();
    }

    static final protected Logger logger = Logger.getLogger("testsuite");
    /** Timeout before the test gets killed. */
    protected long timeout = CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue();
    /** Timer to kill the test after the timeout. */
    static final private Timer timer = new Timer("functional test timer", true);
    static final private AtomicReference<TimerTask> timerTask = new AtomicReference<TimerTask>();
    /** Shutdown hook to ensure that process are killed even if afterClass is not run. */
    static final private MyShutdownHook shutdownHook = new MyShutdownHook();
    /** ProActive related stuff */
    static volatile private ProActiveSetup paSetup;
    static final private ProcessCleaner cleaner = new ProcessCleaner(".*proactive.test=true.*");

    protected VariableContractImpl getVariableContract() {
        return paSetup.getVariableContract();
    }

    protected String getJvmParameters() {
        return paSetup.getJvmParameters();
    }

    @Before
    public void prepareForTest() throws Exception {

        CentralPAPropertyRepository.PA_TEST.setValue(true);
        CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);

        if (!ConsecutiveMode.isConsecutiveMode()) {
            // Ensure that the host is clean
            System.err.println("Running test in 'clean environment' mode");
            cleaner.killAliveProcesses();
        } else {
            if (canBeExecutedInConsecutiveMode(this.getClass())) {
                System.err.println("Running test in 'consecutive' mode");
            } else {
                // skipping this test execution
                System.err.println("Test does not support the 'consecutive' mode execution");
                Assume.assumeTrue(false);
                return;
            }
        }

        // Ensure that the host will eventually be cleaned
        System.err.println("Arming timer " + timeout);
        TimerTask tt = new MyTimerTask();
        if (timerTask.compareAndSet(null, tt)) {
            timer.schedule(new MyTimerTask(), timeout);
        } else {
            throw new IllegalStateException("timer task should be null");
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        // Should be final and initialized in a static block but we can't since
        // child classes must be able to configure PAProperties using static block before
        // calling ProActiveSetup.ctor()
        paSetup = new ProActiveSetup();
        paSetup.start();
    }

    private static void configurePAHome() {
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), System.getProperty(
              PAResourceManagerProperties.RM_HOME.getKey()));
        }
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            String defaultLog4jConfig = System.getProperty(
              PAResourceManagerProperties.RM_HOME.getKey()) + "/config/log4j/log4j-junit";
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(),
              "file:" + defaultLog4jConfig);
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

    private boolean canBeExecutedInConsecutiveMode(Class<?> cls) {
        if (cls.isAnnotationPresent(Consecutive.class)) {
            return true;
        } else if (cls.getSuperclass() != null) {
            return canBeExecutedInConsecutiveMode(cls.getSuperclass());
        }
        return false;
    }

    @After
    public void afterClass() throws Exception {
        // Disable timer and shutdown hook
        TimerTask tt = timerTask.getAndSet(null);
        if (tt != null) {
            tt.cancel();
        }
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        if (!ConsecutiveMode.isConsecutiveMode()) {
            // Cleanup proactive
            if (paSetup != null) {
                paSetup.shutdown();
            }
            // Kill everything
            cleaner.killAliveProcesses();
        }
    }

    static private class MyShutdownHook extends Thread {
        @Override
        public void run() {
            System.err.println("Shutdown hook. Killing remaining processes");
            try {
                timer.cancel();
                if (!ConsecutiveMode.isConsecutiveMode()) {
                    paSetup.shutdown();
                    cleaner.killAliveProcesses();
                }
            } catch (Exception e) {
                logger.error("Failed to kill remaining proccesses", e);
            }
        }
    }

    static private class MyTimerTask extends SafeTimerTask {
        @Override
        public void safeRun() {
            System.err.println("Timeout reached. Killing remaining processes");
            System.err.println("Dumping thread states before killing processes");
            printAllThreadsStackTraces(System.err);
            if (!ConsecutiveMode.isConsecutiveMode()) {
                try {
                    cleaner.killAliveProcesses();
                    System.err.println("Killing current JVM");
                    System.exit(-42);
                } catch (Exception e) {
                    logger.error("Failed to kill remaining proccesses", e);
                }
            } else {
                System.exit(0);
            }
        }

        private static void printAllThreadsStackTraces(PrintStream stream) {
            for (Map.Entry<Thread, StackTraceElement[]> threadEntry : Thread.getAllStackTraces().entrySet()) {
                stream.println(threadEntry.getKey());
                for (StackTraceElement stackTraceElement : threadEntry.getValue()) {
                    stream.println("\t" + stackTraceElement);
                }

            }
        }
    }
}
