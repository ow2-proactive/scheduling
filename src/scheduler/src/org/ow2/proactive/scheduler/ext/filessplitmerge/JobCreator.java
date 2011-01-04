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
package org.ow2.proactive.scheduler.ext.filessplitmerge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.InvalidInputDataException;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.NotInitializedException;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;
import org.ow2.proactive.scheduler.ext.filessplitmerge.util.MySchedulerProxy;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.utils.Tools;


/**
 * This is an abstract class that creates and submits a job to the Scheduler see
 * javadoc for {@link #createNativeTask(File, int)} method in order to
 * understand the methods of this class that you must override in order to
 * enable tasks creation
 * 
 * @author esalagea
 * 
 */
public abstract class JobCreator {

    public static int DEFAULT_MAX_NB_OF_EXECUTION_PER_TASK = 5;

    /**
     * Creates a TaskFlow job and submits it to the Scheduler.<p/> The
     * scheduler url, username and password are the ones that have been used to
     * initiate the {@link EmbarrasinglyParrallelApplication} singleton object
     * <p/> this method calls
     * {@link #submitJob(String, String, String, String, String, JobPriority, boolean, String, JobConfiguration)}
     * 
     * @param jobName
     * @param jobDescription
     * @param priority
     * @param cancelJobOnTaskError
     *            if set to true, the job will be canceled if a task fails
     * @param logsFilePath
     *            -the file where the logs for this job (i.e. the stdout for the
     *            remote processes is to be stored)
     * @param jobConfiguration -
     *            an object containing the configuration for this job. This
     *            object will be passed in parameter, when the job finishes, to
     *            the
     *            {@link JobPostTreatmentManager#mergeResults(JobConfiguration, int)}
     * @throws SchedulerException
     * @throws LoginException
     * @throws IOException
     * @throws InvalidScriptException
     * @throws InvalidInputDataException
     * @throws NotInitializedException
     */
    public void submitJob(String jobName, String jobDescription, JobPriority priority,
            boolean cancelJobOnTaskError, String logsFilePath, JobConfiguration jobConfiguration)
            throws SchedulerException, IOException, InvalidScriptException, InvalidInputDataException,
            NotInitializedException {
        String schedulerURL = EmbarrasinglyParrallelApplication.instance().getScheduelrURL();
        String username = EmbarrasinglyParrallelApplication.instance().getUserName();
        String password = EmbarrasinglyParrallelApplication.instance().getSchedulerPassword();
        this.submitJob(jobName, jobDescription, schedulerURL, username, password, priority,
                cancelJobOnTaskError, logsFilePath, jobConfiguration);
    }

    /**
     * Creates and submits a TaskFlow job to the scheduler. The job is composed
     * by a number of n native tasks: - the number n of native tasks is given by
     * the the size of the list returned by the <{@link #splitData()} method
     * (each member of the list represents the input data for a single task) -
     * the command line for a task is given by the <{@link #createCommandLineForTask(File, int) }
     * method - the pre-script, post-script and clean-script filePaths and
     * arguments are given by the respective methods - the restart mode and the
     * result preview for each task are given by the respective methods
     * 
     * @param jobName
     * @param jobDescription
     * @param schedulerURL
     * @param username
     * @param password
     * @param priority
     * @param cancelJobOnTaskError
     *            if set to true, the job will be canceled if a task fails
     * @param logsFilePath
     *            -the file where the logs for this job (i.e. the stdout for the
     *            remote processes is to be stored)
     * @param jobConfiguration -
     *            an object containing the configuration for this job. This
     *            object will be passed in parameter, when the job finishes, to
     *            the
     *            {@link JobPostTreatmentManager#mergeResults(JobConfiguration, int)}
     * @throws SchedulerException
     * @throws LoginException
     * @throws IOException
     * @throws InvalidScriptException
     * @throws InvalidInputDataException
     * @throws NotInitializedException
     */
    public void submitJob(String jobName, String jobDescription, String schedulerURL, String username,
            String password, JobPriority priority, boolean cancelJobOnTaskError, String logsFilePath,
            JobConfiguration jobConfiguration) throws SchedulerException, IOException,
            InvalidScriptException, InvalidInputDataException, NotInitializedException {

        //		SchedulerAuthenticationInterface auth = SchedulerConnection
        //				.join(schedulerURL);
        //		UserSchedulerInterface uischeduler = auth.logAsUser(username, password);

        TaskFlowJob job = new TaskFlowJob();

        job.setName(jobName);
        job.setDescription(jobDescription);

        job.setPriority(priority);
        job.setCancelJobOnError(cancelJobOnTaskError);

        List<File> splittedData = splitData();
        List<NativeTask> tasks = null;
        tasks = createTasks(splittedData);
        Iterator<NativeTask> it = tasks.iterator();
        while (it.hasNext()) {
            NativeTask nt = it.next();
            try {
                job.addTask(nt);
            } catch (UserException e2) {
                e2.printStackTrace();
            }
        }

        if ((logsFilePath != null) && (!logsFilePath.trim().equals("")))
            job.setLogFile(logsFilePath);

        attachGenericInformationtoJob(jobConfiguration, job);
        setJobClasPath(job);

        // ******************** SUBMIT THE JOB ***********************
        // submitting a job to the scheduler returns the corresponding jobId
        // this id will be used to talk to the scheduler about this job.
        JobId jobId = null;
        try {
            jobId = MySchedulerProxy.getActiveInstance().submit(job);
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        // Add this id to the GoldJobPostTreatmentManager as an awaited job
        // This way, the GoldJobPostTreatmentManager will merge the results of
        // this
        // job when it is finished
        JobPostTreatmentManagerHolder.getPostTreatmentManager().addAwaitedJob(jobId.value());
    }

    /**
     * This method adds to the job the classpath of the current application. It
     * calls {@link Job#setEnvironment(JobEnvironment)} method As the job
     * created by the {@link JobCreator} only contains NativeTasks, the
     * classpath will only be needed in order to show the results preview in the
     * Scheduler GUI
     * 
     * 
     * Note: The classpsth is serialized and sent to the Scheduler server as
     * byte array. If you don't use result preview in your application,there's
     * no need for this method to be called. Therefore, if
     * {@link #getDefaultResultPreviewClassName()} returns null or an empty
     * String, this method will return
     * 
     * 
     * IMPORTNAT: This method modifies the Job object in argument
     * 
     * @param job
     */
    protected void setJobClasPath(Job job) {

        if ((getDefaultResultPreviewClassName() == null) ||
            (getDefaultResultPreviewClassName().trim().equals("")))
            return;

        String appClassPath = "";
        try {
            File appMainFolder = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            appClassPath = appMainFolder.getAbsolutePath();

        } catch (URISyntaxException e1) {
            LoggerManager
                    .getInstane()
                    .warning(
                            "Preview of the partial results will not be possible as some ressources could not be found by the system. \nThis will not alterate your results in any way. ");
            LoggerManager
                    .getLogger()
                    .warn(
                            "JobCreator: The bin folder of the project is null. It is needed to set the job environment. ",
                            e1);
        }

        JobEnvironment je = new JobEnvironment();
        try {
            je.setJobClasspath(new String[] { appClassPath });
        } catch (IOException e) {

            LoggerManager
                    .getInstane()
                    .warning(
                            "Preview of the partial results will not be possible as the job classpath could not be loaded. \nThis will not alterate your results in any way.");
            LoggerManager.getLogger().warn("Could not add classpath to the job. ", e);
        }
        job.setEnvironment(je);
    }

    /**
     * Attach all the attributes A (given by pair of getA()/setA()) in the
     * jobConfigObject as generic information to the job. The key of the generic
     * information is the attribute name (i.e. A) and the value is the result of
     * the getter. This only applies for the attributes of type String.
     * 
     * @param jobConfig
     * @param job
     */
    private void attachGenericInformationtoJob(JobConfiguration jobConfig, TaskFlowJob job) {
        Class<? extends JobConfiguration> clazz = jobConfig.getClass();
        Method[] ms = clazz.getMethods();

        for (int i = 0; i < ms.length; i++) {
            Method m = ms[i];
            if ((m.getName().startsWith("get")) && (m.getReturnType() == String.class) &&
                (m.getParameterTypes().length == 0)) {

                try {
                    String value = (String) m.invoke(jobConfig, new Object[] {});
                    String property = m.getName().substring(3);
                    job.addGenericInformation(property, value);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }// if method is a getter with no args which returns string
        }// for all methods
    }

    /**
     * Creates and returns a list of {@link NativeTask} the number of tasks
     * created is equal to the number of files in the splitData argument For
     * each File in the splitData, the {@link #createNativeTask(File, int)}
     * method will be called.
     * 
     * @param splitData
     * @return
     * @throws IOException
     * @throws InvalidScriptException
     * @throws InvalidInputDataException
     */
    protected List<NativeTask> createTasks(List<File> splitData) throws IOException, InvalidScriptException,
            InvalidInputDataException {
        LinkedList<NativeTask> tasks = new LinkedList<NativeTask>();
        int taskNb = 1;
        Iterator<File> filesIt = splitData.iterator();
        while (filesIt.hasNext()) {
            File file = filesIt.next();
            NativeTask t = createNativeTask(file, taskNb);
            tasks.add(t);
            taskNb++;
        }
        return tasks;
    }

    /**
     * Creates a generation script if and only if the
     * {@link #getGenerationScriptFilePath()} does not return null If no file
     * path for the script is returned then this method return null which
     * implies that the {@link #createCommandLineForTask(File, int)} method will
     * be used for the native task command. if this method's return value is not
     * null, the the {@link #createCommandLineForTask(File, int)} method will
     * never be called
     * 
     * @param inputFolderForTask
     * @param tasknb
     * @return
     */
    public GenerationScript createGenerationScriptForTask(File inputFolderForTask, int tasknb) {

        String generationScriptFilePath = getGenerationScriptFilePath();
        if ((generationScriptFilePath == null) || (generationScriptFilePath.trim().equals(""))) {
            // no generation file is provided, so we return null
            // no generation script will be used in the application
            return null;
        }

        File f = new File(generationScriptFilePath);

        if (!f.isFile()) {
            LoggerManager
                    .getInstane()
                    .warning(
                            "Generation script cannot be used, the script file cannot be found at " +
                                f.getAbsolutePath() +
                                "\n The tasks might not work on all the platforms. Please see the application configuration file. ");
            return null;
        }

        List<String> scriptArgs = getGenerationScriptArgs(tasknb, inputFolderForTask);
        String[] args = scriptArgs.toArray(new String[0]);

        GenerationScript s;
        try {
            s = new GenerationScript(f, args);
        } catch (InvalidScriptException e) {
            LoggerManager.getInstane().error(
                    "Generation script cannot be created. Some tasks may not work properly", e);
            return null;
        }

        return s;
    }

    /**
     * @return the file path containing the generation script for a task
     * 
     * 	The arguments for the generation script are given by
     * {@link #getGenerationScriptArgs(int, File)} method <p/> 
     * 	If you decide to use a generation script in your implementation you should: <p>
     *  * Override this method in order to provide a generation script file for your tasks <p>
     *  * Override {@link #getGenerationScriptArgs(int, File)} in order to provide arguments for the generation script <p>
     * 	<p>
     *  The default implementation returns null which means that no generation script will be provided (the method  {@link #createGenerationScriptForTask(File, int)} will also return null)
     * </p>
     * In this case the {@link #createCommandLineForTask(File, int)} method will  be used. otherwise, this method will be ignored
     * 
     * Note: a generation script offers the possibility to generate different command lines for a task, depending on the environment it is executed
     * 
     */
    //@snippet-start FileSplitMerge_getGenerationScriptFilePath
    protected String getGenerationScriptFilePath() {
        return null;
    }

    //@snippet-end FileSplitMerge_getGenerationScriptFilePath

    /**
     * Override this method if you are using a generation script in your
     * application. See {@link #getGenerationScriptFilePath()}
     * <p>
     * This method is called by
     * {@link #createGenerationScriptForTask(File, int)}
     * 
     * @param taskNb
     * @param taskFileOnServer
     * @return a list containing the arguments for the generation script
     */
    //@snippet-start FileSplitMerge_MethodsArgs
    //@snippet-start FileSplitMerge_getGenerationScriptArgs
    protected List<String> getGenerationScriptArgs(int taskNb, File taskFileOnServer) {
        return new LinkedList<String>();
    }

    //@snippet-end FileSplitMerge_getGenerationScriptArgs

    //@snippet-break FileSplitMerge_MethodsArgs

    /**
     * Creates a NativeTask.
     * </p>
     * The command line for the created task is given either by a
     * {@link GenerationScript} (if the result of
     * {@link #createGenerationScriptForTask(File, int)} is not null) or by a
     * command line, returned by {@link #createCommandLineForTask(File, int)}
     * </p>
     * The concrete implementations of this class must override at least one of
     * the methods: {@link #createCommandLineForTask(File, int)},
     * {@link #getGenerationScriptFilePath()}
     * </p>
     * The generation script will be used with priority to the command line
     * 
     * The pre-script, post-script, clean-script are given by the
     * {@link #createPreScriptForTask(int, File)},
     * {@link #createPostScriptForTask(int, File)},
     * {@link #createCleanScriptForTask(int, File)} methods
     * </p>
     * </p>
     * The concrete implementations of this (JobCreator) class should provide
     * implementations for {@link #getPreScriptFilePath()},
     * {@link #getPreScriptArgs(int, File)}, {@link #getPostScriptFilePath()},
     * {@link #getPostScriptArgs(int, File)}, {@link #getCleanScriptFilePath()},
     * {@link #getCleanScriptArgs(int, File)}
     * </p>
     * 
     * 
     * @param inputFileForTask
     * @param taskNb
     * @return
     * @throws UnknownHostException
     * @throws InvalidScriptException
     * @throws InvalidInputDataException
     */
    protected NativeTask createNativeTask(File inputFileForTask, int taskNb) throws UnknownHostException,
            InvalidScriptException, InvalidInputDataException {
        NativeTask nt = new NativeTask();
        nt.setName(NameConstants.TASK_NAME_PREFIX + taskNb);

        // If we are provided with a generation script we use this one,
        // Otherwise we use a command line
        GenerationScript gs = this.createGenerationScriptForTask(inputFileForTask, taskNb);
        if (gs != null) {
            nt.setGenerationScript(gs);
        } else {
            String cmd = createCommandLineForTask(inputFileForTask, taskNb);
            String[] res = Tools.parseCommandLine(cmd);
            nt.setCommandLine(res);
        }

        // System.out.println("JobCreator.createNativeTask()-> cmd: "+res);

        nt.setDescription(NameConstants.TASK_DESCRIPTION);
        nt.setMaxNumberOfExecution(getNumberOfExecutionsForTask(nt));
        nt.setRestartTaskOnError(getRestartModeForTask(nt));
        nt.setPreciousResult(true);

        String resultPreview = this.getResultsPreviewClassNameForTask(nt);

        if (!resultPreview.equals("")) {
            nt.setResultPreview(resultPreview);
        }

        Script preScript = null;

        preScript = this.createPreScriptForTask(taskNb, inputFileForTask);

        nt.setPreScript(preScript);

        Script postScript = null;

        postScript = this.createPostScriptForTask(taskNb, inputFileForTask);

        nt.setPostScript(postScript);

        Script cleanScript = null;
        try {
            cleanScript = this.createCleanScriptForTask(taskNb, inputFileForTask);
        } catch (Exception e) {
            // whatever the exception is, we should continue the execution
            // just log the exception
            LoggerManager.getLogger().warn("Clean script for task was not created. " + e.getMessage());
        }
        if (cleanScript != null) {
            nt.setCleaningScript(cleanScript);
        }
        return nt;
    }

    /**
     * The default restart mode is RestartMode.ANYWHERE Override this method in
     * order to specify a different restart mode
     * 
     * @param nt
     * @return
     */
    protected RestartMode getRestartModeForTask(NativeTask nt) {
        return RestartMode.ANYWHERE;
    }

    /**
     * Returns the max number of executions of a task (in case of error)
     * Override this method to define specific number of executions for each task
     * 
     * @param t
     * @return
     */
    protected int getNumberOfExecutionsForTask(NativeTask t) {
        return DEFAULT_MAX_NB_OF_EXECUTION_PER_TASK;
    }

    /**
     * Returns the class name which manages the result preview for a specific
     * task.
     * <p/>
     * By default this method calls {@link #getDefaultResultPreviewClassName()}
     * Override this method in order to attach a result preview for a specific
     * task A default result preview should be available if this method is
     * overriden.
     * 
     * @param nt -
     *            the native task for which a preview is needed
     * @return
     */
    protected String getResultsPreviewClassNameForTask(NativeTask nt) {
        return getDefaultResultPreviewClassName();

    }

    /**
     * Returns the class name which manages the default result preview for all
     * tasks The result preview class must extend <{@link org.ow2.proactive.scheduler.common.task.ResultPreview}
     * By default this method returns an empty string - no results preview are
     * available Override this method in order to attach a result preview
     * 
     * Example : If you have implemented a class
     * com.myproject.MyResultPreviewClass you can return
     * MyResultPreviewClass.class.getName()
     * 
     * 
     */
    protected String getDefaultResultPreviewClassName() {
        return "";
    }

    /**
     * Returns the file path of the script that will be executed on the remote
     * node before the execution of the task <p>
     * This script is usually used for copying, to the remote node, the files needed by the task in order to
     * perform the execution
     *
     * Arguments for the script are given by the {@link #getPreScriptArgs(int, File)} method
     * 
     * @return
     */
    //@snippet-start FileSplitMerge_getPreScriptFilePath
    protected abstract String getPreScriptFilePath();

    //@snippet-end FileSplitMerge_getPreScriptFilePath

    /**
     * This method should return a list of arguments that will be given to the
     * script defined in the file given by the <{@link #getPreScriptFilePath()}
     * 
     * @param taskNb
     *            the task number of the task for which the pre script will be
     *            executed
     * @param taskInputFileOnServer -
     *            refers to the file or folder, on the application's storage location, corresponding to that task
     * @return
     */
    //@snippet-resume FileSplitMerge_MethodsArgs
    //@snippet-start FileSplitMerge_getPreScriptArgs
    protected abstract List<String> getPreScriptArgs(int taskNb, File taskInputFileOnServer);

    //@snippet-end FileSplitMerge_getPreScriptArgs

    //@snippet-break FileSplitMerge_MethodsArgs

    /**
     * Creates the preScript for a task. This script should create, on the
     * client/worker side, a folder for temporary files for the task and copy
     * the files from the taskWorkingDir into that folder
     * 
     * In order to create the script, this method will call {@link #getPreScriptFilePath()} and {@link #getPreScriptArgs(int, File)} methods
     * 
     * @param taskWorkingDir
     *            the input file or folder,on the application's storage location, corresponding to that task
     * @return A script object null, if the script file is not found
     * @throws UnknownHostException
     * @throws InvalidScriptException
     */
    protected Script createPreScriptForTask(int taskNb, File taskFileOnServer) throws UnknownHostException,
            InvalidScriptException, InvalidInputDataException {
        // the prescript filepath
        String preScriptFilePath = getPreScriptFilePath();
        if ((preScriptFilePath == null) || (preScriptFilePath.equals(""))) {
            throw new InvalidInputDataException(
                "File containing the pre script is not defined. Please verify your configuration file. ");
        }

        File f = new File(preScriptFilePath);
        // System.out.println("Script file: "+f.getAbsolutePath());
        if (!f.exists()) {
            throw new InvalidInputDataException("Could not find file for prescript. Exepted at location " +
                f.getAbsolutePath());
        }

        List<String> scriptArgs = getPreScriptArgs(taskNb, taskFileOnServer);
        String[] args = scriptArgs.toArray(new String[0]);
        Script s = new SimpleScript(f, args);
        return s;
    }

    /**
     *  Usually the post script is used to transfer result files from the compute node storage location to a location where the application can read them<p> 
     *  Arguments for the script are given by <{@link #getPostScriptArgs(int taskNb, File taskInputDirOnServer)} method
     *  @return the file containing the post script for a task <p>
     */
    //@snippet-start FileSplitMerge_getPostScriptFilePath
    protected abstract String getPostScriptFilePath();

    //@snippet-end FileSplitMerge_getPostScriptFilePath

    /**
     * This method should return a list of arguments that will be given to the
     * script defined in the file given by the <{@link #getPostScriptFilePath()}
     * 
     * @param taskNb
     *            the task number of the task for which the post script will be
     *            executed
     * @param taskInputFileOnServer -
     *            refers to the file or folder, on the application's storage location, corresponding to that task
     * @return a List of arguments of type java.lang.String
     */
    //@snippet-resume FileSplitMerge_MethodsArgs
    //@snippet-start FileSplitMerge_getPostScriptArgs
    protected abstract List<String> getPostScriptArgs(int taskNb, File taskFileOnServer);

    //@snippet-end FileSplitMerge_getPostScriptArgs

    //@snippet-break FileSplitMerge_MethodsArgs

    /**
     * Creates the postScript for a task. This script should copy the output
     * files for a task from the client to the server <p>
     * In order to create the script, it will call {@link #getPostScriptFilePath()} and {@link #getPostScriptArgs(int, File)} methods
     * 
     * @param taskWorkingDir
     *            the folder, on the server, containing files needed for the
     *            task
     * @return
     * @throws UnknownHostException
     * @throws InvalidScriptException
     */
    protected Script createPostScriptForTask(int taskNb, File taskFileOnServer) throws UnknownHostException,
            InvalidScriptException, InvalidInputDataException {

        String postScriptFilePath = getPostScriptFilePath();

        File f = new File(postScriptFilePath);
        if (postScriptFilePath == null) {
            throw new InvalidInputDataException(
                "File containing the post script is not defined. Please verify your configuration file. ");
        }

        List<String> scriptArgs = getPostScriptArgs(taskNb, taskFileOnServer);
        String[] args = scriptArgs.toArray(new String[0]);

        Script s = new SimpleScript(f, args);
        return s;
    }

    /**
     * 
     * @return the file containing the clean script for a task <p>
     * Arguments for the script are given by <{@link #getCleanScriptArgs(int taskNb, File taskInputDirOnServer)} method
     */
    //@snippet-start FileSplitMerge_getCleanScriptFilePath
    protected abstract String getCleanScriptFilePath();

    //@snippet-end FileSplitMerge_getCleanScriptFilePath

    /**
     * 
     * @param taskNb
     * @param taskFileOnServer
     * @return a list of arguments to be provided as input to the script located in the file returned by {@link #getCleanScriptFilePath()}
     */
    //@snippet-resume FileSplitMerge_MethodsArgs
    //@snippet-start FileSplitMerge_getCleanScriptArgs
    protected abstract List<String> getCleanScriptArgs(int taskNb, File taskFileOnServer);

    //@snippet-end FileSplitMerge_getCleanScriptArgs

    //@snippet-break FileSplitMerge_MethodsArgs

    /**
     * Creates a clean script by calling {@link #getCleanScriptFilePath()} and {@link #getCleanScriptArgs(int, File)} methods
     * The clean script is meant to clean resources on a computing node after using it
     * @param taskNb
     * @param taskWorkingDirOnServer
     * @return
     * @throws InvalidScriptException
     * @throws InvalidInputDataException
     */
    protected Script createCleanScriptForTask(int taskNb, File taskWorkingDirOnServer)
            throws InvalidScriptException, InvalidInputDataException {
        String cleanScriptFilePath = getCleanScriptFilePath();

        if (cleanScriptFilePath == null) {
            throw new InvalidInputDataException(
                "File containing the clean script is not defined.Clean script will not be created");
        }

        File f = new File(cleanScriptFilePath);
        if (!f.exists()) {
            throw new InvalidInputDataException("Could not find clean script file. Expected at " +
                f.getAbsolutePath());
        }
        List<String> scriptArgs = getCleanScriptArgs(taskNb, taskWorkingDirOnServer);
        String[] args = scriptArgs.toArray(new String[0]);

        Script s = new SimpleScript(f, args);
        return s;
    }

    /**
     * 		This method is responsible with splitting the input data for a job in n
     * "slices", one for each task. <p>
     * 		Concrete implementation of this class should
     * provide some init methods in order to set-up the job input data which
     * will be split by this method <P>
     * 		The size of the returned list will
     * determine the number of tasks that will be created
     * 
     * @return a list containing the split data - each member will be passed in
     *         argument to the <{@link #createCommandLineForTask(File file, int taskNb)}
     *         or method. An entry of the list could contain, depending on the
     *         implementation, a file or a folder that constitutes the input
     *         data for one task.
     * 
     * 
     * @throws IOException
     */
    //@snippet-start FileSplitMerge_splitData
    protected abstract List<File> splitData() throws IOException;

    //@snippet-end FileSplitMerge_splitData

    /**
     * 		This method provides a string with the command line to be executed by a
     * task. <p>
     * 		If a generation script is provided for a task, this method will not be called for that task, the generation script will be used instead.<p> 
     *		See {@link #createNativeTask(File, int)} 
     * 
     * @param taskFileOnServer -
     *            a file representing the input data of the task - could be a
     *            regular file or a folder
     * @param taskNb -
     *            the number of the task for which this command will be executed
     *            on a remote node
     * @return The command line, including arguments, separated by SPACE character
     */
    //@snippet-resume FileSplitMerge_MethodsArgs
    //@snippet-start FileSplitMerge_createCommandLineForTask
    protected String createCommandLineForTask(File taskFileOnServer, int taskNb) {
        return "";
    }
    //@snippet-end FileSplitMerge_createCommandLineForTask
    //@snippet-end FileSplitMerge_MethodsArgs

}
