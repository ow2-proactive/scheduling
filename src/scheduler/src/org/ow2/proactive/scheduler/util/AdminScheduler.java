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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.passwordhandler.PasswordField;
import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.Tools;
import org.ow2.proactive.scheduler.job.JobIdImpl;


/**
 * AdminScheduler will help you to manage the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class AdminScheduler {

    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);
    private static final String SCHEDULER_DEFAULT_URL = Tools.getHostURL("//localhost/");

    private static AdminSchedulerInterface scheduler;
    private static final String STAT_CMD = "stat";
    private static final String START_CMD = "start";
    private static final String STOP_CMD = "stop";
    private static final String PAUSE_CMD = "pause";
    private static final String FREEZE_CMD = "freeze";
    private static final String RESUME_CMD = "resume";
    private static final String SHUTDOWN_CMD = "shutdown";
    private static final String KILL_CMD = "kill";
    private static final String EXIT_CMD = "exit";
    private static final String PAUSEJOB_CMD = "pausejob";
    private static final String RESUMEJOB_CMD = "resumejob";
    private static final String KILLJOB_CMD = "killjob";
    private static final String SUBMIT_CMD = "submit";
    private static final String GET_RESULT_CMD = "result";
    private static final String RECONNECT_RM_CMD = "rebind";
    private static boolean stopCommunicator;
    private static boolean displayException = false;

    private static void error(String message, Exception e) {
        logger.error(message);
        if (displayException) {
            logger.error(e);
        }
    }

    /**
     * Start the communicator
     *
     * @param args
     */
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

        Option schedulerURL = new Option("u", "schedulerURL", true, "the scheduler URL (default " +
            SCHEDULER_DEFAULT_URL + ")");
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
                String url;
                if (cmd.hasOption("u")) {
                    url = cmd.getOptionValue("u");
                } else {
                    url = SCHEDULER_DEFAULT_URL;
                }
                logger.info("Trying to connect Scheduler on " + url);
                auth = SchedulerConnection.join(url);
                logger.info("\t-> Connection established on " + url);

                if (cmd.hasOption("e"))
                    displayException = true;

                logger.info("\nConnecting admin to the Scheduler");
                if (cmd.hasOption("l")) {
                    user = cmd.getOptionValue("l");
                    pwdMsg = user + "'s password: ";
                } else {
                    System.out.print("login: ");
                    user = buf.readLine();
                    pwdMsg = "password: ";
                }

                //ask password to User
                char password[] = null;
                try {
                    password = PasswordField.getPassword(System.in, pwdMsg);
                    pwd = String.valueOf(password);
                } catch (IOException ioe) {
                    logger.error(ioe);
                }

                scheduler = auth.logAsAdmin(user, pwd);

                logger.info("\t-> Admin '" + user + "' successfully connected\n");

                stopCommunicator = false;
                startCommandListener();
            }
        } catch (MissingArgumentException e) {
            logger.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (MissingOptionException e) {
            logger.error("Missing option: " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (UnrecognizedOptionException e) {
            logger.error(e.getLocalizedMessage());
            displayHelp = true;
        } catch (AlreadySelectedException e) {
            logger.error(e.getClass().getSimpleName() + " : " + e.getLocalizedMessage());
            displayHelp = true;
        } catch (ParseException e) {
            displayHelp = true;
        } catch (Exception e) {
            logger.error("A fatal error has occured : " + e.getMessage() + "\n Shutdown the shell.\n");
            logger.error(e);
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
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(120);
            hf.printHelp("adminScheduler" + Tools.shellExtension(), options, true);
            System.exit(2);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    private static void handleCommand(String command) {
        if (command.equals("")) {
        } else if (command.equals(EXIT_CMD)) {
            logger.info("Shell will exit.");
            stopCommunicator = true;
        } else if (command.equals("?") || command.equals("help")) {
            helpScreen();
        } else if (command.equals(STAT_CMD)) {
            statScreen();
        } else if (command.equals(START_CMD)) {
            try {
                boolean success = scheduler.start().booleanValue();

                if (success) {
                    logger.info("Scheduler started.");
                } else {
                    logger.warn("Start is impossible!!");
                }
            } catch (SchedulerException e) {
                error("Start is impossible !!", e);
            }
        } else if (command.equals(STOP_CMD)) {
            try {
                boolean success = scheduler.stop().booleanValue();

                if (success) {
                    logger.info("Scheduler stopped.");
                } else {
                    logger.warn("Stop is impossible !!");
                }
            } catch (SchedulerException e) {
                error("Stop is impossible !!", e);
            }
        } else if (command.equals(PAUSE_CMD)) {
            try {
                boolean success = scheduler.pause().booleanValue();

                if (success) {
                    logger.info("Scheduler paused.");
                } else {
                    logger.warn("Pause is impossible !!");
                }
            } catch (SchedulerException e) {
                error("Pause is impossible !!", e);
            }
        } else if (command.equals(FREEZE_CMD)) {
            try {
                boolean success = scheduler.freeze().booleanValue();

                if (success) {
                    logger.info("Scheduler frozen.");
                } else {
                    logger.warn("Freeze is impossible !!");
                }
            } catch (SchedulerException e) {
                error("Freeze is impossible !!", e);
            }
        } else if (command.equals(RESUME_CMD)) {
            try {
                boolean success = scheduler.resume().booleanValue();

                if (success) {
                    logger.info("Scheduler resumed.");
                } else {
                    logger.warn("Resume is impossible !!");
                }
            } catch (SchedulerException e) {
                error("Resume is impossible !!", e);
            }
        } else if (command.equals(SHUTDOWN_CMD)) {
            try {
                if (scheduler.shutdown().booleanValue()) {
                    logger
                            .info("Shutdown sequence initialized, it might take a while to finish all executions, shell will exit.");
                    stopCommunicator = true;
                } else {
                    logger.warn("Shutdown the scheduler is impossible for the moment.");
                }
            } catch (SchedulerException e) {
                error("Shutdown is impossible.", e);
            }
        } else if (command.equals(KILL_CMD)) {
            try {
                if (scheduler.kill().booleanValue()) {
                    logger.info("Sheduler has just been killed, shell will exit.");
                    stopCommunicator = true;
                } else {
                    logger.warn("killed the scheduler is impossible for the moment.");
                }
            } catch (SchedulerException e) {
                error("kill is impossible !!", e);
            }
        } else if (command.startsWith(PAUSEJOB_CMD)) {
            try {
                boolean success = scheduler.pause(JobIdImpl.makeJobId(command.split(" ")[1])).booleanValue();

                if (success) {
                    logger.info("Job paused.");
                } else {
                    logger.warn("Paused job is impossible !!");
                }
            } catch (SchedulerException e) {
                error("Error while pausing this job !!", e);
            }
        } else if (command.startsWith(RESUMEJOB_CMD)) {
            try {
                boolean success = scheduler.resume(JobIdImpl.makeJobId(command.split(" ")[1])).booleanValue();

                if (success) {
                    logger.info("Job resumed.");
                } else {
                    logger.warn("Resume job is impossible !!");
                }
            } catch (SchedulerException e) {
                error("Error while resuming this job !!", e);
            }
        } else if (command.startsWith(KILLJOB_CMD)) {
            try {
                boolean success = scheduler.kill(JobIdImpl.makeJobId(command.split(" ")[1])).booleanValue();

                if (success) {
                    logger.info("Job killed.");
                } else {
                    logger.warn("Kill job is impossible !!");
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
                        JobResult result = scheduler.getJobResult(i + "");

                        if (result != null) {
                            logger.info("Job " + i + " Result => ");

                            for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                                TaskResult tRes = e.getValue();

                                try {
                                    logger.info("\t " + e.getKey() + " : " + tRes.value());
                                } catch (Throwable e1) {
                                    logger.error(tRes.getException());
                                }
                            }
                        } else {
                            logger.info("Job " + i + " is not finished or unknown !");
                        }
                    } catch (SchedulerException e) {
                        logger.error("Error on job " + i + " : " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                error("Error while getting this job result !!", e);
            }
        } else if (command.startsWith(SUBMIT_CMD)) {
            String url = command.split(" ")[1].trim();
            try {
                Job job = JobFactory.getFactory().createJob(url);
                scheduler.submit(job);
                logger.info("Job successfully submitted !");
            } catch (Exception e) {
                logger.error("Error on job Submission (url=" + url + ")" + " : " + e.getMessage());
            }
        } else if (command.startsWith(RECONNECT_RM_CMD)) {
            try {
                String rmurl = command.replaceFirst(RECONNECT_RM_CMD, "");
                boolean success = scheduler.linkResourceManager(rmurl.trim()).booleanValue();
                if (success) {
                    logger
                            .info("The new Resource Manager has been rebind to the scheduler at " + rmurl +
                                ".");
                } else {
                    logger.error("Reconnect a Resource Manager is only possible when RM is dead !");
                }
            } catch (Exception e) {
                error("Cannot join the new RM !", e);
            }
        } else {
            logger.warn("UNKNOWN COMMAND : Please type '?' or 'help' to see the list of commands");
        }
    }

    private static void startCommandListener() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("type command here (type '?' or 'help' to see the list of commands)");

        while (!stopCommunicator) {
            System.out.print(" > ");

            String line = reader.readLine();

            try {
                handleCommand(line);
            } catch (NumberFormatException e) {
                error("Id error !!", e);
            } catch (Exception e) {
                logger.error("Command error ! plz check your last command !");
            }
        }
    }

    private static void statScreen() {
        try {
            Map<String, Object> stat = scheduler.getStats().getProperties();
            String out = "";

            for (Entry<String, Object> e : stat.entrySet()) {
                out += (e.getKey() + " : " + e.getValue() + "\n");
            }

            logger.info(out);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static void helpScreen() {
        StringBuilder out = new StringBuilder("Admin Shell Commands are:\n\n");

        out.append(String.format(" %1$-18s\t Display statistics\n", STAT_CMD));
        out.append(String.format(" %1$-18s\t Starts scheduler\n", START_CMD));
        out.append(String.format(" %1$-18s\t Stops scheduler\n", STOP_CMD));
        out.append(String.format(" %1$-18s\t pauses all running tasks\n", PAUSE_CMD));
        out.append(String.format(" %1$-18s\t freezes all running jobs\n", FREEZE_CMD));
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
        out.append(String.format(" %1$-18s\t Submit a new job (" + SUBMIT_CMD + " job_url)\n", SUBMIT_CMD));
        out.append(String.format(" %1$-18s\t Reconnect a Resource Manager (" + RECONNECT_RM_CMD + " url)\n",
                RECONNECT_RM_CMD));
        out.append(String.format(" %1$-18s\t Exits Shell\n", EXIT_CMD));

        logger.info(out.toString());
    }
}
