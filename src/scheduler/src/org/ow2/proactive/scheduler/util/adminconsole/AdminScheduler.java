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
package org.ow2.proactive.scheduler.util.adminconsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.Tools;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.utils.console.Console;
import org.ow2.proactive.utils.console.SimpleConsole;


/**
 * AdminScheduler will help you to manage the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class AdminScheduler {

    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);
    private static final String SCHEDULER_DEFAULT_URL = Tools.getHostURL("//localhost/");
    private static final String JS_INIT_FILE = "Actions.js";
    private static final String YES = "yes";
    private static final String NO = "no";
    private static final String YES_NO = "(" + YES + "/" + NO + ")";
    private static final String control = "<ctrl> ";

    private static final String START_CMD = "start()";
    private static final String STOP_CMD = "stop()";
    private static final String PAUSE_CMD = "pause()";
    private static final String FREEZE_CMD = "freeze()";
    private static final String RESUME_CMD = "resume()";
    private static final String SHUTDOWN_CMD = "shutdown()";
    private static final String KILL_CMD = "kill()";
    private static final String EXIT_CMD = "exit()";
    private static final String PAUSEJOB_CMD = "pausejob(id)";
    private static final String RESUMEJOB_CMD = "resumejob(id)";
    private static final String KILLJOB_CMD = "killjob(id)";
    private static final String REMOVEJOB_CMD = "removejob(id)";
    private static final String SUBMIT_CMD = "submit(XMLdescriptor)";
    private static final String GET_RESULT_CMD = "result(id)";
    private static final String GET_OUTPUT_CMD = "output(id)";
    private static final String LINK_RM_CMD = "linkrm(rmURL)";
    private static final String EXEC_CMD = "exec(commandFilePath)";
    private static AdminSchedulerInterface scheduler;
    private static boolean initialized = false;
    private static boolean terminated = false;
    private static boolean intercativeMode = false;
    private static ScriptEngine engine;
    private static Console console = new SimpleConsole();

    /**
     * Start the Scheduler administrator
     *
     * @param args the arguments to be passed
     */
    public static void main(String[] args) {

        Options options = new Options();

        Option help = new Option("h", "help", false, "Display this help");
        help.setRequired(false);
        options.addOption(help);

        Option username = new Option("l", "login", true, "The username to join the Scheduler");
        username.setArgName("login");
        username.setRequired(false);
        options.addOption(username);

        Option schedulerURL = new Option("u", "schedulerURL", true, "The scheduler URL (default " +
            SCHEDULER_DEFAULT_URL + ")");
        schedulerURL.setArgName("schedulerURL");
        schedulerURL.setRequired(false);
        options.addOption(schedulerURL);

        addCommandLineOptions(options);

        boolean displayHelp = false;

        try {
            String user = null;
            String pwd = null;
            String pwdMsg = null;
            SchedulerAuthenticationInterface auth = null;

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                displayHelp = true;
            } else {
                String url;
                if (cmd.hasOption("u")) {
                    url = cmd.getOptionValue("u");
                } else {
                    url = SCHEDULER_DEFAULT_URL;
                }
                logger.info("Trying to connect Scheduler on " + url);
                auth = SchedulerConnection.join(url);
                logger.info("\t-> Connection established on " + url);

                logger.info("\nConnecting admin to the Scheduler");
                if (cmd.hasOption("l")) {
                    user = cmd.getOptionValue("l");
                    pwdMsg = user + "'s password: ";
                } else {
                    System.out.print("login: ");
                    BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
                    user = buf.readLine();
                    pwdMsg = "password: ";
                }

                //ask password to User
                char password[] = null;
                try {
                    password = PasswordField.getPassword(System.in, pwdMsg);
                    pwd = String.valueOf(password);
                } catch (IOException ioe) {
                    logger.error("" + ioe);
                }

                scheduler = auth.logAsAdmin(user, pwd);

                logger.info("\t-> Admin '" + user + "' successfully connected\n");

                //start one of the two command behavior
                if (startCommandLine(cmd)) {
                    startCommandListener();
                }
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
            logger.error("An error has occurred : " + e.getMessage() + "\n Shutdown the administrator.\n", e);
            System.exit(1);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(120);
            String note = "\nNOTE : if no commands marked with " + control +
                "is specified, the administrator will start in interactive mode.";
            hf.printHelp("adminScheduler" + Tools.shellExtension(), "", options, note, true);
            System.exit(2);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    private static void addCommandLineOptions(Options options) {
        Option opt = new Option("start", false, control + "Start the Scheduler");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("stop", false, control + "Stop the Scheduler");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("pause", false, control +
            "Pause the Scheduler (cause all non-running jobs to be paused)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("freeze", false, control +
            "Freeze the Scheduler (cause all non-running tasks to be paused)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("resume", false, control + "Resume the Scheduler");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("shutdown", false, control + "Shutdown the Scheduler");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("kill", false, control + "Kill the Scheduler");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("submit", true, control + "Submit the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("pausejob", true, control + "Pause the given job (pause every non-running tasks)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("resumejob", true, control + "Resume the given job (restart every paused tasks)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("killjob", true, control + "Kill the given job (cause the job to finish)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("removejob", true, control + "Remove the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("result", true, control + "Get the result of the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("output", true, control + "Get the output of the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("linkrm", true, control + "Reconnect a RM to the scheduler");
        opt.setArgName("rmURL");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);
    }

    private static void startCommandListener() throws Exception {
        initalize();
        console.start(" > ");
        console.printf("Type command here (type '?' or help() to see the list of commands)\n");
        String stmt;
        while (!terminated) {
            stmt = console.readStatement();
            if (stmt.equals("?")) {
                console.printf("\n" + helpScreen());
            } else {
                eval(stmt);
                console.printf("");
            }
        }
        console.stop();
    }

    private static boolean startCommandLine(CommandLine cmd) {
        intercativeMode = false;
        if (cmd.hasOption("start")) {
            start();
        } else if (cmd.hasOption("stop")) {
            stop();
        } else if (cmd.hasOption("pause")) {
            pause();
        } else if (cmd.hasOption("freeze")) {
            freeze();
        } else if (cmd.hasOption("resume")) {
            resume();
        } else if (cmd.hasOption("shutdown")) {
            shutdown();
        } else if (cmd.hasOption("kill")) {
            kill();
        } else if (cmd.hasOption("pausejob")) {
            pause(cmd.getOptionValue("pausejob"));
        } else if (cmd.hasOption("resumejob")) {
            resume(cmd.getOptionValue("resumejob"));
        } else if (cmd.hasOption("killjob")) {
            kill(cmd.getOptionValue("killjob"));
        } else if (cmd.hasOption("removejob")) {
            remove(cmd.getOptionValue("removejob"));
        } else if (cmd.hasOption("submit")) {
            submit(cmd.getOptionValue("submit"));
        } else if (cmd.hasOption("result")) {
            result(cmd.getOptionValue("result"));
        } else if (cmd.hasOption("output")) {
            output(cmd.getOptionValue("output"));
        } else if (cmd.hasOption("linkrm")) {
            linkRM(cmd.getOptionValue("linkrm"));
        } else {
            intercativeMode = true;
            return intercativeMode;
        }
        return false;
    }

    //***************** COMMAND LISTENER *******************

    private static void printf(String format, Object... args) {
        if (intercativeMode) {
            console.printf(format, args);
        } else {
            System.out.printf(format, args);
        }
    }

    private static void error(String format, Object... args) {
        if (intercativeMode) {
            console.error(format, args);
        } else {
            System.err.printf(format, args);
        }
    }

    public static void help() {
        printf("\n" + helpScreen());
    }

    public static boolean start() {
        boolean success = false;
        try {
            success = scheduler.start().booleanValue();
        } catch (SchedulerException e) {
            error("Start Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler started.");
        } else {
            printf("Scheduler cannot be started in its current state.");
        }
        return success;
    }

    public static boolean stop() {
        boolean success = false;
        try {
            success = scheduler.stop().booleanValue();
        } catch (SchedulerException e) {
            error("Stop Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler stopped.");
        } else {
            printf("Scheduler cannot be stopped in its current state.");
        }
        return success;
    }

    public static boolean pause() {
        boolean success = false;
        try {
            success = scheduler.pause().booleanValue();
        } catch (SchedulerException e) {
            error("Pause Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler paused.");
        } else {
            printf("Scheduler cannot be paused in its current state.");
        }
        return success;
    }

    public static boolean freeze() {
        boolean success = false;
        try {
            success = scheduler.freeze().booleanValue();
        } catch (SchedulerException e) {
            error("Freeze Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler frozen.");
        } else {
            printf("Scheduler cannot be frozen in its current state.");
        }
        return success;
    }

    public static boolean resume() {
        boolean success = false;
        try {
            success = scheduler.resume().booleanValue();
        } catch (SchedulerException e) {
            error("Resume Scheduler is not possible : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Scheduler resumed.");
        } else {
            printf("Scheduler cannot be resumed in its current state.");
        }
        return success;
    }

    public static boolean shutdown() {
        boolean success = false;
        try {
            if (intercativeMode) {
                String s = console.readStatement("Are you sure you want to shutdown the Scheduler ? " +
                    YES_NO + " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || !intercativeMode) {
                try {
                    success = scheduler.shutdown().booleanValue();
                } catch (SchedulerException e) {
                    error("Shutdown Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    printf("Shutdown sequence initialized, it might take a while to finish all executions, shell will exit.");
                    terminated = true;
                } else {
                    printf("Scheduler cannot be shutdown in its current state.");
                }
            } else {
                printf("Shutdown aborted !");
            }
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
        return success;
    }

    public static boolean kill() {
        boolean success = false;
        try {
            if (intercativeMode) {
                String s = console.readStatement("Are you sure you want to kill the Scheduler ? " + YES_NO +
                    " > ");
                success = s.equalsIgnoreCase(YES);
            }
            if (success || !intercativeMode) {
                try {
                    success = scheduler.kill().booleanValue();
                } catch (SchedulerException e) {
                    error("Kill Scheduler is not possible : " + e.getMessage());
                    return false;
                }
                if (success) {
                    printf("Sheduler has just been killed, shell will exit.");
                    terminated = true;
                } else {
                    printf("Scheduler cannot be killed in its current state.");
                }
            } else {
                printf("Kill aborted !");
            }
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
        return success;
    }

    public static String submit(String xmlDescriptor) {
        try {
            Job job = JobFactory.getFactory().createJob(xmlDescriptor);
            JobId id = scheduler.submit(job);
            printf("Job successfully submitted ! (id=" + id.value() + ")");
            return id.value();
        } catch (Exception e) {
            error("Error on job Submission (url=" + xmlDescriptor + ")" + " : " + e.getMessage());
        }
        return "";
    }

    public static boolean pause(String jobId) {
        boolean success = false;
        try {
            success = scheduler.pause(JobIdImpl.makeJobId(jobId)).booleanValue();
        } catch (SchedulerException e) {
            error("Error while pausing job " + jobId + " : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Job " + jobId + " paused.");
        } else {
            printf("Pause job " + jobId + " is not possible !!");
        }
        return success;
    }

    public static boolean resume(String jobId) {
        boolean success = false;
        try {
            success = scheduler.resume(JobIdImpl.makeJobId(jobId)).booleanValue();
        } catch (SchedulerException e) {
            error("Error while resuming job  " + jobId + " : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Job " + jobId + " resumed.");
        } else {
            printf("Resume job " + jobId + " is not possible !!");
        }
        return success;
    }

    public static boolean kill(String jobId) {
        boolean success = false;
        try {
            success = scheduler.kill(JobIdImpl.makeJobId(jobId)).booleanValue();
        } catch (SchedulerException e) {
            error("Error while killing job  " + jobId + " : " + e.getMessage());
            return false;
        }
        if (success) {
            printf("Job " + jobId + " killed.");
        } else {
            printf("kill job " + jobId + " is not possible !!");
        }
        return success;
    }

    public static void remove(String jobId) {
        try {
            scheduler.remove(JobIdImpl.makeJobId(jobId));
            printf("Job " + jobId + " removed.");
        } catch (SchedulerException e) {
            error("Error while removing job  " + jobId + " : " + e.getMessage());
        }
    }

    public static void result(String jobId) {
        try {
            JobResult result = scheduler.getJobResult(jobId);

            if (result != null) {
                printf("Job " + jobId + " result => \n");

                for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                    TaskResult tRes = e.getValue();

                    try {
                        printf("\t " + e.getKey() + " : " + tRes.value() + "\n");
                    } catch (Throwable e1) {
                        error("", tRes.getException());
                    }
                }
            } else {
                printf("Job " + jobId + " is not finished or unknown !");
            }
        } catch (SchedulerException e) {
            error("Error on job " + jobId + " : " + e.getMessage());
        }
    }

    public static void output(String jobId) {
        try {
            JobResult result = scheduler.getJobResult(jobId);

            if (result != null) {
                printf("Job " + jobId + " output => \n");

                for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                    TaskResult tRes = e.getValue();

                    try {
                        printf(e.getKey() + " : \n" + tRes.getOutput().getAllLogs(false) + "\n");
                    } catch (Throwable e1) {
                        error("", tRes.getException());
                    }
                }
            } else {
                printf("Job " + jobId + " is not finished or unknown !");
            }
        } catch (SchedulerException e) {
            error("Error on job " + jobId + " : " + e.getMessage());
        }
    }

    public static boolean linkRM(String rmURL) {
        boolean success = false;
        try {
            success = scheduler.linkResourceManager(rmURL.trim()).booleanValue();
            if (success) {
                printf("The new Resource Manager at " + rmURL + " has been rebind to the scheduler.");
            } else {
                error("Reconnect a Resource Manager is only possible when RM is dead !");
            }
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
        return success;
    }

    public static void exec(String commandFilePath) {
        try {
            File f = new File(commandFilePath.trim());
            BufferedReader br = new BufferedReader(new FileReader(f));
            eval(readFileContent(br));
            br.close();
        } catch (Exception e) {
            error("*ERROR* : " + e.getMessage());
        }
    }

    public static void exit() {
        console.printf("Exiting administrator.");
        terminated = true;
    }

    //***************** OTHER *******************

    private static void initalize() throws IOException {
        if (!initialized) {
            ScriptEngineManager manager = new ScriptEngineManager();
            // Engine selection
            engine = manager.getEngineByName("rhino");
            initialized = true;
            //read and launch Action.js
            BufferedReader br = new BufferedReader(new InputStreamReader(AdminScheduler.class
                    .getResourceAsStream(JS_INIT_FILE)));
            eval(readFileContent(br));
        }
    }

    private static void eval(String cmd) {
        try {
            if (!initialized) {
                initalize();
            }
            //Evaluate the command
            engine.eval(cmd);
        } catch (Exception e) {
            console.error("*SYNTAX ERROR* - " + format(e.getMessage()));
        }
    }

    private static String format(String msg) {
        msg = msg.replaceFirst("[^:]+:", "");
        return msg.replaceFirst("[(].*", "").trim();
    }

    private static String readFileContent(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            sb.append(tmp);
        }
        return sb.toString();
    }

    //***************** HELP SCREEN *******************

    private static String helpScreen() {
        StringBuilder out = new StringBuilder("Scheduler administrator commands are :\n\n");

        out.append(String.format(" %1$-18s\t Starts Scheduler\n", START_CMD));
        out.append(String.format(" %1$-18s\t Stops Scheduler\n", STOP_CMD));
        out.append(String.format(
                " %1$-18s\t pauses Scheduler, causes every jobs but running one to be paused\n", PAUSE_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t freezes Scheduler, causes all jobs to be paused (every non-running tasks are paused)\n",
                                FREEZE_CMD));
        out
                .append(String.format(" %1$-18s\t resumes Scheduler, causes all jobs to be resumed\n",
                        RESUME_CMD));
        out.append(String.format(" %1$-18s\t Waits for running jobs to finish and shutdown Scheduler\n",
                SHUTDOWN_CMD));
        out.append(String.format(" %1$-18s\t Kill every tasks and jobs and shutdown Scheduler\n", KILL_CMD));
        out.append(String.format(
                " %1$-18s\t Pause the given job (parameter is an int or a string representing the jobId)\n",
                PAUSEJOB_CMD));
        out.append(String.format(
                " %1$-18s\t Resume the given job (parameter is an int or a string representing the jobId)\n",
                RESUMEJOB_CMD));
        out.append(String.format(
                " %1$-18s\t Kill the given job (parameter is an int or a string representing the jobId)\n",
                KILLJOB_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Remove the given job from the Scheduler (parameter is an int or a string representing the jobId)\n",
                                REMOVEJOB_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Get the result of the given job (parameter is an int or a string representing the jobId)\n",
                                GET_RESULT_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Get the output of the given job (parameter is an int or a string representing the jobId)\n",
                                GET_OUTPUT_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Submit a new job (parameter is a string representing the job XML descriptor URL)\n",
                                SUBMIT_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Reconnect a Resource Manager (parameter is a string representing the new rmURL)\n",
                                LINK_RM_CMD));
        out
                .append(String
                        .format(
                                " %1$-18s\t Execute the content of the given script file (parameter is a string representing a command-file path)\n",
                                EXEC_CMD));
        out.append(String.format(" %1$-18s\t Exits Scheduler administrator\n", EXIT_CMD));

        return out.toString();
    }

}
