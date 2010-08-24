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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.utils.console.Command;
import org.ow2.proactive.utils.console.ConsoleModel;
import org.ow2.proactive.utils.console.MBeanInfoViewer;


/**
 * SchedulerModel is the class that drives the Scheduler console in the client view.
 * To use this class, get the model, connect a Scheduler and a console, and just start this model.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class SchedulerModel extends ConsoleModel {

    private static final String DEFAULT_INIT_JS = System.getProperty("user.home") + File.separator +
        ".proactive" + File.separator + "scheduler-client.js";
    private static final String JS_INIT_FILE = "Actions.js";

    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String YES_NO = "(" + YES + "/" + NO + ")";

    private static int logsNbLines = 20;
    private static String logsDirectory = System.getProperty("pa.scheduler.home") + File.separator + ".logs";

    private static final String schedulerLogFile = "Scheduler.log";
    private static final String schedulerDevLogFile = "SchedulerDev.log";

    protected Scheduler scheduler;
    protected MBeanInfoViewer jmxInfoViewer = null;

    static {
        TaskState.setSortingBy(TaskState.SORT_BY_ID);
        TaskState.setSortingOrder(TaskState.ASC_ORDER);
        JobState.setSortingBy(JobState.SORT_BY_ID);
        JobState.setSortingOrder(JobState.ASC_ORDER);
    }

    /**
     * Get this model. Also specify if the exit command should do something or not
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return the current model associated to this class.
     */
    public static SchedulerModel getModel(boolean allowExitCommand) {
        if (model == null) {
            model = new SchedulerModel(allowExitCommand);
        }
        return (SchedulerModel) model;
    }

    /**
     * Get a new model. Also specify if the exit command should do something or not
     * WARNING, this method should just be used to re-create an instance of a model, it will disabled previous instance.
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return a brand new model associated to this class.
     */
    public static SchedulerModel getNewModel(boolean allowExitCommand) {
        model = new SchedulerModel(allowExitCommand);
        return (SchedulerModel) model;
    }

    protected SchedulerModel(boolean allowExitCommand) {
        super();
        this.allowExitCommand = allowExitCommand;
        commands.add(new Command("submit(XMLdescriptor)",
            "Submit a new job (parameter is a string representing the job XML descriptor URL)"));
        commands
                .add(new Command(
                    "priority(id,priority)",
                    "Change the priority of the given job (parameters are an int or a string representing the jobId " +
                        "AND a string representing the new priority)" +
                        newline +
                        String.format(" %1$-" + cmdHelpMaxCharLength +
                            "s Priorities are Idle, Lowest, Low, Normal, High, Highest", " ")));
        commands.add(new Command("pausejob(id)",
            "Pause the given job (parameter is an int or a string representing the jobId)"));
        commands.add(new Command("resumejob(id)",
            "Resume the given job (parameter is an int or a string representing the jobId)"));
        commands.add(new Command("killjob(id)",
            "Kill the given job (parameter is an int or a string representing the jobId)"));
        commands.add(new Command("jobresult(id)",
            "Get the result of the given job (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command(
                    "taskresult(id,taskName)",
                    "Get the result of the given task (parameter is an int or a string representing the jobId, and the task name)"));
        commands.add(new Command("joboutput(id)",
            "Get the output of the given job (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command(
                    "taskoutput(id,taskName)",
                    "Get the output of the given task (parameter is an int or a string representing the jobId, and the task name)"));
        commands
                .add(new Command("removejob(id)",
                    "Remove the given job from the Scheduler (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command("jobstate(id)",
                    "Get the current state of the given job (parameter is an int or a string representing the jobId)"));
        commands.add(new Command("listjobs()", "Display the list of jobs managed by the scheduler"));
        commands.add(new Command("stats()", "Display some statistics about the Scheduler"));
        commands.add(new Command("myaccount()", "Display current user account information"));
        commands.add(new Command("account(username)", "Display account information by username"));
        commands.add(new Command("reloadpermissions()", "Reloads the permission file"));
        commands
                .add(new Command("exec(scriptFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a script-file path)"));
        commands.add(new Command("start()", "Start Scheduler"));
        commands.add(new Command("stop()", "Stop Scheduler"));
        commands
                .add(new Command("pause()", "Pause Scheduler, causes every jobs but running one to be paused"));
        commands.add(new Command("freeze()",
            "Freeze Scheduler, causes all jobs to be paused (every non-running tasks are paused)"));
        commands.add(new Command("resume()", "Resume Scheduler, causes all jobs to be resumed"));
        commands.add(new Command("shutdown()", "Wait for running jobs to finish and shutdown Scheduler"));
        commands.add(new Command("kill()", "Kill every tasks and jobs and shutdown Scheduler"));
        commands.add(new Command("linkrm(rmURL)",
            "Reconnect a Resource Manager (parameter is a string representing the new rmURL)"));
        commands.add(new Command("changepolicy(fullName)",
            "Change the current scheduling policy, (argument is the new policy full name)"));
        commands.add(new Command("reconnect()", "Try to reconnect this console to the server"));
        commands.add(new Command("setlogsdir(logsDir)",
            "Set the directory where the log are located, (default is SCHEDULER_HOME/.logs"));
        commands.add(new Command("viewlogs(nbLines)",
            "View the last nbLines lines of the admin logs file, (default nbLines is 20)"));
        commands.add(new Command("viewdevlogs(nbLines)",
            "View the last nbLines lines of the dev logs file, (default nbLines is 20)"));
        commands.add(new Command("cnslhelp() or ?c", "Displays help about the console functions itself"));
        if (allowExitCommand) {
            commands.add(new Command("exit()", "Exit Scheduler controller"));
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.util.userconsole.UserSchedulerModel#initialize()
     */
    @Override
    protected void initialize() throws IOException {
        super.initialize();
        //read and launch Action.js
        BufferedReader br = new BufferedReader(new InputStreamReader(SchedulerController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
        //read default js env file if exist
        if (new File(DEFAULT_INIT_JS).exists()) {
            console.print("! Loading environment from '" + DEFAULT_INIT_JS + "' !" + newline);
            this.exec_(DEFAULT_INIT_JS);
        }
        //read js env argument if any
        if (this.initEnvFileName != null) {
            console.print("! Loading environment from '" + this.initEnvFileName + "' !" + newline);
            this.exec_(this.initEnvFileName);
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#checkIsReady()
     */
    @Override
    protected void checkIsReady() {
        super.checkIsReady();
        if (scheduler == null) {
            throw new RuntimeException("Scheduler is not set, it must be set before starting the model");
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#startModel()
     */
    @Override
    public void startModel() throws Exception {
        checkIsReady();
        console.start(" > ");
        console.addCompletion(getCompletionList());
        console.print("Type command here (type '?' or help() to see the list of commands)" + newline);
        initialize();
        SchedulerStatus status;
        while (!terminated) {
            status = null;
            try {
                status = scheduler.getStatus();
            } catch (Exception e) {
            }
            String prompt = " ";
            if (status != SchedulerStatus.STARTED) {
                try {
                    prompt += "[" + status.toString() + "] ";
                } catch (NullPointerException npe) {
                    //status is null
                    prompt += "[Unknown] ";
                }
            }
            String stmt = console.readStatement(prompt + "> ");
            if ("?".equals(stmt)) {
                console.print(newline + helpScreen());
            } else if ("?c".equals(stmt)) {
                console.print(newline + helpScreenCnsl());
            } else {
                eval(stmt);
                console.print("");
            }
        }
        console.stop();
    }

    @Override
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (t instanceof NotConnectedException) {
            String tmp = "Your session has expired, please try to reconnect server using reconnect() command !";
            if (!displayOnStdStream) {
                console.error(tmp);
            } else {
                System.err.printf(tmp);
            }
        } else if (t instanceof PermissionException) {
            String tmp = msg + " : " + (t.getMessage() == null ? t : t.getMessage());
            if (!displayOnStdStream) {
                console.error(tmp);
            } else {
                System.err.printf(tmp);
            }
        } else if (t instanceof ProActiveRuntimeException) {
            String tmp = msg + " : Scheduler server seems to be unreachable !";
            if (!displayOnStdStream) {
                console.error(tmp);
            } else {
                System.err.printf(tmp);
            }
        } else {
            super.handleExceptionDisplay(msg, t);
        }
    }

    //***************** COMMAND LISTENER *******************
    //note : method marked with a "_" are called from JS evaluation

    public String submit_(String xmlDescriptor) {
        try {
            Job job = JobFactory.getFactory().createJob(xmlDescriptor);
            JobId id = scheduler.submit(job);
            print("Job '" + xmlDescriptor + "' successfully submitted ! (id=" + id.value() + ")");
            return id.value();
        } catch (Exception e) {
            handleExceptionDisplay("Error on job Submission (path=" + xmlDescriptor + ")", e);
        }
        return "";
    }

    public boolean pause_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.pauseJob(jobId);
        } catch (Exception e) {
            handleExceptionDisplay("Error while pausing job " + jobId, e);
            return false;
        }
        if (success) {
            print("Job " + jobId + " paused.");
        } else {
            print("Pause job " + jobId + " is not possible !!");
        }
        return success;
    }

    public boolean resume_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.resumeJob(jobId);
        } catch (Exception e) {
            handleExceptionDisplay("Error while resuming job  " + jobId, e);
            return false;
        }
        if (success) {
            print("Job " + jobId + " resumed.");
        } else {
            print("Resume job " + jobId + " is not possible !!");
        }
        return success;
    }

    public boolean kill_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.killJob(jobId);
        } catch (Exception e) {
            handleExceptionDisplay("Error while killing job  " + jobId, e);
            return false;
        }
        if (success) {
            print("Job " + jobId + " killed.");
        } else {
            print("kill job " + jobId + " is not possible !!");
        }
        return success;
    }

    public void remove_(String jobId) {
        try {
            scheduler.removeJob(jobId);
            print("Job " + jobId + " removed.");
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing job  " + jobId, e);
        }
    }

    public JobResult result_(String jobId) {
        try {
            JobResult result = scheduler.getJobResult(jobId);

            if (result != null) {
                print("Job " + jobId + " result => " + newline);

                for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                    TaskResult tRes = e.getValue();

                    try {
                        print("\t " + e.getKey() + " : " + tRes.value());
                    } catch (Throwable e1) {
                        handleExceptionDisplay("\t " + e.getKey() + " : " + tRes.getException().getMessage(),
                                e1);
                    }
                }
            } else {
                print("Job " + jobId + " is not finished or unknown !");
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
            return null;
        }
    }

    public TaskResult tresult_(String jobId, String taskName) {
        try {
            TaskResult result = scheduler.getTaskResult(jobId, taskName);

            if (result != null) {
                print("Task " + taskName + " result => " + newline);

                try {
                    print("\t " + result.value());
                } catch (Throwable e1) {
                    handleExceptionDisplay("\t " + result.getException().getMessage(), e1);
                }
            } else {
                print("Task '" + taskName + "' is still running !");
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on task " + taskName, e);
            return null;
        }
    }

    public void output_(String jobId) {
        try {
            JobResult result = scheduler.getJobResult(jobId);

            if (result != null) {
                print("Job " + jobId + " output => " + newline);

                for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                    TaskResult tRes = e.getValue();

                    try {
                        print(e.getKey() + " : " + newline + tRes.getOutput().getAllLogs(false));
                    } catch (Throwable e1) {
                        error(tRes.getException().toString());
                    }
                }
            } else {
                print("Job " + jobId + " is not finished or unknown !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
        }
    }

    public void toutput_(String jobId, String taskName) {
        try {
            TaskResult result = scheduler.getTaskResult(jobId, taskName);

            if (result != null) {
                try {
                    print(taskName + " output :");
                    print(result.getOutput().getAllLogs(false));
                } catch (Throwable e1) {
                    handleExceptionDisplay("\t " + result.getException().getMessage(), e1);
                }
            } else {
                print("Task '" + taskName + "' is still running !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error on task " + taskName, e);
        }
    }

    public void priority_(String jobId, String newPriority) {
        try {
            JobPriority prio = JobPriority.findPriority(newPriority);
            scheduler.changeJobPriority(jobId, prio);
            print("Job " + jobId + " priority changed to '" + prio + "' !");
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
        }
    }

    public JobState jobState_(String jobId) {
        List<String> list;
        try {
            JobState js = scheduler.getJobState(jobId);

            JobInfo ji = js.getJobInfo();

            String state = "\n   Job '" + ji.getJobId() + "'    name:" + ji.getJobId().getReadableName() +
                "    owner:" + js.getOwner() + "    status:" + ji.getStatus() + "    #tasks:" +
                ji.getTotalNumberOfTasks() + newline;
            print(state);
            //create formatter
            ObjectArrayFormatter oaf = new ObjectArrayFormatter();
            oaf.setMaxColumnLength(30);
            //space between column
            oaf.setSpace(2);
            //title line
            list = new ArrayList<String>();
            list.add("ID");
            list.add("NAME");
            list.add("ID");
            list.add("DUP");
            list.add("STATUS");
            list.add("HOSTNAME");
            list.add("EXEC DURATION");
            list.add("TOT DURATION");
            list.add("#EXECUTIONS");
            list.add("#NODES KILLED");
            oaf.setTitle(list);
            //separator
            oaf.addEmptyLine();
            //add each lines
            List<TaskState> tasks = js.getTasks();
            Collections.sort(tasks);
            for (TaskState ts : tasks) {
                list = new ArrayList<String>();
                list.add(ts.getId().toString());
                list.add(ts.getName());
                list.add((ts.getIterationIndex() > 0) ? "" + ts.getIterationIndex() : "");
                list.add((ts.getDuplicationIndex() > 0) ? "" + ts.getDuplicationIndex() : "");
                list.add(ts.getStatus().toString());
                list.add((ts.getExecutionHostName() == null) ? "unknown" : ts.getExecutionHostName());
                list.add(Tools.getFormattedDuration(0, ts.getExecutionDuration()));
                list.add(Tools.getFormattedDuration(ts.getFinishedTime(), ts.getStartTime()));
                if (ts.getMaxNumberOfExecution() - ts.getNumberOfExecutionLeft() < ts
                        .getMaxNumberOfExecution()) {
                    list.add((ts.getMaxNumberOfExecution() - ts.getNumberOfExecutionLeft() + 1) + "/" +
                        ts.getMaxNumberOfExecution());
                } else {
                    list.add((ts.getMaxNumberOfExecution() - ts.getNumberOfExecutionLeft()) + "/" +
                        ts.getMaxNumberOfExecution());
                }
                list.add((ts.getMaxNumberOfExecutionOnFailure() - ts.getNumberOfExecutionOnFailureLeft()) +
                    "/" + ts.getMaxNumberOfExecutionOnFailure());
                oaf.addLine(list);
            }
            //print formatter
            print(Tools.getStringAsArray(oaf));

            return js;
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
            return null;
        }
    }

    public SchedulerState schedulerState_() {
        List<String> list;
        try {
            SchedulerState state = scheduler.getState();

            if (state.getPendingJobs().size() + state.getRunningJobs().size() +
                state.getFinishedJobs().size() == 0) {
                print("\n\tThere is no jobs handled by the Scheduler");
                return state;
            }
            //create formatter
            ObjectArrayFormatter oaf = new ObjectArrayFormatter();
            oaf.setMaxColumnLength(30);
            //space between column
            oaf.setSpace(4);
            //title line
            list = new ArrayList<String>();
            list.add("ID");
            list.add("NAME");
            list.add("OWNER");
            list.add("PRIORITY");
            list.add("PROJECT");
            list.add("STATUS");
            list.add("START AT");
            list.add("DURATION");
            oaf.setTitle(list);
            //separator
            oaf.addEmptyLine();
            //sort lists
            Collections.sort(state.getPendingJobs());
            Collections.sort(state.getRunningJobs());
            Collections.sort(state.getFinishedJobs());
            //add lines to formatter
            for (JobState js : state.getFinishedJobs()) {
                oaf.addLine(makeList(js));
            }
            if (state.getRunningJobs().size() > 0) {
                oaf.addEmptyLine();
            }
            for (JobState js : state.getRunningJobs()) {
                oaf.addLine(makeList(js));
            }
            if (state.getPendingJobs().size() > 0) {
                oaf.addEmptyLine();
            }
            for (JobState js : state.getPendingJobs()) {
                oaf.addLine(makeList(js));
            }
            //print formatter
            print(Tools.getStringAsArray(oaf));
            return state;
        } catch (Exception e) {
            handleExceptionDisplay("Error while getting list of jobs", e);
            return null;
        }
    }

    private List<String> makeList(JobState js) {
        List<String> list = new ArrayList<String>();
        list.add(js.getId().toString());
        list.add(js.getName());
        list.add(js.getOwner());
        list.add(js.getPriority().toString());
        list.add(js.getProjectName());
        list.add(js.getStatus().toString());
        list.add(Tools.getFormattedDate(js.getStartTime()));
        list.add(Tools.getFormattedDuration(js.getStartTime(), js.getFinishedTime()));
        return list;
    }

    public void showRuntimeData_() {
        try {
            print(jmxInfoViewer.getInfo("ProActiveScheduler:name=RuntimeData"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showMyAccount_() {
        try {
            print(jmxInfoViewer.getInfo("ProActiveScheduler:name=MyAccount"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showAccount_(final String username) {
        try {
            jmxInfoViewer.setAttribute("ProActiveScheduler:name=AllAccounts", "Username", username);
            print(jmxInfoViewer.getInfo("ProActiveScheduler:name=AllAccounts"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void refreshPermissionPolicy_() {
        try {
            jmxInfoViewer.invoke("ProActiveScheduler:name=Management", "refreshPermissionPolicy",
                    new Object[0]);
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
        print("\nThe permission file has been successfully reloaded.");
    }

    public void test_() {
        try {
            String descriptorPath = System.getProperty("pa.scheduler.home") + "/samples/jobs_descriptors/";
            submit_(descriptorPath + "Job_2_tasks.xml");
            submit_(descriptorPath + "Job_8_tasks.xml");
            submit_(descriptorPath + "Job_Aborted.xml");
            submit_(descriptorPath + "Job_fork.xml");
            submit_(descriptorPath + "Job_nativ.xml");
            submit_(descriptorPath + "Job_PI.xml");
            submit_(descriptorPath + "Job_pre_post.xml");
            submit_(descriptorPath + "Job_with_dep.xml");
            submit_(descriptorPath + "Job_with_select_script.xml");
        } catch (Exception e) {
            handleExceptionDisplay("Error while starting examples", e);
        }
    }

    public void exec_(String commandFilePath) {
        try {
            File f = new File(commandFilePath.trim());
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br));
            br.close();
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public void exit_() {
        if (allowExitCommand) {
            console.print("Exiting controller.");
            try {
                scheduler.disconnect();
            } catch (Exception e) {
            }
            terminated = true;
        } else {
            console.print("Exit command has been disabled !");
        }
    }

    public Scheduler getUserScheduler_() {
        return scheduler;
    }

    public boolean start_() {
        boolean success = false;
        try {
            success = scheduler.start();
        } catch (Exception e) {
            handleExceptionDisplay("Start Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler started.");
        } else {
            print("Scheduler cannot be started in its current state.");
        }
        return success;
    }

    public boolean stop_() {
        boolean success = false;
        try {
            success = scheduler.stop();
        } catch (Exception e) {
            handleExceptionDisplay("Stop Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler stopped.");
        } else {
            print("Scheduler cannot be stopped in its current state.");
        }
        return success;
    }

    public boolean pause_() {
        boolean success = false;
        try {
            success = scheduler.pause();
        } catch (Exception e) {
            handleExceptionDisplay("Pause Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler paused.");
        } else {
            print("Scheduler cannot be paused in its current state.");
        }
        return success;
    }

    public boolean freeze_() {
        boolean success = false;
        try {
            success = scheduler.freeze();
        } catch (Exception e) {
            handleExceptionDisplay("Freeze Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler frozen.");
        } else {
            print("Scheduler cannot be frozen in its current state.");
        }
        return success;
    }

    public boolean resume_() {
        boolean success = false;
        try {
            success = scheduler.resume();
        } catch (Exception e) {
            handleExceptionDisplay("Resume Scheduler is not possible", e);
            return false;
        }
        if (success) {
            print("Scheduler resumed.");
        } else {
            print("Scheduler cannot be resumed in its current state.");
        }
        return success;
    }

    public boolean shutdown_() {
        boolean success = false;
        try {
            if (!displayOnStdStream) {
                String s = console.readStatement("Are you sure you want to shutdown the Scheduler ? " +
                    YES_NO + " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || displayOnStdStream) {
                try {
                    success = scheduler.shutdown();
                } catch (SchedulerException e) {
                    error("Shutdown Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    print("Shutdown sequence initialized, it might take a while to finish all executions, shell will exit.");
                    terminated = true;
                } else {
                    print("Scheduler cannot be shutdown in its current state.");
                }
            } else {
                print("Shutdown aborted !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
        return success;
    }

    public boolean kill_() {
        boolean success = false;
        try {
            if (!displayOnStdStream) {
                String s = console.readStatement("Are you sure you want to kill the Scheduler ? " + YES_NO +
                    " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || displayOnStdStream) {
                try {
                    success = scheduler.kill();
                } catch (SchedulerException e) {
                    error("Kill Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    print("Sheduler has just been killed, shell will exit.");
                    terminated = true;
                } else {
                    print("Scheduler cannot be killed in its current state.");
                }
            } else {
                print("Kill aborted !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
        return success;
    }

    public boolean linkRM_(String rmURL) {
        boolean success = false;
        try {
            success = scheduler.linkResourceManager(rmURL.trim());
            if (success) {
                print("The new Resource Manager at " + rmURL + " has been rebound to the scheduler.");
            } else {
                error("Reconnect a Resource Manager is only possible when RM is dead !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
        return success;
    }

    public void reconnect_() {
        try {
            connectScheduler(authentication, credentials);
            print("Console has been successfully re-connected to the Scheduler !");
        } catch (LoginException e) {
            //should not append in such a context !
        } catch (RuntimeException re) {
            try {
                authentication = SchedulerConnection.join(this.serverURL);
                connectScheduler(authentication, credentials);
                print("Console has been successfully re-connected to the Scheduler !");
            } catch (Exception e) {
                handleExceptionDisplay("*ERROR*", e);
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public void setLogsDir_(String logsDir) {
        if (logsDir == null || "".equals(logsDir)) {
            error("Given logs directory is null or empty !");
            return;
        }
        File dir = new File(logsDir);
        if (!dir.exists()) {
            error("Given logs directory does not exist !");
            return;
        }
        if (!dir.isDirectory()) {
            error("Given logsDir is not a directory !");
            return;
        }
        dir = new File(logsDir + File.separator + schedulerLogFile);
        if (!dir.exists()) {
            error("Given logs directory does not contains Scheduler logs files !");
            return;
        }
        print("Logs Directory set to '" + logsDir + "' !");
        logsDirectory = logsDir;
    }

    public void viewlogs_(String nbLines) {
        if (!"".equals(nbLines)) {
            try {
                logsNbLines = Integer.parseInt(nbLines);
            } catch (NumberFormatException nfe) {
                //logsNbLines not set
            }
        }
        print(readLastNLines(schedulerLogFile));
    }

    public void viewDevlogs_(String nbLines) {
        if (!"".equals(nbLines)) {
            try {
                logsNbLines = Integer.parseInt(nbLines);
            } catch (NumberFormatException nfe) {
                //logsNbLines not set
            }
        }
        print(readLastNLines(schedulerDevLogFile));
    }

    /**
     * Return the logsNbLines last lines of the given file.
     *
     * @param fileName the file to be displayed
     * @return the N last lines of the given file
     */
    private static String readLastNLines(String fileName) {
        StringBuilder toret = new StringBuilder();
        File f = new File(logsDirectory + File.separator + fileName);
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            long cursor = raf.length() - 2;
            int nbLines = logsNbLines;
            byte b;
            raf.seek(cursor);
            while (nbLines > 0) {
                if ((b = raf.readByte()) == '\n') {
                    nbLines--;
                }
                cursor--;
                raf.seek(cursor);
                if (nbLines > 0) {
                    toret.insert(0, (char) b);
                }
            }
        } catch (Exception e) {
        }
        return toret.toString();
    }

    @SuppressWarnings("unchecked")
    public void changePolicy_(String newPolicyFullName) {
        boolean success = false;
        try {
            Class<? extends Policy> klass = (Class<? extends Policy>) Class.forName(newPolicyFullName);
            success = scheduler.changePolicy(klass);
            if (success) {
                print("Policy successfully changed !");
            } else {
                error("Policy was not changed !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    //****************** HELP SCREEN ********************

    @Override
    protected String helpScreen() {
        StringBuilder out = new StringBuilder("Scheduler controller commands are :" + newline + newline);

        for (int i = 6; i < commands.size(); i++) {
            out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s %2$s" + newline, commands.get(i)
                    .getName(), commands.get(i).getDescription()));
        }

        return out.toString();
    }

    //**************** GETTER / SETTER ******************

    /**
     * Get the scheduler
     *
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    protected String serverURL;
    protected SchedulerAuthenticationInterface authentication;
    protected Credentials credentials;

    /**
     * Connect the scheduler using given authentication interface and creadentials
     *
     * @param auth the authentication interface on which to connect
     * @param credentials the credentials to be used for the connection
     * @throws LoginException If bad credentials are provided
     */
    public void connectScheduler(SchedulerAuthenticationInterface auth, Credentials credentials)
            throws LoginException {
        if (auth == null || credentials == null) {
            throw new NullPointerException("Given authentication part is null");
        }
        try {
            this.scheduler = auth.login(credentials);
            this.authentication = auth;
            this.credentials = credentials;
            this.serverURL = auth.getHostURL();
        } catch (AlreadyConnectedException e) {
            //miam miam
        }
    }

    public void connectScheduler(Scheduler scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("Given scheduler must not be null");
        }
        this.scheduler = scheduler;
    }

    /**
     * Set the JMX information : it is not a mandatory option, if set, it will show informations, if not nothing will be displayed.
     *
     * @param info the jmx information about the current connection
     */
    public void setJMXInfo(MBeanInfoViewer info) {
        jmxInfoViewer = info;
    }

}
