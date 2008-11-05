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
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


public class FlatJobFactory {

    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.FACTORY);

    private static final String CMD_FILE_COMMENT_CHAR = "#";

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
     * 
     * @param commandFilePath
     * @param jobName
     * @param selectionScriptPath
     * @param outputFile
     * @param userName
     * @return
     * @throws JobCreationException
     */
    public Job createNativeJobFromCommandsFile(String commandFilePath, String jobName,
            String selectionScriptPath, String outputFile, String userName) throws JobCreationException {

        if (jobName == null) {
            jobName = "Job_" + userName;
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
     * 
     * @param command
     * @param jobName
     * @param selectionScriptPath
     * @param outputFile
     * @param userName
     * @return
     * @throws JobCreationException
     */
    public Job createNativeJobFromCommand(String command, String jobName, String selectionScriptPath,
            String outputFile, String userName) throws JobCreationException {
        if (command == null || "".equalsIgnoreCase(command)) {
            throw new JobCreationException("Error, command cannot be null");
        }

        if (jobName == null) {
            jobName = "Job_" + userName;
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
     * @param command
     * @param taskName
     * @param selectionScriptPath
     * @return
     * @throws InvalidScriptException
     */
    private NativeTask createNativeTaskFromCommandString(String command, String taskName,
            String selectionScriptPath) throws InvalidScriptException {
        NativeTask desc = new NativeTask();
        desc.setCommandLine(command);
        desc.setName(taskName);

        if (selectionScriptPath != null) {
            SelectionScript script = new SelectionScript(
                new SimpleScript(new File(selectionScriptPath), null), true);
            desc.setSelectionScript(script);
        }
        return desc;
    }
}
