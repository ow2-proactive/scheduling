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
package org.objectweb.proactive.extra.scheduler.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * AdminCommunicator ...
 *
 * @author jlscheef - ProActiveTeam
 * @date 24 oct. 07
 * @version 3.2
 *
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
    private static boolean stopCommunicator;

    /**
     * @param args
     */
    private static void output(String message) {
        System.out.print(message);
    }

    private static void error(String message) {
        System.err.print(message);
    }

    public static void main(String[] args) {
        try {
            SchedulerAuthenticationInterface auth;

            if (args.length > 0) {
                auth = SchedulerConnection.join(args[0]);
            } else {
                auth = SchedulerConnection.join(null);
            }

            scheduler = auth.logAsAdmin("jl", "jl");
            stopCommunicator = false;
            startCommandListener();
        } catch (Exception e) {
            error("A fatal error has occured : " + e.getMessage() +
                "\n Will shut down communicator.\n");
            e.printStackTrace();
            System.exit(1);
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
                output("Start is impossible!! Cause :" + e.getMessage() + "\n");
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
                output("Stop is impossible!! Cause :" + e.getMessage() + "\n");
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
                output("Pause is impossible!! Cause :" + e.getMessage() + "\n");
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
                output("Freeze is impossible!! Cause :" + e.getMessage() +
                    "\n");
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
                output("Resume is impossible!! Cause :" + e.getMessage() +
                    "\n");
            }
        } else if (command.equals(SHUTDOWN_CMD)) {
            try {
                if (scheduler.shutdown().booleanValue()) {
                    output(
                        "Shutdown sequence initialized, it might take a while to finish all executions, communicator will exit.\n");
                    stopCommunicator = true;
                } else {
                    output(
                        "Shutdown the scheduler is impossible for the moment.\n");
                }
            } catch (SchedulerException e) {
                output("Shutdown is impossible!! Cause :" + e.getMessage() +
                    "\n");
            }
        } else if (command.equals(KILL_CMD)) {
            try {
                if (scheduler.kill().booleanValue()) {
                    output(
                        "Sheduler has just been killed, communicator will exit.\n");
                    stopCommunicator = true;
                } else {
                    output(
                        "killed the scheduler is impossible for the moment.\n");
                }
            } catch (SchedulerException e) {
                output("Kill is impossible!! Cause :" + e.getMessage() + "\n");
            }
        } else if (command.startsWith(PAUSEJOB_CMD)) {
            try {
                boolean success = scheduler.pause(JobId.makeJobId(
                            command.split(" ")[1])).booleanValue();

                if (success) {
                    output("Job paused.\n");
                } else {
                    output("Paused job is impossible !!\n");
                }
            } catch (SchedulerException e) {
                output("Error while pausing this job !! : " + e.getMessage() +
                    "\n");
            }
        } else if (command.startsWith(RESUMEJOB_CMD)) {
            try {
                boolean success = scheduler.resume(JobId.makeJobId(
                            command.split(" ")[1])).booleanValue();

                if (success) {
                    output("Job resumed.\n");
                } else {
                    output("Resume job is impossible !!\n");
                }
            } catch (SchedulerException e) {
                output("Error while resuming this job !! : " + e.getMessage() +
                    "\n");
            }
        } else if (command.startsWith(KILLJOB_CMD)) {
            try {
                boolean success = scheduler.kill(JobId.makeJobId(
                            command.split(" ")[1])).booleanValue();

                if (success) {
                    output("Job killed.\n");
                } else {
                    output("Kill job is impossible !!\n");
                }
            } catch (SchedulerException e) {
                output("Error while killing this job !! : " + e.getMessage() +
                    "\n");
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
                        JobResult result = scheduler.getJobResult(JobId.makeJobId(i +
                                    ""));

                        if (result != null) {
                            System.out.println("Job " + i + " Result => ");

                            for (Entry<String, TaskResult> e : result.getAllResults()
                                                                     .entrySet()) {
                                TaskResult tRes = e.getValue();

                                try {
                                    System.out.println("\t " + e.getKey() +
                                        " : " + tRes.value());
                                } catch (Throwable e1) {
                                    System.out.println(
                                        "\t ERROR during execution of " +
                                        e.getKey() + "... ");
                                    tRes.getException().printStackTrace();
                                }
                            }
                        } else {
                            System.out.println("Job " + i +
                                " is not finished or unknown !");
                        }
                    } catch (SchedulerException e) {
                        System.out.println("Error on job " + i + " : " +
                            e.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                output("Error while getting this job result !! : " +
                    e.getMessage() + "\n");
            }
        } else {
            output(
                "UNKNOWN COMMAND!!... Please type '?' or 'help' to see the list of commands\n");
        }
    }

    private static void startCommandListener() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    System.in));

        while (!stopCommunicator) {
            output(" > ");

            String line = reader.readLine();

            try {
                handleCommand(line);
            } catch (NumberFormatException e) {
                error("Id error !!\n");
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
        String out = "";
        out += "Communicator Commands are:\n\n";
        out += String.format(" %1$-18s\t Display statistics\n", STAT_CMD);
        out += String.format(" %1$-18s\t Starts scheduler\n", START_CMD);
        out += String.format(" %1$-18s\t Stops scheduler\n", STOP_CMD);
        out += String.format(" %1$-18s\t pauses all running tasks\n", PAUSE_CMD);
        out += String.format(" %1$-18s\t pauses immediately all running jobs\n",
            PAUSE_IM_CMD);
        out += String.format(" %1$-18s\t resumes all queued tasks\n", RESUME_CMD);
        out += String.format(" %1$-18s\t Waits for running tasks to finish and shutdown\n",
            SHUTDOWN_CMD);
        out += String.format(" %1$-18s\t Kill every tasks and jobs and shutdown\n",
            KILL_CMD);
        out += String.format(" %1$-18s\t Pause the given job (pausejob num_job)\n",
            PAUSEJOB_CMD);
        out += String.format(" %1$-18s\t Resume the given job (resumejob num_job)\n",
            RESUMEJOB_CMD);
        out += String.format(" %1$-18s\t Kill the given job (killjob num_job)\n",
            KILLJOB_CMD);
        out += String.format(" %1$-18s\t get the result of the given job (result num_job | result num_job to num_job)\n",
            GET_RESULT_CMD);
        out += String.format(" %1$-18s\t Exits Communicator\n", EXIT_CMD);
        output(out);
    }
}
