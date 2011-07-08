package org.ow2.proactive.scheduler.ext.scilab.worker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.FileUtils;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabTaskException;
import org.ow2.proactive.scheduler.ext.scilab.worker.util.ScilabEngineConfig;
import org.ow2.proactive.scheduler.ext.scilab.worker.util.ScilabFinder;


/**
 * ScilabExecutable
 *
 * @author The ProActive Team
 */
public class ScilabExecutable extends JavaExecutable {
    /** The ISO8601 for debug format of the date that precedes the log message */
    private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

    private static String HOSTNAME;

    private static OperatingSystem os;

    private static char fs;

    static {
        try {
            HOSTNAME = java.net.InetAddress.getLocalHost().getHostName();
            os = OperatingSystem.getOperatingSystem();
            fs = os.fileSeparator();
        } catch (UnknownHostException e) {
        }
    }

    /** For debug purpose see {@link ScilabExecutable#createLogFileOnDebug()} */
    private PrintWriter outDebugWriter;

    /** The global configuration */
    private PASolveScilabGlobalConfig paconfig;

    /** The task configuration */
    private PASolveScilabTaskConfig taskconfig;

    /** The SCILAB configuration */
    private ScilabEngineConfig scilabEngineConfig;

    /** The root of the local space and a temporary dir inside */
    private File localSpaceRootDir, tempSubDir;

    /** The connection to SCILAB from scilabcontrol API */
    private ScilabConnection scilabConnection;

    /** The SCILAB script to execute */
    private String script;

    public ScilabExecutable() {
        this.paconfig = new PASolveScilabGlobalConfig();
        this.taskconfig = new PASolveScilabTaskConfig();
    }

    @Override
    public void init(final Map<String, Serializable> args) throws Exception {
        Object obj;

        // Read global configuration
        obj = args.get("global_config");
        if (obj != null) {
            this.paconfig = (PASolveScilabGlobalConfig) obj;
        }

        // Create a log file if debug is enabled
        this.createLogFileOnDebug();

        // Read task configuration
        obj = args.get("task_config");
        if (obj != null) {
            this.taskconfig = (PASolveScilabTaskConfig) obj;
        }

        // Read the main script to execute
        this.script = (String) args.get("script");
        if (this.script == null || "".equals(this.script)) {
            throw new java.lang.IllegalArgumentException("Unable to execute task, no script specified");
        }

        // Initialize SCILAB location
        this.initScilabConfig();

        // Initialize LOCAL SPACE
        this.initLocalSpace();

        // Initialize transfers
        this.initTransferSource();
        this.initTransferEnv();
        this.initTransferInputFiles();
        this.initTransferVariables();
    }

    @Override
    public Serializable execute(final TaskResult... results) throws Throwable {
        if (results != null) {
            for (TaskResult res : results) {
                if (res.hadException()) {
                    throw res.getException();
                }
            }
        }

        final String scilabCmd = this.scilabEngineConfig.getFullCommand();
        this.printLog("Acquiring SCILAB connection using " + scilabCmd);

        // Acquire a connection to SCILAB

        this.scilabConnection = new ScilabConnectionRImpl();

        scilabConnection.acquire(scilabCmd, this.localSpaceRootDir, this.paconfig, this.taskconfig);

        Serializable result = null;

        try {
            // Execute the SCILAB script and receive the result
            result = this.executeScript();
        } finally {
            this.printLog("Closing SCILAB...");
            this.scilabConnection.release();
        }

        printLog("Task completed successfully");

        return result;
    }

    @Override
    public void kill() {
        if (this.scilabConnection != null) {
            // Release the connection
            this.scilabConnection.release();
            this.scilabConnection = null;
        }

        // The upper-class method will set this executable as killed
        super.kill();
    }

    /**
     * Executes both input and main scripts on the engine
     *
     * @throws Throwable
     */
    protected final Serializable executeScript() throws Throwable {

        // Add sources, load workspace and input variables
        scilabConnection.init();

        this.addSources();
        this.loadWorkspace();
        this.loadInputVariables();

        printLog("Running SCILAB command: " + this.script);

        scilabConnection.evalString(this.script);

        printLog("SCILAB command completed successfully, receiving output... ");

        storeOutputVariable();

        // outputFiles
        transferOutputFiles();

        scilabConnection.launch();

        testOutput();

        return new Boolean(true);
    }

    /*********** PRIVATE METHODS ***********/

    protected MatSciEngineConfig initScilabConfig() throws Exception {
        ScilabEngineConfig conf = (ScilabEngineConfig) ScilabEngineConfig.getCurrentConfiguration();
        if (conf == null) {
            conf = (ScilabEngineConfig) ScilabFinder.getInstance().findMatSci(paconfig.getVersionPref(),
                    paconfig.getVersionRej(), paconfig.getVersionMin(), paconfig.getVersionMax());
            if (conf == null) {
                throw new IllegalStateException("No valid Scilab configuration found, aborting...");
            }

        }
        scilabEngineConfig = conf;
        return scilabEngineConfig;
    }

    private void initLocalSpace() throws Exception {
        final DataSpacesFileObject dsLocalSpace = this.getLocalSpace();
        final String dsURI = dsLocalSpace.getRealURI();

        if (!dsLocalSpace.exists()) {
            throw new IllegalStateException("Unable to execute task, the local space " + dsURI +
                " doesn't exists");
        }
        if (!dsLocalSpace.isReadable()) {
            throw new IllegalStateException("Unable to execute task, the local space " + dsURI +
                " is not readable");
        }
        if (!dsLocalSpace.isWritable()) {
            throw new IllegalStateException("Unable to execute task, the local space " + dsURI +
                " is not writable");
        }

        // Create a temp dir in the root dir of the local space
        this.localSpaceRootDir = new File(new URI(dsURI));
        this.tempSubDir = new File(this.localSpaceRootDir, this.paconfig.getTempSubDirName());

        // Set the local space of the global configuration
        this.paconfig.setLocalSpace(new URI(dsURI));
    }

    private void initTransferSource() throws Exception {
        if (paconfig.isZipSourceFiles()) {
            String sourceZipFileName = taskconfig.getSourceZipFileName();
            if (sourceZipFileName == null) {
                sourceZipFileName = paconfig.getSourceZipFileName();
            }
            taskconfig.setSourceZipFileURI(new URI(getLocalFile(
                    paconfig.getTempSubDirName() + "/" + sourceZipFileName).getRealURI()));

            File sourceZip = new File(taskconfig.getSourceZipFileURI());

            printLog("Unzipping source files from " + sourceZip);

            if (!sourceZip.exists() || !sourceZip.canRead()) {
                System.err.println("Error, source zip file cannot be accessed at " + sourceZip);
                throw new IllegalStateException("Error, source zip file cannot be accessed at " + sourceZip);
            }

            // Uncompress the source files into the temp dir
            if (!FileUtils.unzip(sourceZip, tempSubDir)) {
                System.err.println("Unable to unzip source file " + sourceZip);
                throw new IllegalStateException("Unable to unzip source file " + sourceZip);
            }
        }

        if (paconfig.isDebug()) {
            printLog("Contents of " + tempSubDir);
            for (File f : tempSubDir.listFiles()) {
                printLog(f.getName());
            }
        }
    }

    private void initTransferEnv() throws Exception {
        if (!paconfig.isTransferEnv()) {
            return;
        }

        taskconfig.setEnvMatFileURI(new URI(getLocalFile(
                paconfig.getTempSubDirName() + "/" + paconfig.getEnvMatFileName()).getRealURI()));

    }

    private void initTransferInputFiles() throws Exception {
        if (taskconfig.isInputFilesThere() && paconfig.isZipInputFiles()) {
            int n = taskconfig.getInputFilesZipNames().length;
            URI[] uris = new URI[n];
            for (int i = 0; i < n; i++) {
                uris[i] = new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + taskconfig.getInputFilesZipNames()[i])
                        .getRealURI());
            }
            taskconfig.setInputZipFilesURI(uris);

            printLog("Unzipping input files...");

            for (URI uri : taskconfig.getInputZipFilesURI()) {
                File inputZip = new File(uri);
                if (!inputZip.exists()) {
                    System.err.println("Error, input zip file cannot be accessed at : " + inputZip);
                    throw new IllegalStateException("Error, input zip file cannot be accessed at : " +
                        inputZip);
                }
                // Uncompress the input file
                if (!FileUtils.unzip(inputZip, localSpaceRootDir)) {
                    System.err.println("Unable to unzip the input file " + inputZip);
                    throw new IllegalStateException("Unable to the input file " + inputZip);
                }
            }
        }
    }

    private void initTransferVariables() throws Exception {

        taskconfig.setInputVariablesFileURI(new URI(getLocalFile(
                paconfig.getTempSubDirName() + "/" + taskconfig.getInputVariablesFileName()).getRealURI()));

        if (taskconfig.getComposedInputVariablesFileName() != null) {
            taskconfig.setComposedInputVariablesFileURI(new URI(getLocalFile(
                    paconfig.getTempSubDirName() + "/" + taskconfig.getComposedInputVariablesFileName())
                    .getRealURI()));
        }

    }

    private void addSources() throws Exception {
        if (tempSubDir != null) {
            printLog("Adding to scilabpath sources from " + tempSubDir);
            // Add unzipped source files to the MATALAB path
            scilabConnection.evalString("try;getd('" + tempSubDir + "');catch; end;");
            if (taskconfig.getFunctionVarFiles() != null) {
                for (String fileName : taskconfig.getFunctionVarFiles()) {
                    scilabConnection.evalString("load('" + this.tempSubDir + fs + fileName + "');");
                }
            }
        }
    }

    private void loadWorkspace() throws Exception {
        if (paconfig.isTransferEnv()) {
            File envMat = new File(taskconfig.getEnvMatFileURI());
            printLog("Loading workspace from " + envMat);
            // Load workspace using SCILAB command
            scilabConnection.evalString("load('" + envMat + "');");
        }
    }

    private void loadInputVariables() throws Exception {

        File inMat = new File(taskconfig.getInputVariablesFileURI());

        printLog("Loading input variables from " + inMat);

        scilabConnection.evalString("load('" + inMat + "');");
        if (taskconfig.getComposedInputVariablesFileURI() != null) {
            File compinMat = new File(taskconfig.getComposedInputVariablesFileURI());
            scilabConnection.evalString("load('" + compinMat + "');in=out;clear out;");
        }

    }

    private void storeOutputVariable() throws Exception {
        File outputFile = new File(tempSubDir, taskconfig.getOutputVariablesFileName());

        printLog("Storing 'out' variable into " + outputFile);

        scilabConnection.evalString("save('" + outputFile + "',out);");

        //if (!outputFile.exists()) {
        //    throw new ScilabTaskException("Unable to store 'out' variable, the output file does not exist");
        //}
    }

    private void transferOutputFiles() throws Exception {
        if (taskconfig.isOutputFilesThere() && paconfig.isZipOutputFiles()) {

            printLog("Zipping output files...");

            String[] outputFiles = taskconfig.getOutputFiles();
            String[] names = taskconfig.getOutputFilesZipNames();

            for (int i = 0; i < names.length; i++) {
                File outputZip = new File(tempSubDir, names[i]);
                //conn.evalString("ProActiveOutputFiles=cell(1,"+outputFiles.length+");");
                //for (int i=0; i < outputFiles.length; i++) {
                String updatedFile = outputFiles[i].replaceAll("/", File.separator);
                //conn.evalString("ProActiveOutputFiles{"+(i+1)+"}='"+updatedFile+"';");
                //}
                FileUtils.zip(outputZip, new File[] { new File(updatedFile) });
            }

        }
    }

    private void testOutput() throws Exception {
        printLog("Receiving and testing output...");

        File outputFile = new File(tempSubDir, taskconfig.getOutputVariablesFileName());

        if (!outputFile.exists()) {
            throw new ScilabTaskException("Cannot find output variable file.");
        }

    }

    private void printLog(final String message) {
        if (!this.paconfig.isDebug()) {
            return;
        }
        final Date d = new Date();
        final String log = "[" + ISO8601FORMAT.format(d) + " " + HOSTNAME + "] " + message;
        System.out.println(log);
        System.out.flush();
        if (this.outDebugWriter != null) {
            this.outDebugWriter.println(log);
            this.outDebugWriter.flush();
        }

    }

    /** Creates a log file in the java.io.tmpdir if debug is enabled */
    private void createLogFileOnDebug() throws Exception {
        if (!this.paconfig.isDebug()) {
            return;
        }
        String nodeName = MatSciEngineConfigBase.getNodeName();

        String tmpPath = System.getProperty("java.io.tmpdir");
        // system temp dir (not using java.io.tmpdir since in runasme it can be
        // inaccesible and the scratchdir property can inherited from parent jvm)
        //        if (this.paconfig.isRunAsMe()) {
        //            tmpPath = System.getProperty("node.dataspace.scratchdir");
        //        }

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
            FileWriter outFile = new FileWriter(logFile);
            PrintWriter out = new PrintWriter(outFile);

            outDebugWriter = out;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
