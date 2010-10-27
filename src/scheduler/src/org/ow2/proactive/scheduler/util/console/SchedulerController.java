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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import jline.ConsoleReader;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.FlatJobFactory;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.utils.console.Console;
import org.ow2.proactive.utils.console.JVMPropertiesPreloader;
import org.ow2.proactive.utils.console.JlineConsole;
import org.ow2.proactive.utils.console.MBeanInfoViewer;
import org.ow2.proactive.utils.console.StdOutConsole;


/**
 * SchedulerController will help you to manage and interact with the scheduler.<br>
 * Use this class to submit jobs, get results, pause job, etc...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class SchedulerController {

    protected static final String SCHEDULER_DEFAULT_URL = Tools.getHostURL("//localhost/");

    protected static final String control = "<ctl> ";
    protected static final String newline = System.getProperty("line.separator");
    protected static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);
    protected static SchedulerController shell;

    protected CommandLine cmd = null;
    protected String user = null;
    protected String pwd = null;
    protected Credentials credentials = null;

    protected SchedulerAuthenticationInterface auth = null;
    protected SchedulerModel model;

    protected String jsEnv = null;

    /**
     * Start the Scheduler controller
     *
     * @param args the arguments to be passed
     */
    public static void main(String[] args) throws Throwable {
        args = JVMPropertiesPreloader.overrideJVMProperties(args);
        shell = new SchedulerController(null);
        shell.load(args);
    }

    /**
     * Create a new instance of SchedulerController
     */
    protected SchedulerController() {
    }

    /**
     * Create a new instance of SchedulerController
     *
     * Convenience constructor to let the default one do nothing
     */
    protected SchedulerController(Object o) {
        model = SchedulerModel.getModel(true);
    }

    public void load(String[] args) {
        Options options = new Options();

        Option help = new Option("h", "help", false, "Display this help");
        help.setRequired(false);
        options.addOption(help);

        Option username = new Option("l", "login", true, "The username to join the Scheduler");
        username.setArgName("login");
        username.setArgs(1);
        username.setRequired(false);
        options.addOption(username);

        Option schedulerURL = new Option("u", "url", true, "The scheduler URL (default " +
            SCHEDULER_DEFAULT_URL + ")");
        schedulerURL.setArgName("schedulerURL");
        schedulerURL.setRequired(false);
        options.addOption(schedulerURL);

        Option keyfile = new Option("k", "key", true, "(Optional) The path to a private SSH key");
        keyfile.setArgName("sshkeyFilePath");
        keyfile.setArgs(1);
        keyfile.setRequired(false);
        options.addOption(keyfile);

        addCommandLineOptions(options);

        boolean displayHelp = false;

        try {
            String pwdMsg = null;

            Parser parser = new GnuParser();
            cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                displayHelp = true;
            } else {

                if (cmd.hasOption("env")) {
                    model.setInitEnv(cmd.getOptionValue("env"));
                }
                String url;
                if (cmd.hasOption("url")) {
                    url = cmd.getOptionValue("url");
                } else {
                    url = SCHEDULER_DEFAULT_URL;
                }
                logger.info("Trying to connect Scheduler on " + url);
                auth = SchedulerConnection.join(url);
                logger.info("\t-> Connection established on " + url);

                logger.info(newline + "Connecting client to the Scheduler");

                if (cmd.hasOption("login")) {
                    user = cmd.getOptionValue("login");
                }

                if (cmd.hasOption("credentials")) {
                    if (cmd.getOptionValues("credentials").length == 1) {
                        System.setProperty(Credentials.credentialsPathProperty, cmd
                                .getOptionValue("credentials"));
                    }
                    try {
                        this.credentials = Credentials.getCredentials();
                    } catch (KeyException e) {
                        logger.error("Could not retreive credentials... Try to adjust the System property: " +
                            Credentials.credentialsPathProperty + " or use the -c option.");
                        throw e;
                    }
                } else {
                    ConsoleReader console = new ConsoleReader(System.in, new PrintWriter(System.out));
                    if (cmd.hasOption("login")) {
                        pwdMsg = user + "'s password: ";
                    } else {
                        user = console.readLine("login: ");
                        pwdMsg = "password: ";
                    }

                    //ask password to User
                    try {
                        console.setDefaultPrompt(pwdMsg);
                        pwd = console.readLine('*');
                    } catch (IOException ioe) {
                        logger.error("" + ioe);
                    }

                    PublicKey pubKey = null;
                    try {
                        // first attempt at getting the pubkey : ask the scheduler
                        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
                        pubKey = auth.getPublicKey();
                        System.out.println("Retrieved public key from Scheduler at " + url);
                    } catch (Exception e) {
                        try {
                            // second attempt : try default location
                            pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
                            System.out.println("Using public key at " + Credentials.getPubKeyPath());
                        } catch (Exception exc) {
                            System.out
                                    .println("Could not find a public key. Contact the administrator of the Scheduler.");
                            exc.printStackTrace();
                            System.exit(0);
                        }
                    }
                    try {
                        if (cmd.hasOption("key")) {
                            byte[] keyfileContent = FileToBytesConverter.convertFileToByteArray(new File(cmd
                                    .getOptionValue("key")));
                            this.credentials = Credentials.createCredentials(new CredData(user, pwd,
                                keyfileContent), pubKey);
                        } else {
                            this.credentials = Credentials.createCredentials(new CredData(user, pwd), pubKey);
                        }
                    } catch (FileNotFoundException fnfe) {
                        logger.error("SSH keyfile not found : '" + cmd.getOptionValue("key") + "'");
                        System.exit(1);
                    } catch (Exception e) {
                        logger.error("Could not create credentials... " + e);
                        throw e;
                    }
                }

                //connect to the scheduler
                connect();
                //connect JMX service
                connectJMXClient();
                //start the command line or the interactive mode
                start();
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
        } catch (LoginException e) {
            logger.error(e.getMessage() + newline + "Shutdown the controller." + newline);
            System.exit(1);
        } catch (SchedulerException e) {
            logger.error(e.getMessage() + newline + "Shutdown the controller." + newline);
            System.exit(1);
        } catch (Exception e) {
            logger.error("An error has occurred : " + e.getMessage() + newline + "Shutdown the controller." +
                newline, e);
            System.exit(1);
        }

        if (displayHelp) {
            logger.info("");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(135);
            String note = newline + "NOTE : if no " + control +
                "command is specified, the controller will start in interactive mode.";
            hf.printHelp(getCommandName() + Tools.shellExtension(), "", options, note, true);
            System.exit(2);
        }

        // if execution reaches this point this means it must exit
        System.exit(0);
    }

    protected void connect() throws LoginException, AlreadyConnectedException {
        model.connectScheduler(auth, credentials);
        String userStr = (user != null) ? "'" + user + "' " : "";
        logger.info("\t-> Client " + userStr + "successfully connected" + newline);
    }

    private void start() throws Exception {
        //start one of the two command behavior
        if (startCommandLine(cmd)) {
            startCommandListener();
        }
    }

    protected OptionGroup addCommandLineOptions(Options options) {
        OptionGroup actionGroup = new OptionGroup();

        Option opt = new Option("s", "submit", true, control + "Submit the given job XML file");
        opt.setArgName("XMLDescriptor");
        opt.setRequired(false);
        opt.setArgs(Option.UNLIMITED_VALUES);
        actionGroup.addOption(opt);

        opt = new Option("cmd", "command", false, control +
            "If mentionned, -submit argument becomes a command line, ie: -submit command args...");
        opt.setRequired(false);
        options.addOption(opt);
        opt = new Option("cmdf", "commandf", false, control +
            "If mentionned, -submit argument becomes a text file path containing command lines to schedule");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "output", true, control +
            "Used with submit action, specify a log file path to store job output");
        opt.setArgName("logFile");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("ss", "selectscript", true, control +
            "Used with -cmd or -cmdf, specify a selection script");
        opt.setArgName("selectScript");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("jn", "jobname", true, control + "Used with -cmd or -cmdf, specify the job name");
        opt.setArgName("jobName");
        opt.setRequired(false);
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option("pj", "pausejob", true, control +
            "Pause the given job (pause every non-running tasks)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("rj", "resumejob", true, control +
            "Resume the given job (restart every paused tasks)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("kj", "killjob", true, control + "Kill the given job (cause the job to finish)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("rmj", "removejob", true, control + "Remove the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("jr", "jobresult", true, control + "Get the result of the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("tr", "taskresult", true, control + "Get the result of the given task");
        opt.setArgName("jobId taskName");
        opt.setRequired(false);
        opt.setArgs(2);
        actionGroup.addOption(opt);

        opt = new Option("jo", "joboutput", true, control + "Get the output of the given job");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("to", "taskoutput", true, control + "Get the output of the given task");
        opt.setArgName("jobId taskName");
        opt.setRequired(false);
        opt.setArgs(2);
        actionGroup.addOption(opt);

        opt = new Option("jp", "jobpriority", true, control +
            "Change the priority of the given job (Idle, Lowest, Low, Normal, High, Highest)");
        opt.setArgName("jobId newPriority");
        opt.setRequired(false);
        opt.setArgs(2);
        actionGroup.addOption(opt);

        opt = new Option("js", "jobstate", true, control +
            "Get the current state of the given job (Also tasks description)");
        opt.setArgName("jobId");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("lj", "listjobs", false, control +
            "Display the list of jobs managed by the scheduler");
        opt.setRequired(false);
        opt.setArgs(0);
        actionGroup.addOption(opt);

        opt = new Option("stats", "statistics", false, control +
            "Display some statistics about the Scheduler");
        opt.setRequired(false);
        opt.setArgs(0);
        actionGroup.addOption(opt);

        opt = new Option("ma", "myaccount", false, control + "Display current user account information");
        opt.setRequired(false);
        opt.setArgs(0);
        actionGroup.addOption(opt);

        opt = new Option("ua", "useraccount", false, control + "Display account information by username");
        opt.setRequired(false);
        opt.setArgs(1);
        opt.setArgName("username");
        actionGroup.addOption(opt);

        opt = new Option("rp", "reloadpermissions", false, control + "Reloads the permission file");
        opt.setRequired(false);
        opt.setArgs(0);
        actionGroup.addOption(opt);

        opt = new Option("sf", "script", true, control +
            "Execute the given javascript file with optional arguments.");
        opt.setArgName("filePath arg1=val1 arg2=val2 ...");
        opt.setRequired(false);
        opt.setArgs(Option.UNLIMITED_VALUES);
        opt.setOptionalArg(true);
        actionGroup.addOption(opt);

        opt = new Option("env", "environment", true,
            "Execute the given script as an environment for the interactive mode");
        opt.setArgName("filePath");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("test", false, control +
            "Test if the Scheduler is successfully started by committing some examples");
        opt.setRequired(false);
        opt.setArgs(0);
        actionGroup.addOption(opt);

        opt = new Option("c", "credentials", true, "Path to the credentials (" +
            Credentials.getCredentialsPath() + ").");
        opt.setRequired(false);
        opt.setOptionalArg(true);
        options.addOption(opt);

        options.addOptionGroup(actionGroup);

        opt = new Option("start", "schedulerstart", false, control + "Start the Scheduler");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("stop", "schedulerstop", false, control + "Stop the Scheduler");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("pause", "schedulerpause", false, control +
            "Pause the Scheduler (cause all non-running jobs to be paused)");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("freeze", "schedulerfreeze", false, control +
            "Freeze the Scheduler (cause all non-running tasks to be paused)");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("resume", "schedulerresume", false, control + "Resume the Scheduler");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("shutdown", "schedulershutdown", false, control + "Shutdown the Scheduler");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("kill", "schedulerkill", false, control + "Kill the Scheduler");
        opt.setRequired(false);
        actionGroup.addOption(opt);

        opt = new Option("lrm", "linkrm", true, control + "Reconnect a RM to the scheduler");
        opt.setArgName("rmURL");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        opt = new Option("p", "policy", true, control + "Change the current scheduling policy");
        opt.setArgName("fullName");
        opt.setRequired(false);
        opt.setArgs(1);
        actionGroup.addOption(opt);

        options.addOptionGroup(actionGroup);

        return actionGroup;
    }

    private void startCommandListener() throws Exception {
        Console console;
        console = new JlineConsole();
        model.connectConsole(console);
        model.startModel();
    }

    protected boolean startCommandLine(CommandLine cmd) {
        /* start stdConsole */
        model.connectConsole(new StdOutConsole());
        if (cmd.hasOption("pausejob")) {
            model.pause_(cmd.getOptionValue("pausejob"));
        } else if (cmd.hasOption("resumejob")) {
            model.resume_(cmd.getOptionValue("resumejob"));
        } else if (cmd.hasOption("killjob")) {
            model.kill_(cmd.getOptionValue("killjob"));
        } else if (cmd.hasOption("removejob")) {
            model.remove_(cmd.getOptionValue("removejob"));
        } else if (cmd.hasOption("submit")) {
            if (cmd.hasOption("cmd") || cmd.hasOption("cmdf")) {
                submitCMD();
            } else {
                model.submit_(cmd.getOptionValue("submit"));
            }
        } else if (cmd.hasOption("jobresult")) {
            model.result_(cmd.getOptionValue("jobresult"));
        } else if (cmd.hasOption("taskresult")) {
            String[] optionValues = cmd.getOptionValues("taskresult");
            if (optionValues == null || optionValues.length != 2) {
                model.error("taskresult must have two arguments. Start with --help for more informations");
            }
            model.tresult_(optionValues[0], optionValues[1]);
        } else if (cmd.hasOption("joboutput")) {
            model.output_(cmd.getOptionValue("joboutput"));
        } else if (cmd.hasOption("taskoutput")) {
            String[] optionValues = cmd.getOptionValues("taskoutput");
            if (optionValues == null || optionValues.length != 2) {
                model.error("taskoutput must have two arguments. Start with --help for more informations");
            }
            model.toutput_(optionValues[0], optionValues[1]);
        } else if (cmd.hasOption("jobpriority")) {
            try {
                model.priority_(cmd.getOptionValues("jobpriority")[0], cmd.getOptionValues("jobpriority")[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                model.print("Missing arguments for job priority. Arguments must be <jobId> <newPriority>" +
                    newline + "\t" + "where priorities are Idle, Lowest, Low, Normal, High, Highest");
            }
        } else if (cmd.hasOption("jobstate")) {
            model.jobState_(cmd.getOptionValue("jobstate"));
        } else if (cmd.hasOption("listjobs")) {
            model.schedulerState_();
        } else if (cmd.hasOption("stats")) {
            model.showRuntimeData_();
        } else if (cmd.hasOption("ma")) {
            model.showMyAccount_();
        } else if (cmd.hasOption("ua")) {
            model.showAccount_(cmd.getOptionValue("ua"));
        } else if (cmd.hasOption("rp")) {
            model.refreshPermissionPolicy_();
        } else if (cmd.hasOption("script")) {
            model.execWithParam_(cmd.getOptionValues("script"));
        } else if (cmd.hasOption("test")) {
            model.test_();
        } else if (cmd.hasOption("start")) {
            model.start_();
        } else if (cmd.hasOption("stop")) {
            model.stop_();
        } else if (cmd.hasOption("pause")) {
            model.pause_();
        } else if (cmd.hasOption("freeze")) {
            model.freeze_();
        } else if (cmd.hasOption("resume")) {
            model.resume_();
        } else if (cmd.hasOption("shutdown")) {
            model.shutdown_();
        } else if (cmd.hasOption("kill")) {
            model.kill_();
        } else if (cmd.hasOption("linkrm")) {
            model.linkRM_(cmd.getOptionValue("linkrm"));
        } else if (cmd.hasOption("policy")) {
            model.changePolicy_(cmd.getOptionValue("policy"));
        } else {
            return true;
        }
        return false;
    }

    private String submitCMD() {
        try {
            Job job;
            String jobGivenName = null;
            String jobGivenOutput = null;
            String givenSelScript = null;
            if (cmd.hasOption("jobname")) {
                jobGivenName = cmd.getOptionValue("jobname");
            }
            if (cmd.hasOption("output")) {
                jobGivenOutput = cmd.getOptionValue("output");
            }
            if (cmd.hasOption("selectscript")) {
                givenSelScript = cmd.getOptionValue("selectscript");
            }

            if (cmd.hasOption("cmd")) {
                //create job from a command to launch specified in command line
                String cmdTab[] = cmd.getOptionValues("submit");
                String jobCommand = "";

                for (String s : cmdTab) {
                    jobCommand += (s + " ");
                }
                jobCommand = jobCommand.trim();
                job = FlatJobFactory.getFactory().createNativeJobFromCommand(jobCommand, jobGivenName,
                        givenSelScript, jobGivenOutput, user);
            } else {
                String commandFilePath = cmd.getOptionValue("submit");
                job = FlatJobFactory.getFactory().createNativeJobFromCommandsFile(commandFilePath,
                        jobGivenName, givenSelScript, jobGivenOutput, user);
            }
            JobId id = model.getScheduler().submit(job);
            model.print("Job successfully submitted ! (id=" + id.value() + ")");
            return id.value();
        } catch (Exception e) {
            model.handleExceptionDisplay("Error on job Submission", e);
        }
        return "";
    }

    protected void connectJMXClient() {
        final MBeanInfoViewer viewer = new MBeanInfoViewer(auth, user, credentials);
        this.model.setJMXInfo(viewer);
    }

    protected String getCommandName() {
        return "scheduler-client";
    }

}
