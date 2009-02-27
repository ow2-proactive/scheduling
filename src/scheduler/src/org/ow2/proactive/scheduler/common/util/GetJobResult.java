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
package org.ow2.proactive.scheduler.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * GetJobResult help you to get the result of your job.<br>
 * Your job is represented by its ID.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class GetJobResult {

    private static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);
    private static final String SCHEDULER_DEFAULT_URL = "//localhost/";

    /**
     * Start the jobResult receiver.
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

            UserSchedulerInterface scheduler = null;
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

                logger.info("\nConnecting user to the Scheduler");
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

                //log as user
                scheduler = auth.logAsUser(user, pwd);
                logger.info("\t-> User '" + user + "' successfully connected");

                String jID;
                final String prompt = "\nPlease enter the job id to get its result or 'exit' to exit : ";
                System.out.print(prompt);

                while (!(jID = buf.readLine()).equals("exit")) {
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
                                        logger.error("\t ERROR during execution of " + e.getKey() + "... ");
                                        logger.error(tRes.getException());
                                    }
                                }
                            } else {
                                logger.info("Job " + i + " is not finished !");
                            }
                        } catch (SchedulerException e) {
                            logger.info("Error job " + i + " : " + e.getMessage());
                        }
                    }

                    System.out.print("prompt");
                }
            }
        } catch (MissingArgumentException e) {
            logger.error(e);
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
            hf.printHelp("result" + Tools.shellExtension(), options, true);
            System.exit(1);
        }

        System.exit(0);
    }
}
