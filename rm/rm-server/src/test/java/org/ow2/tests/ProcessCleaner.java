/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.utils.OperatingSystem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;


/**
 * Kill all Java processes matching a pattern
 *
 *
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public class ProcessCleaner {
    final private Pattern pattern;

    public ProcessCleaner(String regex) {
        this(Pattern.compile(regex));
    }

    public ProcessCleaner(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Get PID of all alive Java processes matching the pattern.
     *
     * @return
     *    PIDs
     * @throws IOException
     *    If PIDs cannot be retrieved
     */
    final public Set<Integer> getAliveProcesses() throws IOException {
        return getAliveProcesses(false);
    }

    final public Set<Integer> getAliveProcesses(boolean printProcesses) throws IOException {
        // JPS is only available on some JDK
        // We need a fall back in case jps does not exist.
        try {
            getJavaTool("jps");
            Set<Integer> pids = getAliveWithJps(printProcesses);
            pids.addAll(getAliveWithSigar(printProcesses));
            return pids;
        } catch (FileNotFoundException e) {
            return getAliveWithNative();
        }
    }

    private Set<Integer> getAliveWithSigar(boolean printProcesses) {
        Sigar sigar = new Sigar();
        try {
            long[] pids = new ProcessFinder(sigar).find("State.Name.eq=java,Args.*.re=" + pattern);
            Set<Integer> setOfPids = new HashSet<Integer>();
            for (long pid : pids) {
                setOfPids.add((int) pid);
                if (printProcesses)
                    System.out.println("Running java processes matching regex:" + pid);
            }
            return setOfPids;
        } catch (SigarException e) {
            return Collections.emptySet();
        }
    }

    private Set<Integer> getAliveWithJps(boolean printProcesses) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(getJavaTool("jps").getAbsolutePath(), "-mlv");
        pb.redirectErrorStream(true);
        Process p = pb.start();

        Set<Integer> pids= new HashSet<Integer>();

        Reader r = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(r);

        String line;
        while ((line = br.readLine()) != null) {
            Matcher m = this.pattern.matcher(line);
            if (m.matches()) {
                if (printProcesses)
                    System.out.println("Running java processes matching regex:" + line);
                String pid = line.substring(0, line.indexOf(" "));
                pids.add(Integer.parseInt(pid));
            }
        }
        return pids;
    }

    private Set<Integer> getAliveWithNative() throws IOException {
        OperatingSystem os = OperatingSystem.getOperatingSystem();
        switch (os) {
            case unix:

                ProcessBuilder pb;

                // Mac Os
                String osName = System.getProperty("os.name").toLowerCase();

                if (osName.contains("mac")) {
                    // Mac OS / Mac Os X
                    pb = new ProcessBuilder("/bin/sh", "-c",
                      "for PID in $(ps axc|awk \"{if (\\$5==\\\"java\\\") print \\$1}\") ; do ps $PID | grep -q -- '" + this.pattern.toString() +
                        "' && echo $PID ; done");
                } else {
                    // Linux / Unix
                    pb = new ProcessBuilder("/bin/sh", "-c", "for PID in $(pidof java) ; do grep -q -- '" +
                      this.pattern.toString() + "' /proc/$PID/cmdline && echo $PID ; done");
                }

                pb.redirectErrorStream(true);
                Process p = pb.start();

                Set<Integer> pids = new HashSet<Integer>();

                Reader r = new InputStreamReader(p.getInputStream());
                BufferedReader br = new BufferedReader(r);

                String line;
                while ((line = br.readLine()) != null) {
                    pids.add(Integer.parseInt(line));
                }

                return pids;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }
    }

    /**
     * Kill all alive Java processes matching the pattern.
     *
     * @throws IOException
     *    If some processes cannot be killed
     */
    final public void killAliveProcesses() throws IOException {
        ProcessKiller pk = ProcessKiller.get();


        Set<Integer> pids = this.getAliveProcesses();
        for (int pid : pids) {
            try {
                printStackTrace(pid);
                pk.kill(pid);
                System.err.println("Killed process " + pid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Set<Integer> pidsLeft = getAliveProcesses(true);
        if (!pidsLeft.isEmpty()) {
            System.err.println("Processes left running: " + pidsLeft);
        }
    }

    private void printStackTrace(int pid) {
        try {
            ProcessBuilder pb = new ProcessBuilder(getJavaTool("jstack").getAbsolutePath(), "" + pid);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            Reader r = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(r);

            String line;
            while ((line = br.readLine()) != null) {
                System.err.println(line);
            }

            br.close();
            r.close();
        } catch (Exception e) {
            System.err.println("Cannot print stack trace of the process with pid " + pid);
        }
    }

    /**
     * Returns the command to start the jps util
     *
     * @return command to start jps
     */
    static private File getJavaTool(String name) throws FileNotFoundException {
        final String osSpecificName;
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                osSpecificName = name;
                break;
            case windows:
                osSpecificName = name+".exe";
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        File ret = new File(getJavaBinDir(), osSpecificName);
        if (!ret.exists()) {
            throw new FileNotFoundException(name + " not found: " + ret + " does not exist");
        }

        return ret;
    }

    /**
     * Returns the Java bin dir
     *
     * @return Java bin/ dir
     */
    static private File getJavaBinDir() {
        return new File(System.getProperty(
          "java.home") + File.separatorChar + ".." + File.separatorChar + "bin" + File.separatorChar);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Killing all processes related to the test execution");
        new ProcessCleaner(".*proactive.test=true.*").killAliveProcesses();
    }
}
