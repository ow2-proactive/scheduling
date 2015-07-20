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
package functionaltests;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.tests.Consecutive;
import org.ow2.tests.ProActiveTest;
import org.ow2.tests.ProcessCleaner;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;


public abstract class RMFunctionalTest extends ProActiveTest {
    static {
        configureLogging();
        ProActiveConfiguration.load();
    }

    protected static final Logger logger = Logger.getLogger("RMTests");

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue(),
        TimeUnit.MILLISECONDS);

    /**
     * Shutdown hook to ensure that process are killed even if afterClass is not run.
     */
    private static KillAllProcessShutdownHook shutdownHook;

    protected RMTHelper rmHelper;

    static {
        try {
            new ProcessCleaner(".*proactive.test=true.*").killAliveProcesses();
            new ProcessCleaner(".*RMNodeStarter.*").killAliveProcesses();
            new ProcessCleaner(".*RMStarterForFunctionalTest.*").killAliveProcesses();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_TEST.setValue(true);
        CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);

        boolean isConsecutive = shouldBeExecutedInConsecutiveMode(this.getClass());

        rmHelper = RMTHelper.getDefaultInstance();

        if (isConsecutive) {
            logger.info("Running test in 'consecutive' mode");
            rmHelper.reset();
        } else {
            logger.info("Running test in 'clean environment' mode");
            killAliveProcesses();
            rmHelper.killRM();
        }

        if (shutdownHook != null) {
            shutdownHook = new KillAllProcessShutdownHook(); // TODO add it only once
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }

    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            URL defaultLog4jConfig = RMFunctionalTest.class.getResource("/log4j-junit");
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig.toString());
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

    protected boolean shouldBeExecutedInConsecutiveMode(Class<?> cls) {
        // class must have an annotation Consecutive
        if (cls.isAnnotationPresent(Consecutive.class)) {
            return true;
        } else if (cls.getSuperclass() != null) {
            return shouldBeExecutedInConsecutiveMode(cls.getSuperclass());
        }
        return false;
    }

    @After
    public void killAllProcessesIfNeeded() throws Exception {
        if (!shouldBeExecutedInConsecutiveMode(this.getClass())) {
            rmHelper.killRM();
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

    private class KillAllProcessShutdownHook extends Thread {

        private RMTHelper rm = RMFunctionalTest.this.rmHelper;

        @Override
        public void run() {
            System.err.println("Shutdown hook. Killing remaining processes");
            try {
                rm.killRM();
                killAliveProcesses();
            } catch (Exception e) {
                logger.error("Failed to kill remaining processes", e);
            }
        }
    }

}
