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

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.proactive.AOTaskPool;
import org.objectweb.proactive.extensions.calcium.exceptions.PanicException;
import org.objectweb.proactive.extensions.calcium.exceptions.TaskException;
import org.objectweb.proactive.extensions.calcium.task.Task;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEvent;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extra.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;


public class AOJobListener implements SchedulerEventListener, InitActive {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    AOTaskPool taskpool;
    HashMap<JobId, Collection<Task>> processing;
    boolean shutdown;
    private UserSchedulerInterface scheduler;
    SchedulerAuthenticationInterface auth;
    String user;
    String password;

    /**
     * This is a reserved constructor for ProActive's MOP. Don't use directly!
     */
    @Deprecated
    public AOJobListener() {
    }

    static protected AOJobListener createAOJobListener(Node frameworkNode,
        TaskPool taskpool, SchedulerAuthenticationInterface auth, String user,
        String password) throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Job Listener");
        }

        AOJobListener monitor = (AOJobListener) ProActiveObject.newActive(AOJobListener.class.getName(),
                new Object[] { taskpool, auth, user, password }, frameworkNode);

        return monitor;
    }

    public AOJobListener(AOTaskPool taskpool,
        SchedulerAuthenticationInterface auth, String user, String password) {
        this.taskpool = taskpool;
        this.processing = new HashMap<JobId, Collection<Task>>();
        this.shutdown = false;

        this.scheduler = null;
        this.auth = auth;
        this.password = password;
        this.user = user;
    }

    public void initActivity(Body body) {
        try {
            this.scheduler = auth.logAsUser(user, password);

            AOJobListener stubOnThis = (AOJobListener) ProActiveObject.getStubOnThis();

            this.scheduler.addSchedulerEventListener(stubOnThis,
                SchedulerEvent.JOB_KILLED); //JOB_KILLED("jobKilledEvent"),
            this.scheduler.addSchedulerEventListener(stubOnThis,
                SchedulerEvent.RUNNING_TO_FINISHED_JOB); //RUNNING_TO_FINISHED_JOB("runningToFinishedJobEvent")
            this.scheduler.addSchedulerEventListener(stubOnThis,
                SchedulerEvent.KILLED); //KILLED("schedulerKilledEvent"),
            this.scheduler.addSchedulerEventListener(stubOnThis,
                SchedulerEvent.SHUTDOWN); //SHUTDOWN("schedulerShutDownEvent"),
            this.scheduler.addSchedulerEventListener(stubOnThis,
                SchedulerEvent.SHUTTING_DOWN); //SHUTTING_DOWN("schedulerShuttingDownEvent"),
        } catch (Exception e) {
            taskpool.panic(new PanicException(e));
            shutdown = true;
        }
    }

    /**
     * This Event Listener will wait for relevant events from the scheduler.
     * When an event is recieved proper action will be taken and transmitted to the skeleton-tasks
     * encapsulated inside this job.
     *
     * @param jobId  The id of the job submitted to the scheduler.
     * @param tasks The list of skeleton-tasks that had been submitted to the scheduler.
     */
    public void put(JobId jobId, Collection<Task> tasks) {
        if (shutdown) {
            throw new IllegalArgumentException(
                "AOJobCollector is shutting down. No further request for job collectin can be accepted.");
        }

        processing.put(jobId, tasks);
    }

    private void jobDidNotSucceed(JobId jobId, Exception ex) {
        logger.error("Job did not succeed: " + ex.getMessage());

        if (!processing.containsKey(jobId)) {
            return;
        }

        Collection<Task> tList = processing.remove(jobId);

        for (Task task : tList) {
            task.setException(ex);
            taskpool.putProcessedTask(task);
        }
    }

    /* **************************************************************************
     *                      BEGIN USEFUL EVENTS
     * *************************************************************************/
    public void runningToFinishedJobEvent(JobEvent event) {
        if (event == null) {
            return;
        }

        if (!processing.containsKey(event.getJobId())) {
            return;
        }

        JobResult jResult = null;

        try {
            jResult = scheduler.getJobResult(event.getJobId());
        } catch (SchedulerException e) {
            jobDidNotSucceed(event.getJobId(),
                new TaskException("Failed to get result form scheduler: ", e));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Updating results of job: " + jResult.getName());
        }

        Collection<Task> tasksOld = processing.remove(event.getJobId());

        for (Task task : tasksOld) {
            if (logger.isDebugEnabled()) {
                logger.debug("Looking for result of task:" +
                    task.taskId.toString());
            }

            TaskResult result = jResult.getTaskResults()
                                       .get(task.taskId.toString());

            if (result == null) {
                task.setException(new TaskException("Task id=" + task.taskId +
                        " was not returned by the scheduler"));
                logger.error("Task result not found in job result: " +
                    task.getException().getMessage());
            } else if (result.hadException()) { //Exception took place inside the framework
                task.setException(new TaskException(
                        "Throwable error took place in scheduler ",
                        result.getException()));
                logger.error("Task result contains exception: " +
                    task.getException().getMessage());
            } else {
                try {
                    Task computedTask = (Task) result.value();

                    if (!task.taskId.equals(computedTask.taskId)) {
                        throw new TaskException(
                            "Task changed id while being computed: " +
                            task.taskId.value() + "=>" +
                            computedTask.taskId.value());
                    }

                    //Everything is OK
                    taskpool.putProcessedTask(computedTask);
                    continue;
                } catch (Throwable e) {
                    task.setException(new Exception(e));
                    logger.error(task.getException().getMessage());
                }
            }

            //Some Error took place
            logger.error(task.getException().getMessage());
            taskpool.putProcessedTask(task);
        }
    }

    public void jobKilledEvent(JobId jobId) {
        if (!processing.containsKey(jobId)) {
            return;
        }

        TaskException ex = new TaskException("Job id=" +
                jobId.getCurrentValue() + "was killed by scheduler " +
                scheduler);

        jobDidNotSucceed(jobId, ex);
    }

    public void schedulerKilledEvent() {
        shutdown = true;

        TaskException ex = new TaskException("Scheduler was killed: " +
                scheduler);

        Collection<JobId> jList = processing.keySet();

        for (JobId job : jList) {
            jobDidNotSucceed(job, ex);
        }
    }

    public void schedulerShutDownEvent() {
        shutdown = true;

        TaskException ex = new TaskException("Scheduler was shut down: " +
                scheduler);

        Collection<JobId> jList = processing.keySet();

        for (JobId job : jList) {
            jobDidNotSucceed(job, ex);
        }
    }

    public void schedulerShuttingDownEvent() {
        shutdown = true;
    }

    /* **********************************************************************
     *                  USELESS EVENTS
     ************************************************************************/
    public void changeJobPriorityEvent(JobEvent event) {
        // nothing to do
    }

    public void jobPausedEvent(JobEvent event) {
        // nothing to do
    }

    public void jobResumedEvent(JobEvent event) {
        // nothing to do
    }

    public void newPendingJobEvent(InternalJob job) {
        // nothing to do
    }

    public void pendingToRunningJobEvent(JobEvent event) {
        // nothing to do
    }

    public void removeFinishedJobEvent(JobEvent event) {
        // nothing to do
    }

    public void pendingToRunningTaskEvent(TaskEvent event) {
        // TODO Auto-generated method stub
    }

    public void runningToFinishedTaskEvent(TaskEvent event) {
        // TODO Auto-generated method stub
    }

    public void schedulerImmediatePausedEvent() {
        // nothing to do
    }

    public void schedulerPausedEvent() {
        // nothig to do
    }

    public void schedulerResumedEvent() {
        // nothing to do
    }

    public void schedulerStartedEvent() {
        // nothing to do
    }

    public void schedulerStoppedEvent() {
        // nothing to do
    }

    public void newPendingJobEvent(Job job) {
        // TODO Auto-generated method stub
    }
}
