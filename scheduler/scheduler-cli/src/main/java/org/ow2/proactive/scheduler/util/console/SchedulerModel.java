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
package org.ow2.proactive.scheduler.util.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.SortOrder;
import org.ow2.proactive.db.SortParameter;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.JobSortParameter;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.factories.FlatJobFactory;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
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

    protected Scheduler scheduler;
    protected MBeanInfoViewer jmxInfoViewer = null;

    static {
        TaskState.setSortingBy(TaskState.SORT_BY_ID);
        TaskState.setSortingOrder(TaskState.ASC_ORDER);
    }

    @SuppressWarnings("unchecked")
    private static final List<SortParameter<JobSortParameter>> JOB_SORT_PARAMS = Arrays.asList(
            new SortParameter<JobSortParameter>(JobSortParameter.STATE, SortOrder.ASC),
            new SortParameter<JobSortParameter>(JobSortParameter.ID, SortOrder.DESC));

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
        super(Logger.getLogger(SchedulerModel.class));
        this.allowExitCommand = allowExitCommand;
        commands.add(new Command("submit(XMLdescriptor)",
            "Submit a new job (parameter is a string representing the job XML descriptor URL)"));
        commands.add(new Command("submitArchive(archive)", "Submit a new job (parameter is a job archive)"));
        commands
                .add(new Command("submitCmd(CommandFilePath,jobName,output,selectionScript)",
                    "Submit a new job where each task is a line in the commandFile path. Other arguments are optional."));
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
                    "taskresult(id,taskName[,inc])",
                    "Get the result of the given task (parameter is an int or a string representing the jobId, and the task name)"));
        commands.add(new Command("joboutput(id[,taskSort])",
            "Get the output of the given job (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command(
                    "taskoutput(id,taskName)",
                    "Get the output of the given task (parameter is an int or a string representing the jobId, and the task name)"));
        commands
                .add(new Command("removejob(id)",
                    "Remove the given job from the Scheduler (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command("preempttask(id,taskName[,delay])",
                    "Stop the given task execution and re-schedules it after the given delay (parameter delay is in second)"));
        commands
                .add(new Command("restarttask(id,taskName[,delay])",
                    "Terminate the given task execution and re-schedules it after the given delay (parameter delay is in second)"));
        commands.add(new Command("killtask(id,taskName)", "kill the given task"));
        commands
                .add(new Command("jobstate(id[,taskSort])",
                    "Get the current state of the given job (parameter is an int or a string representing the jobId)"));
        commands.add(new Command("listjobs()", "Display the list of jobs managed by the scheduler"));
        commands.add(new Command("logs(jobId,[taskName])", "Get server logs of given job or task"));
        commands.add(new Command("stats()", "Display some statistics about the Scheduler"));
        commands.add(new Command("myaccount()", "Display current user account information"));
        commands.add(new Command("account(username)", "Display account information by username"));
        commands
                .add(new Command("reloadconfig()", "Reloads the scheduler permission policy and log4j config"));
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
        commands.add(new Command("reloadpolicy()", "Reload the configuration of the policy"));
        commands.add(new Command("changepolicy(fullName)",
            "Change the current scheduling policy, (argument is the new policy full name)"));
        commands.add(new Command("reconnect()", "Try to reconnect this console to the server"));
        commands.add(new Command("cnslhelp() or ?c", "Displays help about the console functions itself"));
        if (allowExitCommand) {
            commands.add(new Command("exit()", "Exit Scheduler controller"));
        }
    }

    @Override
    protected void initialize() throws IOException {
        super.initialize();
        //read and launch Action.js
        BufferedReader br = new BufferedReader(new InputStreamReader(SchedulerController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
        //read default js env file if exist
        if (new File(DEFAULT_INIT_JS).exists()) {
            print("! Loading environment from '" + DEFAULT_INIT_JS + "' !" + newline);
            this.exec_(DEFAULT_INIT_JS);
        }
        //read js env argument if any
        if (this.initEnvFileName != null) {
            print("! Loading environment from '" + this.initEnvFileName + "' !" + newline);
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
        Map<String, String> conf = new HashMap<String, String>();
        conf.put("history_filepath", System.getProperty("user.home") + File.separator + ".proactive" +
            File.separator + "scheduler.hist");
        conf.put("history_size", "" + 30);
        console.configure(conf);
        console.start(" > ");
        console.addCompletion(getCompletionList());
        console.print("Type command here (type '?' or help() to see the list of commands)" + newline);
        console.print("");
        initialize();
        SchedulerStatus status;
        console.print("");
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
                print(newline + helpScreen());
            } else if ("?c".equals(stmt)) {
                print(newline + helpScreenCnsl());
            } else {
                eval(stmt);
            }
            print("");
        }
        console.stop();
    }

    @Override
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (t instanceof NotConnectedException) {
            String tmp = "Your session has expired, please try to reconnect server using reconnect() command !";
            console.error(tmp);
            logUserException(tmp, t);
        } else if (t instanceof PermissionException) {
            String tmp = msg + " : " + (t.getMessage() == null ? t : t.getMessage());
            console.error(tmp);
            logUserException(tmp, t);
        } else if (t instanceof ProActiveRuntimeException) {
            String tmp = msg + " : Scheduler server seems to be unreachable !";
            console.error(tmp);
            logUserException(tmp, t);
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

    public String submitArchive_(String archive) {
        try {
            Job job = JobFactory.getFactory().createJobFromArchive(archive);
            JobId id = scheduler.submit(job);
            print("Job archive '" + archive + "' successfully submitted ! (id=" + id.value() + ")");
            return id.value();
        } catch (Exception e) {
            handleExceptionDisplay("Error on job Submission (path=" + archive + ")", e);
        }
        return "";
    }

    public String submitCmd_(String commandFilePath, String jobName, String selectscript) {
        try {
            Job job = FlatJobFactory.getFactory().createNativeJobFromCommandsFile(commandFilePath, jobName,
                    selectscript, selectscript);
            JobId id = scheduler.submit(job);
            print("Job '" + commandFilePath + "' successfully submitted ! (id=" + id.value() + ")");
            return id.value();
        } catch (Exception e) {
            handleExceptionDisplay("Error on job Submission (path=" + commandFilePath + ")", e);
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

    public boolean remove_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.removeJob(jobId);
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing job  " + jobId, e);
            return false;
        }
        if (success) {
            print("Job " + jobId + " removed.");
        } else {
            print("Remove job " + jobId + " is not possible !!");
        }
        return success;
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
                        handleExceptionDisplay("\t " + e.getKey(), e1);
                    }
                }
            } else {
                print("Job " + jobId + " is not finished !");
                return null;
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
            return null;
        }
    }

    public TaskResult tresult_(String jobId, String taskName, String inc) {
        int rInc;
        try {
            rInc = Integer.parseInt(inc);
        } catch (NumberFormatException nfe) {
            handleExceptionDisplay("Bad value for 3rd argument : 'incarnation' ", nfe);
            return null;
        }
        try {
            TaskResult result = scheduler.getTaskResultFromIncarnation(jobId, taskName, rInc);

            if (result != null) {
                print("Task " + taskName + " result => " + newline);

                try {
                    print("\t " + result.value());
                } catch (Throwable e1) {
                    handleExceptionDisplay("\t " + result.getException().getMessage(), e1);
                }
            } else {
                print("Task '" + taskName + "' is not finished !");
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on task " + taskName, e);
            return null;
        }
    }

    public boolean preemptt_(String jobId, String taskName, String restartDelay) {
        int rDelay;
        try {
            rDelay = Integer.parseInt(restartDelay);
        } catch (NumberFormatException nfe) {
            handleExceptionDisplay("Bad value for 3rd argument : 'delay' ", nfe);
            return false;
        }
        try {
            boolean result = scheduler.preemptTask(jobId, taskName, rDelay);

            if (result) {
                print("Task " + taskName + " has been stopped, it will be re-scheduled after " +
                    restartDelay + " sec." + newline);
            } else {
                print("Task '" + taskName + "' cannot be stopped, it is probably not running.");
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on task " + taskName, e);
            return false;
        }
    }

    public boolean restartt_(String jobId, String taskName, String restartDelay) {
        int rDelay;
        try {
            rDelay = Integer.parseInt(restartDelay);
        } catch (NumberFormatException nfe) {
            handleExceptionDisplay("Bad value for 3rd argument : 'delay' ", nfe);
            return false;
        }
        try {
            boolean result = scheduler.restartTask(jobId, taskName, rDelay);

            if (result) {
                print("Task " + taskName + " has been terminated, it will be re-scheduled after " +
                    restartDelay + " sec." + newline);
            } else {
                print("Task '" + taskName + "' cannot be terminated, it is probably not running.");
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on task " + taskName, e);
            return false;
        }
    }

    public boolean killt_(String jobId, String taskName) {
        try {
            boolean result = scheduler.killTask(jobId, taskName);

            if (result) {
                print("Task " + taskName + " has been killed." + newline);
            } else {
                print("Task '" + taskName + "' cannot be killed, it is probably not running.");
            }
            return result;
        } catch (Exception e) {
            handleExceptionDisplay("Error on task " + taskName, e);
            return false;
        }
    }

    public void output_(String jobId) {
        output_(jobId, "" + TaskState.SORT_BY_ID);
    }

    public void output_(String jobId, String tSort) {
        int sort = TaskState.SORT_BY_ID;
        try {
            sort = Integer.parseInt(tSort);
        } catch (NumberFormatException nfe) {
            handleExceptionDisplay("Bad value for 2rd argument : 'sort' ", nfe);
        }

        try {
            JobResult result = scheduler.getJobResult(jobId);

            if (result != null) {
                print("Job " + jobId + " output => '" + tSort + "' " + newline);

                JobState js = scheduler.getJobState(jobId);
                List<TaskState> tasks = js.getTasks();
                TaskState.setSortingBy(sort);
                Collections.sort(tasks);

                for (TaskState ts : tasks) {
                    TaskResult tRes = null;
                    try {
                        tRes = result.getResult(ts.getName());
                        print(ts.getName() + " : " + newline + tRes.getOutput().getAllLogs(false));
                    } catch (UnknownTaskException e) {
                        print(ts.getName() + " : " + newline + "No output available !");
                    } catch (Exception e1) {
                        if (tRes == null) {
                            throw e1;
                        }
                        error(tRes.getException().toString());
                    }
                }
            } else {
                print("Job " + jobId + " is not finished !");
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
        }
    }

    public void toutput_(String jobId, String taskName) {
        try {
            TaskResult result = null;

            result = scheduler.getTaskResult(jobId, taskName);
            if (result != null) {
                try {
                    print(taskName + " output :");
                    print(result.getOutput().getAllLogs(false));
                } catch (Throwable e1) {
                    handleExceptionDisplay("\t " + result.getException().getMessage(), e1);
                }
            } else {
                print("Task '" + taskName + "' is not finished !");
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
        return jobState_(jobId, "" + TaskState.SORT_BY_ID);
    }

    public JobState jobState_(String jobId, String tSort) {
        int sort = TaskState.SORT_BY_ID;
        try {
            sort = Integer.parseInt(tSort);
        } catch (NumberFormatException nfe) {
            handleExceptionDisplay("Bad value for 2rd argument : 'sort' ", nfe);
        }

        List<String> list;
        try {
            JobState js = scheduler.getJobState(jobId);

            JobInfo ji = js.getJobInfo();

            String state = newline + "   Job '" + ji.getJobId() + "'    name:" +
                ji.getJobId().getReadableName() + "    owner:" + js.getOwner() + "    status:" +
                ji.getStatus() + "    #tasks:" + ji.getTotalNumberOfTasks() + newline + newline;

            //create formatter
            ObjectArrayFormatter oaf = new ObjectArrayFormatter();
            oaf.setMaxColumnLength(30);
            //space between column
            oaf.setSpace(2);
            //title line
            list = new ArrayList<String>();
            list.add("ID");
            list.add("NAME");
            list.add("ITER");
            list.add("DUP");
            list.add("STATUS");
            list.add("HOSTNAME");
            list.add("EXEC DURATION");
            list.add("TOT DURATION");
            list.add("#NODES USED");
            list.add("#EXECUTIONS");
            list.add("#NODES KILLED");
            oaf.setTitle(list);
            //separator
            oaf.addEmptyLine();
            //add each lines
            List<TaskState> tasks = js.getTasks();
            TaskState.setSortingBy(sort);
            Collections.sort(tasks);
            for (TaskState ts : tasks) {
                list = new ArrayList<String>();
                list.add(ts.getId().toString());
                list.add(ts.getName());
                list.add((ts.getIterationIndex() > 0) ? "" + ts.getIterationIndex() : "");
                list.add((ts.getReplicationIndex() > 0) ? "" + ts.getReplicationIndex() : "");
                list.add(ts.getStatus().toString());
                list.add((ts.getExecutionHostName() == null) ? "unknown" : ts.getExecutionHostName());
                list.add(Tools.getFormattedDuration(0, ts.getExecutionDuration()));
                list.add(Tools.getFormattedDuration(ts.getFinishedTime(), ts.getStartTime()));
                list.add("" + ts.getNumberOfNodesNeeded());
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
            print(state + Tools.getStringAsArray(oaf));

            return js;
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
            return null;
        }
    }

    public void listjobs_() {
        List<String> list;
        try {
            List<JobInfo> jobs = scheduler.getJobs(0, -1, new JobFilterCriteria(false, true, true, true),
                    JOB_SORT_PARAMS);

            if (jobs.size() == 0) {
                print("\n\tThere are no jobs handled by the Scheduler");
                return;
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
            list.add("STATUS");
            list.add("START AT");
            list.add("DURATION");
            oaf.setTitle(list);

            JobStatus lastStatus = null;
            for (JobInfo jobInfo : jobs) {
                if (changedOfStateGroup(lastStatus, jobInfo.getStatus())) {
                    oaf.addEmptyLine();
                }
                oaf.addLine(makeList(jobInfo));
                lastStatus = jobInfo.getStatus();
            }

            //print formatter
            print(Tools.getStringAsArray(oaf));
        } catch (Exception e) {
            handleExceptionDisplay("Error while getting list of jobs", e);
        }
    }

    // jobs are grouped in 3 groups (by state)
    private boolean changedOfStateGroup(JobStatus lastStatus, JobStatus newStatus) {
        if (isFirstGroup(lastStatus) && !isFirstGroup(newStatus)) {
            return true;
        } else if (isSecondGroup(lastStatus) && !isSecondGroup(newStatus)) {
            return true;
        } else if (isFirstGroup(lastStatus) && isThirdGroup(newStatus)) {
            return true;
        }
        return false;
    }

    private boolean isFirstGroup(JobStatus status) {
        return JobStatus.PENDING.equals(status);
    }

    private boolean isSecondGroup(JobStatus status) {
        return JobStatus.RUNNING.equals(status) || JobStatus.STALLED.equals(status) ||
            JobStatus.PAUSED.equals(status);
    }

    private boolean isThirdGroup(JobStatus status) {
        return !isFirstGroup(status) && !isSecondGroup(status);
    }

    private List<String> makeList(JobInfo jobInfo) {
        List<String> list = new ArrayList<String>();
        list.add(jobInfo.getJobId().value());
        list.add(jobInfo.getJobId().getReadableName());
        list.add(jobInfo.getJobOwner());
        list.add(jobInfo.getPriority().toString());
        list.add(jobInfo.getStatus().toString());
        long startTime = jobInfo.getStartTime();
        String date = Tools.getFormattedDate(startTime);
        if (startTime != -1) {
            date += " (" + Tools.getElapsedTime(startTime) + ")";
        }
        list.add(date);
        list.add(Tools.getFormattedDuration(startTime, jobInfo.getFinishedTime()));
        return list;
    }

    public void showRuntimeData_() {
        try {
            printMap(jmxInfoViewer.getMappedInfo("ProActiveScheduler:name=RuntimeData"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showMyAccount_() {
        try {
            printMap(jmxInfoViewer.getMappedInfo("ProActiveScheduler:name=MyAccount"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void showAccount_(final String username) {
        try {
            jmxInfoViewer.setAttribute("ProActiveScheduler:name=AllAccounts", "Username", username);
            printMap(jmxInfoViewer.getMappedInfo("ProActiveScheduler:name=AllAccounts"));
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public void reloadConfig_() {
        try {
            jmxInfoViewer.invoke("ProActiveScheduler:name=Management", "refreshConfiguration", new Object[0]);
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
        print("\nThe configs has been successfully reloaded.");
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

    /**
     * Execute a JS command file with arguments.
     *
     * @param params command file path must be the first param
     */
    public void execWithParam_(String... params) {
        try {
            File f = new File(params[0].trim());
            //parse arguments
            Map<String, String> mArgs = new HashMap<String, String>();
            for (String p : params) {
                if (p.contains("=")) {
                    String[] argVal = p.split("=");
                    mArgs.put(argVal[0], argVal[1]);
                }
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br), mArgs);
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
            String s = console.readStatement("Are you sure you want to shutdown the Scheduler ? " + YES_NO +
                " > ");
            //s == null is true if console has no input
            success = (s == null || s.equalsIgnoreCase(YES));
            if (success) {
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
            String s = console.readStatement("Are you sure you want to kill the Scheduler ? " + YES_NO +
                " > ");
            //s == null is true if console has no input
            success = (s == null || s.equalsIgnoreCase(YES));
            if (success) {
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

    public boolean reloadPolicyConf_() {
        boolean success = false;
        try {
            success = scheduler.reloadPolicyConfiguration();
            if (success) {
                print("The policy configuration has been reloaded.");
            } else {
                error("An error occured while reloading the policy. (see log file for details)");
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

    public void changePolicy_(String newPolicyFullName) {
        boolean success = false;
        try {
            success = scheduler.changePolicy(newPolicyFullName);
            if (success) {
                print("Policy successfully changed !");
            } else {
                error("Policy was not changed ! (see log file for details)");
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

        out.append(newline);
        out.append("Task sorting by ");
        out.append("id:");
        out.append(TaskState.SORT_BY_ID);
        out.append(", name:");
        out.append(TaskState.SORT_BY_NAME);
        out.append(", status:");
        out.append(TaskState.SORT_BY_STATUS);
        out.append(", start time:");
        out.append(TaskState.SORT_BY_STARTED_TIME);
        out.append(", finish time:");
        out.append(TaskState.SORT_BY_FINISHED_TIME);
        out.append(", host:");
        out.append(TaskState.SORT_BY_HOST_NAME);
        out.append(", duration:");
        out.append(TaskState.SORT_BY_EXEC_DURATION);

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
     * @param auth        the authentication interface on which to connect
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

    /**
     * Print a map on two column
     *
     * @param map the map to be printed
     */
    private void printMap(Map<String, String> map) {
        ObjectArrayFormatter oaf = new ObjectArrayFormatter();
        oaf.setMaxColumnLength(80);
        //space between column
        oaf.setSpace(2);
        //fake title line
        List<String> list = new ArrayList<String>();
        list.add("");
        list.add("");
        oaf.setTitle(list);
        //lines content
        for (Entry<String, String> e : map.entrySet()) {
            list = new ArrayList<String>();
            list.add(e.getKey());
            list.add(e.getValue());
            oaf.addLine(list);
        }
        print(Tools.getStringAsArray(oaf));
    }

    /**
     * Retrieves logs for particular job or tasks
     */
    public void logs_(String[] optionValues) {
        if (optionValues == null || optionValues.length < 1) {
            model.error("Please specify at least job id. Start with --help for more informations");
        }
        try {
            String jobId = optionValues[0];
            if (optionValues.length == 1 || optionValues.length == 2 && optionValues[1] == "undefined") {
                print(scheduler.getJobServerLogs(jobId));
            } else {
                String taskName = optionValues[1];
                print(scheduler.getTaskServerLogs(jobId, taskName));
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleExceptionDisplay("*ERROR*", e);
        }
    }

}
