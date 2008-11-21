package org.ow2.proactive.scheduler.common.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.scripting.InvalidScriptException;
import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;
import org.ow2.proactive.resourcemanager.common.scripting.SimpleScript;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.scheduler.Tools;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


/**
 * This class implements static methods use to parse a text file containing commands, and from it build
 * a ProActive Scheduler job made of native tasks. each task of the jobs corresponds to a line of the 
 * parsed file. This is a way to avoid creation of XML job descriptor for creation of simple jobs.
 * 
 * Every line of the text file is taken and considered as a native command from which a native task is built, 
 * except lines beginning with {@value FlatJobFactory#CMD_FILE_COMMENT_CHAR} and empty lines.
 * dependencies between tasks cannot be set, task names are automatically set. A log file can be specified. 
 * A selection script can be associated for all the tasks, but not specific selection script for each tasks.
 * A Job name can be specified too.
 * 
 * This class does not intend to provide a job specification with all ProActive Scheduler jobs feature, but is 
 * way to define quickly jobs made of native tasks to execute in parallel.
 * If you need to define jobs with dependencies, jobs with java Tasks, specific selection script for each task,
 * or generation scripts... you should rather use XML job descriptors and {@link JobFactory}.
 * 
 * 
 * the class presents too a way to create a job made of one task from a String representing a native command to launch.
 * 
 * @author ProActive team
 *
 */
public class FlatJobFactory {

    /**
     * Log4j logger name 
     */
    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FACTORY);

    /**
     * comment character used to ignore line in text file containing
     * native commands
     */
    public static final String CMD_FILE_COMMENT_CHAR = "#";

    /**
     * String prefix used to build default job name (if no job name is specified).
     */
    public static final String JOB_DEFAULT_NAME_PREFIX = "Job_";

    /**
     * Singleton Pattern
     */
    private static FlatJobFactory factory = null;

    /**
     * Return the instance of the jobFactory.
     *
     * @return the instance of the jobFactory.
     */
    public static FlatJobFactory getFactory() {
        if (factory == null) {
            factory = new FlatJobFactory();
        }
        return factory;
    }

    /**
     * Create a job from a String representing file path, this text file contains native commands to launch
     * Every line of the text file is taken and considered as a native command from which a native task is built, 
     * except lines beginning with {@value FlatJobFactory#JOB_DEFAULT_NAME_PREFIX} and empty lines.
     * So job in result is made of several native tasks without dependencies.
     * 
     * @param commandFilePath a string representing a text file containing native commands.
     * @param jobName A String representing a name to give to the job. If null, default job name is made of 
     * {@value FlatJobFactory#JOB_DEFAULT_NAME_PREFIX} + userName parameter.
     * @param selectionScriptPath a Path to a file containing a selection script, or null if 
     * no script is needed.
     * @param outputFile a path to file that will contain log of STDOUT and STDERR of job's tasks execution.
     * @param userName name of connected user that asked job creation, null otherwise. This parameter
     * is only used for default job's name creation.
     * @return a job object representing created job and ready-to-schedule job.
     * @throws JobCreationException with a relevant error message if an error occurs.
     */
    public Job createNativeJobFromCommandsFile(String commandFilePath, String jobName,
            String selectionScriptPath, String outputFile, String userName) throws JobCreationException {

        if (jobName == null) {
            jobName = JOB_DEFAULT_NAME_PREFIX + userName;
        }
        Job nativeJob = new TaskFlowJob();
        nativeJob.setName(jobName);
        if (outputFile != null) {
            nativeJob.setLogFile(outputFile);
        }

        logger.debug("Job : " + nativeJob.getName());

        try {
            File commandFile = new File(commandFilePath);
            if (!commandFile.isFile()) {
                throw new JobCreationException("Error occured during Job creation, " + "check that file " +
                    commandFilePath + " exists and is a readable file");
            }
            String commandLine;
            int task_number = 0;

            BufferedReader reader = new BufferedReader(new FileReader(commandFile));
            ArrayList<String> commandList = new ArrayList<String>();
            while ((commandLine = reader.readLine()) != null) {
                commandLine = commandLine.trim();
                if (!commandLine.startsWith(CMD_FILE_COMMENT_CHAR, 0) && !"".equals(commandLine)) {
                    commandList.add(commandLine);
                }
            }
            if (commandList.size() == 0) {
                throw new JobCreationException("Error occured during Job creation, " +
                    "No any valid command line has been built from" + commandFilePath + "");
            }

            //compute padding for task number
            int numberOfDigit = Integer.toString(commandList.size()).length();
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumIntegerDigits(numberOfDigit);
            nf.setMinimumIntegerDigits(numberOfDigit);

            for (String command : commandList) {
                NativeTask t = createNativeTaskFromCommandString(command, "task_" +
                    (nf.format(++task_number)), selectionScriptPath);
                t.setPreciousResult(true);
                ((TaskFlowJob) nativeJob).addTask(t);
                logger.debug("-> Task Name = " + t.getName());
                logger.debug("-> command = " + t.getCommandLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            throw new JobCreationException(e.getMessage(), e);
        } catch (IOException e) {
            throw new JobCreationException(e.getMessage(), e);
        } catch (InvalidScriptException e) {
            throw new JobCreationException(e.getMessage(), e);
        } catch (UserException e) {
            throw new JobCreationException(e.getMessage(), e);
        }
        return nativeJob;
    }

    /**
     * Creates a job from a String representing a native command to launch. So job in result is made
     * of one native task.
     * 
     * @param command a string representing an executable command to launch.
     * @param jobName A String representing a name to give to the job, if null. default job name is made of 
     * {@value FlatJobFactory#JOB_DEFAULT_NAME_PREFIX} + userName parameter.
     * @param selectionScriptPath A Path to a file containing a selection script, or null if 
     * no script is needed.
     * @param outputFile a path to file that will contain log of STDOUT and STDERR of the task's execution
     * @param userName name of connected user that asked job creation, null otherwise. This parameter
     * is just used for default job's name creation.
     * @return a job object representing created job and ready-to-schedule job.
     * @throws JobCreationException with a relevant error message if an error occurs.
     */
    public Job createNativeJobFromCommand(String command, String jobName, String selectionScriptPath,
            String outputFile, String userName) throws JobCreationException {
        if (command == null || "".equalsIgnoreCase(command)) {
            throw new JobCreationException("Error, command cannot be null");
        }

        if (jobName == null) {
            jobName = JOB_DEFAULT_NAME_PREFIX + userName;
        }
        Job nativeJob = new TaskFlowJob();
        nativeJob.setName(jobName);
        if (outputFile != null) {
            nativeJob.setLogFile(outputFile);
        }

        logger.debug("Job : " + nativeJob.getName());
        try {
            NativeTask t = createNativeTaskFromCommandString(command, "task1", selectionScriptPath);
            t.setPreciousResult(true);
            ((TaskFlowJob) nativeJob).addTask(t);
            logger.debug("-> Task Name = " + t.getName());
            logger.debug("-> command = " + t.getCommandLine() + "\n");
        } catch (UserException e) {
            throw new JobCreationException(e.getMessage(), e);
        } catch (InvalidScriptException e) {
            throw new JobCreationException(e.getMessage(), e);
        }
        return nativeJob;
    }

    /**
     * Creates a native task from a string representing a native command to execute.
     * @param command a String representing a native command.
     * @param taskName an eventual name for the task.
     * @param selectionScriptPath path to an existing file containing a selection script code.
     * @return a NativeTask object that can be put in a Job Object.
     * @throws InvalidScriptException if an error occurs in definition of selection script
     * from file path specified.
     */
    private NativeTask createNativeTaskFromCommandString(String command, String taskName,
            String selectionScriptPath) throws InvalidScriptException {
        NativeTask desc = new NativeTask();
        desc.setCommandLine(Tools.parseCommandLine(command));
        desc.setName(taskName);

        if (selectionScriptPath != null) {
            SelectionScript script = new SelectionScript(
                new SimpleScript(new File(selectionScriptPath), null), true);
            desc.setSelectionScript(script);
        }
        return desc;
    }
}
