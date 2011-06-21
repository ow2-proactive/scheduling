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

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.FileUtils;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabFinder;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;

import java.io.*;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * This class represents the executable of a MATLAB task. It's configuration is
 * composed of :
 * <ul>
 * <li>An instance of {@link PASolveMatlabGlobalConfig}: The global configuration.
 * <li>An instance of {@link PASolveMatlabTaskConfig}: The task configuration.
 * <li>An instance of {@link MatlabEngineConfig}: The task configuration.
 * </ul>
 * The incoming calls order are: the {@link MatlabExecutable#MatlabExecutable()},
 * the {@link MatlabExecutable#init(Map)} method is called, then the {@link MatlabExecutable#execute(TaskResult...)} method.
 * The {@link MatlabExecutable#init(Map)} initializes configuration and file transfer logic, then
 * once all checks are done the execute method create a connection with MATLAB.
 *
 * @author The ProActive Team
 */
public class MatlabExecutable extends JavaExecutable {

    /** The ISO8601 for debug format of the date that precedes the log message */
    private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");

    private static String HOSTNAME;

    static {
        try {
            HOSTNAME = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
    }

    /** For debug purpose see {@link MatlabExecutable#createLogFileOnDebug()} */
    private PrintWriter outDebugWriter;

    private int index = -1;

    /** The global configuration */
    private PASolveMatlabGlobalConfig paconfig;

    /** The task configuration */
    private PASolveMatlabTaskConfig taskconfig;

    /** The MATLAB configuration */
    private MatlabEngineConfig matlabEngineConfig;

    /** The root of the local space and a temporary dir inside */
    private File localSpaceRootDir, tempSubDir;

    /** The connection to MATLAB from matlabcontrol API */
    private MatlabConnection matlabConnection;

    /** The MATLAB script to execute */
    private String script;

    public MatlabExecutable() {
        this.paconfig = new PASolveMatlabGlobalConfig();
        this.taskconfig = new PASolveMatlabTaskConfig();
    }

    @Override
    public void init(final Map<String, Serializable> args) throws Exception {
        Object obj;

        // Read global configuration
        obj = args.get("global_config");
        if (obj != null) {
            this.paconfig = (PASolveMatlabGlobalConfig) obj;
        }

        // Create a log file if debug is enabled
        this.createLogFileOnDebug();

        // Read task configuration
        obj = args.get("task_config");
        if (obj != null) {
            this.taskconfig = (PASolveMatlabTaskConfig) obj;
        }

        // Read the main script to execute
        this.script = (String) args.get("script");
        if (this.script == null || "".equals(this.script)) {
            throw new java.lang.IllegalArgumentException("Unable to execute task, no script specified");
        }

        // Initialize MATLAB location
        this.initMatlabConfig();

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

        final String matlabCmd = this.matlabEngineConfig.getFullCommand();
        this.printLog("Acquiring MATLAB connection using " + matlabCmd);

        // Acquire a connection to MATLAB
        this.matlabConnection = MatlabConnection.acquire(matlabCmd, // MATLAB exe path
                this.localSpaceRootDir); // working dir

        Serializable result = null;

        try {
            // We execute the task on the worker
            if (!paconfig.isTransferVariables()) {
                if (results != null) {
                    if (results.length > 1) {
                        throw new InvalidNumberOfParametersException(results.length);
                    }

                    if (results.length == 1) {
                        TaskResult res = results[0];

                        if (index != -1) {
                            if (!(res.value() instanceof SplittedResult)) {
                                throw new InvalidParameterException(res.value().getClass());
                            }

                            SplittedResult sr = (SplittedResult) res.value();
                            Object tok = sr.getResult(index);
                            this.matlabConnection.put("in", tok);
                        } else {
                            if (!(res.value() instanceof Object)) {
                                throw new InvalidParameterException(res.value().getClass());
                            }

                            Object in = (Object) res.value();
                            this.matlabConnection.put("in", in);
                        }
                    }
                }
            }
            // Execute the MATLAB script and receive the result
            result = this.executeScript();
        } finally {
            this.printLog("Closing MATLAB...");

            this.matlabConnection.release();
        }

        printLog("Task completed successfully");

        return result;
    }

    @Override
    public void kill() {
        if (this.matlabConnection != null) {
            // Release the connection
            this.matlabConnection.release();
            this.matlabConnection = null;
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
        this.addSources();
        this.loadWorkspace();
        this.loadInputVariables();

        if (paconfig.isDebug()) {
            matlabConnection.evalString("who");
        }

        printLog("Running MATLAB command: " + this.script);

        matlabConnection.evalString(this.script);

        printLog("MATLAB command completed successfully, receiving output... ");

        testOutput();

        Object out = null;

        if (paconfig.isTransferVariables()) {
            out = storeOutputVariable();
        } else {
            out = matlabConnection.get("out");
        }

        printLog(out.toString());

        // outputFiles
        transferOutputFiles();

        return (Serializable) out;
    }

    /*********** PRIVATE METHODS ***********/

    protected MatSciEngineConfig initMatlabConfig() throws Exception {
        MatlabEngineConfig conf = (MatlabEngineConfig) MatlabEngineConfig.getCurrentConfiguration();
        if (conf == null) {
            conf = (MatlabEngineConfig) MatlabFinder.getInstance().findMatSci(paconfig.getVersionPref(),
                    paconfig.getVersionRej(), paconfig.getVersionMin(), paconfig.getVersionMax());
            if (conf == null) {
                throw new IllegalStateException("No valid Matlab configuration found, aborting...");
            }

        }
        matlabEngineConfig = conf;
        return matlabEngineConfig;
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
        // The sources are ALWAYS transfered and zipped
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
        if (paconfig.isZipEnvFile()) {
            taskconfig.setEnvZipFileURI(new URI(getLocalFile(
                    paconfig.getTempSubDirName() + "/" + paconfig.getEnvZipFileName()).getRealURI()));

            printLog("Unzipping workspace file...");

            File envZip = new File(taskconfig.getEnvZipFileURI());
            if (!envZip.exists()) {
                System.err.println("Error, workspace zip file cannot be accessed at " + envZip);
                throw new IllegalStateException("Error, workspace zip file cannot be accessed at " + envZip);
            }
            File envZipFolder = envZip.getParentFile();
            // Uncompress the workspace
            if (!FileUtils.unzip(envZip, envZipFolder)) {
                System.err.println("Unable to unzip the workspace file " + envZip);
                throw new IllegalStateException("Unable to the workspace file " + envZip);
            }
        } else {
            taskconfig.setEnvMatFileURI(new URI(getLocalFile(
                    paconfig.getTempSubDirName() + "/" + paconfig.getEnvMatFileName()).getRealURI()));
        }
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
        if (paconfig.isTransferVariables()) {
            taskconfig
                    .setInputVariablesFileURI(new URI(getLocalFile(
                            paconfig.getTempSubDirName() + "/" + taskconfig.getInputVariablesFileName())
                            .getRealURI()));

            // TODO: Remove the following ??... see fviale
            if (taskconfig.getComposedInputVariablesFileName() != null) {
                taskconfig.setComposedInputVariablesFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + taskconfig.getComposedInputVariablesFileName())
                        .getRealURI()));
            }
        }
    }

    private void addSources() throws Exception {
        if (paconfig.isTransferSource() && tempSubDir != null) {
            printLog("Adding to matlabpath sources from " + tempSubDir);
            // Add unzipped source files to the MATALAB path
            matlabConnection.evalString("addpath('" + tempSubDir + "');");
        }
    }

    private void loadWorkspace() throws Exception {
        if (paconfig.isTransferEnv()) {
            File envMat = new File(taskconfig.getEnvMatFileURI());
            printLog("Loading workspace from " + envMat);
            // Load workspace using MATLAB command
            matlabConnection.evalString("load('" + envMat + "');");
        }
    }

    private void loadInputVariables() throws Exception {
        if (paconfig.isTransferVariables()) {
            File inMat = new File(taskconfig.getInputVariablesFileURI());

            printLog("Loading input variables from " + inMat);

            matlabConnection.evalString("load('" + inMat + "');");
            if (taskconfig.getComposedInputVariablesFileURI() != null) {
                File compinMat = new File(taskconfig.getComposedInputVariablesFileURI());
                matlabConnection.evalString("load('" + compinMat + "');in=out;clear out;");
            }
        }
    }

    private Serializable storeOutputVariable() throws Exception {
        File outputFile = new File(tempSubDir, taskconfig.getOutputVariablesFileName());

        printLog("Storing 'out' variable into " + outputFile);

        if (paconfig.getMatFileOptions() != null) {
            matlabConnection.evalString("save('" + outputFile + "','out','" + paconfig.getMatFileOptions() +
                "');");
        } else {
            matlabConnection.evalString("save('" + outputFile + "','out');");
        }

        if (!outputFile.exists()) {
            throw new MatlabTaskException("Unable to store 'out' variable, the output file does not exist");
        }
        Boolean out = new Boolean(true);
        return out;
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
                matlabConnection.evalString("zip('" + outputZip + "',{'" + updatedFile + "'});");
            }
        }
    }

    private void testOutput() throws Exception {
        printLog("Receiving and testing output...");

        matlabConnection.evalString("outok=exist('out','var');");
        Object ok = matlabConnection.get("outok");
        boolean okj = false;
        if (ok instanceof double[]) {
            okj = (((double[]) ok)[0] == 1.0);
        }
        if (!okj) {
            throw new MatlabTaskException("Cannot find variable 'out'");
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
        if (this.paconfig.isRunAsMe()) {
		tmpPath = System.getProperty("node.dataspace.scratchdir");
        }

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
            FileWriter outFile = new FileWriter(logFile);
            PrintWriter out = new PrintWriter(outFile);

            outDebugWriter = out;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
