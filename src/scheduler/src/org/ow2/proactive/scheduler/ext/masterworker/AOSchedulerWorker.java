/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.masterworker;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.core.AOWorker;
import org.objectweb.proactive.extensions.masterworker.core.ResultInternImpl;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;


public class AOSchedulerWorker extends AOWorker implements SchedulerEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;

    /**
     * interface to scheduler
     */
    private Scheduler scheduler;

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
     * User classpath (i.e. where to find user classes
     */
    private String[] jobclasspath;

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
            final Map<String, Serializable> initialMemory, String schedulerUrl, String user, String passwd,
            String[] jobclasspath) throws SchedulerException, LoginException {
        super(name, provider, initialMemory);
        this.schedulerUrl = schedulerUrl;
        this.user = user;
        this.password = passwd;
        this.jobclasspath = jobclasspath;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.extensions.masterworker.core.AOWorker#initActivity(org.objectweb.proactive.Body)
     */
    @Override
    public void initActivity(Body body) {
        stubOnThis = (AOSchedulerWorker) PAActiveObject.getStubOnThis();
        SchedulerAuthenticationInterface auth;
        try {
            auth = SchedulerConnection.join(schedulerUrl);
            Credentials creds = null;
            try {
                creds = Credentials.createCredentials(new CredData(CredData.parseLogin(user), CredData
                        .parseDomain(user), password), auth.getPublicKey());
            } catch (KeyException e) {
                throw new LoginException("" + e);
            } catch (LoginException e) {
                throw new LoginException(
                    "Could not retrieve public key, contact the Scheduler admininistrator" + e);
            }
            this.scheduler = auth.login(creds);
        } catch (Exception e) {
            throw new ProActiveRuntimeException(e);
        }

        this.processing = new HashMap<JobId, Collection<TaskIntern<Serializable>>>();

        // We register this active object as a listener
        try {
            this.scheduler.addEventListener((AOSchedulerWorker) stubOnThis, false,
                    SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.JOB_PENDING_TO_FINISHED,
                    SchedulerEvent.KILLED, SchedulerEvent.SHUTDOWN, SchedulerEvent.SHUTTING_DOWN);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        PAActiveObject.setImmediateService("heartBeat");
        PAActiveObject.setImmediateService("terminate");

        // Initial Task
        stubOnThis.getTaskAndSchedule();
    }

    @Override
    public void clear() {
        for (JobId id : processing.keySet()) {
            try {
                scheduler.killJob(id);
            } catch (SchedulerException e) {
                logger.error(e.getMessage());
            }
        }
        processing.clear();
        Service service = new Service(PAActiveObject.getBodyOnThis());
        service.flushAll();
        provider.isCleared(stubOnThis);
    }

    /**
     * ScheduleTask : find a new task to run (actually here a task is a scheduler job)
     */
    @Override
    public void scheduleTask() {
        if (debug) {
            logger.debug(name + " schedules tasks...");
        }
        while (pendingTasksFutures.size() > 0) {
            pendingTasks.addAll(pendingTasksFutures.remove());
        }
        if (!suspended && pendingTasks.size() > 0) {

            TaskFlowJob job = new TaskFlowJob();
            job.setName("Master-Worker Framework Job " + pendingTasks.peek().getId());
            job.setPriority(JobPriority.NORMAL);
            job.setCancelJobOnError(false);
            job.setDescription("Set of parallel master-worker tasks");

            JobEnvironment je = new JobEnvironment();
            try {
                je.setJobClasspath(jobclasspath);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            job.setEnvironment(je);

            Collection<TaskIntern<Serializable>> newTasks = new ArrayList<TaskIntern<Serializable>>();
            while (pendingTasks.size() > 0) {
                TaskIntern<Serializable> task = pendingTasks.remove();
                newTasks.add(task);

                JavaTask schedulerTask = new JavaTask();
                schedulerTask.setName("MasterWorker" + task.getId());
                schedulerTask.setPreciousResult(true);
                schedulerTask.setExecutableClassName(SchedulerExecutableAdapter.class.getName());
                schedulerTask.addArgument("taskCode", task);

                schedulerTask.addArgument("workerMem", (Serializable) initialMemory);

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
        wakingup = false;

    }

    /**
     * Terminate this worker
     */
    @Override
    public BooleanWrapper terminate() {
        try {
            scheduler.disconnect();
        } catch (SchedulerException e) {
            // ignore
        }
        return super.terminate();
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case KILLED:
            case SHUTTING_DOWN:
                for (JobId jobId : processing.keySet()) {
                    jobDidNotSucceed(jobId, new TaskException(new InternalSchedulerException(
                        "Scheduler was " + eventType)));
                }
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_FINISHED:
            case JOB_RUNNING_TO_FINISHED:
                JobInfo info = notification.getData();
                if (info.getStatus() == JobStatus.KILLED) {
                    if (!processing.containsKey(info.getJobId())) {
                        return;
                    }

                    jobDidNotSucceed(info.getJobId(), new TaskException(new InternalSchedulerException(
                        "Job id=" + info.getJobId() + " was killed")));
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
                    //Map<String, TaskResult> allTaskResults = jResult.getAllResults();
                    Map<String, TaskResult> allTaskResults = null;
                    if (jResult.hadException()) {
                        allTaskResults = jResult.getExceptionResults();
                    } else {
                        allTaskResults = jResult.getAllResults();
                    }
                    for (TaskIntern<Serializable> task : tasksOld) {
                        if (debug) {
                            logger.debug(this.getName() + ": looking for result of task: " + task.getId());
                        }
                        ResultIntern<Serializable> intres = new ResultInternImpl(task.getId());
                        TaskResult result = allTaskResults.get("MasterWorker" + task.getId());

                        if (result == null) {
                            intres.setException(new InternalSchedulerException("Task id=" + task.getId() +
                                " was not returned by the scheduler"));
                            if (debug) {
                                logger.debug("Task result not found in job result: " +
                                    intres.getException().getMessage());
                            }
                        } else if (result.hadException()) { //Exception took place inside the framework
                            intres.setException(result.getException());
                            if (debug) {
                                logger.debug("Task result contains exception: " +
                                    intres.getException().getMessage());
                            }
                        } else {
                            try {
                                Serializable computedResult = (Serializable) result.value();

                                intres.setResult(computedResult);

                            } catch (Throwable e) {
                                intres.setException(e);
                                if (debug) {
                                    logger.debug(intres.getException().getMessage());
                                }
                            }
                        }

                        results.add(intres);

                    }

                    Queue<TaskIntern<Serializable>> newTasks = provider.sendResultsAndGetTasks(results, name,
                            true);

                    pendingTasksFutures.offer(newTasks);

                    // Schedule a new job
                    stubOnThis.scheduleTask();
                }
                break;
        }

    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState job) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
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

}
