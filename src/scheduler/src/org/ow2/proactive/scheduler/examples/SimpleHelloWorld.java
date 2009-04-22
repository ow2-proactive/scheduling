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
package org.ow2.proactive.scheduler.examples;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.common.util.logforwarder.util.SimpleLoggerServer;


/**
 * Here is a class that explains how to simply use the scheduler.
 * You'll create a one task job that will print "HelloWorld !".
 * You'll be able to get the output of the task and/or get the result.
 * According that the API is not yet full implemented,
 * adding task and job creation are not on their final implementation.
 * TaskDescriptor may also be removed from user view.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class SimpleHelloWorld {
    /**
     * Start the exemple.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            //*********************** GET SCHEDULER *************************
            //get authentication interface from existing scheduler based on scheduler host URL
            //(localhost) followed by the scheduler name (here the default one)
            SchedulerAuthenticationInterface auth = SchedulerConnection.join("//localhost/");

            //Now you are connected you must log on with a couple of username/password matching an entry in login and group files.
            //(groups.cfg, login.cfg in the same directory)
            //you can also log on as admin if your username is in admin group. (it provides you more power ;) )
            UserSchedulerInterface scheduler = auth.logAsUser("user1", "pwd1");

            //if this point is reached, that's we are connected to the scheduler under "user1".
            //@snippet-start taskflow_params
            //******************** CREATE A NEW JOB ***********************
            //params are respectively : name, priority,cancelOnError, description.
            //@snippet-start task_flow_job_creation
            TaskFlowJob job = new TaskFlowJob();
            //@snippet-end task_flow_job_creation
            job.setName("job name");
            job.setPriority(JobPriority.NORMAL);
            job.setCancelJobOnError(false);
            job.setDescription("A simple hello world example !");
            //@snippet-end taskflow_params
            //******************** CREATE A NEW TASK ***********************

            //Create the java task
            JavaTask task = new JavaTask();
            task.setName("toto");
            //adding the task to the job
            task.setExecutableClassName(WaitAndPrint.class.getName());
            //this task is final, it means that the job result will contain this task result.
            task.setPreciousResult(true);

            //add the task to the job
            try {
                job.addTask(task);
            } catch (UserException e2) {
                e2.printStackTrace();
            }

            //******************** SUBMIT THE JOB ***********************
            //submitting a job to the scheduler returns the attributed jobId
            //this id will be used to talk the scheduler about this job.
            System.out.println("Submitting job...");
            JobId jobId = scheduler.submit(job);

            //******************** GET JOB OUTPUT ***********************
            SimpleLoggerServer simpleLoggerServer;
            System.out.println("Getting job output...");
            try {
                // it will launch a listener that will listen connection on any free port
                LogForwardingService lfs = new LogForwardingService(
                    "org.ow2.proactive.scheduler.common.util.logforwarder.providers.SocketBasedForwardingProvider");
                lfs.initialize();
                // next, this method will forward task output on the previous loggerServer
                scheduler.listenLog(jobId, lfs.getAppenderProvider());
            } catch (LogForwardingException e) {
                e.printStackTrace();
            }

            //******************** GET JOB RESULT ***********************
            // it is better to get the result when the job is terminated.
            // if you want the result as soon as possible we suggest this loop.
            // In the future you could get the result like a future in ProActive or with a listener.
            JobResult result = null;

            while (result == null) {
                try {
                    Thread.sleep(2000);
                    result = scheduler.getJobResult(jobId);

                    //the result is null if the job is not finished.
                } catch (SchedulerException se) {
                    se.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            result.getPreciousResults().get("toto");
            System.out.println("Result : " + result);
        } catch (SchedulerException e) {
            //the scheduler had a problem
            e.printStackTrace();
        } catch (LoginException e) {
            //there was a problem during scheduler authentication
            e.printStackTrace();
        }
        System.exit(0);
    }

    private class InternalExec extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) {
            System.out.println("Hello World !");

            return "HelloWorld Sample host : " + ProActiveInet.getInstance().getHostname();
        }
    };
}
