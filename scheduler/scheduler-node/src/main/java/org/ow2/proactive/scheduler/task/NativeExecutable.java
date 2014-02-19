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
package org.ow2.proactive.scheduler.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.exception.RunningProcessException;
import org.ow2.proactive.scheduler.exception.StartProcessException;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.OneShotDecrypter;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.SchedulerVars;
import org.ow2.proactive.scheduler.task.launcher.utils.ForkerUtils;
import org.ow2.proactive.rm.util.process.ProcessTreeKiller;
import org.ow2.proactive.scheduler.util.process.ThreadReader;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.PropertyUtils;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.Tools;


/**
 * This is the execution entry point for the native task.
 * The execute(TaskResult...) method will be override by the scheduler to launch the native process.
 * This class provide a getProcess method that will return the current running native process.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class NativeExecutable extends Executable {

    public static final Logger logger = Logger.getLogger(NativeExecutable.class);

    private static String GENERATION_SCRIPT_ERR = "\nNo command eligible was found by generation script.\n" +
        "A generation script must define a variable named '" + GenerationScript.RESULT_VARIABLE +
        "' or a variable named '" + GenerationScript.RESULTLIST_VARIABLE + "' which contains " +
        "the native command to launch. \n" + "Script details :\n";

    /**
     * Environment variable exported to the the process
     * used for kill the task
     */
    private static String COOKIE_ENV = "PROACTIVE_COOKIE";
    /** Env var exported for the number and name of nodes */
    private static String CORE_FILE_ENV = "PAS_NODESFILE";
    private static String CORE_NB = "PAS_NODESNUMBER";
    // BACKWARD COMPATIBILITY
    private static String LEGACY_CORE_FILE_ENV = "PAS_NODEFILE";
    private static String LEGACY_CORE_NB = "PAS_CORE_NB";

    /** Process that start the native task */
    private transient Process process;

    /** file use to set the working dir of the native command */
    private File wDirFile;

    /**
     * HM of environment variables used
     * for kill action of the task, processes that export
     * theses environment variables will be killed.
     * Used by ProcessTreeKiller
     */
    private Map<String, String> modelEnvVar = null;

    /**
     * additional environment variables
     */
    private Map<String, String> otherEnvVar = new HashMap<String, String>();

    /** Generated command */
    private String[] command;

    // DO NOT RENAME nodesfiles and nodesNumber FIELDS
    // see NativeTaskLauncher.replaceCommandNodesInfosTags()

    /** File used to store nodes URL */
    private File nodesFiles = null;

    /** Nodes number */
    private int nodesNumber = 1;

    /** Decrypter to start native process */
    private OneShotDecrypter decrypter = null;

    /**
     * Initialize the executable using the given executable container.
     *
     * @param execInitializer the executable initializer used to init the executable itself
     *
     * @throws Exception an exception if something goes wrong during executable initialization.
     */
    // WARNING WHEN REMOVE OR RENAME, called by task launcher by introspection
    private void internalInit(NativeExecutableInitializer execInitializer) throws Exception {
        //set working dir file
        String wDir = execInitializer.getWorkingDir();
        if (wDir != null && !"".equals(wDir)) {
            File wDirFile = new File(wDir);
            if (wDirFile.exists() && wDirFile.isDirectory()) {
                this.wDirFile = wDirFile;
                logger.debug("Working dir set to : " + wDirFile.getAbsolutePath());
            }
        } else {
            logger.debug("Working dir not set !");
        }

        //get command (launch generation Script if needed)
        command = execInitializer.getCommand();
        GenerationScript gs = execInitializer.getGenerationScript();
        if (gs != null) {
            Object generationScriptDefinedCommand = this.executeGenerationScript(gs);

            //no command has been returned by generation script
            if ((generationScriptDefinedCommand == null) ||
                generationScriptDefinedCommand.equals(GenerationScript.DEFAULT_COMMAND_VALUE)) {

                logger.error(GENERATION_SCRIPT_ERR + gs.getId());

                throw new UserException(GENERATION_SCRIPT_ERR + gs.getId());
            } else {
                //generation script has defined a command, so set the command to launch
                String commandLine = null;
                if (generationScriptDefinedCommand instanceof String) {
                    command = Tools.parseCommandLine((String) generationScriptDefinedCommand);
                } else {
                    command = ((List<String>) generationScriptDefinedCommand).toArray(new String[0]);
                }
            }
        }

        // build a file containing list of cores for the jobs, if several cores have been booked
        List<String> nodes = execInitializer.getNodesHost();
        if (nodes != null && nodes.size() > 1) {
            nodesFiles = File.createTempFile("pa_nodes", null);
            FileWriter outputWriter = new FileWriter(nodesFiles);
            for (String nodeHost : nodes) {
                outputWriter.append(nodeHost + System.getProperty("line.separator"));
            }
            outputWriter.close();
        }
        nodesNumber = nodes.size();
        //set decrypter
        this.decrypter = execInitializer.getDecrypter();
    }

    /**
     * Execute the generationScript on the default node
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     * @return the value of the variable GenerationScript.COMMAND_NAME after the script evaluation.
     */
    @SuppressWarnings("unchecked")
    private Object executeGenerationScript(GenerationScript script) throws ActiveObjectCreationException,
            NodeException, UserException {
        ScriptHandler handler = ScriptLoader.createHandler(PAActiveObject.getNode());
        ScriptResult<Object> res = handler.handle(script);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger.error("", res.getException());
            throw new UserException("Command generation script execution has failed on the current node");
        }

        return res.getResult();
    }

    /**
     * Adds the given variable to the process environment
     * @param name name of the environment variable
     * @param value value of the environment variable
     */
    public void addToEnvironmentVariables(String name, String value) {
        otherEnvVar.put(name, value);
    }

    /**
     * Build environment variables for this native executable : the task's environment variables :
     * (task name job name, job id, task id...), system environment variables of the JVM,
     * and the cookie environment variable used by ProcessTreeKiller
     * @param processEnvAvailable is true if environment can be passed to forked process
     * if env cannot be passed, throws exception if user intend to alter env.
     */
    private Map<String, String> buildEnvironmentVariables(boolean processEnvAvailable) {
        //Set Model environment variable HashMap for the executable;
        //if this process must be killed by ProcessTreeKiller
        String cookie_value = ProcessTreeKiller.createCookie();
        modelEnvVar = new HashMap<String, String>();
        modelEnvVar.put(COOKIE_ENV, cookie_value);

        Map<String, String> envVarsTab = new HashMap<String, String>();
        //Set environment variables array used to launch the external native command,
        //with system environment variables, ProActive Scheduling environment variables,
        Map<String, String> taskEnvVariables = new Hashtable<String, String>(15);
        taskEnvVariables.put(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_TASK_ITERATION.toString(), System
                .getProperty(SchedulerVars.JAVAENV_TASK_ITERATION.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_TASK_REPLICATION.toString(), System
                .getProperty(SchedulerVars.JAVAENV_TASK_REPLICATION.toString()));
        //add to the returnTab the task environment variables
        for (Map.Entry<String, String> entry : taskEnvVariables.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            envVarsTab.put(convertJavaenvNameToSysenvName(name), value);
        }

        // adds the extra environment variable (coming from the TaskLauncher)
        envVarsTab.putAll(otherEnvVar);

        //get exported properties
        String allVars = System.getProperty(PropertyUtils.EXPORTED_PROPERTIES_VAR_NAME);
        Map<String, String> taskExportedProperties = null;
        if (allVars != null) {
            StringTokenizer parser = new StringTokenizer(allVars, PropertyUtils.VARS_VAR_SEPARATOR);
            taskExportedProperties = new Hashtable<String, String>(parser.countTokens());
            while (parser.hasMoreTokens()) {
                String key = parser.nextToken();
                String value = System.getProperty(key);
                if (value != null) {
                    logger.debug("Value of exported property " + key + " is " + value);
                    taskExportedProperties.put(key, value);
                } else {
                    logger.warn("Exported property " + key + " is not set !");
                }
            }
            System.clearProperty(PropertyUtils.EXPORTED_PROPERTIES_VAR_NAME);
        }
        //add exported properties
        if (taskExportedProperties != null) {
            //TODO SCHEDULING-986 : remove if when environment can be modified with runAsMe
            //processEnvAvailable is true if environment can be passed to forked process
            //if env cannot be passed, throws exception if user intend to alter env.
            if (!processEnvAvailable && taskExportedProperties.size() > 0) {
                throw new StartProcessException(
                    "Process cannot be started because user intend to update system environment using exported property. This is not possible when runAsMe is activated.");
            }
            for (Map.Entry<String, String> entry : taskExportedProperties.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                envVarsTab.put(convertJavaenvNameToSysenvName(name), value);
            }
        }

        //add core number and core file
        if (nodesFiles != null) {
            final String nodesFilePath = nodesFiles.getAbsolutePath();
            envVarsTab.put(CORE_FILE_ENV, nodesFilePath);
            envVarsTab.put(LEGACY_CORE_FILE_ENV, nodesFilePath);
        }
        envVarsTab.put(CORE_NB, "" + this.nodesNumber);
        envVarsTab.put(LEGACY_CORE_NB, "" + this.nodesNumber);

        //then the cookie used by ProcessTreeKiller
        envVarsTab.put(COOKIE_ENV, cookie_value);

        return envVarsTab;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) {
        try {
            //WARNING : if this.command is unknown, it will create a defunct process
            //it's due to a known java bug
            try {
                process = createProcessAndStart();
            } catch (Exception e) {
                //in this case, the error is certainly due to the user (ie : command not found)
                //we have to inform him about the cause.
                logger.info("", e);
                System.err.println(e);
                throw new StartProcessException(e.getMessage(), e);
            }

            try {
                // redirect streams
                BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Thread tsout = new Thread(new ThreadReader(sout, System.out, this));
                tsout.start();
                // wait for process completion
                process.waitFor();
                // wait for log flush
                // In case the process spawned other native subprocesses, the join will ensure to wait until those processes
                // are properly finished (indeed the IO Stream should remain alive while the subprocesses live)
                // This may look unuseful to do so, but often end-user programs are started via a simple batch or bash script which terminates
                // quickly but starts and configure a main executable program. Without this wait, our task would stop
                // instead of waiting for the main program to finish
                tsout.join();

                //killTreeProcess(process);
                return process.exitValue();
            } catch (Exception e) {
                logger.error("", e);
                //exception during process
                //means that for most cases, user is not responsible
                throw new RunningProcessException(e.getMessage(), e);
            }
        } finally {
            if (nodesFiles != null) {
                nodesFiles.delete();
            }
        }
    }

    /**
     * Creating a child native process, intercepting stdout and stderr
     * Also ask for credentials with new generated keypair if needed
     *
     * @throws IOException
     */
    private Process createProcessAndStart() throws Exception {
        //build process
        OSProcessBuilder ospb = null;
        //check if it must be run under user and if so, apply the proper method
        if (isRunAsUser()) {
            ospb = ForkerUtils.getOSProcessBuilderFactory().getBuilder(
                    ForkerUtils.checkConfigAndGetUser(this.decrypter));
        } else {
            ospb = ForkerUtils.getOSProcessBuilderFactory().getBuilder();
        }
        ospb.redirectErrorStream(true);
        //add command and directory
        ospb.command(this.command);
        ospb.directory(this.wDirFile);
        //manage environment
        try {
            //the following line can throw NotImplementedException
            Map<String, String> env = ospb.environment();
            //if no exception was raised, add environment with no restriction
            env.putAll(buildEnvironmentVariables(true));
        } catch (NotImplementedException e) {
            //TODO SCHEDULING-986 : remove catch block when environment can be modified with runAsMe
            //if NotImplementedException was raised, just check for user environment modification
            //as environment cannot be passed to sub process, the following method just check the user
            //has not modified it. Throws an exception if modified, just do nothing if not modified.
            buildEnvironmentVariables(false);
        }
        logger.info("Running command : " + Arrays.asList(this.command));
        logger.info("in directory : " + this.wDirFile);
        //and start process
        return ospb.start();
    }

    /**
     * Return true if this task is to be ran under a user account id or not.
     *
     * @return true if this task is to be ran under a user account id, false otherwise.
     */
    private boolean isRunAsUser() {
        return this.decrypter != null;
    }

    /**
     * interrupt native process and its children (if launched)
     * set killedState boolean to finalize ThreadReaders 
     * which listen SDTOUT/STDERR of the native process
     */
    @Override
    public void kill() {
        super.kill();
        if (process != null) {
            try {
                ProcessTreeKiller.get().kill(process, modelEnvVar);
                // WARN jlscheef destroy() may be useless but it's not working
                // yet without it.
                // processTreeKiller seems not to kill current process...
            } catch (Throwable e) {
                logger.info("Unable to kill " + command[0] + " process", e);
            } finally {
                process.destroy();
            }
        }
    }

    /**
     * Get the command
     *
     * @return the command
     */
    public String[] getCommand() {
        return command;
    }

    /**
     * Convert the Java Property name to the System environment name
     *
     * @param javaPropName the java Property name.
     * @return the System environment name.
     */
    private static String convertJavaenvNameToSysenvName(String javaPropName) {
        return javaPropName.toUpperCase().replace('.', '_');
    }

    /** System property Key of the get progress native file directory */
    private static final String GETPROGRESS_DIRECTORY_KEY = "pas.launcher.getprogress.directory";
    private final static String DEFAULT_PATH;
    private final static int NB_TRIES_BEFORE_GIVEUP = 10;
    private int nbTries = 0;
    private long lastModified = 0;

    static {
        String dir = System.getProperty(GETPROGRESS_DIRECTORY_KEY);
        if (dir != null && new File(dir).isDirectory()) {
            DEFAULT_PATH = dir;
        } else {
            DEFAULT_PATH = System.getProperty("java.io.tmpdir") + File.separator + "job_progress" +
                File.separator;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProgress() {
        if (nbTries > NB_TRIES_BEFORE_GIVEUP) {
            //max tries has been reached, always return previous value
            //means user do not use progress value update
            return super.getProgress();
        }
        //get taskId
        String tid = System.getProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString());
        //get tmp file where to read the progress value
        File f = new File(DEFAULT_PATH, tid + ".progress");
        if (!f.exists()) {
            //f does not exist, increment nbTries and return previous value
            nbTries++;
            return super.getProgress();
        }
        //f exists
        long lm = f.lastModified();
        //check if file has been modified since the last getProgress
        if (lm > lastModified) {
            FileReader fr = null;
            BufferedReader br = null;
            try {
                fr = new FileReader(f);
                br = new BufferedReader(fr);
                //following line can cause a NPE, means value is not readable
                //will go to the catch clause and the progress won't be modified
                String s = br.readLine().trim();
                super.setProgress(Integer.parseInt(s));
                lastModified = lm;
            } catch (FileNotFoundException e) {
                //should not happen as file exist here
            } catch (Exception e) {
                //in any case here, just drop the read and return the previous value
            } finally {
                try {
                    br.close();
                    fr.close();
                } catch (Throwable t) {
                    //save catch
                }
            }
        }
        //in any case, return the up-to-date progress
        return super.getProgress();
    }
}
