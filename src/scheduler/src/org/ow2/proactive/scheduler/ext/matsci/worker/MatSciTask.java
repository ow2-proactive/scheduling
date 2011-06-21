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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.worker;

import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.common.util.FileUtils;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matsci.common.JVMSpawnHelper;
import org.ow2.proactive.scheduler.ext.matsci.common.PASolveMatSciGlobalConfig;
import org.ow2.proactive.scheduler.ext.matsci.common.PASolveMatSciTaskConfig;
import org.ow2.proactive.scheduler.ext.matsci.common.ProcessInitializer;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfigBase;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciJVMInfo;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciTaskServerConfig;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabInitializationException;
import org.ow2.proactive.scheduler.ext.scilab.common.exception.ScilabInitializationHanged;
import org.ow2.proactive.scheduler.util.process.ProcessTreeKiller;

import java.io.*;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 * MatSciTask
 *
 * @author The ProActive Team
 */
public abstract class MatSciTask<W extends MatSciWorker, C extends MatSciEngineConfig, P extends PASolveMatSciGlobalConfig, T extends PASolveMatSciTaskConfig>
        extends JavaExecutable implements ProcessInitializer {

    /**
     * the index when the input is the result of a SplitTask
     */
    protected int index = -1;

    /**
     * This hostname, for debugging purpose
     */
    protected static String host = null;

    /**
     * Node name where this task is being executed
     */
    protected String nodeName = null;

    /**
     * The URI to which the spawned JVM(Node) is registered
     */
    protected static Map<String, MatSciJVMInfo> jvmInfos = new HashMap<String, MatSciJVMInfo>();

    /**
     * Tells if the shutdownhook has been set up for this runtime
     */
    protected static boolean shutdownhookSet = false;

    protected static Thread shutdownHook = null;

    protected int taskCount = 0;
    protected static int taskCountBeforeJVMRespawn;

    protected boolean startingProcess = false;
    protected boolean redeploying = false;

    protected MatSciTaskServerConfig serverConfig;

    protected P paconfig;

    protected T taskconfig;

    /**
     * the OS where this JVM is running
     */
    protected static OperatingSystem os = OperatingSystem.getOperatingSystem();

    private static boolean threadstarted = false;

    private int MAX_NB_ATTEMPTS;

    private int nbAttempts = 0;

    protected PrintStream outDebug;

    protected File nodeTmpDir;

    protected JVMSpawnHelper helper;

    /**
     *  The process holding the spawned JVM
     */
    // protected static Process process = null;
    static {
        if (host == null) {
            try {
                host = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        if (os.equals(OperatingSystem.windows)) {
            WinProcess.enableDebugPrivilege();
            taskCountBeforeJVMRespawn = 30;
        } else {
            taskCountBeforeJVMRespawn = 100;
        }
    }

    abstract protected String getWorkerClassName();

    abstract protected String getExtensionName();

    abstract protected MatSciTaskServerConfig getTaskServerConfig() throws Exception;

    abstract protected void initPASolveConfig(Map<String, Serializable> args);

    public void init(Map<String, Serializable> args) throws Exception {

        initPASolveConfig(args);

        String d = (String) args.get("debug");
        if (d != null) {
            paconfig.setDebug(Boolean.parseBoolean(d));
        }

        String ke = (String) args.get("keepEngine");
        if (ke != null) {
            paconfig.setKeepEngine(Boolean.parseBoolean(ke));
        }

        // index when doing fork/join taskflows
        Object ind = args.get("index");

        if (ind != null) {
            index = Integer.parseInt((String) ind);
        }

        paconfig.setVersionPref((String) args.get("versionPref"));

        paconfig.setVersionMin((String) args.get("versionMin"));

        paconfig.setVersionMax((String) args.get("versionMax"));

        String vrej = (String) args.get("versionRej");

        if (vrej != null) {
            paconfig.setVersionRejAsString(vrej);
        }

        host = java.net.InetAddress.getLocalHost().getHostName();

    }

    /**
     * Internal version of the execute method
     *
     * @param results results from preceding tasks
     * @return result of the task
     * @throws Throwable
     */
    protected Serializable executeInternal(TaskResult... results) throws Throwable {

        Serializable res = null;
        W sw = null;

        // boolean notInitializationTask = inputScript.indexOf("PROACTIVE_INITIALIZATION_CODE") == -1;
        MatSciJVMInfo<W, C> jvminfo = jvmInfos.get(nodeName);
        sw = jvminfo.getWorker();

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Initializing : " +
                PAActiveObject.getBodyOnThis().getID());
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Initializing : " + PAActiveObject.getBodyOnThis().getID());

        }

        DataSpacesFileObject dsLocalSpace = this.getLocalSpace();
        String dsURI = dsLocalSpace.getRealURI();

        File localSpaceRootDir = new File(new URI(dsURI));
        File tempSubDir = new File(localSpaceRootDir, this.paconfig.getTempSubDirName());

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

                File sourceZip = new File(taskconfig.getSourceZipFileURI());

                if (paconfig.isDebug()) {
                    System.out.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Unzipping source files from " + sourceZip);
                    outDebug.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Unzipping source files from " + sourceZip);
                }

                if (!sourceZip.exists() || !sourceZip.canRead()) {
                    System.err.println("Error, source zip file cannot be accessed at " + sourceZip);
                    throw new IllegalStateException("Error, source zip file cannot be accessed at " +
                        sourceZip);
                }

                // Uncompress the source files into the temp dir
                if (!FileUtils.unzip(sourceZip, tempSubDir)) {
                    System.err.println("Unable to unzip source file " + sourceZip);
                    throw new IllegalStateException("Unable to unzip source file " + sourceZip);
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

            taskconfig.setEnvMatFileURI(new URI(getLocalFile(
                    paconfig.getTempSubDirName() + "/" + paconfig.getEnvMatFileName()).getRealURI()));

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

        initWorker(sw);

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Executing");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Executing");
        }

        try {

            // We execute the task on the worker
            res = sw.execute(index, results);
            // We wait for the result
            res = (Serializable) PAFuture.getFutureValue(res);

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
                    BooleanWrapper ok = sw.terminate();
                    PAFuture.waitFor(ok);
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
                boolean ok = sw.cleanup();
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

        int nbAttempts = 1;
        //if (keepEngine) {
        taskCount++;
        if (taskCount == taskCountBeforeJVMRespawn || MatSciEngineConfigBase.hasChangedConf()) {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Redeploying JVM...");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Redeploying JVM...");
            }
            destroyProcess(jvminfo);
        }
        //}
        redeploying = false;
        try {
            while (res == null) {

                handleProcess(jvminfo, getWorkerClassName());

                if (paconfig.isDebug()) {
                    System.out.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Executing the task, try " + nbAttempts);
                    outDebug.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Executing the task, try " + nbAttempts);
                }

                // finally we call the internal version of the execute method

                try {

                    redeploying = false;
                    res = executeInternal(results);

                } catch (ScilabInitializationException e) {
                    redeployOrLeave(e, jvminfo,
                            "Scilab Engine couldn't initialize, this can happen sometimes even when paths are correct");
                } catch (ScilabInitializationHanged e) {
                    redeployOrLeave(e, jvminfo, "Scilab Engine initialization hanged");
                } catch (FutureMonitoringPingFailureException e) {
                    redeployOrLeave(e, jvminfo, "Spawned JVM crashed");
                } catch (ptolemy.kernel.util.IllegalActionException e) {
                    redeployOrLeave(e, jvminfo, "Unable to initialize Matlab Engine, or engine error");

                } catch (java.lang.OutOfMemoryError e) {
                    leave(e, jvminfo, "Out of memory error in spawned JVM");
                } catch (Throwable e) {
                    if (paconfig.isDebug()) {
                        System.out.println("[" + new java.util.Date() + " " + host + " " +
                            this.getClass().getSimpleName() + "] Exception occurred");
                        e.printStackTrace();
                        e.printStackTrace(outDebug);
                    }
                    throw e;

                }
            }
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

    public void kill() {
        MatSciJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Task killed.");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Task killed.");
        }
        destroyProcess(jvminfo);
        afterExecute(jvminfo);
        super.kill();
    }

    abstract protected void afterExecute(MatSciJVMInfo jvminfo);

    protected MatSciJVMInfo firstInit() throws Throwable {
        nodeName = MatSciEngineConfigBase.getNodeName();

        if (paconfig.isDebug()) {
            // system temp dir
            String tmpPath = System.getProperty("java.io.tmpdir");

            // log file writer used for debugging
            File tmpDirFile = new File(tmpPath);
            nodeTmpDir = new File(tmpDirFile, nodeName);
            if (!nodeTmpDir.exists()) {
                nodeTmpDir.mkdirs();
            }
            File logFile = new File(tmpPath, "" + this.getClass().getSimpleName() + "" + nodeName + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            outDebug = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));
        }
        serverConfig = getTaskServerConfig();

        MAX_NB_ATTEMPTS = serverConfig.getMaxNbAttempts();

        taskCountBeforeJVMRespawn = serverConfig.getTaskCountBeforeJVMRespawn();

        MatSciJVMInfo jvminfo = jvmInfos.get(nodeName);
        if (jvminfo == null) {
            jvminfo = new MatSciJVMInfo();
            jvmInfos.put(nodeName, jvminfo);
        }
        return jvminfo;
    }

    abstract protected MatSciEngineConfig initEngineConfig(MatSciJVMInfo jvminfo) throws Throwable;

    protected void destroyProcess(MatSciJVMInfo jvminfo) {

        taskCount = 1;

        if (os.equals(OperatingSystem.windows)) {
            destroyProcessWindows(jvminfo);
        } else {
            destroyProcessUnix(jvminfo);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
        //jvminfo.getProcess().destroy();

        jvminfo.setProcess(null);
        jvminfo.setWorker(null);
        removeShutdownHook();

    }

    protected void destroyProcessWindows(MatSciJVMInfo jvminfo) {

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Destroying JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Destroying JVM");
        }
        try {
            if (jvminfo.getWorker() != null) {
                jvminfo.getWorker().terminate();
            }
        } catch (Exception e1) {
        }

        Process proc = jvminfo.getProcess();

        if (proc != null) {

            WinProcess pi = new WinProcess(proc);

            try {
                if (paconfig.isDebug()) {
                    System.out.println("Killing process " + pi.getPid());
                    outDebug.println("Killing process " + pi.getPid());
                }
                Runtime.getRuntime().exec("taskkill /PID " + pi.getPid() + " /T");
                Runtime.getRuntime().exec("tskill " + pi.getPid());

            } catch (IOException e) {
                e.printStackTrace();
            }

            killProcessWindowsWithEnv("NODE_NAME", nodeName);
        }

    }

    protected void killProcessWindowsWithEnv(String envName, String envValue) {
        Map<String, String> modelEnv = new HashMap<String, String>();
        modelEnv.put(envName, envValue);
        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Destroying processes with " + envName + "=" + envValue);
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Destroying processes with " + envName + "=" + envValue);
        }
        for (WinProcess p : WinProcess.all()) {
            if (p.getPid() < 10)
                continue; // ignore system processes like "idle process"

            boolean matched;

            try {
                matched = hasMatchingEnvVars(p.getEnvironmentVariables(), modelEnv);
            } catch (WinpException e) {
                // likely a missing privilege
                continue;
            }

            if (matched) {
                if (paconfig.isDebug()) {
                    outDebug.println("Matched :");
                    outDebug.println(p.getCommandLine());

                    String val = p.getEnvironmentVariables().get(envName);

                    outDebug.println(envName + "=" + val);
                    outDebug.println("Killing process " + p.getPid());
                }
                try {

                    Runtime.getRuntime().exec("taskkill /PID " + p.getPid() + " /T");
                    Runtime.getRuntime().exec("tskill " + p.getPid());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //p.kill();
            }
        }
    }

    protected void destroyProcessUnix(MatSciJVMInfo jvminfo) {
        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Destroying JVM");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Destroying JVM");
        }
        try {
            if (jvminfo.getWorker() != null) {
                jvminfo.getWorker().terminate();
            }
        } catch (Exception e1) {
        }

        Process proc = jvminfo.getProcess();
        if (proc != null) {

            Map<String, String> modelEnv = new HashMap<String, String>();
            modelEnv.put("NODE_NAME", nodeName);
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Destroying processes with NODE_NAME=" + nodeName);
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Destroying processes with NODE_NAME=" + nodeName);
            }

            ProcessTreeKiller.get().kill(proc, modelEnv);
        }

    }

    protected boolean hasMatchingEnvVars(Map<String, String> envVar, Map<String, String> modelEnvVar) {
        if (modelEnvVar.isEmpty())
            // sanity check so that we don't start rampage.
            return false;

        for (Map.Entry<String, String> e : modelEnvVar.entrySet()) {
            String v = envVar.get(e.getKey());
            if (v == null || !v.equals(e.getValue()))
                return false; // no match
        }
        return true;
    }

    protected void leave(Throwable e, MatSciJVMInfo jvminfo, String message) throws Throwable {
        if (paconfig.isDebug()) {
            e.printStackTrace(outDebug);
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] " + message + ", leaving");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] " + message + ", leaving");
        }
        destroyProcess(jvminfo);
        throw e;

    }

    protected void redeployOrLeave(Throwable e, MatSciJVMInfo jvminfo, String message) throws Throwable {

        destroyProcess(jvminfo);

        if (nbAttempts >= MAX_NB_ATTEMPTS) {
            if (paconfig.isDebug()) {
                e.printStackTrace(outDebug);
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] " + message + ", leaving");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] " + message + ", leaving");
            }
            throw e;
        }
        redeploying = true;
        if (paconfig.isDebug()) {
            e.printStackTrace(outDebug);
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] " + message + ", redeploying");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] " + message + ", redeploying");
        }
        nbAttempts++;
    }

    protected void addShutdownHook() {
        shutdownHook = new Thread(new Runnable() {
            public void run() {
                for (MatSciJVMInfo info : jvmInfos.values()) {
                    try {
                        try {
                            afterExecute(info);
                        } catch (Throwable e) {
                        }
                        destroyProcess(info);
                        //info.getProcess().destroy();
                    } catch (Throwable e) {
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        shutdownhookSet = true;
    }

    protected void removeShutdownHook() {
        if (shutdownHook != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } else {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] WARN Shutdowhook not found...");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] WARN Shutdowhook not found...");
            }
        }
        shutdownHook = null;
        shutdownhookSet = false;
    }

    protected void handleProcess(MatSciJVMInfo<W, C> jvminfo, String workerClassName) throws Throwable {
        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Checking Processes...");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Checking Processes...");
        }
        if (jvminfo.getProcess() == null) {

            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Starting the Java Process");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Starting the Java Process");
            }

            // We spawn a new JVM with the library paths
            helper = new JVMSpawnHelper(paconfig.isDebug(), outDebug, nodeTmpDir, nodeName, serverConfig
                    .getSemaphoreTimeout(), serverConfig.getSemaphoreRetryAquire());
            startingProcess = true;
            Process p = helper.startProcess(getExtensionName(), this, jvminfo);
            jvminfo.setProcess(p);
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Process successfully started");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Process successfully started");
            }
            if (!shutdownhookSet) {
                if (paconfig.isDebug()) {
                    System.out.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Adding shutDownHook");
                    outDebug.println("[" + new java.util.Date() + " " + host + " " +
                        this.getClass().getSimpleName() + "] Adding shutDownHook");
                }
                addShutdownHook();
            }
        }

        //TODO for multi node, threadstarted must be different for each node
        if (!threadstarted) {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Starting the Threads");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Starting the Threads");
            }
            // We define the loggers which will write on standard output what comes from the java process
            IOTools.LoggingThread lt1;
            if (paconfig.isDebug()) {
                lt1 = new IOTools.LoggingThread(jvminfo.getProcess(), "[ " + host + " ]", System.out,
                    System.err, outDebug);

            } else {
                lt1 = new IOTools.LoggingThread(jvminfo.getProcess(), "[" + host + " OUT]", System.out,
                    System.err, null, null, null);
            }

            jvminfo.setLogger(lt1);

            IOTools.RedirectionThread rt1 = null;
            if (serverConfig.isDeployIoThread()) {
                rt1 = new IOTools.RedirectionThread(System.in, jvminfo.getProcess().getOutputStream());
                jvminfo.setIoThread(rt1);
            }

            // We start the loggers thread

            Thread t1 = new Thread(lt1, "OUT " + getExtensionName());
            t1.setDaemon(true);
            t1.start();

            if (serverConfig.isDeployIoThread()) {
                Thread t3 = new Thread(rt1, "Redirecting I/O Scilab");
                t3.setDaemon(true);
                t3.start();
            }

            threadstarted = true;
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Threads started");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Threads started");
            }
        } else if (startingProcess) {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Connecting process out to threads");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Connecting process out to threads");
            }

            jvminfo.getLogger().setProcess(jvminfo.getProcess());

            if (serverConfig.isDeployIoThread()) {
                jvminfo.getIoThread().setOutputStream(jvminfo.getProcess().getOutputStream());
            }

            if (!redeploying) {
                if (paconfig.isDebug()) {
                    jvminfo.getLogger().setOutStream(System.out, System.err, outDebug);
                } else {
                    jvminfo.getLogger().setOutStream(System.out, System.err);
                }
            }
            startingProcess = false;
        } else {
            if (paconfig.isDebug()) {
                jvminfo.getLogger().setOutStream(System.out, System.err, outDebug);
            } else {
                jvminfo.getLogger().setOutStream(System.out, System.err);
            }
        }

        W sw = jvminfo.getWorker();
        if (sw == null) {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] waiting for deployment");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] waiting for deployment");
            }

            try {
                helper.waitForRegistration(jvminfo.getProcess());
            } catch (Throwable e) {
                // SCHEDULING-1127: if the proactive runtime couldn't start, we need to clean processes
                destroyProcess(jvminfo);
                throw e;
            }

            sw = deploy(workerClassName);

            helper.unsubscribeJMXRuntimeEvent();
        }
    }

    /**
     * Deploy an Active Object on the given Node uri
     *
     * @throws Throwable
     */
    protected W deploy(String className) throws Throwable {
        ProActiveException ex = null;
        MatSciJVMInfo<W, C> jvminfo = jvmInfos.get(nodeName);

        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Deploying Worker");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Deploying Worker");
        }

        final W worker = (W) PAActiveObject.newActive(className, new Object[] { jvminfo.getConfig() },
                jvminfo.getNode());

        jvminfo.setWorker(worker);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    worker.terminate();
                } catch (Exception e) {
                }
            }
        }));

        return worker;
    }

    abstract protected void initWorker(W worker) throws Throwable;

}
