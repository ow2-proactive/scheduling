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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.utils.SafeTimerTask;


@Ignore
public class FunctionalTest {
    static {
        ProActiveConfiguration.load();
    }

    static final protected Logger logger = Logger.getLogger("testsuite");
    /** Timeout before the test gets killed. */
    static final private long timeout = CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue();
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

    @BeforeClass
    final static public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_TEST.setValue(true);

        // Ensure that the host is clean
        cleaner.killAliveProcesses();

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

    static private void threadDumpPoccesses() {
        try {
            int[] pids = cleaner.getAliveProcesses();
            for (int pid : pids) {
                System.err.println("Alive proccess: " + pid);
                System.err.println(cleaner.getThreadDump(pid));
            }
        } catch (Exception e) {
            logger.error("Failed to generate thread dump of remaining processes", e);
        }
    }

    @AfterClass
    final static public void afterClass() throws Exception {
        // Disable timer and shutdown hook
        TimerTask tt = timerTask.getAndSet(null);
        if (tt != null) {
            tt.cancel();
        }
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        // Cleanup proactive
        paSetup.shutdown();

        // Kill everything
        cleaner.killAliveProcesses();
    }

    static private class MyShutdownHook extends Thread {
        @Override
        public void run() {
            System.err.println("Shutdown hook. Killing remaining processes");
            try {
                timer.cancel();
                paSetup.shutdown();
                cleaner.killAliveProcesses();
            } catch (Exception e) {
                logger.error("Failed to kill remaining proccesses", e);
            }
        }
    }

    static private class MyTimerTask extends SafeTimerTask {
        @Override
        public void safeRun() {
            System.err.println("Timeout reached. Killing remaining processes");
            try {
                cleaner.killAliveProcesses();
            } catch (Exception e) {
                logger.error("Failed to kill remaining proccesses", e);
            }
        }
    }
}
