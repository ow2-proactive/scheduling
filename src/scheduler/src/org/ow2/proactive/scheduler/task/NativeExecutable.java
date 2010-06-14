/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.exception.RunningProcessException;
import org.ow2.proactive.scheduler.exception.StartProcessException;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.SchedulerVars;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scheduler.util.process.ProcessTreeKiller;
import org.ow2.proactive.scheduler.util.process.ThreadReader;
import org.ow2.proactive.scripting.PropertyUtils;
import org.ow2.proactive.scripting.GenerationScript;
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

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    private static String GENERATION_SCRIPT_ERR = "\nNo command eligible was found by generation script.\n"
        + "A generation script must define a variable named 'command' which contains "
        + "the native command to launch. \n" + "Script details :\n";

    /**
     * Environment variable exported to the the process
     * used for kill the task
     */
    private static String COOKIE_ENV = "PROACTIVE_COOKIE";
    /** Env var exported for the number and name of nodes */
    private static String CORE_FILE_ENV = "PAS_NODEFILE";
    private static String CORE_NB = "PAS_CORE_NB";

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

    /** Env vars used by system call */
    private String[] envVarsTab;

    /** Generated command */
    private String[] command;

    /** File used to store nodes URL */
    private File nodesFiles = null;

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
                logger_dev.debug("Working dir set to : " + wDirFile.getAbsolutePath());
            }
        } else {
            logger_dev.debug("Working dir not set !");
        }

        //get command (launch generation Script if needed)
        command = execInitializer.getCommand();
        GenerationScript gs = execInitializer.getGenerationScript();
        if (gs != null) {
            String generationScriptDefinedCommand = this.executeGenerationScript(gs);

            //no command has been returned by generation script
            if ((generationScriptDefinedCommand == null) ||
                generationScriptDefinedCommand.equals(GenerationScript.DEFAULT_COMMAND_VALUE)) {

                logger_dev.error(GENERATION_SCRIPT_ERR + gs.getId());

                throw new UserException(GENERATION_SCRIPT_ERR + gs.getId());
            } else {
                //generation script has defined a command, so set the command to launch
                command = Tools.parseCommandLine(generationScriptDefinedCommand);
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

        //set environment variables
        this.setEnvironmentVariables(nodes.size());
    }

    /**
     * Execute the generationScript on the default node
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     * @return the value of the variable GenerationScript.COMMAND_NAME after the script evaluation.
     */
    @SuppressWarnings("unchecked")
    private String executeGenerationScript(GenerationScript script) throws ActiveObjectCreationException,
            NodeException, UserException {
        ScriptHandler handler = ScriptLoader.createHandler(PAActiveObject.getNode());
        ScriptResult<String> res = handler.handle(script);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("", res.getException());
            throw new UserException("Command generation script execution has failed on the current node");
        }

        return res.getResult();
    }

    /**
     * Build environment variables for this native executable : the task's environment variables :
     * (task name job name, job id, task id...), system environment variables of the JVM,
     * and the cookie environment variable used by ProcessTreeKiller
     *
     */
    private void setEnvironmentVariables(int coresNumber) {
        //Set Model environment variable HashMap for the executable;
        //if this process must be killed by ProcessTreeKiller
        String cookie_value = ProcessTreeKiller.createCookie();
        modelEnvVar = new HashMap<String, String>();
        modelEnvVar.put(COOKIE_ENV, cookie_value);

        //Set environment variables array used to launch the external native command,
        //with system environment variables, ProActive Scheduling environment variables,
        //and ProcessTreeKiller environment variable (cookie)

        Map<String, String> taskEnvVariables = new Hashtable<String, String>(4);
        taskEnvVariables.put(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString()));
        taskEnvVariables.put(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString(), System
                .getProperty(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString()));

        // exported properties
        String allVars = System.getProperty(PropertyUtils.EXPORTED_PROPERTIES_VAR_NAME);
        Map<String, String> taskExportedProperties = null;
        if (allVars != null) {
            StringTokenizer parser = new StringTokenizer(allVars, PropertyUtils.VARS_VAR_SEPARATOR);
            taskExportedProperties = new Hashtable<String, String>(parser.countTokens());
            while (parser.hasMoreTokens()) {
                String key = parser.nextToken();
                String value = System.getProperty(key);
                if (value != null) {
                    logger_dev.debug("Value of exported property " + key + " is " + value);
                    taskExportedProperties.put(key, value);
                } else {
                    logger_dev.warn("Exported property " + key + " is not set !");
                }
            }
            System.clearProperty(PropertyUtils.EXPORTED_PROPERTIES_VAR_NAME);
        }

        // current env
        Map<String, String> systemEnvVariables = System.getenv();

        int i = 0;
        int nbVars = taskEnvVariables.size() + systemEnvVariables.size() +
            ((taskExportedProperties != null) ? taskExportedProperties.size() : 0);
        if (nodesFiles != null) {
            envVarsTab = new String[nbVars + 3];
            envVarsTab[i++] = CORE_FILE_ENV + "=" + nodesFiles.getAbsolutePath();
        } else {
            envVarsTab = new String[nbVars + 2];
        }

        envVarsTab[i++] = CORE_NB + "=" + coresNumber;

        //first we add to the returnTab the task environment variables
        for (Map.Entry<String, String> entry : taskEnvVariables.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            envVarsTab[i++] = convertJavaenvNameToSysenvName(name) + "=" + value;
        }

        //after we add to the returnTab the system environment variables
        for (Map.Entry<String, String> entry : systemEnvVariables.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            envVarsTab[i++] = name + "=" + value;
        }

        //then we add exported properties
        if (taskExportedProperties != null) {
            for (Map.Entry<String, String> entry : taskExportedProperties.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                envVarsTab[i++] = convertJavaenvNameToSysenvName(name) + "=" + value;
            }
        }

        //then the cookie used by ProcessTreeKiller
        envVarsTab[i] = COOKIE_ENV + "=" + cookie_value;
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
                process = Runtime.getRuntime().exec(this.command, this.envVarsTab, this.wDirFile);
            } catch (Exception e) {
                //in this case, the error is certainly due to the user (ie : command not found)
                //we have to inform him about the cause.
                logger_dev.info("", e);
                System.err.println(e);
                throw new StartProcessException(e.getMessage());
            }

            try {
                // redirect streams
                BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader serr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                Thread tsout = new Thread(new ThreadReader(sout, System.out, this));
                Thread tserr = new Thread(new ThreadReader(serr, System.err, this));
                tsout.start();
                tserr.start();
                // wait for process completion
                process.waitFor();
                // wait for log flush
                tsout.join();
                tserr.join();

                //killTreeProcess(process);
                return process.exitValue();
            } catch (Exception e) {
                logger_dev.error("", e);
                //exception during process
                //means that for most cases, user is not responsible
                throw new RunningProcessException(e.getMessage());
            }
        } finally {
            if (nodesFiles != null) {
                nodesFiles.delete();
            }
        }
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
            ProcessTreeKiller.get().kill(process, modelEnvVar);
            //WARN jlscheef destroy() may be useless but it's not working yet without it.
            //processTreeKiller seems not to kill current process...
            process.destroy();
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
}
