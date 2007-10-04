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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobFactory;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;


public class JobLauncher {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    public static void main(String[] args) {
        try {
            //GET SCHEDULER
            String jobUrl = null;
            int nbJob = 1;
            SchedulerAuthenticationInterface auth = null;
            if (args.length > 2) {
                jobUrl = args[0];
                nbJob = Integer.parseInt(args[1]);
                auth = SchedulerConnection.join(args[2]);
            } else if (args.length > 1) {
                jobUrl = args[0];
                nbJob = Integer.parseInt(args[1]);
                auth = SchedulerConnection.join(null);
            } else if (args.length > 0) {
                jobUrl = args[0];
                auth = SchedulerConnection.join(null);
            } else {
                System.err.println("You must enter a job descriptor");
                System.exit(0);
            }
            UserSchedulerInterface scheduler = auth.logAsUser("chri", "chri");

            //CREATE JOB
            Job j = JobFactory.getFactory().createJob(jobUrl);
            for (int i = 0; i < nbJob; i++) {
                // SUBMIT JOB
                scheduler.submit(j);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
