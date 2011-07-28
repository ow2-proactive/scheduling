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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab.worker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.UnreachableLicenseProxyException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.UnsufficientLicencesException;

import com.activeeon.proactive.license_saver.client.LicenseSaverClient;


/**
 * MatlabConnectionRImpl
 *
 * @author The ProActive Team
 */
public class MatlabConnectionRImpl implements MatlabConnection {

    /** System-dependent line separator */
    public static final String nl = System.getProperty("line.separator");

    /** Pattern used to remove Matlab startup message from logs */
    private static final String startPattern = "---- MATLAB START ----";

    /** Startup Options of the Matlab process */
    protected String[] startUpOptions;

    /** Location of the Matlab process */
    protected String matlabLocation;

    /** Full Matlab code which will be executed by the Matlab process */
    protected StringBuilder fullcommand = new StringBuilder();

    /** File used to store MATLAB code to be executed */
    protected File mainFuncFile;

    /** Directory where the MATLAB process should start (Localspace) */
    protected File workingDirectory;

    /** File used to capture MATLAB process output (in addition to Threads) */
    protected File logFile;

    /** Stream used for debug output */
    private final PrintStream outDebug;

    /** The temp directory */
    private final String tmpDir;

    /** The ProActive node name */
    private final String nodeName;

    /** Timeout for the matlab process startup x 10 ms */
    protected static final int TIMEOUT_START = 6000;

    /** Debug mode */
    protected boolean debug;

    /** MATLAB Process */
    protected Process process;

    /** Lock used to prevent process destroy while starting up */
    protected Boolean running = false;

    /** Licensing Proxy Server Client */
    private LicenseSaverClient lclient;

    /** Matlab configuration of the current job */
    PASolveMatlabGlobalConfig paconfig;

    /** Matlab configuration of the current task */
    PASolveMatlabTaskConfig tconfig;

    public MatlabConnectionRImpl(final String tmpDir, final PrintStream outDebug, final String nodeName) {
        this.tmpDir = tmpDir;
        this.outDebug = outDebug;
        this.nodeName = nodeName;
    }

    public void acquire(String matlabExecutablePath, File workingDir, PASolveMatlabGlobalConfig paconfig,
            PASolveMatlabTaskConfig tconfig) throws MatlabInitException {
        this.matlabLocation = matlabExecutablePath;
        this.workingDirectory = workingDir;
        this.debug = paconfig.isDebug();
        this.paconfig = paconfig;
        this.tconfig = tconfig;
        this.startUpOptions = paconfig.getStartupOptions();

        this.logFile = new File(tmpDir, "MatlabStart" + nodeName + ".log");
        this.mainFuncFile = new File(workingDir, "PAMain.m");
        if (!mainFuncFile.exists()) {
            try {
                mainFuncFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (paconfig.getLicenseServerUrl() != null) {
            try {
                this.lclient = new LicenseSaverClient(paconfig.getLicenseServerUrl());
            } catch (ProActiveException e) {
                throw new MatlabInitException(new UnreachableLicenseProxyException(
                    "License Proxy Server at url " + paconfig.getLicenseServerUrl() +
                        " could not be contacted.", e));
            }
        }

    }

    public void init() {
        fullcommand.append("function PAmain()" + nl);
        fullcommand.append("disp('" + startPattern + "');" + nl);
        fullcommand.append("try" + nl);
    }

    public void release() {
        synchronized (running) {
            if (process != null) {
                try {
                    process.destroy();
                    process = null;
                } catch (Exception e) {

                }
            }
            running = false;
        }
    }

    public void execCheckToolboxes(String command) {

        fullcommand.append(command);
    }

    public void evalString(String command) throws MatlabTaskException {
        fullcommand.append(command + nl);
    }

    public Object get(String variableName) throws MatlabTaskException {
        throw new UnsupportedOperationException();
    }

    public void put(String variableName, Object value) throws MatlabTaskException {
        throw new UnsupportedOperationException();
    }

    public void launch() throws Exception {
        fullcommand.append("catch ME" + nl + "disp('Error occured in .');" + nl + "disp(getReport(ME));" +
            nl + "end" + nl + "exit();");
        PrintStream out = null;

        try {
            out = new PrintStream(new BufferedOutputStream(new FileOutputStream(mainFuncFile)));
        } catch (FileNotFoundException e) {
            sendAck(false);
            throw e;
        }

        out.println(fullcommand);
        out.flush();
        out.close();

        synchronized (running) {
            process = createMatlabProcess("cd('" + this.workingDirectory + "');PAMain();");
            running = true;
        }

        IOTools.LoggingThread lt1;

        if (debug) {
            lt1 = new IOTools.LoggingThread(process, "[MATLAB]", System.out, System.err, outDebug, null,
                null, "License checkout failed");
        } else {
            lt1 = new IOTools.LoggingThread(process, "[MATLAB OUT]", System.out, System.err, startPattern,
                null, "License checkout failed");

        }
        Thread t1 = new Thread(lt1, "OUT MATLAB");
        t1.setDaemon(true);
        t1.start();

        File ackFile = new File(workingDirectory, "matlab.ack");
        File nackFile = new File(workingDirectory, "matlab.nack");
        int cpt = 0;
        while (!ackFile.exists() && !nackFile.exists() && (cpt < TIMEOUT_START) && !lt1.patternFound &&
            running) {
            try {
                int exitValue = process.exitValue();
                sendAck(false);

                // lt1.goon = false; unnecessary as matlab process exited
                throw new MatlabInitException("Matlab process exited with code : " + exitValue);
            } catch (Exception e) {
                // ok process still exists
            }
            Thread.sleep(10);
            cpt++;
        }
        if (ackFile.exists()) {
            ackFile.delete();
        }

        if (nackFile.exists()) {
            nackFile.delete();
            sendAck(false);

            lt1.goon = false;
            throw new UnsufficientLicencesException();
        }
        if (lt1.patternFound) {
            process.destroy();
            process = null;
            lt1.goon = false;
            sendAck(false);
            throw new UnsufficientLicencesException();
        }
        if (cpt >= TIMEOUT_START) {
            process.destroy();
            process = null;
            lt1.goon = false;
            sendAck(false);
            throw new MatlabInitException("Timeout occured while starting Matlab");
        }
        if (!running) {
            lt1.goon = false;
            sendAck(false);
            throw new MatlabInitException("Task killed while initialization");
        }
        sendAck(true);

        int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new MatlabInitException("Matlab process exited with code : " + exitValue +
                " after task started.");
        }

    }

    protected void sendAck(boolean ack) throws Exception {
        if (lclient != null) {
            try {
                lclient.notifyLicenseStatus(tconfig.getRid(), ack);
            } catch (Exception e) {
                throw new UnreachableLicenseProxyException(
                    "Error while sending ack to License Proxy Server at url " +
                        paconfig.getLicenseServerUrl(), e);
            }
        }
    }

    protected Process createMatlabProcess(String runArg) throws Exception {
        // Attempt to run MATLAB
        final ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(this.matlabLocation);
        commandList.addAll(Arrays.asList(this.startUpOptions));
        commandList.add("-logfile");
        commandList.add(logFile.toString());
        commandList.add("-r");
        commandList.add(runArg);

        String[] command = (String[]) commandList.toArray(new String[commandList.size()]);

        ProcessBuilder b = new ProcessBuilder();
        // invalid on windows, it affects the starter only
        b.directory(this.workingDirectory);
        b.command(command);

        // Fix for SCHEDULING-1309: If MATLAB client uses RunAsMe option the MATLAB
        // worker jvm can crash if the client user has never started any MATLAB
        // session on the worker host

        // Since the user profile can be missing on Windows with RunAsMe, by setting
        // the MATLAB_PREFDIR variable to a writable dir (can be non-writable on Windows with RunAsMe) 
        // the MATLAB doesn't crash no more

        Map<String, String> env = b.environment();

        // Transmit the prefdir as env variable
        String matlabPrefdir = System.getProperty(MatlabExecutable.MATLAB_PREFDIR);
        if (matlabPrefdir != null) {
            env.put("MATLAB_PREFDIR", matlabPrefdir);
        }
        // Transmit the tmpdir as env variable
        String matlabTmpvar = System.getProperty(MatlabExecutable.MATLAB_TASK_TMPDIR);
        if (matlabTmpvar != null) {
            env.put("TEMP", matlabTmpvar);
            env.put("TMP", matlabTmpvar);
        }

        return b.start();
    }
}
