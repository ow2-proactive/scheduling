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

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.logforwarder.SimpleLoggerServer;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobFactory;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.core.SchedulerCore;


public class JobLauncher {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    public static void main(String[] args) {
        try {
            //GET SCHEDULER
            String jobUrl = null;
            int nbJob = 1;
            SchedulerAuthenticationInterface auth = null;
            boolean logIt = false;
            int pos = 0;
            if ("-log".equals(args[0])) {
                logIt = true;
                pos++;
            }

            if (args.length > 2) {
                jobUrl = args[pos];
                nbJob = Integer.parseInt(args[pos + 1]);
                auth = SchedulerConnection.join(args[pos + 2]);
            } else if (args.length > 1) {
                jobUrl = args[pos];
                nbJob = Integer.parseInt(args[pos + 1]);
                auth = SchedulerConnection.join(null);
            } else if (args.length > 0) {
                jobUrl = args[pos];
                auth = SchedulerConnection.join(null);
            } else {
                System.err.println("You must enter a job descriptor");
                System.exit(0);
            }
            UserSchedulerInterface scheduler = auth.logAsUser("chri", "chri");

            //CREATE JOB
            Job j = JobFactory.getFactory().createJob(jobUrl);

            //******************** GET JOB OUTPUT ***********************
            SimpleLoggerServer simpleLoggerServer = null;
            if (logIt) {
                try {
                    // it will launch a listener that will listen connection on any free port
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
                    // next, this method will forward task output on the previous loggerServer
                    scheduler.listenLog(id,
                        URIBuilder.getLocalAddress().getHostName(),
                        simpleLoggerServer.getPort());
                    Logger l = Logger.getLogger(SchedulerCore.LOGGER_PREFIX +
                            id);

                    // coucou Guillaume !
                    DateFormat dateFormat = new SimpleDateFormat(
                            "hh'h'mm'm'_dd-MM-yy");
                    FileAppender fa = new FileAppender(new PatternLayout(
                                "%m %n"),
                            "./logs/job[" + j.getName() + "," + id + "]_" +
                            dateFormat.format(new Date()) + ".log", true);
                    l.addAppender(fa);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
