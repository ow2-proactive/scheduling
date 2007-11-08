/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.calcium.environment.proactivescheduler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.proactive.AOTaskPool;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extra.scheduler.common.exception.UserException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableJavaTask;
import org.objectweb.proactive.extra.scheduler.common.task.JavaTask;


public class TaskDispatcher extends Thread {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    static final int DEFAULT_SLEEP_TIME = 2 * 1000; //time out to sleep in the loop
    private AOTaskPool taskpool;
    private boolean shutdown;
    private FileServerClient fserver;
    AOJobListener monitor;
    SchedulerAuthenticationInterface auth;
    String user;
    String password;

    /**
     * This is a reserved constructor for ProActive's MOP. Don't use directly!
     * @param password
     * @param user
     * @param auth
     */
    @Deprecated
    public TaskDispatcher() {
    }

    public TaskDispatcher(AOTaskPool taskpool, FileServerClient fserver,
        AOJobListener monitor, SchedulerAuthenticationInterface auth,
        String user, String password) {
        super();
        shutdown = false;

        // Create Active Objects
        this.taskpool = taskpool;
        this.fserver = fserver;
        this.monitor = monitor;

        this.auth = auth;
        this.user = user;
        this.password = password;
    }

    //Producer-Consumer
    public void run() {
        UserSchedulerInterface scheduler = null;
        try {
            scheduler = auth.logAsUser(user, password);
        } catch (Exception e) {
            taskpool.panic(new PanicException(e));
            shutdown = true;
        }

        while (!shutdown) {
            Vector<Task> taskV = taskpool.getReadyTasks(0);
            taskV = (Vector<Task>) ProFuture.getFutureValue(taskV);

            if (taskV.size() > 0) {
                try {
                    //submit to the scheduler
                    JobId jobId = scheduler.submit(newJob(taskV));

                    //Register to the event listener
                    monitor.put(jobId, taskV);
                } catch (Exception e) {
                    for (Task t : taskV) {
                        t.setException(e);
                    }

                    taskpool.putProcessedTask(taskV);
                    taskpool.panic(new PanicException(e));

                    shutdown();
                    return;
                }
            } else {
                try {
                    Thread.sleep(DEFAULT_SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            scheduler.disconnect();
        } catch (Exception e) {
            //We don't care about scheduler shutdown errors
            //e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    private TaskFlowJob newJob(Vector<Task> taskV) throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Skeleton Framework Job (id=" + taskV.get(0).parentId +
            ")");
        job.setPriority(JobPriority.NORMAL);
        job.setCancelOnError(true);
        job.setDescription("Set of tasks data parallel skeleton-tasks id=" +
            taskV.get(0).taskId.getFamilyId() + "/" +
            taskV.get(0).taskId.getParentId());

        for (Task task : taskV) {
            ExecutableJavaTask schedInterp = new SchedulerInterpreter(task,
                    fserver);

            JavaTask schedulerTask = new JavaTask();
            schedulerTask.setName(task.taskId.toString());
            schedulerTask.setFinalTask(true);
            schedulerTask.setTaskInstance(schedInterp);

            job.addTask(schedulerTask);
        }

        return job;
    }
}
