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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.objectweb.proactive.core.util.OperatingSystem;


public class FunctionalTest {
    static final protected Logger logger = Logger.getLogger("testsuite");
    static final public String JVM_PARAMETERS = "-Dproactive.test=true";

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
                        // 1- Advertise the failure
                        logger.warn("FunctionalTest timeout reached !");

                        // 2- Display jstack output
                        // It can be useful to debug (live|dead)lock
                        Map<String, String> pids = getPids();

                        for (String pid : pids.keySet()) {
                            System.err.println("PID: " + pid + " Command: " +
                                pids.get(pid));
                            System.err.println();

                            try {
                                Process p = Runtime.getRuntime()
                                                   .exec(getJSTACKCommand() +
                                        " " + pid);
                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                            p.getInputStream()));

                                for (String line = br.readLine(); line != null;
                                        line = br.readLine()) {
                                    System.err.println("\t" + line);
                                }

                                System.err.println();
                                System.err.println(
                                    "---------------------------------------------------------------");
                                System.err.println();
                                System.err.println();
                                System.err.flush();
                            } catch (IOException e) {
                                // Should not happen
                                e.printStackTrace();
                            }
                        }

                        // 3- That's all
                        // Shutdown hook will be triggered to kill all remaining ProActive Runtimes
                        System.exit(0);
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

    /**
     * Returns a map<PID, CommandLine> of ProActive JVMs
     * @return
     */
    static public Map<String, String> getPids() {
        HashMap<String, String> pids = new HashMap<String, String>();

        try {
            // Run JPS to list all JVMs on this machine
            Process p = Runtime.getRuntime().exec(getJPSCommand() + " -ml");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));

            for (String line = br.readLine(); line != null;
                    line = br.readLine()) {
                // Discard all non ProActive JVM
                if (line.contains("org.objectweb.proactive")) {
                    String[] fields = line.split(" ", 2);
                    pids.put(fields[0], fields[1]);
                }
            }
        } catch (IOException e) {
            // Should not happen
            e.printStackTrace();
        }
        return pids;
    }

    /**
     * Returns the command to start the jps util
     *
     * @return command to start jps
     */
    static public String getJPSCommand() {
        return getJavaBinDir() + "jps";
    }

    /**
     * Returns the command to start the jstack util
     *
     * @return command to start jstack
     */
    static public String getJSTACKCommand() {
        return getJavaBinDir() + "jstack";
    }

    /**
     * Returns the Java bin dir
     *
     * @return Java bin/ dir
     */
    static public String getJavaBinDir() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("java.home"));
        sb.append(File.separatorChar);
        sb.append("..");
        sb.append(File.separatorChar);
        sb.append("bin");
        sb.append(File.separatorChar);
        return sb.toString();
    }
}
