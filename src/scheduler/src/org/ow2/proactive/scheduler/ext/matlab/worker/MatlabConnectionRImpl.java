package org.ow2.proactive.scheduler.ext.matlab.worker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.UnreachableLicenseProxyException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.UnsufficientLicencesException;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;

import com.activeeon.proactive.license_saver.client.LicenseSaverClient;


/**
 * MatlabConnectionRImpl
 *
 * @author The ProActive Team
 */
public class MatlabConnectionRImpl implements MatlabConnection {

    /**
     * Full Matlab code which will be executed by the Matlab process
     */
    protected StringBuilder fullcommand = new StringBuilder();

    /**
     * File used to store matlab code to be executed
     */
    protected File mainFuncFile;

    /**
     * Internals
     */
    protected String nl = System.getProperty("line.separator");

    protected final String tmpDir = System.getProperty("java.io.tmpdir");

    protected String nodeName;

    protected OperatingSystem os = OperatingSystem.getOperatingSystem();

    /**
     * Startup Options of the Matlab process
     */
    protected String[] startUpOptions;

    /**
     * Location of the Matlab process
     */
    protected String matlabLocation;

    /**
     * Directory where the matlab process should start (Localspace)
     */
    protected File workingDirectory;

    /**
     * Timeout for the matlab process startup x 10 ms
     */
    protected static final int TIMEOUT_START = 6000;

    /**
     * File used to capture matlab process output (in addition to Threads)
     */
    protected File logFile;

    /**
     * Debug mode
     */
    protected boolean debug;

    /**
     * Matlab Process
     */
    protected Process process;

    /**
     * lock used to prevent process destroy while starting up
     */
    protected Boolean running = false;

    /**
     * Pattern used to remove Matlab startup message from logs
     */
    private static final String startPattern = "---- MATLAB START ----";

    /**
     * Stream used to
     */
    private PrintStream outDebug;

    /**
     * Matlab configuration of the current job
     */
    PASolveMatlabGlobalConfig paconfig;

    /**
     * Matlab configuration of the current task
     */
    PASolveMatlabTaskConfig tconfig;

    /**
     * Licensing Proxy Server Client
     */
    private LicenseSaverClient lclient;

    public MatlabConnectionRImpl() {

    }

    public void acquire(String matlabExecutablePath, File workingDir, PASolveMatlabGlobalConfig paconfig,
            PASolveMatlabTaskConfig tconfig) throws MatlabInitException {
        this.matlabLocation = matlabExecutablePath;
        this.workingDirectory = workingDir;
        this.debug = paconfig.isDebug();
        this.paconfig = paconfig;
        this.tconfig = tconfig;
        if (os == OperatingSystem.windows) {
            this.startUpOptions = paconfig.getWindowsStartupOptions();
        } else {
            this.startUpOptions = paconfig.getLinuxStartupOptions();
        }

        try {
            this.nodeName = MatSciEngineConfigBase.getNodeName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logFile = new File(tmpDir, "MatlabStart" + nodeName + ".log");
        mainFuncFile = new File(workingDir, "PAMain.m");
        if (!mainFuncFile.exists()) {
            try {
                mainFuncFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            createLogFileOnDebug();
        } catch (Exception e) {
            e.printStackTrace();
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

        Process p = b.start();

        return p;

    }

    private void createLogFileOnDebug() throws Exception {
        if (!this.debug) {
            return;
        }
        String nodeName = MatSciEngineConfigBase.getNodeName();

        String tmpPath = System.getProperty("java.io.tmpdir");

        // log file writer used for debugging
        File tmpDirFile = new File(tmpPath);
        File nodeTmpDir = new File(tmpDirFile, nodeName);
        if (!nodeTmpDir.exists()) {
            nodeTmpDir.mkdirs();
        }
        File logFile = new File(tmpPath, "MatlabExecutable_" + nodeName + ".log");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        try {
            FileOutputStream outFile = new FileOutputStream(logFile);
            PrintStream out = new PrintStream(outFile);

            outDebug = out;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
