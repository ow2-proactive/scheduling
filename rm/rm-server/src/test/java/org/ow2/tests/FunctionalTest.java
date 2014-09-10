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

import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.Timeout;


@Ignore
public class FunctionalTest extends ProActiveTest {
    static {
        configureLogging();
        configurePAHome();
        ProActiveConfiguration.load();
    }

    protected static final Logger logger = Logger.getLogger("testsuite");

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue());

    /**
     * Shutdown hook to ensure that process are killed even if afterClass is not run.
     */
    private static KillAllProcessShutdownHook shutdownHook;

    private static int getTestSlice(String testName, int maxValue) {
        return Math.abs(testName.hashCode() % maxValue) + 1;
    }

    protected boolean shouldBeExecuted() {
        String testName = this.getClass().getSimpleName();
        return shouldBeExecuted(testName);
    }

    public static boolean shouldBeExecuted(String testName) {

        String sliceProperty = System.getProperty("test_slice");
        String maxSliceProperty = System.getProperty("max_test_slice");

        if (sliceProperty != null && maxSliceProperty != null) {
            try {

                int targetSlice = Integer.parseInt(sliceProperty);
                int maxSlice = Integer.parseInt(maxSliceProperty);

                int currentTestSlice = getTestSlice(testName, maxSlice);

                System.err.println("Test slice: " + currentTestSlice);

                if (currentTestSlice != targetSlice) {
                    return false;
                }

                return true;
            } catch (NumberFormatException ex) {
            }
        }
        return true;
    }

    @Before
    public void prepareForTest() throws Exception {

        if (!shouldBeExecuted()) {
            Assume.assumeTrue(false);
        }

        CentralPAPropertyRepository.PA_TEST.setValue(true);
        CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);

        boolean isConsecutive = shouldBeExecutedInConsecutiveMode(this.getClass());

        if (isConsecutive) {
            System.err.println("Running test in 'consecutive' mode");
        } else {
            // skipping this test execution
            System.err.println("Test does not support the 'consecutive' mode execution");
            System.err.println("Running test in 'clean environment' mode");
            killAliveProcesses();
        }

        shutdownHook = new KillAllProcessShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);

    }

    private static void configurePAHome() {
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), System
                    .getProperty(PAResourceManagerProperties.RM_HOME.getKey()));
        }
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            URL defaultLog4jConfig = FunctionalTest.class.getResource("/log4j-junit");
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig.toString());
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

    protected boolean shouldBeExecutedInConsecutiveMode(Class<?> cls) {
        // it should be explicitly allowed by the property
        String consecutiveProperty = System.getProperty("pa.tests.consecutive");
        boolean allowConsecutive = consecutiveProperty != null && Boolean.parseBoolean(consecutiveProperty);

        if (!allowConsecutive) {
            return false;
        }

        // second class must have an annotation Consecutive
        if (cls.isAnnotationPresent(Consecutive.class)) {
            return true;
        } else if (cls.getSuperclass() != null) {
            return shouldBeExecutedInConsecutiveMode(cls.getSuperclass());
        }
        return false;
    }

    @After
    public void afterClass() throws Exception {
        if (!shouldBeExecuted()) {
            return;
        }

        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        if (!shouldBeExecutedInConsecutiveMode(this.getClass())) {
            System.err.println("Killing all alive proactive processes");
            killAliveProcesses();
        } else {
            System.err.println("Keep the scheduler & rm running after the test");
        }
    }

    private static void killAliveProcesses() throws IOException {
        new ProcessCleaner(".*proactive.test=true.*").killAliveProcesses();
        new ProcessCleaner(".*RMNodeStarter.*").killAliveProcesses();
        new ProcessCleaner(".*SchedulerTStarter.*").killAliveProcesses();
        new ProcessCleaner(".*RMTStarter.*").killAliveProcesses();
    }

    private static class KillAllProcessShutdownHook extends Thread {
        @Override
        public void run() {
            System.err.println("Shutdown hook. Killing remaining processes");
            try {
                killAliveProcesses();
            } catch (Exception e) {
                logger.error("Failed to kill remaining processes", e);
            }
        }
    }

}
