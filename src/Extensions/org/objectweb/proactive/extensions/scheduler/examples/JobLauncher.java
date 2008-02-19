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

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.scheduler.common.exception.JobCreationException;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.JobFactory;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extensions.scheduler.util.logforwarder.SimpleLoggerServer;

import com.sun.security.auth.callback.TextCallbackHandler;


public class JobLauncher {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    /**
     * @param args
     * [0] username
     * [2] schedulerURL
     * [3] jobPath
     */
    public static void main(String[] args) {
        //GET SCHEDULER
        String jobUrl = null;
        String username = null;
        String schedulerUrl = null;
        String password = null;
        UserSchedulerInterface scheduler = null;
        SchedulerAuthenticationInterface auth = null;
        Job jobToLaunch = null;
        int nbJob = 1;
        boolean logIt = false;
        boolean bad_params = false;
        int pos = 0;

        if (args.length != 0 && "-log".equals(args[pos])) {
            if (args.length != 5 && args.length != 4)
                bad_params = true;
        } else {
            if (args.length != 4 && args.length != 3)
                bad_params = true;
        }

        if (bad_params) {
            System.out
                    .println("Usage jobLauncher.sh [-log] username schedulerUrl jobFilePath [number_of_job]");
            System.exit(1);
        }

        if ("-log".equals(args[pos])) {
            logIt = true;
            pos++;
        }

        username = args[pos];
        schedulerUrl = args[pos + 1];
        jobUrl = args[pos + 2];

        try {
            if ((args.length == 4 && !logIt) || (args.length == 5 && logIt))
                nbJob = Integer.parseInt(args[pos + 3]);
        } catch (NumberFormatException e) {
            System.out.println("Number awaited for sumbissions number !");
            System.out
                    .println("Usage jobLauncher.sh [-log] username schedulerUrl jobFilePath [number_of_job]");
            System.exit(1);
        }

        try {
            auth = SchedulerConnection.join(schedulerUrl);
        } catch (SchedulerException e) {
            e.toString();
            e.printStackTrace();
            System.exit(1);
        }

        //ask password to User 
        TextCallbackHandler handler = new TextCallbackHandler();
        PasswordCallback pwdCallBack = new PasswordCallback(username + "'s Password : ", false);
        Callback[] callbacks = new Callback[] { pwdCallBack };
        try {
            handler.handle(callbacks);
        } catch (IOException e) {
            e.toString();
        } catch (UnsupportedCallbackException e) {
            e.toString();
        }

        password = new String(pwdCallBack.getPassword());
        try {
            scheduler = auth.logAsUser(username, password);
        } catch (SchedulerException e) {
            e.toString();
            System.exit(1);
        } catch (LoginException e) {
            System.out.println("Unable to authenticate user " + username +
                ", check your username and password");
            e.toString();
            System.exit(1);
        }

        try {
            //CREATE JOB           	
            jobToLaunch = JobFactory.getFactory().createJob(jobUrl);
        } catch (JobCreationException e) {
            e.toString();
            System.exit(1);
        }

        //******************** GET JOB OUTPUT ***********************
        SimpleLoggerServer simpleLoggerServer = null;

        if (logIt) {
            try {
                // it will launch a listener that will listen connection on any free port
                simpleLoggerServer = SimpleLoggerServer.createLoggerServer();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        try {
            for (int i = 0; i < nbJob; i++) {
                // SUBMIT JOB
                JobId id = scheduler.submit(jobToLaunch);

                if (logIt) {
                    // next, this method will forward task output on the previous loggerServer
                    scheduler.listenLog(id, ProActiveInet.getInstance().getInetAddress().getHostName(),
                            simpleLoggerServer.getPort());

                    Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + id);

                    DateFormat dateFormat = new SimpleDateFormat("hh'h'mm'm'_dd-MM-yy");
                    FileAppender fa = new FileAppender(Log4JTaskLogs.DEFAULT_LOG_LAYOUT, "./logs/job[" +
                        jobToLaunch.getName() + "," + id + "]_" + dateFormat.format(new Date()) + ".log",
                        true);
                    l.addAppender(fa);
                }

                System.out.println("Here is your job id : " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(1);
    }
}
