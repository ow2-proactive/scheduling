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

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extra.logforwarder.SimpleLoggerServer;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.exception.UserException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.task.JavaExecutable;
import org.objectweb.proactive.extra.scheduler.common.task.JavaTask;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * Here is a class that explains how to simply use the scheduler.
 * You'll create a one task job that will print "HelloWorld !".
 * You'll be able to get the output of the task and/or get the result.
 * According that the API is not yet full implemented,
 * adding task and job creation are not on their final implementation.
 * TaskDescriptor may also be removed from user view.
 *
 * @author jlscheef - ProActiveTeam
 *
 */
public class SimpleHelloWorld {
    public static void main(String[] args) {
        try {
            //*********************** GET SCHEDULER *************************
            //get authentication interface from existing scheduler based on scheduler host URL
            //(localhost) followed by the scheduler name (here the default one)
            SchedulerAuthenticationInterface auth = SchedulerConnection.join(
                    "//localhost/" +
                    SchedulerConnection.SCHEDULER_DEFAULT_NAME);

            //Now you are connected you must log on with a couple of username/password matching an entry in login and group files.
            //(groups.cfg, login.cfg in the same directory)
            //you can also log on as admin if your username is in admin group. (it provides you more power ;) )
            UserSchedulerInterface scheduler = auth.logAsUser("chri", "chri");

            //if this point is reached, that's we are connected to the scheduler under "chri".

            //******************** CREATE A NEW JOB ***********************
            //params are respectively : name, priority,cancelOnError, description.
            TaskFlowJob job = new TaskFlowJob();
            job.setName("job name");
            job.setPriority(JobPriority.NORMAL);
            job.setCancelOnError(false);
            job.setDescription("A simple hello world example !");

            //******************** CREATE A NEW TASK ***********************
            //creating a new task
            JavaExecutable task = new JavaExecutable() {
                    private static final long serialVersionUID = 1938122426482626365L;

                    public Object execute(TaskResult... results) {
                        System.out.println("Hello World !");

                        try {
                            return "HelloWorld Sample host : " +
                            URIBuilder.getLocalAddress().toString();
                        } catch (UnknownHostException e) {
                            return "HelloWorld Sample host : unknow host";
                        }
                    }
                };

            //Create the java task
            JavaTask desc = new JavaTask();
            desc.setName("toto");
            //adding the task to the job
            desc.setTaskInstance(task);
            //this task is final, it means that the job result will contain this task result.
            desc.setPreciousResult(true);

            //add the task to the job
            try {
                job.addTask(desc);
            } catch (UserException e2) {
                e2.printStackTrace();
            }

            //******************** SUBMIT THE JOB ***********************
            //submitting a job to the scheduler returns the attributed jobId
            //this id will be used to talk the scheduler about this job.
            JobId jobId = scheduler.submit(job);

            //******************** GET JOB OUTPUT ***********************
            SimpleLoggerServer simpleLoggerServer;

            try {
                // it will launch a listener that will listen connection on any free port
                simpleLoggerServer = SimpleLoggerServer.createLoggerServer();
                // next, this method will forward task output on the previous loggerServer
                scheduler.listenLog(jobId,
                    URIBuilder.getLocalAddress().getHostName(),
                    simpleLoggerServer.getPort());
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
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
    }
}
