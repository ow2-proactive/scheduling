package org.ow2.proactive.scheduler.ext.matlab.worker;

import matlabcontrol.MatlabInvocationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabTaskException;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciJVMInfo;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;


/**
 * MatlabControlTask
 *
 * @author The ProActive Team
 */
public class MatlabControlTask extends MatlabTask<AOMatlabWorker> {

    static String nl = System.getProperty("line.separator");

    static String fs = System.getProperty("file.separator");

    static File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    File sourceZipFolder = null;

    /** Path to local space subdirectory **/
    private File tempSubDir = null;

    /** Path to local space **/
    private File localSpace = null;

    public MatlabControlTask() {

    }

    public Serializable execute(TaskResult... results) throws Throwable {
        if (results != null) {
            for (TaskResult res : results) {
                if (res.hadException()) {
                    throw res.getException();
                }
            }

        }

        Serializable res = null;

        MatSciJVMInfo jvminfo = firstInit();

        initEngineConfig(jvminfo);

        try {

            res = executeInternal(results);

        } finally {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Performing after task actions");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Performing after task actions ");
            }
            afterExecute(jvminfo);
        }

        return res;
    }

    protected Serializable executeInternal(TaskResult... results) throws Throwable {

        Serializable res = null;

        // boolean notInitializationTask = inputScript.indexOf("PROACTIVE_INITIALIZATION_CODE") == -1;
        MatSciJVMInfo<AOMatlabWorker, MatlabEngineConfig> jvminfo = jvmInfos.get(nodeName);

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Initializing : " +
                PAActiveObject.getBodyOnThis().getID());
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Initializing : " + PAActiveObject.getBodyOnThis().getID());

        }

        if (paconfig.isTransferSource()) {
            if (paconfig.isZipSourceFiles()) {
                if (taskconfig.getSourceZipFileName() != null) {
                    taskconfig.setSourceZipFileURI(new URI(getLocalFile(
                            paconfig.getTempSubDirName() + "/" + taskconfig.getSourceZipFileName())
                            .getRealURI()));
                } else {
                    taskconfig.setSourceZipFileURI(new URI(getLocalFile(
                            paconfig.getTempSubDirName() + "/" + paconfig.getSourceZipFileName())
                            .getRealURI()));
                }
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
        if (paconfig.isTransferEnv()) {
            if (paconfig.isZipEnvFile()) {
                taskconfig.setEnvZipFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + paconfig.getEnvZipFileName()).getRealURI()));
            } else {
                taskconfig.setEnvMatFileURI(new URI(getLocalFile(
                        paconfig.getTempSubDirName() + "/" + paconfig.getEnvMatFileName()).getRealURI()));
            }
        }
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

        paconfig.setLocalSpace(new URI(getLocalSpace().getRealURI()));

        initWorker(inputScript, scriptLines, paconfig, taskconfig, matlabEngineConfig);

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Executing");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Executing");
        }

        try {

            // We execute the task on the worker
            res = executeInternalInternal(index, results);

            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Received result");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Received result");
            }

        } finally {
            if (!paconfig.isKeepEngine()) {
                if (paconfig.isDebug()) {
                    System.out.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Terminating " + getExtensionName() + " engine");
                    outDebug.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Terminating " + getExtensionName() + "engine");
                }
                try {
                    terminate();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (paconfig.isDebug()) {
                        e.printStackTrace(outDebug);
                    }
                }

            } else {
                if (paconfig.isDebug()) {
                    System.out.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Packing memory in " + getExtensionName() +
                        " engine");
                    outDebug.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Packing memory in " + getExtensionName() +
                        "engine");
                }
                boolean ok = cleanup();
            }
        }

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Task completed successfully");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Task completed successfully");

        }

        return res;
    }

    public void initWorker(String inputScript, ArrayList<String> scriptLines,
            PASolveMatlabGlobalConfig paconfig, PASolveMatlabTaskConfig taskconfig,
            MatlabEngineConfig matlabConfig) {
        if (MatlabControlEngine.getConfiguration() != null &&
            !MatlabControlEngine.getConfiguration().equals(matlabConfig)) {
            MatlabControlEngine.close();
        }
        MatlabControlEngine.setConfiguration(matlabConfig);
        this.inputScript = inputScript;
        this.scriptLines = scriptLines;
        this.paconfig = paconfig;
        this.taskconfig = taskconfig;

    }

    public Serializable executeInternalInternal(int index, TaskResult... results) throws Throwable {
        Serializable out = null;
        MatlabControlEngine.Connection conn = MatlabControlEngine.acquire();
        conn.testEngineInitOrRestart();
        try {
            conn.clear();
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

            out = executeScript(conn);

        } finally {
            conn.release();
        }
        return out;
    }

    /**
     * Terminates the Matlab engine
     *
     * @return true for synchronous call
     */
    public void terminate() {
        MatlabControlEngine.close();

    }

    public boolean cleanup() {
        MatlabControlEngine.Connection conn = MatlabControlEngine.acquire();
        try {
            if (sourceZipFolder != null) {
                conn.evalString("rmpath('" + sourceZipFolder + "');");
            }
            conn.evalString("v=fopen('all');for i=1:length(v),fclose(v(i)), end");
            conn.evalString("cd('" + tmpDir + "');");
            conn.evalString("clear all;");
            conn.evalString("pack;");
            conn.release();
        } catch (MatlabInvocationException e) {
        }
        return true;
    }

    private void initDS(MatlabControlEngine.Connection conn) throws Exception {
        // Changing dir to local space
        URI ls = paconfig.getLocalSpace();
        if (ls != null) {
            localSpace = new File(ls);
            if (localSpace.exists() && localSpace.canRead() && localSpace.canWrite()) {
                conn.evalString("cd('" + localSpace + "');");
                tempSubDir = new File(localSpace, paconfig.getTempSubDirName());

            } else {
                System.err.println("Error, can't write on : " + localSpace);
                throw new IllegalStateException("Error, can't write on : " + localSpace);
            }
        }

    }

    private void transferSource(MatlabControlEngine.Connection conn) throws Exception {
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

    private void transferEnv(MatlabControlEngine.Connection conn) throws Exception {
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

    private void transferInputVariables(MatlabControlEngine.Connection conn) throws Exception {
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

    private void transferInputFiles(MatlabControlEngine.Connection conn) throws Exception {
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

    private Serializable transferOutputVariable(MatlabControlEngine.Connection conn) throws Exception {
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

    private void transferOutputFiles(MatlabControlEngine.Connection conn) throws Exception {
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
                String updatedFile = outputFiles[i].replaceAll("/", fs);
                //conn.evalString("ProActiveOutputFiles{"+(i+1)+"}='"+updatedFile+"';");
                //}
                conn.evalString("zip('" + outputZip + "',{'" + updatedFile + "'});");
            }

        }
    }

    private void testOutput(MatlabControlEngine.Connection conn) throws Exception {
        if (paconfig.isDebug()) {
            System.out.println("Receiving output:");
            System.out.println("Testing output:");
        }
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
     * Executes both input and main scripts on the engine
     *
     * @throws Throwable
     */
    protected final Serializable executeScript(MatlabControlEngine.Connection conn) throws Throwable {

        Object out = null;

        initDS(conn);

        transferSource(conn);

        transferEnv(conn);

        transferInputVariables(conn);

        if (paconfig.isDebug()) {
            conn.evalString("who");
        }

        transferInputFiles(conn);

        if (inputScript != null) {
            if (paconfig.isDebug()) {
                System.out.println("Feeding input:");
                //outDebug.println("Feeding input:");
                System.out.println(inputScript);
                //	outDebug.println(inputScript);
            }
            conn.evalString(inputScript);
        }

        String execScript = prepareScript();
        if (paconfig.isDebug()) {
            System.out.println("Executing Matlab command:");
            //outDebug.println("Executing Matlab command:");
            System.out.println(execScript);
            //outDebug.println(execScript);
            System.out.flush();
            //	outDebug.flush();
        }
        conn.evalString(execScript);
        if (paconfig.isDebug()) {
            System.out.println("Matlab command completed successfully");
            //outDebug.println("Matlab command completed successfully");
        }

        if (paconfig.isDebug()) {
            System.out.println("Receiving output:");
        }

        testOutput(conn);

        if (paconfig.isTransferVariables()) {
            out = transferOutputVariable(conn);
        } else {
            out = conn.get("out");
        }
        if (paconfig.isDebug()) {
            System.out.println(out);
            //outDebug.println(out);
            System.out.flush();
            //	outDebug.flush();
        }

        // outputFiles
        transferOutputFiles(conn);

        return (Serializable) out;

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
            script += nl;
        }

        return script;
    }
}
