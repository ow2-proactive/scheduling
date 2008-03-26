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
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;

import com.sun.security.auth.callback.TextCallbackHandler;


/**
 * GetJobResult ...
 * 
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public class GetJobResult {

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

            UserSchedulerInterface scheduler = null;
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

                //log as user
                scheduler = auth.logAsUser(user, pwd);

                String jID;
                System.out.print("\nPlease enter the job id to get its result or 'exit' to exit :  ");

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
                            JobResult result = scheduler.getJobResult(JobId.makeJobId(i + ""));

                            if (result != null) {
                                System.out.println("Job " + i + " Result => ");

                                for (Entry<String, TaskResult> e : result.getAllResults().entrySet()) {
                                    TaskResult tRes = e.getValue();

                                    try {
                                        System.out.println("\t " + e.getKey() + " : " + tRes.value());
                                    } catch (Throwable e1) {
                                        System.out.println("\t ERROR during execution of " + e.getKey() +
                                            "... ");
                                        tRes.getException().printStackTrace();
                                    }
                                }
                            } else {
                                System.out.println("Job " + i + " is not finished !");
                            }
                        } catch (SchedulerException e) {
                            System.out.println("Error job " + i + " : " + e.getMessage());
                        }
                    }

                    System.out.print("\nPlease enter the job id to get its result or 'exit' to exit :  ");
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
            System.out.println("Error: " + e.getMessage());
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
            new HelpFormatter().printHelp("getResult", options, true);
            System.exit(2);
        }

        System.exit(0);
    }
}
