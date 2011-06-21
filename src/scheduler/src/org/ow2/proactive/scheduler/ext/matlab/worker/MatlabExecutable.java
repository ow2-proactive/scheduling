package org.ow2.proactive.scheduler.ext.matlab.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabFinder;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;


/**
 * This class is used to execute MATLAB tasks.
 * It will create a MATLAB process using {@link MatlabConnection} then
 * evaluate the task script as a MATLAB command.
 *
 * @author vbodnart
 */
public class MatlabExecutable extends JavaExecutable {

    private static String HOSTNAME;

    static {
        try {
            HOSTNAME = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
    }

    private int index = -1; // ???

    /** Global configuration */
    protected PASolveMatlabGlobalConfig paconfig;

    /** Task specific configuration */
    protected PASolveMatlabTaskConfig taskconfig;

    /** MATLAB specific configuration */
    protected MatlabEngineConfig matlabEngineConfig;

    /** The lines of inputScript */
    protected String inputScript;

    /** The lines of the MATLAB script */
    // TODO: WTF ? Why the script is not a string ?
    protected ArrayList<String> scriptLines;

    protected File sourceZipFolder = null;

    /** Path to local space subdirectory **/
    protected File tempSubDir = null;

    /** Path to local space **/
    protected File localSpace = null;

    protected MatlabConnection matlabConnection;

    public MatlabExecutable() {
        this.paconfig = new PASolveMatlabGlobalConfig();
        this.taskconfig = new PASolveMatlabTaskConfig();

        this.scriptLines = new ArrayList<String>();
    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        Object obj;

        // Read global serialized configuration
        obj = args.get("global_config");
        if (obj != null) {
            this.paconfig = (PASolveMatlabGlobalConfig) obj;
        }

        // Read task configuration
        obj = args.get("task_config");
        if (obj != null) {
            this.taskconfig = (PASolveMatlabTaskConfig) obj;
        }

        // main script to execute (embedded, url or file)
        obj = args.get("script");
        if (obj != null) {
            this.scriptLines.add((String) obj);
        }
        obj = args.get("scriptUrl");
        if (obj != null) {
            URL scriptURL = new URI((String) obj).toURL();
            InputStream is = scriptURL.openStream();
            this.scriptLines = IOTools.getContentAsList(is);
        }
        obj = args.get("scriptFile");
        if (obj != null) {
            FileInputStream fis = new FileInputStream((String) obj);
            scriptLines = IOTools.getContentAsList(fis);
        }
        if (this.scriptLines.size() == 0) {
            throw new IllegalArgumentException(
                "Either one of \"script\" \"scripturl\" \"scriptfile\" must be given");
        }

        // an input script, launched before the main script (embedded only)
        obj = args.get("input");
        if (obj != null) {
            this.inputScript = (String) obj;
        }

        // Update configuration from args
        this.paconfig.updateFromMap(args);

        // Initialize MATLAB location
        this.initMatlabConfig();

        this.initTransferSource();
        this.initTransferEnv();
        this.initTransferVariables();

        // Previously from initDS()

        // Changing dir to local space
        this.paconfig.setLocalSpace(new URI(getLocalSpace().getRealURI()));
        URI ls = this.paconfig.getLocalSpace();
        if (ls != null) {
            this.localSpace = new File(ls);
            if (!localSpace.exists() || !localSpace.canRead() || !localSpace.canWrite()) {
                System.err.println("Error, can't write on : " + localSpace);
                throw new IllegalStateException("Error, can't write on : " + localSpace);
            }
            this.tempSubDir = new File(this.localSpace, this.paconfig.getTempSubDirName());
        }
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        if (results != null) {
            for (TaskResult res : results) {
                if (res.hadException()) {
                    throw res.getException();
                }
            }
        }

        printLog("Initializing MATLAB ...");

        MatlabConnection conn = MatlabConnection.acquire(this);
        this.matlabConnection = conn;

        printLog("Executing ...");

        Serializable result = null;
        try {
            // Clear ... TODO: WTF ? It should be cleared ... (this should be done after task if keepEngine = true)
            conn.clear();
            // TODO: ?? what is index for ?
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
                            conn.put("in", tok);
                        } else {
                            if (!(res.value() instanceof Object)) {
                                throw new InvalidParameterException(res.value().getClass());
                            }

                            Object in = (Object) res.value();
                            conn.put("in", in);
                        }
                    }
                }
            }

            result = executeScript(conn);

            printLog("Received result ...");
        } finally {
            if (!paconfig.isKeepEngine()) {

                printLog("Closing MATLAB ...");

                try {
                    this.matlabConnection.release();
                } catch (Exception e) {
                    if (this.paconfig.isDebug()) {
                        e.printStackTrace(System.out);
                    }
                }

            } else {

                printLog("Packing memory in MATLAB ...");
                //boolean ok = cleanup(); TODO: pack memory befor leaving matlab
            }
        }

        this.printLog("Task completed successfully");

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

    @Override
    public OSUser getUser() {
        OSUser user;
        try {
            user = super.getUser();
        } catch (Exception e) {
            e.printStackTrace();
            user = null;
        }
        return user;
    }

    /*********** PRIVATE METHODS ***********/

    private void initTransferSource() throws Exception {
        if (paconfig.isTransferSource()) {
            if (paconfig.isZipSourceFiles()) {
                String sourceZipFileName = taskconfig.getSourceZipFileName();
                if (sourceZipFileName == null) {
                    sourceZipFileName = paconfig.getSourceZipFileName();
                }
                taskconfig.setSourceZipFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + sourceZipFileName).getRealURI()));
            } else {
                if (taskconfig.getSourceNames() != null) {
                    String[] names = taskconfig.getSourceNames();
                    URI[] sourceURIs = new URI[names.length];
                    for (int i = 0; i < names.length; i++) {
                        sourceURIs[i] = new URI(getLocalFile(paconfig.getTempSubDirName() + "/" + names[i])
                                .getRealURI());
                    }
                    taskconfig.setSourcesFilesURIs(sourceURIs);
                }
            }
        }
    }

    public void initTransferEnv() throws Exception {
        if (paconfig.isTransferEnv()) {
            if (paconfig.isZipEnvFile()) {
                taskconfig.setEnvZipFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + paconfig.getEnvZipFileName()).getRealURI()));
            } else {
                taskconfig.setEnvMatFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + paconfig.getEnvMatFileName()).getRealURI()));
            }
        }
    }

    public void initTransferVariables() throws Exception {
        if (paconfig.isTransferVariables()) {
            taskconfig
                    .setInputVariablesFileURI(new URI(getLocalFile(
                            paconfig.getTempSubDirName() + "/" + taskconfig.getInputVariablesFileName())
                            .getRealURI()));
            if (taskconfig.getComposedInputVariablesFileName() != null) {
                taskconfig.setComposedInputVariablesFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + taskconfig.getComposedInputVariablesFileName())
                        .getRealURI()));
            }
        }

        if (taskconfig.isInputFilesThere() && paconfig.isZipInputFiles()) {
            int n = taskconfig.getInputFilesZipNames().length;
            URI[] uris = new URI[n];
            for (int i = 0; i < n; i++) {
                uris[i] = new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + taskconfig.getInputFilesZipNames()[i])
                        .getRealURI());
            }
            taskconfig.setInputZipFilesURI(uris);
        }

        //paconfig.setLocalSpace(new URI(getLocalSpace().getRealURI()));
    }

    // TODO: Understand the logic of this method ...
    protected MatSciEngineConfig initMatlabConfig() throws Exception {
        // First we try to find MATLAB
        if (matlabEngineConfig == null) {
            //            if (paconfig.isDebug()) {
            //                System.out.println("[" + new java.util.Date() + " " + host + " " +
            //                    this.getClass().getSimpleName() + "] Looking for " + getExtensionName() + "...");
            //                outDebug.println("[" + new java.util.Date() + " " + host + " " +
            //                    this.getClass().getSimpleName() + "] Looking for " + getExtensionName() + "...");
            //            }
            matlabEngineConfig = (MatlabEngineConfig) MatlabEngineConfig.getCurrentConfiguration();
            if (matlabEngineConfig == null) {
                matlabEngineConfig = (MatlabEngineConfig) MatlabFinder.getInstance().findMatSci(
                        paconfig.getVersionPref(), paconfig.getVersionRej(), paconfig.getVersionMin(),
                        paconfig.getVersionMax());
                //                if (paconfig.isDebug()) {
                //                    System.out.println(matlabEngineConfig);
                //                    outDebug.println(matlabEngineConfig);
                //                }
                if (matlabEngineConfig == null)
                    throw new IllegalStateException("No valid Matlab configuration found, aborting...");
            }
        } else {
            MatlabEngineConfig conf = (MatlabEngineConfig) MatlabEngineConfig.getCurrentConfiguration();
            if (conf != null) {
                matlabEngineConfig = conf;
            }
        }
        //info.setConfig(matlabEngineConfig);
        return matlabEngineConfig;
    }

    //    // THIS can be done by specifying the current directory as localSpace
    //    private void initDS(MatlabConnection conn) throws Exception {
    //        // Changing dir to local space
    //        URI ls = paconfig.getLocalSpace();
    //        if (ls != null) {
    //            localSpace = new File(ls);
    //            if (localSpace.exists() && localSpace.canRead() && localSpace.canWrite()) {
    //                conn.evalString("cd('" + localSpace + "');");
    //                tempSubDir = new File(localSpace, paconfig.getTempSubDirName());
    //
    //            } else {
    //                System.err.println("Error, can't write on : " + localSpace);
    //                throw new IllegalStateException("Error, can't write on : " + localSpace);
    //            }
    //        }
    //    }

    private void transferSource(MatlabConnection conn) throws Exception {
        if (paconfig.isTransferSource() && tempSubDir != null) {
            if (paconfig.isDebug()) {
                System.out.println("Unzipping source files");
            }
            File sourceZip = new File(taskconfig.getSourceZipFileURI());
            if (sourceZip.exists() && sourceZip.canRead()) {
                sourceZipFolder = sourceZip.getParentFile();
                if (!sourceZipFolder.exists() || (!sourceZipFolder.canWrite())) {
                    System.err.println("Error, can't write on : " + sourceZipFolder);
                    throw new IllegalStateException("Error, can't write on : " + sourceZipFolder);
                }
                conn.evalString("restoredefaultpath;");
                conn.evalString("addpath('" + sourceZipFolder + "');");
                conn.evalString("unzip('" + sourceZip + "','" + sourceZipFolder + "');");
                if (paconfig.isDebug()) {
                    System.out.println("Contents of " + sourceZipFolder);
                    for (File f : sourceZipFolder.listFiles()) {
                        System.out.println(f);
                    }
                }
            } else {
                System.err.println("Error, source zip file cannot be accessed at : " + sourceZip);
                throw new IllegalStateException("Error, source zip file cannot be accessed at : " + sourceZip);
            }
        }
    }

    private void transferEnv(MatlabConnection conn) throws Exception {
        if (paconfig.isTransferEnv()) {
            if (paconfig.isZipEnvFile()) {
                if (paconfig.isDebug()) {
                    System.out.println("Unzipping env file");
                }
                File envZip = new File(taskconfig.getEnvZipFileURI());
                if (envZip.exists()) {
                    File envZipFolder = envZip.getParentFile();
                    conn.evalString("unzip('" + envZip + "','" + envZipFolder + "');");

                } else {
                    System.err.println("Error, env zip file cannot be accessed at : " + envZip);
                    throw new IllegalStateException("Error, env zip file cannot be accessed at : " + envZip);
                }
            }
            File envMat = new File(taskconfig.getEnvMatFileURI());
            conn.evalString("load('" + envMat + "');");
        }
    }

    private void transferInputVariables(MatlabConnection conn) throws Exception {
        if (paconfig.isTransferVariables()) {
            File inMat = new File(taskconfig.getInputVariablesFileURI());
            if (paconfig.isDebug()) {
                System.out.println("Loading Input Variable file " + inMat);
            }
            conn.evalString("load('" + inMat + "');");
            if (taskconfig.getComposedInputVariablesFileURI() != null) {
                File compinMat = new File(taskconfig.getComposedInputVariablesFileURI());
                conn.evalString("load('" + compinMat + "');in=out;clear out;");
            }
        }
    }

    private void transferInputFiles(MatlabConnection conn) throws Exception {
        if (taskconfig.isInputFilesThere() && paconfig.isZipInputFiles()) {
            if (paconfig.isDebug()) {
                System.out.println("Unzipping input files");
            }
            for (URI uri : taskconfig.getInputZipFilesURI()) {
                File inputZip = new File(uri);
                if (inputZip.exists()) {
                    conn.evalString("unzip('" + inputZip + "','" + localSpace + "');");
                } else {
                    System.err.println("Error, input zip file cannot be accessed at : " + inputZip);
                    throw new IllegalStateException("Error, input zip file cannot be accessed at : " +
                        inputZip);
                }
            }

        }
    }

    private Serializable transferOutputVariable(MatlabConnection conn) throws Exception {
        File outputFile = new File(tempSubDir, taskconfig.getOutputVariablesFileName());
        if (paconfig.isDebug()) {
            System.out.println("Saving Output Variable file " + outputFile);
        }
        if (paconfig.getMatFileOptions() != null) {
            conn.evalString("save('" + outputFile + "','out','" + paconfig.getMatFileOptions() + "');");
        } else {
            conn.evalString("save('" + outputFile + "','out');");
        }

        if (!outputFile.exists()) {
            throw new MatlabTaskException("Cannot find variable \"out\"");
        }
        Boolean out = new Boolean(true);
        return out;
    }

    // TODO: Why is MATLAB doing this ? Can't we do it locally ?
    private void transferOutputFiles(MatlabConnection conn) throws Exception {
        if (taskconfig.isOutputFilesThere() && paconfig.isZipOutputFiles()) {
            if (paconfig.isDebug()) {
                System.out.println("Zipping output files");
            }
            String[] outputFiles = taskconfig.getOutputFiles();
            String[] names = taskconfig.getOutputFilesZipNames();
            for (int i = 0; i < names.length; i++) {
                File outputZip = new File(tempSubDir, names[i]);
                //conn.evalString("ProActiveOutputFiles=cell(1,"+outputFiles.length+");");
                //for (int i=0; i < outputFiles.length; i++) {
                String updatedFile = outputFiles[i].replaceAll("/", System.getProperty("file.separator"));
                //conn.evalString("ProActiveOutputFiles{"+(i+1)+"}='"+updatedFile+"';");
                //}
                conn.evalString("zip('" + outputZip + "',{'" + updatedFile + "'});");
            }
        }
    }

    /**
     * Executes both input and main scripts on the engine
     *
     * @throws Throwable
     */
    protected final Serializable executeScript(MatlabConnection conn) throws Throwable {

        Object out = null;

        //initDS(conn);
        transferSource(conn);
        transferEnv(conn);
        transferInputVariables(conn);

        if (paconfig.isDebug()) {
            conn.evalString("who");
        }

        transferInputFiles(conn);

        if (inputScript != null) {
            printLog("Feeding input: " + inputScript);
            conn.evalString(inputScript);
        }

        String execScript = prepareScript();

        printLog("Executing MATLAB command: " + execScript);

        // Evaluate the script as a MATLAB command
        conn.evalString(execScript);

        printLog("Command completed successfully, looking for output ... ");

        testOutput(conn);

        if (paconfig.isTransferVariables()) {
            out = transferOutputVariable(conn);
        } else {
            out = conn.get("out");
        }

        printLog("Command output: " + out);

        // outputFiles
        transferOutputFiles(conn);

        return (Serializable) out;
    }

    private void testOutput(MatlabConnection conn) throws Exception {
        conn.evalString("outok=exist('out','var');");
        Object ok = conn.get("outok");
        boolean okj = false;
        if (ok instanceof double[]) {
            okj = (((double[]) ok)[0] == 1.0);
        }
        if (!okj) {
            throw new MatlabTaskException("Cannot find variable \"out\"");
        }
    }

    /**
     * Appends all the script's lines as a single string
     *
     * @return
     */
    private String prepareScript() {
        String script = "";

        for (String line : scriptLines) {
            script += line;
            script += System.getProperty("line.separator");
        }

        return script;
    }

    /** The output of this method will be the output of the task */
    private void printLog(String message) {
        if (!this.paconfig.isDebug()) {
            return;
        }

        String log = "[" + new java.util.Date() + " " + HOSTNAME + " " + this.getClass().getSimpleName() +
            "] " + message;
        System.out.println(log);
    }
}
