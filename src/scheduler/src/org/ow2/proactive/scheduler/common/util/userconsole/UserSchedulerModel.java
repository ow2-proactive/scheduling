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
import java.util.ArrayList;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.utils.console.Command;
import org.ow2.proactive.utils.console.ConsoleModel;


/**
 * UserSchedulerModel is the class that drives the Scheduler console in the user view.
 * To use this class, get the model, connect a Scheduler and a console, and just start this model.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class UserSchedulerModel extends ConsoleModel {

    protected static UserSchedulerModel userModel;
    protected String jsInitFile = "UserActions.js";

    protected UserSchedulerInterface scheduler;

    protected ArrayList<Command> commands;

    public static UserSchedulerModel getModel() {
        if (userModel == null) {
            userModel = new UserSchedulerModel();
        }
        return userModel;
    }

    private UserSchedulerModel() {
        setJS_INIT_FILE(jsInitFile);
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
                        "AND a string representing the new priority)\n" +
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
        //commands.add(new Command("jmxinfo()", "Display some statistics provided by the Scheduler MBean"));
        commands
                .add(new Command("exec(commandFilePath)",
                    "Execute the content of the given script file (parameter is a string representing a command-file path)"));
        commands.add(new Command("exit()", "Exit Scheduler controller"));
    }

    protected void checkIsReady() {
        super.checkIsReady();
        if (scheduler == null) {
            throw new RuntimeException("Scheduler is not set, it must be set before starting the model");
        }
    }

    public void start() throws IOException, SchedulerException {
        checkIsReady();
        console.start(" > ");
        console.print("Type command here (type '?' or help() to see the list of commands)\n");
        initialize();
        String stmt;
        while (!terminated) {
            SchedulerStatus status = scheduler.getStatus();
            String prompt = " ";
            if (status != SchedulerStatus.STARTED) {
                prompt += "[" + status.toString() + "] ";
            }
            stmt = console.readStatement(prompt + "> ");
            if ("?".equals(stmt)) {
                console.print("\n" + helpScreen());
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

    private void help_() {
        print("\n" + helpScreen());
    }

    public static String submit(String xmlDescriptor) {
        getModel().checkIsReady();
        return getModel().submit_(xmlDescriptor);
    }

    private String submit_(String xmlDescriptor) {
        try {
            Job job = JobFactory.getFactory().createJob(xmlDescriptor);
            JobId id = scheduler.submit(job);
            print("Job successfully submitted ! (id=" + id.value() + ")");
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
                print("Job " + jobId + " result => \n");

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
                print("Task " + taskName + " result => \n");

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
                print("Job " + jobId + " output => \n");

                for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                    TaskResult tRes = e.getValue();

                    try {
                        print(e.getKey() + " : \n" + tRes.getOutput().getAllLogs(false));
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

    //    public static void JMXinfo() {
    //    	userModel.JMXinfo_();
    //    }
    //
    //    private void JMXinfo_() {
    //        try {
    //            print(mbeanInfoViewer.getInfo());
    //        } catch (Exception e) {
    //            handleExceptionDisplay("Error while retrieving JMX informations", e);
    //        }
    //    }

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
        console.print("Exiting controller.");
        try {
            scheduler.disconnect();
        } catch (Exception e) {
        }
        terminated = true;
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
        StringBuilder out = new StringBuilder("Scheduler controller commands are :\n\n");

        out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s %2$s\n\n", commands.get(0).getName(),
                commands.get(0).getDescription()));

        for (int i = 1; i < commands.size(); i++) {
            out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s %2$s\n", commands.get(i).getName(),
                    commands.get(i).getDescription()));
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

}
