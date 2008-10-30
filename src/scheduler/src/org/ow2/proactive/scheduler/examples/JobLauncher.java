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
package org.ow2.proactive.scheduler.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.passwordhandler.PasswordField;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.common.scheduler.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.logforwarder.SimpleLoggerServer;


/**
 * This class provides a way for submit jobs to a scheduler.
 * 
 * @author The ProActive Team
 */
public class JobLauncher {

    private static final String SCHEDULER_DEFAULT_URL = "//localhost/" +
        PASchedulerProperties.SCHEDULER_DEFAULT_NAME;

    /**
     * Start the job launcher process.
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

        Option job = new Option("j", "job", true, "the job file descriptor");
        job.setArgName("job");
        job.setRequired(true);
        options.addOption(job);

        Option schedulerURL = new Option("u", "schedulerURL", true, "the scheduler URL (default " +
            SCHEDULER_DEFAULT_URL + ")");
        schedulerURL.setArgName("schedulerURL");
        schedulerURL.setRequired(false);
        options.addOption(schedulerURL);

        Option number = new Option("n", "number", true, "number of job to launch (default 1)");
        number.setArgName("number");
        number.setRequired(false);
        options.addOption(number);

        // FIXME Cdelbe
        // Option log = new Option("r", "redirect", false, "output is redirected
        // into stdout");
        // log.setArgName("redirect");
        // log.setRequired(false);
        // options.addOption(log);

        boolean displayHelp = false;

        InputStreamReader reader = new InputStreamReader(System.in);
        BufferedReader buf = new BufferedReader(reader);
        try {
            String user = null;
            String pwd = null;
            String jobUrl = null;
            String pwdMsg = null;
            int nbJob = 1;
            SchedulerAuthenticationInterface auth = null;
            boolean logIt = false;

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                displayHelp = true;
            else {
                if (cmd.hasOption("u"))
                    auth = SchedulerConnection.join(cmd.getOptionValue("u"));
                else
                    auth = SchedulerConnection.join(SCHEDULER_DEFAULT_URL);

                // if (cmd.hasOption("r"))
                // logIt = true;

                if (cmd.hasOption("n"))
                    nbJob = new Integer(cmd.getOptionValue("n"));

                jobUrl = cmd.getOptionValue("j");

                // CREATE JOB
                Job j = JobFactory.getFactory().createJob(jobUrl);
                System.out.println();

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
                    pwd = (password == null) ? "" : String.valueOf(password);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                // Log as user
                UserSchedulerInterface scheduler = auth.logAsUser(user, pwd);

                // ******************** GET JOB OUTPUT ***********************
                SimpleLoggerServer simpleLoggerServer = null;

                if (logIt) {
                    try {
                        // it will launch a listener that will listen connection
                        // on
                        // any free port
                        simpleLoggerServer = SimpleLoggerServer.createLoggerServer();
                    } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < nbJob; i++) {
                    // SUBMIT JOB
                    JobId id = scheduler.submit(j);

                    if (logIt) {
                        // next, this method will forward task output on the
                        // previous loggerServer
                        scheduler.listenLog(id, ProActiveInet.getInstance().getInetAddress().getHostName(),
                                simpleLoggerServer.getPort());

                        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + id);

                        DateFormat dateFormat = new SimpleDateFormat("hh'h'mm'm'_dd-MM-yy");
                        FileAppender fa = new FileAppender(Log4JTaskLogs.getTaskLogLayout(), "./logs/job[" +
                            j.getName() + "," + id + "]_" + dateFormat.format(new Date()) + ".log", true);
                        l.addAppender(fa);
                    }

                    System.out.println("Here is your job id : " + id);
                }
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
            new HelpFormatter().printHelp("submit", options, true);
            System.exit(2);
        }

        //        System.exit(0);
    }
}