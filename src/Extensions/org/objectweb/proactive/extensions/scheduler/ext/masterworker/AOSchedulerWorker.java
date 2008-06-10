package org.objectweb.proactive.extensions.scheduler.ext.masterworker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.core.AOWorker;
import org.objectweb.proactive.extensions.masterworker.core.ResultInternImpl;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.TaskIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerMaster;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.exception.UserException;
import org.objectweb.proactive.extensions.scheduler.common.job.Job;
import org.objectweb.proactive.extensions.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extensions.scheduler.common.job.JobResult;
import org.objectweb.proactive.extensions.scheduler.common.job.TaskFlowJob;
import org.objectweb.proactive.extensions.scheduler.common.job.UserIdentification;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerConnection;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEvent;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.UserSchedulerInterface;
import org.objectweb.proactive.extensions.scheduler.common.task.JavaTask;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;


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
     * @throws SchedulerException 
     * @throws LoginException 
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
            e.printStackTrace();
            return;
        } catch (SchedulerException e1) {
            e1.printStackTrace();
            return;
        }

        this.processing = new HashMap<JobId, Collection<TaskIntern<Serializable>>>();

        // We register this active object as a listener
        try {
            this.scheduler.addSchedulerEventListener((AOSchedulerWorker) stubOnThis,
                    SchedulerEvent.JOB_KILLED, SchedulerEvent.JOB_RUNNING_TO_FINISHED, SchedulerEvent.KILLED,
                    SchedulerEvent.SHUTDOWN, SchedulerEvent.SHUTTING_DOWN);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        PAActiveObject.setImmediateService("heartBeat");
        PAActiveObject.setImmediateService("terminate");

        // Initial Task
        stubOnThis.getTaskAndSchedule();
    }

    /**
     * ScheduleTask : find a new task to run (actually here a task is a scheduler job)
     */
    public void scheduleTask() {
        if (logger.isDebugEnabled()) {
            logger.debug(name + " schedules tasks...");
        }
        while (pendingTasksFutures.size() > 0) {
            pendingTasks.addAll(pendingTasksFutures.remove());
        }
        if (pendingTasks.size() > 0) {

            TaskFlowJob job = new TaskFlowJob();
            job.setName("Master-Worker Framework Job " + pendingTasks.peek().getId());
            job.setPriority(JobPriority.NORMAL);
            job.setCancelOnError(true);
            job.setDescription("Set of parallel master-worker tasks");
            Collection<TaskIntern<Serializable>> newTasks = new ArrayList<TaskIntern<Serializable>>();
            while (pendingTasks.size() > 0) {
                TaskIntern<Serializable> task = pendingTasks.remove();
                newTasks.add(task);
                JavaExecutable schedExec = new SchedulerExecutableAdapter(task);

                JavaTask schedulerTask = new JavaTask();
                schedulerTask.setName("" + task.getId());
                schedulerTask.setPreciousResult(true);
                schedulerTask.setTaskInstance(schedExec);

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
            if (logger.isDebugEnabled()) {
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

    public void jobChangePriorityEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobKilledEvent(JobId jobId) {
        // TODO Auto-generated method stub

    }

    public void jobPausedEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobPendingToRunningEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobRemoveFinishedEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobResumedEvent(JobEvent event) {
        // TODO Auto-generated method stub

    }

    public void jobRunningToFinishedEvent(JobEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug(name + " receives job finished event...");
        }

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
            jobDidNotSucceed(event.getJobId(), new TaskException(e));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this.getName() + ": updating results of job: " + jResult.getName());
        }

        Collection<TaskIntern<Serializable>> tasksOld = processing.remove(event.getJobId());

        for (TaskIntern<Serializable> task : tasksOld) {
            if (logger.isDebugEnabled()) {
                logger.debug(this.getName() + ": looking for result of task: " + task.getId());
            }
            ResultIntern<Serializable> intres = new ResultInternImpl(task);
            TaskResult result = jResult.getAllResults().get("" + task.getId());

            if (result == null) {
                intres.setException(new TaskException(new SchedulerException("Task id=" + task.getId() +
                    " was not returned by the scheduler")));
                logger.error("Task result not found in job result: " + intres.getException().getMessage());
            } else if (result.hadException()) { //Exception took place inside the framework
                intres.setException(new TaskException(result.getException()));
                logger.error("Task result contains exception: " + intres.getException().getMessage());
            } else {
                try {
                    Serializable computedResult = (Serializable) result.value();

                    intres.setResult(computedResult);

                } catch (Throwable e) {
                    intres.setException(new TaskException(e));
                    logger.error(intres.getException().getMessage());
                }
            }

            Queue<TaskIntern<Serializable>> newTasks = provider.sendResultAndGetTasks(intres, name, false);

            pendingTasksFutures.offer(newTasks);
        }

        // Schedule a new job
        ((AOWorker) stubOnThis).scheduleTask();

    }

    public void jobSubmittedEvent(Job job) {
        // TODO Auto-generated method stub

    }

    public void schedulerFrozenEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerKilledEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerPausedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerResumedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerShutDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerShuttingDownEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerStartedEvent() {
        // TODO Auto-generated method stub

    }

    public void schedulerStoppedEvent() {
        // TODO Auto-generated method stub

    }

    public void taskPendingToRunningEvent(TaskEvent event) {
        // TODO Auto-generated method stub

    }

    public void taskRunningToFinishedEvent(TaskEvent event) {
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
        logger.error("Job did not succeed: " + ex.getMessage());

        if (!processing.containsKey(jobId)) {
            return;
        }

        Collection<TaskIntern<Serializable>> tList = processing.remove(jobId);

        for (TaskIntern<Serializable> task : tList) {

            ResultIntern<Serializable> intres = new ResultInternImpl(task);
            intres.setException(ex);
            Queue<TaskIntern<Serializable>> newTasks = provider.sendResultAndGetTasks(intres, name, false);

            pendingTasksFutures.offer(newTasks);
        }

        // Schedule a new job
        ((AOWorker) stubOnThis).scheduleTask();
    }

    public void usersUpdate(UserIdentification userIdentification) {
        // TODO Auto-generated method stub

    }

    public void taskWaitingForRestart(TaskEvent event) {
        // TODO Auto-generated method stub

    }
}
