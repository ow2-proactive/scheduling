/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;


/**
 * Stress test for the scheduler. Creates several virtual users which randomly
 * submit jobs and retrieve results.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerTester {
    /** directory containing jobs to be submitted */
    public static String JOBS_HOME;
    /** Scheduler loggers. */
    public static Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);

    // scheduler connection
    private static final String DEFAULT_URL = "//localhost/";
    private SchedulerAuthenticationInterface authentication;

    // users
    private static Set<User> users;

    // max submission period
    private final static int DEFAULT_MSP = 120000;
    private int maxSubmissionPeriod;

    // max jobs number
    private final static int DEFAULT_MNJ = 3;
    private int maxNbJobs;

    // nb jobs
    private final static int DEFAULT_TOTAL_NL = 30;
    /** Maximum number of jobs to execute. */
    public static int totalMaxJobs;
    /** Number of job already started. */
    public static int currentNBjobs = 0;
    /** Used as a semaphore to synchronized action. */
    public final static Object synchro = new Object();
    private Map<String, Job> alreadySubmitted = new HashMap<String, Job>();

    /**
     * Start the scheduler tester.
     *
     * args[0] = [schedulerURL]
     * args[1] = [jobs directory]
     * args[2] = [submission period]
     * args[3] = [nb jobs]
     * args[4] = [nb total jobs]
     * @param args the arguments that have to be passed to this process.
     */
    public static void main(String[] args) {
        logger.info("");
        logger.info("***********************************************");
        logger.info("****** Press ENTER to stop submit orders ******");
        logger.info("***********************************************");
        logger.info("");
        BufferedReader bu = null;
        try {
            // almost optional arguments...
            JOBS_HOME = (args[1].endsWith(System.getProperty("file.separator"))) ? args[1] : args[1] +
                System.getProperty("file.separator");
            SchedulerTester schedulerTester = new SchedulerTester((args.length > 1) ? Integer
                    .parseInt(args[2]) : DEFAULT_MSP, (args.length > 3) ? Integer.parseInt(args[3])
                    : DEFAULT_MNJ);
            totalMaxJobs = (args.length > 4) ? Integer.parseInt(args[4]) : DEFAULT_TOTAL_NL;
            schedulerTester.authentication = SchedulerConnection.join((args.length > 0) ? args[0]
                    : DEFAULT_URL);
            schedulerTester.randomizedTest();
            bu = new BufferedReader(new InputStreamReader(System.in));
            boolean isActive = true;
            String tmp = null;
            while (isActive) {
                try {
                    tmp = bu.readLine();
                } catch (IOException e) {
                    logger.error("", e);
                }
                if ("".equals(tmp)) {
                    for (User u : users) {
                        u.stopSubmit();
                    }
                }
            }
        } catch (SchedulerException e) {
            logger.error("", e);
        } finally {
            if (bu != null) {
                try {
                    bu.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * Create a new instance of SchedulerTester.
     *
     * @param msp
     * @param mnj
     */
    public SchedulerTester(int msp, int mnj) {
        users = new HashSet<User>();
        this.maxNbJobs = mnj;
        this.maxSubmissionPeriod = msp;
    }

    /**
     * Randomized Test
     *
     */
    public void randomizedTest() {
        Map<String, String> logins = new HashMap<String, String>();
        Vector<String> jobs = null;
        FileReader l = null;
        BufferedReader br = null;
        try {
            // read logins
            l = new FileReader("login.cfg");
            br = new BufferedReader(l);
            String current = br.readLine();
            while (current != null) {
                StringTokenizer sep = new StringTokenizer(current, ":");
                logins.put(sep.nextToken(), sep.nextToken());
                current = br.readLine();
            }
            br.close();
            l.close();

            String msg = "[SCHEDULER TEST] Used logins are : ";
            for (String s : logins.keySet()) {
                msg += (s + ", ");
            }
            logger.info(msg);

            // read jobs
            File d = new File(JOBS_HOME);
            logger.info("===> " + JOBS_HOME);
            String[] jobsTmp = d.list();
            // remove non *xml
            jobs = new Vector<String>();
            for (int i = 0; i < jobsTmp.length; i++) {
                if (jobsTmp[i].endsWith("xml") && !jobsTmp[i].matches(".*lab.*") &&
                    !jobsTmp[i].matches("_ProActive")) {
                    jobs.add(jobsTmp[i]);
                }
            }

            msg = "Used jobs are : ";
            for (String s : jobs) {
                msg += (s + ", ");
                //preparing the jobs
                logger.info("\tPreparing " + s);
                //Create job
                Job j = JobFactory.getFactory().createJob(JOBS_HOME + s);
                alreadySubmitted.put(s, j);
            }
            logger.info(msg);

            for (String s : logins.keySet()) {
                User user = new User(s, logins.get(s), jobs, authentication);
                users.add(user);
                new Thread(user).start();
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            if (l != null) {
                try {
                    l.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * User...
     *
     * @author The ProActive Team
     *
     */
    public class User implements Runnable {
        private String login;
        private String pswd;
        private Vector<String> jobs;
        private UserSchedulerInterface scheduler;
        private boolean isActive;
        private Vector<JobId> results = new Vector<JobId>();
        private boolean submit = true;

        /**
         * Create a new instance of User.
         *
         * @param login
         * @param pswd
         * @param jobs
         * @param authentication
         */
        public User(String login, String pswd, Vector<String> jobs,
                SchedulerAuthenticationInterface authentication) {
            super();
            this.login = login;
            this.pswd = pswd;
            this.jobs = jobs;
            this.isActive = true;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @SuppressWarnings("unchecked")
        public void run() {
            Random generator = new Random();

            // connect from a different thread (i.e. not in the constructor)
            try {
                Credentials cred = Credentials.createCredentials(login, pswd, authentication.getPublicKey());
                this.scheduler = authentication.logAsUser(cred);
            } catch (Exception e) {
                logger.error(e);
            }

            while (isActive) {
                if (submit) {
                    try {
                        int nbJob = generator.nextInt(SchedulerTester.this.maxNbJobs) + 1;
                        int job = generator.nextInt(jobs.size());

                        // get the random job name
                        String jobName = jobs.get(job);

                        logger.info(login + " : Trying to submit " + jobName + " (" + nbJob + " instances)");

                        for (int i = 0; i < nbJob; i++) {
                            // Submit job
                            synchronized (synchro) {
                                if (currentNBjobs < totalMaxJobs) {
                                    currentNBjobs++;
                                } else {
                                    this.submit = false;
                                    logger
                                            .info(login +
                                                " can't submit anymore, the total job count has been reached, but he is already trying to get his job's result");
                                }
                            }
                            if (submit)
                                results.add(scheduler.submit(alreadySubmitted.get(jobName)));
                            else
                                break;
                        }
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }

                // Sleep
                try {
                    Thread.sleep(generator.nextInt(maxSubmissionPeriod));
                } catch (InterruptedException e) {
                    logger.error("", e);
                }

                if (results.size() == 0)
                    isActive = false;
                else {
                    // Get results if any ...
                    Vector<JobId> resultsTmp = (Vector<JobId>) this.results.clone();
                    for (JobId id : resultsTmp) {
                        try {
                            if (scheduler.getJobResult(id) != null) {
                                this.results.remove(id);
                            }
                        } catch (SchedulerException e) {
                            logger.error("", e);
                        }
                    }
                }
            }

            synchronized (synchro) {
                users.remove(this);
                logger.info(login + " has shutdown");
                if (users.size() == 0) {
                    System.exit(0);
                }
            }
        }

        /**
         * stop submitting job.
         *
         */
        public void stopSubmit() {
            this.submit = false;
            logger.info(login + " has stopped to submit, but he is already trying to get his job's result");
        }
    }
}
