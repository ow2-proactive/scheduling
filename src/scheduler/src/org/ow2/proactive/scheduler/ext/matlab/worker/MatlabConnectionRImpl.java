package org.ow2.proactive.scheduler.ext.matlab.worker;

import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.UnsufficientLicencesException;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * MatlabConnectionRImpl
 *
 * @author The ProActive Team
 */
public class MatlabConnectionRImpl implements MatlabConnection {

    protected StringBuilder fullcommand = new StringBuilder();
    protected String nl = System.getProperty("line.separator");

    protected final String tmpDir = System.getProperty("java.io.tmpdir");

    protected String nodeName;

    protected String[] startUpOptions;
    protected String matlabLocation;
    protected File workingDirectory;

    protected static final int TIMEOUT_START = 6000;

    protected File logFile;
    protected boolean debug;

    protected File mainFuncFile;

    protected Process process;

    private static final String startPattern = "---- MATLAB START ----";

    private PrintStream outDebug;

    public MatlabConnectionRImpl() {

    }

    public void acquire(String matlabExecutablePath, File workingDir, boolean debug, String[] startupOptions)
            throws MatlabInitException {
        this.matlabLocation = matlabExecutablePath;
        this.workingDirectory = workingDir;
        this.debug = debug;
        this.startUpOptions = startupOptions;
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

    }

    public void init() {
        fullcommand.append("function PAmain()" + nl);
        fullcommand.append("disp('" + startPattern + "');");
        fullcommand.append("try" + nl);
    }

    public void release() {

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
            e.printStackTrace();
        }
        out.println(fullcommand);
        out.flush();
        out.close();

        process = createMatlabProcess("PAMain();");

        IOTools.LoggingThread lt1;
        IOTools.LoggingThread lt2;
        if (debug) {
            lt1 = new IOTools.LoggingThread(process.getInputStream(), "[MATLAB OUT]", System.out, outDebug);
            lt2 = new IOTools.LoggingThread(process.getErrorStream(), "[MATLAB ERR]", System.err, outDebug);

        } else {
            lt1 = new IOTools.LoggingThread(process.getInputStream(), "[MATLAB OUT]", System.out,
                startPattern, null);
            lt2 = new IOTools.LoggingThread(process.getErrorStream(), "[MATLAB ERR]", System.err,
                startPattern, null);
        }
        Thread t1 = new Thread(lt1, "OUT DS");
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(lt2, "ERR DS");
        t2.setDaemon(true);
        t2.start();

        File ackFile = new File(workingDirectory, "matlab.ack");
        File nackFile = new File(workingDirectory, "matlab.nack");
        int cpt = 0;
        while (!ackFile.exists() && !nackFile.exists() && (cpt < TIMEOUT_START)) {
            try {
                int exitValue = process.exitValue();
                throw new MatlabInitException("Matlab process exited with code : " + exitValue);
            } catch (Exception e) {
                // ok process still exists
            }
            Thread.sleep(10);
        }
        if (ackFile.exists()) {
            ackFile.delete();
        }
        // TODO do the callback to the proxy server
        if (nackFile.exists()) {

            nackFile.delete();

            throw new UnsufficientLicencesException();
        }
        if (cpt >= TIMEOUT_START) {
            throw new MatlabInitException("Timeout occured while starting Matlab");
        }

        int exitValue = process.waitFor();
        lt1.goon = false;
        lt2.goon = false;
        if (exitValue != 0) {
            throw new MatlabInitException("Matlab process exited with code : " + exitValue);
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
