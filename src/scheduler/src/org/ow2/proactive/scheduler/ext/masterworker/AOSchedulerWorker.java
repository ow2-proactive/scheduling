/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.masterworker;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.core.AOWorker;
import org.objectweb.proactive.extensions.masterworker.core.ResultInternImpl;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


public class AOSchedulerWorker extends AOWorker implements SchedulerEventListener {

    /**
     * interface to scheduler
     */
    private UserSchedulerInterface scheduler;

    /**
     * Current tasks processed by the scheduler
     */
    private HashMap<JobId, Collection<TaskIntern<Serializable>>> processing;

    /**
     * url to the scheduler
     */
    private String schedulerUrl;

    /**
     * user name
     */
    private String user;

    /**
     * password
     */
    private String password;

    /**
     * ProActive no arg contructor
     */
    public AOSchedulerWorker() {
    }

    /**
     * Creates a worker with the given name connected to a scheduler
     * @param name name of the worker
     * @param provider the entity which will provide tasks to the worker
     * @param initialMemory initial memory of the worker
     * @param schedulerUrl url of the scheduler
     * @param user username
     * @param passwd paswword
     * @throws SchedulerException 
     * @throws LoginException 
     */
    public AOSchedulerWorker(final String name, final WorkerMaster provider,
            final Map<String, Serializable> initialMemory, String schedulerUrl, String user, String passwd)
            throws SchedulerException, LoginException {
        super(name, provider, initialMemory);
        this.schedulerUrl = schedulerUrl;
        this.user = user;
        this.password = passwd;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.masterworker.core.AOWorker#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        stubOnThis = (AOSchedulerWorker) PAActiveObject.getStubOnThis();
        SchedulerAuthenticationInterface auth;
        try {
            auth = SchedulerConnection.join(schedulerUrl);

            this.scheduler = auth.logAsUser(user, password);
        } catch (LoginException e) {
            throw new ProActiveRuntimeException(e);
        } catch (SchedulerException e1) {
            throw new ProActiveRuntimeException(e1);
        }

        this.processing = new HashMap<JobId, Collection<TaskIntern<Serializable>>>();

        // We register this active object as a listener
        try {
            this.scheduler.addSchedulerEventListener((AOSchedulerWorker) stubOnThis,
                    SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN,
                    SchedulerEvent.SHUTTING_DOWN);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        PAActiveObject.setImmediateService("heartBeat");
        PAActiveObject.setImmediateService("terminate");

        // Initial Task
        stubOnThis.getTaskAndSchedule();
    }

    public void clear() {
        for (JobId id : processing.keySet()) {
            try {
                scheduler.kill(id);
            } catch (SchedulerException e) {
                logger.error(e.getMessage());
            }
        }
        processing.clear();
        provider.isCleared(stubOnThis);
    }

    /**
     * ScheduleTask : find a new task to run (actually here a task is a scheduler job)
     */
    public void scheduleTask() {
        if (debug) {
            logger.debug(name + " schedules tasks...");
        }
        while (pendingTasksFutures.size() > 0) {
            pendingTasks.addAll(pendingTasksFutures.remove());
        }
        if (pendingTasks.size() > 0) {

            TaskFlowJob job = new TaskFlowJob();
            job.setName("Master-Worker Framework Job " + pendingTasks.peek().getId());
            job.setPriority(JobPriority.NORMAL);
            job.setCancelJobOnError(true);
            job.setDescription("Set of parallel master-worker tasks");
            Collection<TaskIntern<Serializable>> newTasks = new ArrayList<TaskIntern<Serializable>>();
            while (pendingTasks.size() > 0) {
                TaskIntern<Serializable> task = pendingTasks.remove();
                newTasks.add(task);
                JavaExecutable schedExec = new SchedulerExecutableAdapter(task);

                JavaTask schedulerTask = new JavaTask();
                schedulerTask.setName("" + task.getId());
                schedulerTask.setPreciousResult(true);
                //                schedulerTask.setTaskInstance(schedExec);

                try {
                    job.addTask(schedulerTask);
                } catch (UserException e) {
                    e.printStackTrace();
                }

            }

            try {
                JobId jobId = scheduler.submit(job);
                processing.put(jobId, newTasks);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } else {
            // if there is nothing to do we sleep i.e. we do nothing
            if (debug) {
                logger.debug(name + " sleeps...");
            }
        }
    }

    /**
     * Terminate this worker
     */
    public BooleanWrapper terminate() {
        try {
            scheduler.disconnect();
        } catch (SchedulerException e) {
            // ignore
        }
        return super.terminate();
    }

    public void jobChangePriorityEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobPausedEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobPendingToRunningEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobRemoveFinishedEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobResumedEvent(JobInfo info) {
        // TODO Auto-generated method stub

    }

    public void jobRunningToFinishedEvent(JobInfo info) {
        if (info.getState() == JobState.KILLED) {
            if (!processing.containsKey(info.getJobId())) {
                return;
            }

            jobDidNotSucceed(info.getJobId(), new TaskException(new SchedulerException("Job id=" +
                info.getJobId() + " was killed")));
        } else {

            if (debug) {
                logger.debug(name + " receives job finished event...");
            }

            if (info == null) {
                return;
            }

            if (!processing.containsKey(info.getJobId())) {
                return;
            }

            JobResult jResult = null;

            try {
                jResult = scheduler.getJobResult(info.getJobId());
            } catch (SchedulerException e) {
                jobDidNotSucceed(info.getJobId(), new TaskException(e));
                return;
            }

            if (debug) {
                logger.debug(this.getName() + ": updating results of job: " + jResult.getName());
            }

            Collection<TaskIntern<Serializable>> tasksOld = processing.remove(info.getJobId());

            ArrayList<ResultIntern<Serializable>> results = new ArrayList<ResultIntern<Serializable>>();
            Map<String, TaskResult> allTaskResults = jResult.getAllResults();

            for (TaskIntern<Serializable> task : tasksOld) {
                if (debug) {
                    logger.debug(this.getName() + ": looking for result of task: " + task.getId());
                }
                ResultIntern<Serializable> intres = new ResultInternImpl(task.getId());
                TaskResult result = allTaskResults.get("" + task.getId());

                if (result == null) {
                    intres.setException(new TaskException(new SchedulerException("Task id=" + task.getId() +
                        " was not returned by the scheduler")));
                    if (debug) {
                        logger.debug("Task result not found in job result: " +
                            intres.getException().getMessage());
                    }
                } else if (result.hadException()) { //Exception took place inside the framework
                    intres.setException(new TaskException(result.getException()));
                    if (debug) {
                        logger.debug("Task result contains exception: " + intres.getException().getMessage());
                    }
                } else {
                    try {
                        Serializable computedResult = (Serializable) result.value();

                        intres.setResult(computedResult);

                    } catch (Throwable e) {
                        intres.setException(new TaskException(e));
                        if (debug) {
                            logger.debug(intres.getException().getMessage());
                        }
                    }
                }

                results.add(intres);

            }

            Queue<TaskIntern<Serializable>> newTasks = provider.sendResultsAndGetTasks(results, name, true);

            pendingTasksFutures.offer(newTasks);

            // Schedule a new job
            stubOnThis.scheduleTask();
        }
    }

    public void jobSubmittedEvent(Job job) {
        // TODO Auto-generated method stub

    }

    public void schedulerFrozenEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerKilledEvent() {
        for (JobId jobId : processing.keySet()) {
            jobDidNotSucceed(jobId, new TaskException(new SchedulerException("Scheduler was killed")));
        }

    }

    public void schedulerPausedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerResumedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerShutDownEvent() {

    }

    public void schedulerShuttingDownEvent() {
        for (JobId jobId : processing.keySet()) {
            jobDidNotSucceed(jobId, new TaskException(new SchedulerException("Scheduler is shutting down")));
        }

    }

    public void schedulerStartedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerStoppedEvent() {
        // TODO Auto-generated method stub

    }

    public void taskPendingToRunningEvent(TaskInfo info) {
        // TODO Auto-generated method stub

    }

    public void taskRunningToFinishedEvent(TaskInfo info) {
        // TODO Auto-generated method stub 
    }

    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerRMUpEvent() {
        // TODO Auto-generated method stub

    }

    /**
     * The job failed
     * @param jobId id of the job
     * @param ex exception thrown
     */
    private void jobDidNotSucceed(JobId jobId, Exception ex) {
        if (debug) {
            logger.debug("Job did not succeed: " + ex.getMessage());
        }

        if (!processing.containsKey(jobId)) {
            return;
        }

        Collection<TaskIntern<Serializable>> tList = processing.remove(jobId);

        ArrayList<ResultIntern<Serializable>> results = new ArrayList<ResultIntern<Serializable>>();

        for (TaskIntern<Serializable> task : tList) {

            ResultIntern<Serializable> intres = new ResultInternImpl(task.getId());
            intres.setException(ex);
            results.add(intres);
        }

        Queue<TaskIntern<Serializable>> newTasks = provider.sendResultsAndGetTasks(results, name, true);

        pendingTasksFutures.offer(newTasks);

        // Schedule a new job
        stubOnThis.scheduleTask();
    }

    public void usersUpdate(UserIdentification userIdentification) {
        // TODO Auto-generated method stub

    }

    public void taskWaitingForRestart(TaskInfo info) {
        // TODO Auto-generated method stub

    }

    public void schedulerPolicyChangedEvent(String newPolicyName) {
        // TODO Auto-generated method stub

    }
}
