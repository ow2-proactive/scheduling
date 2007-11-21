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
import java.util.Map.Entry;

import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * GetJobResult ...
 *
 * @author jlscheef - ProActiveTeam
 * @date 23 oct. 07
 * @version 3.2
 *
 */
public class GetJobResult {
    public static void main(String[] args) {
        try {
            //GET SCHEDULER
            UserSchedulerInterface scheduler;
            SchedulerAuthenticationInterface auth;

            if (args.length > 0) {
                auth = SchedulerConnection.join("//" + args[0] + "/" +
                        SchedulerConnection.SCHEDULER_DEFAULT_NAME);
            } else {
                auth = SchedulerConnection.join(null);
            }

            scheduler = auth.logAsUser("chri", "chri");

            InputStreamReader reader = new InputStreamReader(System.in);

            // Wrap the reader with a buffered reader.
            BufferedReader buf = new BufferedReader(reader);
            String jID;
            System.out.print(
                "\nPlease enter the job id to get its result or 'exit' to exit :  ");

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
                        System.out.println("Error job " + i + " : " +
                            e.getMessage());
                    }
                }

                System.out.print(
                    "\nPlease enter the job id to get its result or 'exit' to exit :  ");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            //e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}
