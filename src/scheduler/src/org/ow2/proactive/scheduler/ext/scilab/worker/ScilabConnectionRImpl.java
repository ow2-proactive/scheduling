package org.ow2.proactive.scheduler.ext.scilab.worker;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabInitException;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabTaskException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * ScilabConnectionRImpl
 *
 * @author The ProActive Team
 */
public class ScilabConnectionRImpl implements ScilabConnection {
    protected StringBuilder fullcommand = new StringBuilder();
    protected String nl = System.getProperty("line.separator");

    protected final String tmpDir = System.getProperty("java.io.tmpdir");

    protected String nodeName;

    protected String[] startUpOptions;
    protected String scilabLocation;
    protected File workingDirectory;

    protected static final int TIMEOUT_START = 6000;

    protected File logFile;
    protected boolean debug;

    protected File mainFuncFile;

    protected Process process;

    protected OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static final String startPattern = "---- SCILAB START ----";

    private PrintStream outDebug;

    PASolveScilabGlobalConfig paconfig;

    PASolveScilabTaskConfig tconfig;

    public ScilabConnectionRImpl() {

    }

    public void acquire(String scilabExecutablePath, File workingDir, PASolveScilabGlobalConfig paconfig,
            PASolveScilabTaskConfig tconfig) throws ScilabInitException {
        this.scilabLocation = scilabExecutablePath;
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
        logFile = new File(tmpDir, "ScilabStart" + nodeName + ".log");
        mainFuncFile = new File(workingDir, "PAMain.sce");
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
        fullcommand.append("disp('" + startPattern + "');" + nl);
        fullcommand.append("try" + nl);
    }

    public void release() {
        if (process != null) {
            try {
                process.destroy();
            } catch (Exception e) {

            }
        }
    }

    public void evalString(String command) throws ScilabTaskException {
        fullcommand.append(command + nl);
    }

    public Object get(String variableName) throws ScilabTaskException {
        throw new UnsupportedOperationException();
    }

    public void put(String variableName, Object value) throws ScilabTaskException {
        throw new UnsupportedOperationException();
    }

    public void launch() throws Exception {
        fullcommand
                .append("catch" +
                    nl +
                    "[str2,n2,line2,func2]=lasterror(%t);printf('!-- error %i\\n%s\\n at line %i of function %s\\n',n2,str2,line2,func2)" +
                    nl + "errclear();" + nl + "end" + nl + "exit();");
        PrintStream out = null;
        try {
            out = new PrintStream(new BufferedOutputStream(new FileOutputStream(mainFuncFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println(fullcommand);
        out.flush();
        out.close();

        process = createScilabProcess("PAMain.sce");

        IOTools.LoggingThread lt1;
        if (debug) {
            lt1 = new IOTools.LoggingThread(process, "[SCILAB]", System.out, System.err, outDebug, null,
                null, null);

        } else {
            lt1 = new IOTools.LoggingThread(process, "[SCILAB]", System.out, System.err, startPattern, null,
                null);
        }
        Thread t1 = new Thread(lt1, "OUT SCILAB");
        t1.setDaemon(true);
        t1.start();

        int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new ScilabInitException("Scilab process exited with code : " + exitValue);
        }

    }

    protected Process createScilabProcess(String runArg) throws Exception {
        // Attempt to run SCILAB
        final ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(this.scilabLocation);
        commandList.addAll(Arrays.asList(this.startUpOptions));
        // TODO find a way to use a log file
        //commandList.add("-logfile");
        //commandList.add(logFile.toString());
        commandList.add("-f");
        commandList.add(runArg);

        String[] command = (String[]) commandList.toArray(new String[commandList.size()]);

        ProcessBuilder b = new ProcessBuilder();
        // invalid on windows ?
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
        File logFile = new File(tmpPath, "ScilabExecutable_" + nodeName + ".log");
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
