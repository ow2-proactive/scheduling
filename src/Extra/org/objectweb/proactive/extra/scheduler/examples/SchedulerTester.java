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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobFactory;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;


/**
 * Stress test for the scheduler.
 * Creates several virtual users which randomly submit jobs and retreive results.
 * @author cdelbe
 * @since ProActive 3.9
 */
public class SchedulerTester {
    // directory containing jobs to be submitted
    public static String JOBS_HOME;
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    // scheduler connection
    private static final String DEFAULT_URL = null;
    private SchedulerAuthenticationInterface authentication;

    // users
    private Set<Thread> users;

    // submission period
    private final static int DEFAULT_MSP = 120000;
    private int maxSubmissionPeriod;

    // nb jobs
    private final static int DEFAULT_MNJ = 3;
    private int maxNbJobs;

    // nb jobs
    private final static int DEFAULT_TOTAL_NL = 30;
    public static int totalMaxJobs;
    public static int currentNBjobs = 0;
    public final static Object synchro = new Object();

    /**
     * args[0] = [schedulerURL]
     * args[1] = [jobs directory]
     * args[2] = [submission period]
     * args[3] = [nb jobs]
     * args[4] = [nb total jobs]
     */
    public static void main(String[] args) {
        try {
            // almost optional arguments...
            JOBS_HOME = args[1];
            SchedulerTester jl = new SchedulerTester((args.length > 1)
                    ? Integer.parseInt(args[2]) : DEFAULT_MSP,
                    (args.length > 3) ? Integer.parseInt(args[3]) : DEFAULT_MNJ);
            totalMaxJobs = (args.length > 4) ? Integer.parseInt(args[4])
                                             : DEFAULT_TOTAL_NL;
            jl.authentication = SchedulerConnection.join((args.length > 0)
                    ? args[0] : DEFAULT_URL);
            jl.randomizedTest();
        } catch (SchedulerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public SchedulerTester(int msp, int mnj) {
        this.users = new HashSet<Thread>();
        this.maxNbJobs = mnj;
        this.maxSubmissionPeriod = msp;
    }

    public void randomizedTest() {
        HashMap<String, String> logins = new HashMap<String, String>();
        Vector<String> jobs = null;
        try {
            // read logins
            FileReader l = new FileReader(SchedulerTester.class.getResource(
                        "login.cfg").getFile());
            BufferedReader br = new BufferedReader(l);
            String current = br.readLine();
            while (current != null) {
                StringTokenizer sep = new StringTokenizer(current, ":");
                logins.put(sep.nextToken(), sep.nextToken());
                current = br.readLine();
            }
            l.close();

            System.out.print("[SCHEDULER TEST] Used logins are : ");
            for (String s : logins.keySet()) {
                System.out.print(s + ", ");
            }
            System.out.println();

            // read jobs
            File d = new File(JOBS_HOME);
            System.out.println("===> " + JOBS_HOME);
            String[] jobsTmp = d.list();
            // remove non *xml
            jobs = new Vector<String>();
            for (int i = 0; i < jobsTmp.length; i++) {
                if (jobsTmp[i].endsWith("xml")) {
                    jobs.add(jobsTmp[i]);
                }
            }

            System.out.print("[SCHEDULER TEST] Used jobs are : ");
            for (String s : jobs) {
                System.out.print(s + ", ");
            }
            System.out.println();

            for (String s : logins.keySet()) {
                Thread user = new Thread(new User(s, logins.get(s), jobs,
                            authentication));
                user.start();
                this.users.add(user);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class User implements Runnable {
        private String login;
        private String pswd;
        private Vector<String> jobs;
        private UserSchedulerInterface scheduler;
        private boolean isActive;
        private Vector<JobId> results = new Vector<JobId>();
        private boolean submit = true;

        /**
         *
         * @param login
         * @param pswd
         * @param jobs
         */
        public User(String login, String pswd, Vector<String> jobs,
            SchedulerAuthenticationInterface authentication) {
            super();
            this.login = login;
            this.pswd = pswd;
            this.jobs = jobs;
            this.isActive = true;
        }

        @SuppressWarnings("unchecked")
        public void run() {
            Random generator = new Random();

            // connect from a different thread (i.e. not in the constructor)
            try {
                this.scheduler = authentication.logAsUser(login, pswd);
            } catch (LoginException e) {
                e.printStackTrace();
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    int nbJob = generator.nextInt(SchedulerTester.this.maxNbJobs) +
                        1;
                    int job = generator.nextInt(jobs.size());

                    //Create job
                    Job j = JobFactory.getFactory()
                                      .createJob(JOBS_HOME + jobs.get(job));
                    System.out.println("[SCHEDULER TEST] Submitting " +
                        jobs.get(job) + " (" + nbJob + " instances) by " +
                        this.login);

                    for (int i = 0; i < nbJob; i++) {
                        // Submit job
                        synchronized (synchro) {
                            if (currentNBjobs < totalMaxJobs) {
                                currentNBjobs++;
                            } else {
                                this.submit = false;
                            }
                        }
                        if (submit) {
                            results.add(scheduler.submit(j));
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Sleep
                try {
                    Thread.sleep(generator.nextInt(
                            SchedulerTester.this.maxSubmissionPeriod) + 12000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Get results if any ...
                Vector<JobId> resultsTmp = (Vector<JobId>) this.results.clone();
                for (JobId id : resultsTmp) {
                    try {
                        if (scheduler.getJobResult(id) != null) {
                            this.results.remove(id);
                        }
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void stop() {
            this.isActive = false;
        }
    }
}
