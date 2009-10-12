/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.util.userconsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.util.Tools;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.console.Command;
import org.ow2.proactive.utils.console.ConsoleModel;
import org.ow2.proactive.utils.console.MBeanInfoViewer;


/**
 * UserSchedulerModel is the class that drives the Scheduler console in the user view.
 * To use this class, get the model, connect a Scheduler and a console, and just start this model.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class UserSchedulerModel extends ConsoleModel {

    private static final String JS_INIT_FILE = "UserActions.js";
    protected static final int cmdHelpMaxCharLength = 24;
    protected UserSchedulerInterface scheduler;
    private ArrayList<Command> commands;

    protected MBeanInfoViewer jmxInfoViewer = null;

    /**
     * Get this model. Also specify if the exit command should do something or not
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return the current model associated to this class.
     */
    public static UserSchedulerModel getModel(boolean allowExitCommand) {
        if (model == null) {
            model = new UserSchedulerModel(allowExitCommand);
        }
        return (UserSchedulerModel) model;
    }

    /**
     * Get a new model. Also specify if the exit command should do something or not
     * WARNING, this method should just be used to re-create an instance of a model, it will disabled previous instance.
     *
     * @param allowExitCommand true if the exit command is part of the commands, false if exit command does not exist.
     * @return a brand new model associated to this class.
     */
    public static UserSchedulerModel getNewModel(boolean allowExitCommand) {
        model = new UserSchedulerModel(allowExitCommand);
        return (UserSchedulerModel) model;
    }

    private static UserSchedulerModel getModel() {
        return (UserSchedulerModel) model;
    }

    protected UserSchedulerModel(boolean allowExitCommand) {
        this.allowExitCommand = allowExitCommand;
        commands = new ArrayList<Command>();
        commands
                .add(new Command(
                    "exMode(display,onDemand)",
                    "Change the way exceptions are displayed (if display is true, stacks are displayed - if onDemand is true, prompt before displaying stacks)"));
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
        commands.add(new Command("result(id)",
            "Get the result of the given job (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command(
                    "tresult(id,taskName)",
                    "Get the result of the given task (parameter is an int or a string representing the jobId, and the task name)"));
        commands.add(new Command("output(id)",
            "Get the output of the given job (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command(
                    "toutput(id,taskName)",
                    "Get the output of the given task (parameter is an int or a string representing the jobId, and the task name)"));
        commands
                .add(new Command("removejob(id)",
                    "Remove the given job from the Scheduler (parameter is an int or a string representing the jobId)"));
        commands
                .add(new Command("jobstate(id)",
                    "Get the current state of the given job (parameter is an int or a string representing the jobId)"));
        commands.add(new Command("listjobs()", "Display the list of jobs managed by the scheduler"));
        commands.add(new Command("jmxinfo()", "Display some statistics provided by the Scheduler MBean"));
        commands
                .add(new Command("exec(scriptFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a script-file path)"));
        if (allowExitCommand) {
            commands.add(new Command("exit()", "Exit Scheduler controller"));
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
     * @see org.ow2.proactive.utils.console.ConsoleModel#initialize()
     */
    @Override
    protected void initialize() throws IOException {
        super.initialize();
        //read and launch Action.js
        BufferedReader br = new BufferedReader(new InputStreamReader(UserController.class
                .getResourceAsStream(JS_INIT_FILE)));
        eval(readFileContent(br));
    }

    /**
     * @see org.ow2.proactive.utils.console.ConsoleModel#startModel()
     */
    public void startModel() throws Exception {
        checkIsReady();
        console.start(" > ");
        console.print("Type command here (type '?' or help() to see the list of commands)" + newline);
        initialize();
        String stmt;
        while (!terminated) {
            SchedulerStatus status = scheduler.getSchedulerStatus();
            String prompt = " ";
            if (status != SchedulerStatus.STARTED) {
                try {
                    prompt += "[" + status.toString() + "] ";
                } catch (NullPointerException npe) {
                    //status is null
                    prompt += "[Unknown] ";
                }
            }
            stmt = console.readStatement(prompt + "> ");
            if ("?".equals(stmt)) {
                console.print(newline + helpScreen());
            } else {
                eval(stmt);
                console.print("");
            }
        }
        console.stop();
    }

    //***************** COMMAND LISTENER *******************

    public static void setExceptionMode(boolean displayStack, boolean displayOnDemand) {
        getModel().checkIsReady();
        getModel().setExceptionMode_(displayStack, displayOnDemand);
    }

    public static void help() {
        getModel().checkIsReady();
        getModel().help_();
    }

    public static String submit(String xmlDescriptor) {
        getModel().checkIsReady();
        return getModel().submit_(xmlDescriptor);
    }

    private String submit_(String xmlDescriptor) {
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

    public static boolean pause(String jobId) {
        getModel().checkIsReady();
        return getModel().pause_(jobId);
    }

    private boolean pause_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.pause(jobId).booleanValue();
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

    public static boolean resume(String jobId) {
        getModel().checkIsReady();
        return getModel().resume_(jobId);
    }

    private boolean resume_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.resume(jobId).booleanValue();
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

    public static boolean kill(String jobId) {
        getModel().checkIsReady();
        return getModel().kill_(jobId);
    }

    private boolean kill_(String jobId) {
        boolean success = false;
        try {
            success = scheduler.kill(jobId).booleanValue();
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

    public static void remove(String jobId) {
        getModel().checkIsReady();
        getModel().remove_(jobId);
    }

    private void remove_(String jobId) {
        try {
            scheduler.remove(jobId);
            print("Job " + jobId + " removed.");
        } catch (Exception e) {
            handleExceptionDisplay("Error while removing job  " + jobId, e);
        }
    }

    public static JobResult result(String jobId) {
        getModel().checkIsReady();
        return getModel().result_(jobId);
    }

    private JobResult result_(String jobId) {
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

    public static TaskResult tresult(String jobId, String taskName) {
        getModel().checkIsReady();
        return getModel().tresult_(jobId, taskName);
    }

    private TaskResult tresult_(String jobId, String taskName) {
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

    public static void output(String jobId) {
        getModel().checkIsReady();
        getModel().output_(jobId);
    }

    private void output_(String jobId) {
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

    public static void toutput(String jobId, String taskName) {
        getModel().checkIsReady();
        getModel().toutput_(jobId, taskName);
    }

    private void toutput_(String jobId, String taskName) {
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

    public static void priority(String jobId, String newPriority) {
        getModel().checkIsReady();
        getModel().priority_(jobId, newPriority);
    }

    private void priority_(String jobId, String newPriority) {
        try {
            JobPriority prio = JobPriority.findPriority(newPriority);
            scheduler.changePriority(jobId, prio);
            print("Job " + jobId + " priority changed to '" + prio + "' !");
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
        }
    }

    public static JobState jobState(String jobId) {
        getModel().checkIsReady();
        return getModel().jobState_(jobId);
    }

    private JobState jobState_(String jobId) {
        try {
            JobState js = scheduler.getJobState(jobId);
            JobInfo ji = js.getJobInfo();
            String state = "Job '" + ji.getJobId() + "'    name:" + ji.getJobId().getReadableName() +
                "    owner:" + js.getOwner() + "    status:" + ji.getStatus() + "    #tasks:" +
                ji.getTotalNumberOfTasks() + newline;
            print(state);
            int[] namesSize = new int[] { 2, 4, 6, 8, 8, 13 };
            String formattedDuration;
            String nodesKilled;
            for (TaskState ts : js.getTasks()) {
                formattedDuration = Tools.getFormattedDuration(ts.getFinishedTime(), ts.getStartTime());
                nodesKilled = (ts.getMaxNumberOfExecutionOnFailure() - ts.getNumberOfExecutionOnFailureLeft()) +
                    "/" + ts.getMaxNumberOfExecutionOnFailure();
                namesSize[0] = (ts.getId().toString().length() > namesSize[0]) ? ts.getId().toString()
                        .length() : namesSize[0];
                namesSize[1] = (cutNchar(ts.getName(), 20).length() > namesSize[1]) ? cutNchar(ts.getName(),
                        20).length() : namesSize[1];
                namesSize[2] = (ts.getStatus().toString().length() > namesSize[2]) ? ts.getStatus()
                        .toString().length() : namesSize[2];
                namesSize[3] = (cutNchar(ts.getExecutionHostName(), 24).length() > namesSize[3]) ? cutNchar(
                        ts.getExecutionHostName(), 24).length() : namesSize[3];
                namesSize[4] = (formattedDuration.length() > namesSize[4]) ? formattedDuration.length()
                        : namesSize[4];
                namesSize[5] = (nodesKilled.length() > namesSize[5]) ? nodesKilled.length() : namesSize[5];
            }
            namesSize[0] += 2;
            namesSize[1] += 2;
            namesSize[2] += 2;
            namesSize[3] += 2;
            namesSize[4] += 2;
            namesSize[5] += 2;

            StringBuilder stateSB = new StringBuilder();
            stateSB.append(String.format("\t%1$-" + namesSize[0] + "s", "ID"));
            stateSB.append(String.format(" %1$-" + namesSize[1] + "s", "NAME"));
            stateSB.append(String.format(" %1$-" + namesSize[2] + "s", "STATUS"));
            stateSB.append(String.format(" %1$-" + namesSize[3] + "s", "HOSTNAME"));
            stateSB.append(String.format(" %1$-" + namesSize[4] + "s", "DURATION"));
            stateSB.append(String.format(" %1$-" + namesSize[5] + "s", "#NODES KILLED"));
            print(stateSB.toString());
            for (TaskState ts : js.getTasks()) {
                stateSB = new StringBuilder();
                formattedDuration = Tools.getFormattedDuration(ts.getFinishedTime(), ts.getStartTime());
                nodesKilled = (ts.getMaxNumberOfExecutionOnFailure() - ts.getNumberOfExecutionOnFailureLeft()) +
                    "/" + ts.getMaxNumberOfExecutionOnFailure();
                stateSB.append(String.format("\t%1$-" + namesSize[0] + "s", ts.getId()));
                stateSB.append(String.format(" %1$-" + namesSize[1] + "s", cutNchar(ts.getName(), 20)));
                stateSB.append(String.format(" %1$-" + namesSize[2] + "s", ts.getStatus()));
                stateSB.append(String.format(" %1$-" + namesSize[3] + "s", cutNchar(
                        ts.getExecutionHostName(), 24)));
                stateSB.append(String.format(" %1$-" + namesSize[4] + "s", formattedDuration));
                stateSB.append(String.format(" %1$-" + namesSize[5] + "s", nodesKilled));
                print(stateSB.toString());
            }
            return js;
        } catch (Exception e) {
            handleExceptionDisplay("Error on job " + jobId, e);
            return null;
        }
    }

    public static void schedulerState() {
        getModel().checkIsReady();
        getModel().schedulerState_();
    }

    private void schedulerState_() {
        try {
            SchedulerState state = scheduler.getSchedulerState();
            int[] namesSize = new int[] { 2, 4, 5, 8, 7, 6, 8, 8 };
            for (JobState js : state.getPendingJobs()) {
                computeSpaces(namesSize, js);
            }
            for (JobState js : state.getRunningJobs()) {
                computeSpaces(namesSize, js);
            }
            for (JobState js : state.getFinishedJobs()) {
                computeSpaces(namesSize, js);
            }
            namesSize[0] += 4;
            namesSize[1] += 4;
            namesSize[2] += 4;
            namesSize[3] += 4;
            namesSize[4] += 4;
            namesSize[5] += 4;
            namesSize[6] += 4;
            namesSize[7] += 4;
            StringBuilder stateSB = new StringBuilder();
            stateSB.append(String.format("\t%1$-" + namesSize[0] + "s", "ID"));
            stateSB.append(String.format(" %1$-" + namesSize[1] + "s", "NAME"));
            stateSB.append(String.format(" %1$-" + namesSize[2] + "s", "OWNER"));
            stateSB.append(String.format(" %1$-" + namesSize[3] + "s", "PRIORITY"));
            stateSB.append(String.format(" %1$-" + namesSize[4] + "s", "PROJECT"));
            stateSB.append(String.format(" %1$-" + namesSize[5] + "s", "STATUS"));
            stateSB.append(String.format(" %1$-" + namesSize[6] + "s", "START AT"));
            stateSB.append(String.format(" %1$-" + namesSize[7] + "s", "DURATION"));
            print(stateSB.toString() + "\n");
            JobState.setSortingBy(JobState.SORT_BY_ID);
            JobState.setSortingOrder(JobState.ASC_ORDER);
            Collections.sort(state.getPendingJobs());
            Collections.sort(state.getRunningJobs());
            Collections.sort(state.getFinishedJobs());
            for (JobState js : state.getFinishedJobs()) {
                print(printFormatted(namesSize, js).toString());
            }
            print("");
            for (JobState js : state.getRunningJobs()) {
                print(printFormatted(namesSize, js).toString());
            }
            print("");
            for (JobState js : state.getPendingJobs()) {
                print(printFormatted(namesSize, js).toString());
            }
        } catch (Exception e) {
            handleExceptionDisplay("Error while getting list of jobs", e);
        }

    }

    private void computeSpaces(int[] namesSize, JobState js) {
        String formattedDuration = Tools.getFormattedDuration(js.getFinishedTime(), js.getStartTime());
        String time = Tools.getFormattedDate(js.getStartTime());
        namesSize[0] = (js.getId().toString().length() > namesSize[0]) ? js.getId().toString().length()
                : namesSize[0];
        namesSize[1] = (cutNchar(js.getName(), 20).length() > namesSize[1]) ? cutNchar(js.getName(), 20)
                .length() : namesSize[1];
        namesSize[2] = (js.getOwner().length() > namesSize[2]) ? js.getOwner().length() : namesSize[2];
        namesSize[3] = (js.getPriority().toString().length() > namesSize[3]) ? js.getPriority().toString()
                .length() : namesSize[3];
        namesSize[4] = (cutNchar(js.getProjectName(), 20).length() > namesSize[4]) ? cutNchar(
                js.getProjectName(), 20).length() : namesSize[4];
        namesSize[5] = (js.getStatus().toString().length() > namesSize[5]) ? js.getStatus().toString()
                .length() : namesSize[5];
        namesSize[6] = (time.length() > namesSize[6]) ? time.length() : namesSize[6];
        namesSize[7] = (formattedDuration.length() > namesSize[7]) ? formattedDuration.length()
                : namesSize[7];
    }

    private StringBuilder printFormatted(int[] namesSize, JobState js) {
        StringBuilder stateSB = new StringBuilder();
        String formattedDuration = Tools.getFormattedDuration(js.getFinishedTime(), js.getStartTime());
        String time = Tools.getFormattedDate(js.getStartTime());
        stateSB.append(String.format("\t%1$-" + namesSize[0] + "s", js.getId()));
        stateSB.append(String.format(" %1$-" + namesSize[1] + "s", cutNchar(js.getName(), 20)));
        stateSB.append(String.format(" %1$-" + namesSize[2] + "s", js.getOwner()));
        stateSB.append(String.format(" %1$-" + namesSize[3] + "s", js.getPriority()));
        stateSB.append(String.format(" %1$-" + namesSize[4] + "s", js.getProjectName()));
        stateSB.append(String.format(" %1$-" + namesSize[5] + "s", js.getStatus()));
        stateSB.append(String.format(" %1$-" + namesSize[6] + "s", time));
        stateSB.append(String.format(" %1$-" + namesSize[7] + "s", formattedDuration));
        return stateSB;
    }

    /**
     * Cut the given string to the specified number of character
     *
     * @param str the string to cut.
     * @param nbChar the maximum length of the string to be returned
     * @return a string that is the same as the given one if str.length() is lesser or equals to nbChar,
     * 			otherwise a shortcut string endind with '...'
     */
    private String cutNchar(String str, int nbChar) {
        if (str == null) {
            return "";
        }
        nbChar--;//use to have a space after the returned string
        if (str == null || str.length() <= nbChar) {
            return str;
        }
        return str.substring(0, nbChar - 3) + "...";
    }

    public static void JMXinfo() {
        getModel().JMXinfo_();
    }

    private void JMXinfo_() {
        try {
            print(jmxInfoViewer.getInfo());
        } catch (Exception e) {
            handleExceptionDisplay("Error while retrieving JMX informations", e);
        }
    }

    public static void test() {
        getModel().test_();
    }

    private void test_() {
        try {
            String descriptorPath = PASchedulerProperties.SCHEDULER_HOME + "/samples/jobs_descriptors/";
            if (System.getProperty("pa.scheduler.home") == null) {
                System.setProperty("pa.scheduler.home", PASchedulerProperties.SCHEDULER_HOME
                        .getValueAsString());
            }
            getModel().submit_(descriptorPath + "Job_2_tasks.xml");
            getModel().submit_(descriptorPath + "Job_8_tasks.xml");
            getModel().submit_(descriptorPath + "Job_Aborted.xml");
            getModel().submit_(descriptorPath + "Job_fork.xml");
            getModel().submit_(descriptorPath + "Job_nativ.xml");
            getModel().submit_(descriptorPath + "Job_PI.xml");
            getModel().submit_(descriptorPath + "Job_pre_post.xml");
            getModel().submit_(descriptorPath + "Job_with_dep.xml");
            getModel().submit_(descriptorPath + "Job_with_select_script.xml");
        } catch (Exception e) {
            handleExceptionDisplay("Error while starting examples", e);
        }
    }

    public static void exec(String commandFilePath) {
        getModel().checkIsReady();
        getModel().exec_(commandFilePath);
    }

    private void exec_(String commandFilePath) {
        try {
            File f = new File(commandFilePath.trim());
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br));
            br.close();
        } catch (Exception e) {
            handleExceptionDisplay("*ERROR*", e);
        }
    }

    public static void exit() {
        getModel().checkIsReady();
        getModel().exit_();
    }

    private void exit_() {
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

    public static UserSchedulerInterface getUserScheduler() {
        getModel().checkIsReady();
        return getModel().getUserScheduler_();
    }

    private UserSchedulerInterface getUserScheduler_() {
        return scheduler;
    }

    //****************** HELP SCREEN ********************

    protected String helpScreen() {
        StringBuilder out = new StringBuilder("Scheduler controller commands are :" + newline + newline);

        out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s %2$s" + newline + newline, commands.get(
                0).getName(), commands.get(0).getDescription()));

        for (int i = 1; i < commands.size(); i++) {
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
    public UserSchedulerInterface getScheduler() {
        return scheduler;
    }

    /**
     * Connect the scheduler value to the given scheduler value
     *
     * @param scheduler the scheduler to connect
     */
    public void connectScheduler(UserSchedulerInterface scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("Given Scheduler is null");
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
