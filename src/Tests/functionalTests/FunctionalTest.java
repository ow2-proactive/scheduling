/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.objectweb.proactive.core.util.OperatingSystem;


public class FunctionalTest {
    static final protected Logger logger = Logger.getLogger("testsuite");

    /** The amount of time given to a test to success or fail */
    static final private long TIMEOUT = 300000;

    /** A shutdown hook to kill all forked JVMs when exiting the main JVM */
    static final private Thread shutdownHook = new Thread() {
            public void run() {
                logger.trace("FunctionalTest Shutdown Hook");
                killProActive();
            }
        };

    @BeforeClass
    public static void beforeClass() {
        logger.trace("beforeClass");

        logger.trace("Killing remaining ProActive Runtime");
        killProActive();

        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IllegalArgumentException e) {
            logger.trace("shutdownHook already registered");
        }

        long timeout = TIMEOUT;
        String ptt = System.getProperty("proactive.test.timeout");
        if (ptt != null) {
            timeout = Long.parseLong(ptt);
        }

        if (timeout != 0) {
            logger.trace("Timer will be fired in " + timeout + " ms");
            TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        logger.warn("FunctionalTest timeout reached !");
                        killProActive();
                        System.err.println("Timeout reached, aborting !");
                        System.exit(12);
                    }
                };

            Timer timer = new Timer(true);
            timer.schedule(timerTask, timeout);
        } else {
            logger.trace("Timeout disabled");
        }
    }

    @AfterClass
    public static void afterClass() {
        logger.trace("afterClass");

        logger.trace("Removing the shutdownHook");
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        logger.trace("Killing remaining ProActive Runtime");
        killProActive();
    }

    /**
     * Kill all ProActive runtimes
     */
    public static void killProActive() {
        File dir = new File(System.getProperty("proactive.dir"));
        File cmd = new File(dir + "/dev/scripts/killTests");
        if (cmd.exists()) {
            try {
                Process p = null;

                switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    p = Runtime.getRuntime()
                               .exec(new String[] { cmd.getAbsolutePath() },
                            null, dir);
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.err.println("TODO: Kill JVMs on Windows also !");
                    break;
                }

                p.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(cmd + "does not exist");
        }
    }
}
