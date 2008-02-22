/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extensions.scheduler.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobResult;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.AdminSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;

import com.sun.security.auth.callback.TextCallbackHandler;


/**
 * AdminCommunicator ...
 *
 * @author SCHEEFER Jean-Luc & FRADJ Johann
 * @since ProActive 3.9
 */
public class AdminCommunicator {
    private static AdminSchedulerInterface scheduler;
    private static final String STAT_CMD = "stat";
    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String PAUSE_CMD = "pause";
    private static final String PAUSE_IM_CMD = "pausei";
    private static final String RESUME_CMD = "resume";
    private static final String SHUTDOWN_CMD = "shutdown";
    private static final String KILL_CMD = "kill";
    private static final String EXIT_CMD = "exit";
    private static final String PAUSEJOB_CMD = "pausejob";
    private static final String RESUMEJOB_CMD = "resumejob";
    private static final String KILLJOB_CMD = "killjob";
    private static final String GET_RESULT_CMD = "result";
    private static final String RECONNECT_RM_CMD = "rebind";
    private static boolean stopCommunicator;
    private static boolean displayException = false;

    private static void output(String message) {
        System.out.print(message);
    }

    private static void error(String message) {
        System.err.print(message);
    }

    private static void error(String message, Exception e) {
        error(message);
        if (displayException) {
            e.printStackTrace();
            System.out.println();
        }
    }

    public static void main(String[] args) {

        Options options = new Options();

        Option help = new Option("h", "help", false, "to display this help");
        help.setArgName("help");
        help.setRequired(false);
        options.addOption(help);

        Option username = new Option("l", "login", true, "the login");
        username.setArgName("login");
        username.setRequired(false);
        options.addOption(username);

        Option exception = new Option("e", "exception", false, "display every exceptions");
        exception.setArgName("exception");
        exception.setRequired(false);
        options.addOption(exception);

        Option schedulerURL = new Option("u", "schedulerURL", true, "the scheduler URL (default //localhost)");
        schedulerURL.setArgName("schedulerURL");
        schedulerURL.setRequired(false);
        options.addOption(schedulerURL);

        boolean displayHelp = false;

        InputStreamReader reader = new InputStreamReader(System.in);
        BufferedReader buf = new BufferedReader(reader);

        try {
            String user = null;
            String pwd = null;
            String pwdMsg = null;
            SchedulerAuthenticationInterface auth = null;

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                displayHelp = true;
            else {
                if (cmd.hasOption("u"))
                    auth = SchedulerConnection.join(cmd.getOptionValue("u"));
                else
                    auth = SchedulerConnection.join(null);

                if (cmd.hasOption("e"))
                    displayException = true;

                if (cmd.hasOption("l")) {
                    user = cmd.getOptionValue("l");
                    pwdMsg = user + "'s password: ";
                } else {
                    System.out.print("login: ");
                    user = buf.readLine();
                    pwdMsg = "password: ";
                }

                //ask password to User 
                TextCallbackHandler handler = new TextCallbackHandler();
                PasswordCallback pwdCallBack = new PasswordCallback(pwdMsg, false);
                Callback[] callbacks = new Callback[] { pwdCallBack };
                handler.handle(callbacks);
                pwd = new String(pwdCallBack.getPassword());

                scheduler = auth.logAsAdmin(user, pwd);

                stopCommunicator = false;
                startCommandListener();
            }
        } catch (MissingArgumentException e) {
            System.out.println(e.getLocalizedMessage());
            displayHelp = true;
        } catch (MissingOptionException e) {
            System.out.println("Missing option: " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            System.out.println(e.getLocalizedMessage());
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            System.out.println(e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (ParseException e) {
            displayHelp = true;
        } catch (Exception e) {
            error("A fatal error has occured : " + e.getMessage() + "\n Will shut down communicator.\n");
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            if (buf != null)
                try {
                    buf.close();
                } catch (IOException e) {
                }
        }

        if (displayHelp) {
            System.out.println();
            new HelpFormatter().printHelp("jobLauncher", options, true);
            System.exit(2);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    private static void handleCommand(String command) {
        if (command.equals("")) {
        } else if (command.equals(EXIT_CMD)) {
            output("Communicator will exit.\n");
            stopCommunicator = true;
        } else if (command.equals("?") || command.equals("help")) {
            helpScreen();
        } else if (command.equals(STAT_CMD)) {
            statScreen();
        } else if (command.equals(START_CMD)) {
            try {
                boolean success = scheduler.start().booleanValue();

                if (success) {
                    output("Scheduler started.\n");
                } else {
                    output("Start is impossible!!\n");
                }
            } catch (SchedulerException e) {
                error("Start is impossible !!", e);
            }
        } else if (command.equals(STOP_CMD)) {
            try {
                boolean success = scheduler.stop().booleanValue();

                if (success) {
                    output("Scheduler stopped.\n");
                } else {
                    output("Stop is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Stop is impossible !!", e);
            }
        } else if (command.equals(PAUSE_CMD)) {
            try {
                boolean success = scheduler.pause().booleanValue();

                if (success) {
                    output("Scheduler paused.\n");
                } else {
                    output("Pause is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Pause is impossible !!", e);
            }
        } else if (command.equals(PAUSE_IM_CMD)) {
            try {
                boolean success = scheduler.pauseImmediate().booleanValue();

                if (success) {
                    output("Scheduler freezed.\n");
                } else {
                    output("Freeze is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Freeze is impossible !!", e);
            }
        } else if (command.equals(RESUME_CMD)) {
            try {
                boolean success = scheduler.resume().booleanValue();

                if (success) {
                    output("Scheduler resumed.\n");
                } else {
                    output("Resume is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Resume is impossible !!", e);
            }
        } else if (command.equals(SHUTDOWN_CMD)) {
            try {
                if (scheduler.shutdown().booleanValue()) {
                    output("Shutdown sequence initialized, it might take a while to finish all executions, communicator will exit.\n");
                    stopCommunicator = true;
                } else {
                    output("Shutdown the scheduler is impossible for the moment.\n");
                }
            } catch (SchedulerException e) {
                error("Shutdown is impossible !!", e);
            }
        } else if (command.equals(KILL_CMD)) {
            try {
                if (scheduler.kill().booleanValue()) {
                    output("Sheduler has just been killed, communicator will exit.\n");
                    stopCommunicator = true;
                } else {
                    output("killed the scheduler is impossible for the moment.\n");
                }
            } catch (SchedulerException e) {
                error("kill is impossible !!", e);
            }
        } else if (command.startsWith(PAUSEJOB_CMD)) {
            try {
                boolean success = scheduler.pause(JobId.makeJobId(command.split(" ")[1])).booleanValue();

                if (success) {
                    output("Job paused.\n");
                } else {
                    output("Paused job is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Error while pausing this job !!", e);
            }
        } else if (command.startsWith(RESUMEJOB_CMD)) {
            try {
                boolean success = scheduler.resume(JobId.makeJobId(command.split(" ")[1])).booleanValue();

                if (success) {
                    output("Job resumed.\n");
                } else {
                    output("Resume job is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Error while resuming this job !!", e);
            }
        } else if (command.startsWith(KILLJOB_CMD)) {
            try {
                boolean success = scheduler.kill(JobId.makeJobId(command.split(" ")[1])).booleanValue();

                if (success) {
                    output("Job killed.\n");
                } else {
                    output("Kill job is impossible !!\n");
                }
            } catch (SchedulerException e) {
                error("Error while killing this job !!", e);
            }
        } else if (command.startsWith(GET_RESULT_CMD)) {
            try {
                String jID = command.replaceFirst(GET_RESULT_CMD, "");
                jID = jID.trim();

                int begin = 0;
                int end = 0;

                if (jID.matches(".* to .*")) {
                    String[] TjID = jID.split(" to ");
                    begin = Integer.parseInt(TjID[0]);
                    end = Integer.parseInt(TjID[1]);
                } else {
                    begin = Integer.parseInt(jID);
                    end = Integer.parseInt(jID);
                }

                for (int i = begin; i <= end; i++) {
                    try {
                        JobResult result = scheduler.getJobResult(JobId.makeJobId(i + ""));

                        if (result != null) {
                            System.out.println("Job " + i + " Result => ");

                            for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                                TaskResult tRes = e.getValue();

                                try {
                                    System.out.println("\t " + e.getKey() + " : " + tRes.value());
                                } catch (Throwable e1) {
                                    System.out.println("\t ERROR during execution of " + e.getKey() + "... ");
                                    tRes.getException().printStackTrace();
                                }
                            }
                        } else {
                            System.out.println("Job " + i + " is not finished or unknown !");
                        }
                    } catch (SchedulerException e) {
                        System.out.println("Error on job " + i + " : " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                error("Error while getting this job result !!", e);
            }
        } else if (command.startsWith(RECONNECT_RM_CMD)) {
            try {
                String rmurl = command.replaceFirst(RECONNECT_RM_CMD, "");
                boolean success = scheduler.linkResourceManager(rmurl.trim()).booleanValue();
                if (success) {
                    output("The new Resource Manager has been rebind to the scheduler at " + rmurl + ".\n");
                } else {
                    output("Reconnect a Resource Manager is possible only when RM is dead !\n");
                }
            } catch (Exception e) {
                error("Cannot join the new RM !", e);
            }
        } else {
            error("UNKNOWN COMMAND!!... Please type '?' or 'help' to see the list of commands\n");
        }
    }

    private static void startCommandListener() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (!stopCommunicator) {
            output(" > ");

            String line = reader.readLine();

            try {
                handleCommand(line);
            } catch (NumberFormatException e) {
                error("Id error !!\n", e);
            }
        }
    }

    private static void statScreen() {
        try {
            HashMap<String, Object> stat = scheduler.getStats().getProperties();
            String out = "";

            for (Entry<String, Object> e : stat.entrySet()) {
                out += (e.getKey() + " : " + e.getValue() + "\n");
            }

            output(out + "\n");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static void helpScreen() {
        StringBuilder out = new StringBuilder("Communicator Commands are:\n\n");

        out.append(String.format(" %1$-18s\t Display statistics\n", STAT_CMD));
        out.append(String.format(" %1$-18s\t Starts scheduler\n", START_CMD));
        out.append(String.format(" %1$-18s\t Stops scheduler\n", STOP_CMD));
        out.append(String.format(" %1$-18s\t pauses all running tasks\n", PAUSE_CMD));
        out.append(String.format(" %1$-18s\t pauses immediately all running jobs\n", PAUSE_IM_CMD));
        out.append(String.format(" %1$-18s\t resumes all queued tasks\n", RESUME_CMD));
        out
                .append(String.format(" %1$-18s\t Waits for running tasks to finish and shutdown\n",
                        SHUTDOWN_CMD));
        out.append(String.format(" %1$-18s\t Kill every tasks and jobs and shutdown\n", KILL_CMD));
        out.append(String.format(" %1$-18s\t Pause the given job (" + PAUSEJOB_CMD + " num_job)\n",
                PAUSEJOB_CMD));
        out.append(String.format(" %1$-18s\t Resume the given job (" + RESUMEJOB_CMD + " num_job)\n",
                RESUMEJOB_CMD));
        out.append(String
                .format(" %1$-18s\t Kill the given job (" + KILLJOB_CMD + " num_job)\n", KILLJOB_CMD));
        out.append(String.format(" %1$-18s\t Get the result of the given job (" + GET_RESULT_CMD +
            " num_job | result num_job to num_job)\n", GET_RESULT_CMD));
        out.append(String.format(" %1$-18s\t Reconnect a Resource Manager (" + RECONNECT_RM_CMD + " url)\n",
                RECONNECT_RM_CMD));
        out.append(String.format(" %1$-18s\t Exits Communicator\n", EXIT_CMD));

        output(out.toString());
    }
}
