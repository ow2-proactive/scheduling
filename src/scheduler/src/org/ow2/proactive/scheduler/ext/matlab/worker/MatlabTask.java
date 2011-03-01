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
package org.ow2.proactive.scheduler.ext.matlab.worker;

import org.jvnet.winp.WinProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.exception.MatlabInitException;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabFinder;
import org.ow2.proactive.scheduler.ext.matsci.common.DummyJVMProcess;
import org.ow2.proactive.scheduler.ext.matsci.worker.MatSciTask;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciJVMInfo;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciTaskServerConfig;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;


/**
 * This class represents a Matlab-specific Task inside the Scheduler
 *
 * @author The ProActive Team
 */
public class MatlabTask<W extends AOMatlabWorker> extends
        MatSciTask<W, MatlabEngineConfig, PASolveMatlabGlobalConfig, PASolveMatlabTaskConfig> {

    /**
     * the index when the input is the result of a SplitTask
     */
    protected int index = -1;

    /**
     * the lines of inputScript
     */
    protected String inputScript = null;

    /**
     * The lines of the Matlab script
     */
    protected ArrayList<String> scriptLines = null;

    /**
     * holds the Matlab environment information on this machine
     */
    protected static MatlabEngineConfig matlabEngineConfig = null;

    public static final String MatSciTaskConfigPath = "extensions/matlab/config/worker/MatSciTask.ini";

    /**
     *  the Active Object worker located in the spawned JVM
     */
    // protected static AOMatlabWorker matlabWorker = null;
    /**
     * Empty Constructor
     */
    public MatlabTask() {

    }

    /**
     * Convenience constructor
     *
     * @param inputScript script that will be launched and will produce an input to the main script
     * @param mainScript  main script to execute
     */
    public MatlabTask(String inputScript, String mainScript) {
        this.inputScript = inputScript;
        this.scriptLines = new ArrayList<String>();
        this.scriptLines.add(mainScript);
    }

    protected MatSciEngineConfig initEngineConfig(MatSciJVMInfo info) throws Throwable {
        // First we try to find MATLAB
        if (matlabEngineConfig == null) {
            if (paconfig.isDebug()) {
                System.out.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Looking for " + getExtensionName() + "...");
                outDebug.println("[" + new java.util.Date() + " " + host + " " +
                    this.getClass().getSimpleName() + "] Looking for " + getExtensionName() + "...");
            }
            matlabEngineConfig = (MatlabEngineConfig) MatlabEngineConfig.getCurrentConfiguration();
            if (matlabEngineConfig == null) {
                matlabEngineConfig = (MatlabEngineConfig) MatlabFinder.getInstance().findMatSci(
                        paconfig.getVersionPref(), paconfig.getVersionRej(), paconfig.getVersionMin(),
                        paconfig.getVersionMax());
                if (paconfig.isDebug()) {
                    System.out.println(matlabEngineConfig);
                    outDebug.println(matlabEngineConfig);
                }
                if (matlabEngineConfig == null)
                    throw new IllegalStateException("No valid Matlab configuration found, aborting...");
            }
        } else {
            MatlabEngineConfig conf = (MatlabEngineConfig) MatlabEngineConfig.getCurrentConfiguration();
            if (conf != null) {
                matlabEngineConfig = conf;
            }
        }
        info.setConfig(matlabEngineConfig);
        return matlabEngineConfig;
    }

    protected String getWorkerClassName() {
        return AOMatlabWorker.class.getName();
    }

    protected String getExtensionName() {
        return "Matlab";
    }

    protected MatSciTaskServerConfig getTaskServerConfig() throws Exception {
        String homestr = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
        File homesched = new File(homestr);
        File confPath = new File(homesched, MatSciTaskConfigPath);
        if (confPath.exists() && confPath.canRead()) {
            if (paconfig.isDebug()) {
                System.out.println("Loading MatlabTask config " + confPath);
                outDebug.println("Loading MatlabTask config " + confPath);
            }
            return MatSciTaskServerConfig.load(confPath);
        } else {
            if (paconfig.isDebug()) {
                System.out.println("Cannot find MatlabTask config " + confPath +
                    ", use default configuration");
                outDebug.println("Cannot find MatlabTask config " + confPath + ", use default configuration");
            }
            return new MatSciTaskServerConfig(false, -1, 2, 30, 5);
        }
    }

    protected void initPASolveConfig(Map<String, Serializable> args) {
        Object conf = args.get("global_config");
        if (conf != null) {
            paconfig = (PASolveMatlabGlobalConfig) conf;
        } else {
            paconfig = new PASolveMatlabGlobalConfig();
        }

        Object tconf = args.get("task_config");
        if (tconf != null) {
            taskconfig = (PASolveMatlabTaskConfig) tconf;
        } else {
            taskconfig = new PASolveMatlabTaskConfig();
        }
    }

    public void init(Map<String, Serializable> args) throws Exception {
        // Retrieving task parameters
        super.init(args);

        // main script to execute (embedded, url or file)
        Object s = args.get("script");

        if (s != null) {
            scriptLines = new ArrayList<String>();
            scriptLines.add((String) s);
        }

        URL scriptURL;

        Object u = args.get("scriptUrl");

        if (u != null) {
            scriptURL = new URI((String) u).toURL();

            InputStream is = scriptURL.openStream();
            scriptLines = IOTools.getContentAsList(is);
        }

        Object f = args.get("scriptFile");

        if (f != null) {
            FileInputStream fis = new FileInputStream((String) f);
            scriptLines = IOTools.getContentAsList(fis);
        }

        if (scriptLines.size() == 0) {
            throw new IllegalArgumentException(
                "Either one of \"script\" \"scripturl\" \"scriptfile\" must be given");
        }

        // an input script, launched before the main script (embedded only)
        Object input = args.get("input");

        if (input != null) {
            inputScript = (String) input;
        }

        String ts = (String) args.get("transferSource");
        if (ts != null) {
            paconfig.setTransferSource(Boolean.parseBoolean(ts));
        }

        String szfn = (String) args.get("sourceZipFileName");
        if (szfn != null) {
            paconfig.setSourceZipFileName(szfn);
        }

        String zfh = (String) args.get("zipFileHash");
        if (zfh != null) {
            paconfig.setSourceZipHash(zfh);
        }

        String te = (String) args.get("transferEnv");
        if (te != null) {
            paconfig.setTransferEnv(Boolean.parseBoolean(te));
        }

        String ezfn = (String) args.get("envZipFileName");
        if (ezfn != null) {
            paconfig.setEnvZipFileName(ezfn);
        }

    }

    @Override
    protected void afterExecute(MatSciJVMInfo jvminfo) {
        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Closing output");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Closing output");
            outDebug.close();
        }
        if (jvminfo.getLogger() != null) {
            jvminfo.getLogger().closeStream();
            jvminfo.getEsLogger().closeStream();
        }
        killReserveMatlabProcess();
    }

    private void killReserveMatlabProcess() {
        if (paconfig.isDebug()) {
            System.out.println("[" + new java.util.Date() + " " + host + " " +
                this.getClass().getSimpleName() + "] Killing selection process...");
            outDebug.println("[" + new java.util.Date() + " " + host + " " + this.getClass().getSimpleName() +
                "] Killing selection process...");
        }
        Process p = matlabEngineConfig.getSelectionScriptProcess();
        if (os.equals(OperatingSystem.windows)) {
            WinProcess pi = new WinProcess(p);
            try {
                if (paconfig.isDebug()) {
                    System.out.println("Killing process " + pi.getPid());
                    outDebug.println("Killing process " + pi.getPid());
                }
                Runtime.getRuntime().exec("taskkill /PID " + pi.getPid() + " /T");
                Runtime.getRuntime().exec("tskill " + pi.getPid());
                killProcessWindowsWithEnv("SELECTION_SCRIPT", nodeName);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            p.destroy();
        }
        matlabEngineConfig.setSelectionScriptProcess(p);
    }

    protected void initWorker(W worker) throws Throwable {
        worker.init(inputScript, scriptLines, paconfig, taskconfig, matlabEngineConfig);
    }

    public void initProcess(DummyJVMProcess javaCommandBuilder, Map<String, String> env) throws Throwable {

        // Classpath specific
        String classpath = prependPtolemyLibDirToClassPath(javaCommandBuilder.getClasspath());
        javaCommandBuilder.setClasspath(classpath);

        // we add matlab directories to LD_LIBRARY_PATH
        if (os.equals(OperatingSystem.unix)) {
            String libPath = env.get("LD_LIBRARY_PATH");
            env.put("LD_LIBRARY_PATH", addPtolemyLibDirToPath(addMatlabToPath(libPath)));
        }

        // used to kill the process later
        env.put("NODE_NAME", nodeName);

        // Used to avoid "Internal Error : A primary message table for module 54 was already registered with differing number of messages" crashes
        env.put("MATLAB", matlabEngineConfig.getFullCommand());

        // we add matlab directories to PATH (Windows)
        if (os.equals(OperatingSystem.windows)) {
            String path = env.get("Path");
            env.put("Path", addPtolemyLibDirToPath(addMatlabToPath(path)));
        }

        String jvlpath = addPtolemyLibDirToPath(null);

        // we set as well the java.library.path property (precaution), we forward as well the RMI port in use
        String options = javaCommandBuilder.getJvmOptions();
        options += " -Djava.library.path=\"" + jvlpath + "\"";
        javaCommandBuilder.setJvmOptions(options);

    }

    private String addPtolemyLibDirToPath(String path) {
        String newpath = path;
        if ((newpath != null) && (newpath != "")) {
            newpath = newpath + os.pathSeparator() + matlabEngineConfig.getPtolemyPath();
        } else {
            newpath = matlabEngineConfig.getPtolemyPath();
        }
        return newpath;
    }

    private String prependPtolemyLibDirToClassPath(String classPath) throws IOException, URISyntaxException,
            MatlabInitException {
        String newcp = classPath;
        newcp = matlabEngineConfig.getPtolemyPath() + os.pathSeparator() + newcp;
        return newcp;
    }

    /**
     * Utility function to add MATLAB directories to the given path-like string
     *
     * @param path path-like string
     * @return an augmented path
     */
    private String addMatlabToPath(String path) {
        String newPath;

        if ((path == null) || (path == "")) {
            newPath = "";
        } else {
            newPath = os.pathSeparator() + path;
        }

        newPath = (matlabEngineConfig.getMatlabHome() + os.fileSeparator() + matlabEngineConfig
                .getMatlabBinDir()) +
            newPath;
        newPath = (matlabEngineConfig.getMatlabHome() + os.fileSeparator() + matlabEngineConfig
                .getMatlabLibDirName()) +
            os.pathSeparator() + newPath;
        if (matlabEngineConfig.getMatlabExtDir() != null) {
            newPath = (matlabEngineConfig.getMatlabHome() + os.fileSeparator() + matlabEngineConfig
                    .getMatlabExtDir()) +
                os.pathSeparator() + newPath;
        }

        return newPath;
    }

}
