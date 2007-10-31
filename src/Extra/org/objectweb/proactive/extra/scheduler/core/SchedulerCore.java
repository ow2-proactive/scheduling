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
package org.objectweb.proactive.extra.scheduler.core;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProException;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.logforwarder.BufferedAppender;
import org.objectweb.proactive.extra.logforwarder.SimpleLoggerServer;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.job.Job;
import org.objectweb.proactive.extra.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extra.scheduler.common.job.JobResult;
import org.objectweb.proactive.extra.scheduler.common.job.JobState;
import org.objectweb.proactive.extra.scheduler.common.job.JobType;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerInitialState;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerState;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableApplicationTask;
import org.objectweb.proactive.extra.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extra.scheduler.common.task.SimpleTaskLogs;
import org.objectweb.proactive.extra.scheduler.common.task.Status;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.core.db.AbstractSchedulerDB;
import org.objectweb.proactive.extra.scheduler.core.db.RecoverableState;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;
import org.objectweb.proactive.extra.scheduler.job.JobDescriptor;
import org.objectweb.proactive.extra.scheduler.job.JobResultImpl;
import org.objectweb.proactive.extra.scheduler.job.TaskDescriptor;
import org.objectweb.proactive.extra.scheduler.policy.PolicyInterface;
import org.objectweb.proactive.extra.scheduler.resourcemanager.InfrastructureManagerProxy;
import org.objectweb.proactive.extra.scheduler.task.AppliTaskLauncher;
import org.objectweb.proactive.extra.scheduler.task.TaskLauncher;
import org.objectweb.proactive.extra.scheduler.task.TaskResultImpl;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalNativeTask;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * <i><font size="-1" color="#FF0000">** Scheduler core ** </font></i>
 * This is the main active object of the scheduler implementation,
 * it communicates with the entity manager to acquire nodes and with a policy
 * to insert and get jobs from the queue.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 27, 2007
 * @since ProActive 3.2
 */
public class SchedulerCore implements SchedulerCoreInterface, RunActive {

    /** serial version UID */
    private static final long serialVersionUID = 1581139478784832488L;

    /** Scheduler logger */
    public static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);

    /** Scheduler main loop time out */
    private static final int SCHEDULER_TIME_OUT = 2000;

    /** Scheduler node ping frequency in ms. */
    private static final int SCHEDULER_NODE_PING_FREQUENCY = 45000;

    /** Selected port for connection logger system */
    private int port;

    /** Host name of the scheduler for logger system. */
    private static String host = null;

    /** Implementation of Infrastructure Manager */
    private InfrastructureManagerProxy resourceManager;

    /** Scheduler front-end. */
    private SchedulerFrontend frontend;

    /** Scheduler current policy */
    private PolicyInterface policy;

    /** list of all jobs managed by the scheduler */
    private HashMap<JobId, InternalJob> jobs = new HashMap<JobId, InternalJob>();

    /** list of pending jobs among the managed jobs */
    private Vector<InternalJob> pendingJobs = new Vector<InternalJob>();

    /** list of running jobs among the managed jobs */
    private Vector<InternalJob> runningJobs = new Vector<InternalJob>();

    /** list of finished jobs among the managed jobs */
    private Vector<InternalJob> finishedJobs = new Vector<InternalJob>();

    /** Scheduler current state */
    private SchedulerState state = SchedulerState.STOPPED;

    /** Thread that will ping the running nodes */
    private Thread pinger;

    /** Store logs for running jobs : SHOULD BE REMOVED */
    private Hashtable<JobId, BufferedAppender> jobLogs = new Hashtable<JobId, BufferedAppender>();

    /**
     * Pro Active empty constructor
     */
    public SchedulerCore() {
    }

    /**
     * Create a new scheduler Core with the given resources manager.
     *
     * @param imp the resource manager on which the scheduler will interact.
     */
    public SchedulerCore(InfrastructureManagerProxy imp,
        SchedulerFrontend frontend, String policyFullName) {
        try {
            this.resourceManager = imp;
            this.frontend = frontend;
            //logger
            host = URIBuilder.getLocalAddress().getHostName();
            try {
                // redirect event only into JobLogs
                SimpleLoggerServer slf = SimpleLoggerServer.createLoggerServer();
                this.port = slf.getPort();
            } catch (IOException e) {
                logger.error("Cannot create logger server : " + e.getMessage());
                throw new RuntimeException(e);
            }
            this.policy = (PolicyInterface) Class.forName(policyFullName)
                                                 .newInstance();
            logger.info("Scheduler Core ready !");
        } catch (InstantiationException e) {
            logger.error("The policy class cannot be found : " +
                e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("The method cannot be accessed " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            logger.error(
                "The class definition cannot be found, it might be due to case sentivity : " +
                e.getMessage());
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            logger.error("Unknown host in host creation : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the pinger thread to detect unActivity on nodes.
     */
    private void createPingThread() {
        pinger = new Thread() {
                    @Override
                    public void run() {
                        while (!isInterrupted()) {
                            try {
                                Thread.sleep(SCHEDULER_NODE_PING_FREQUENCY);
                                if (runningJobs.size() > 0) {
                                    pingDeployedNodes();
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                };
        pinger.start();
    }

    /**
     * Create the list of taskEvent containing in the given job.
     * Clear the task event modify status. It is used to change all status of all tasks
     * with only one request. It has to be cleared after sending events.
     * Send the change to the data base.
     *
     * @param currentJob the job where the task event are.
     */
    private void updateTaskEventsList(InternalJob currentJob) {
        ArrayList<TaskEvent> events = new ArrayList<TaskEvent>();
        for (TaskId id : currentJob.getJobInfo().getTaskStatusModify().keySet()) {
            events.add(currentJob.getHMTasks().get(id).getTaskInfo());
        }
        // don't forget to set the task status modify to null
        currentJob.setTaskStatusModify(null);
        // used when a job has failed
        currentJob.setTaskFinishedTimeModify(null);
        // and to database
        AbstractSchedulerDB.getInstance()
                           .setJobAndTasksEvents(currentJob.getJobInfo(), events);
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        //Rebuild the scheduler if a crash has occurred.
        //recover();
        //listen log as immediate Service.
        //ProActiveObject.setImmediateService("listenLog");
        Service service = new Service(body);

        //set the filter for serveAll method
        RequestFilter filter = new MainLoopRequestFilter("submit", "pause",
                "terminate");
        createPingThread();
        do {
            service.blockingServeOldest();
            while ((state == SchedulerState.STARTED) ||
                    (state == SchedulerState.PAUSED)) {
                try {
                    service.serveAll(filter);
                    schedule();
                    //block the loop until a method is invoked and serve it
                    service.blockingServeOldest(SCHEDULER_TIME_OUT);
                } catch (Exception e) {
                    System.out.println(
                        "SchedulerCore.runActivity(MAIN_LOOP) caught an EXCEPTION - it will not terminate the body !");
                    e.printStackTrace();
                }
            }
        } while ((state != SchedulerState.SHUTTING_DOWN) &&
                (state != SchedulerState.KILLED));
        logger.info("[SCHEDULER] Scheduler is shutting down...");
        //FIXME for the moment if shutdown sequence is enable, paused job becomes running
        for (InternalJob job : jobs.values()) {
            if (job.getState() == JobState.PAUSED) {
                job.setUnPause();
                JobEvent event = job.getJobInfo();
                //send event to front_end
                frontend.jobResumedEvent(event);
                updateTaskEventsList(job);
            }
        }

        //terminating jobs...
        if ((runningJobs.size() + pendingJobs.size()) > 0) {
            logger.info("Terminating jobs...");
        }
        while ((runningJobs.size() + pendingJobs.size()) > 0) {
            try {
                service.serveAll("terminate");
                schedule();
                //block the loop until a method is invoked and serve it
                service.blockingServeOldest(SCHEDULER_TIME_OUT);
            } catch (Exception e) {
                System.out.println("SchedulerCore.runActivity(SHUTTING_DOWN)");
                e.printStackTrace();
            }
        }
        //stop the pinger thread.
        pinger.interrupt();
        //TODO something to do with the database ??
        logger.info("[SCHEDULER] Terminating...");
        //shutdown resource manager proxy
        resourceManager.shutdownProxy();
        if (state == SchedulerState.SHUTTING_DOWN) {
            frontend.schedulerShutDownEvent();
        }
        //destroying scheduler active objects
        frontend.terminate();
        //closing data base
        logger.info("[SCHEDULER] Closing Scheduler data base !");
        AbstractSchedulerDB.clearInstance();
        //terminate this active object
        ProActiveObject.terminateActiveObject(false);
        logger.info("[SCHEDULER] Scheduler is now shutdown !");
        //exit
        System.exit(0);
    }

    /**
     * Schedule computing method
     */
    private void schedule() {
        //get light job list with eligible jobs (running and pending)
        ArrayList<JobDescriptor> LightJobList = new ArrayList<JobDescriptor>();
        for (InternalJob j : runningJobs) {
            LightJobList.add(j.getJobDescriptor());
        }

        //if scheduler is paused it only finishes running jobs
        if (state != SchedulerState.PAUSED) {
            for (InternalJob j : pendingJobs) {
                LightJobList.add(j.getJobDescriptor());
            }
        }

        //ask the policy all the tasks to be schedule according to the jobs list.
        Vector<?extends TaskDescriptor> taskRetrivedFromPolicy = policy.getOrderedTasks(LightJobList);
        while (!taskRetrivedFromPolicy.isEmpty() &&
                resourceManager.hasFreeResources().booleanValue()) {
            TaskDescriptor taskDescriptor = taskRetrivedFromPolicy.get(0);
            InternalJob currentJob = jobs.get(taskDescriptor.getJobId());
            InternalTask internalTask = currentJob.getHMTasks()
                                                  .get(taskDescriptor.getId());

            //TODO improve the way to get the nodes from the resources manager.
            //it can be better to associate scripts and node to ask for more than one node each time,
            //and to be sure that we give the right node with its right script
            logger.info("[SCHEDULING] Asking for " +
                internalTask.getNumberOfNodesNeeded() + " nodes with" +
                ((internalTask.getVerifyingScript() == null) ? "out " : " ") +
                "verif script");
            NodeSet nodeSet = resourceManager.getAtMostNodes(internalTask.getNumberOfNodesNeeded(),
                    internalTask.getVerifyingScript());
            logger.info("[SCHEDULING] Got " + nodeSet.size() + " nodes");
            Node node = null;
            try {
                while (nodeSet.size() > 0) {
                    node = nodeSet.get(0);
                    TaskLauncher launcher = null;

                    //if the job is an application job and if all nodes can be launched at the same time
                    if ((currentJob.getType() == JobType.APPLI) &&
                            (nodeSet.size() >= internalTask.getNumberOfNodesNeeded())) {
                        nodeSet.remove(0);
                        launcher = internalTask.createLauncher(host, port, node);
                        NodeSet nodes = new NodeSet();
                        for (int i = 0;
                                i < (internalTask.getNumberOfNodesNeeded() - 1);
                                i++) {
                            nodes.add(nodeSet.remove(0));
                        }
                        currentJob.getJobResult()
                                  .addTaskResult(internalTask.getName(),
                            ((AppliTaskLauncher) launcher).doTask(
                                (SchedulerCore) ProActiveObject.getStubOnThis(),
                                (ExecutableApplicationTask) internalTask.getTask(),
                                nodes));
                    } else if (currentJob.getType() != JobType.APPLI) {
                        nodeSet.remove(0);
                        launcher = internalTask.createLauncher(host, port, node);
                        //if job is TASKSFLOW, preparing the list of parameters for this task.
                        int resultSize = taskDescriptor.getParents().size();
                        if ((currentJob.getType() == JobType.TASKSFLOW) &&
                                (resultSize > 0)) {
                            TaskResult[] params = new TaskResult[resultSize];
                            for (int i = 0; i < resultSize; i++) {
                                //get parent task number i
                                InternalTask parentTask = currentJob.getHMTasks()
                                                                    .get(taskDescriptor.getParents()
                                                                                       .get(i)
                                                                                       .getId());
                                //set the task result in the arguments array.
                                params[i] = currentJob.getJobResult()
                                                      .getTaskResults()
                                                      .get(parentTask.getName());
                            }
                            currentJob.getJobResult()
                                      .addTaskResult(internalTask.getName(),
                                launcher.doTask(
                                    (SchedulerCore) ProActiveObject.getStubOnThis(),
                                    internalTask.getTask(), params));
                        } else {
                            currentJob.getJobResult()
                                      .addTaskResult(internalTask.getName(),
                                launcher.doTask(
                                    (SchedulerCore) ProActiveObject.getStubOnThis(),
                                    internalTask.getTask()));
                        }
                    }

                    //if a task has been launched
                    if (launcher != null) {
                        logger.info("[SCHEDULER] New task started on " +
                            node.getNodeInformation().getVMInformation()
                                .getHostName() + " [ " + internalTask.getId() +
                            " ]");
                        // set the different informations on job
                        if (currentJob.getStartTime() == -1) {
                            // if it is the first task of this job
                            currentJob.start();
                            pendingJobs.remove(currentJob);
                            runningJobs.add(currentJob);
                            // send job event to front-end
                            frontend.pendingToRunningJobEvent(currentJob.getJobInfo());
                            //create tasks events list
                            updateTaskEventsList(currentJob);
                        }
                        // set the different informations on task
                        currentJob.startTask(internalTask,
                            node.getNodeInformation().getVMInformation()
                                .getHostName());
                        // send task event to front-end
                        frontend.pendingToRunningTaskEvent(internalTask.getTaskInfo());
                        // and to data base
                        AbstractSchedulerDB.getInstance()
                                           .setTaskEvent(internalTask.getTaskInfo());
                    } else {
                        //if no task can be launched on this job, go to the next job.
                        resourceManager.freeNodes(nodeSet);
                        //and leave this loop
                        break;
                    }
                }
                //if everything were OK, removed this task from the processed task.
                taskRetrivedFromPolicy.remove(0);
            } catch (Exception e1) {
                //if we are here, it is that something append while launching the current task.
                logger.warn("Current node has failed due to node failure : " +
                    node);
                //so get back the node to the resource manager
                resourceManager.freeDownNode(internalTask.getExecuterInformations()
                                                         .getNodeName());
                e1.printStackTrace();
            }
        }
    }

    /**
     * Ping every nodes on which a task is currently running and repair the task if need.
     */
    private void pingDeployedNodes() {
        logger.info("[SCHEDULER] Search for down nodes !");
        for (int i = 0; i < runningJobs.size(); i++) {
            InternalJob job = runningJobs.get(i);
            for (InternalTask td : job.getTasks()) {
                if ((td.getStatus() == Status.RUNNNING) &&
                        !ProActiveObject.pingActiveObject(
                            td.getExecuterInformations().getLauncher())) {
                    logger.info("[SCHEDULER] Node failed on job " +
                        job.getId() + ", task [ " + td.getId() + " ]");
                    if (td.getRerunnableLeft() > 0) {
                        td.setRerunnableLeft(td.getRerunnableLeft() - 1);
                        job.reStartTask(td);
                        //TODO if the job is paused, send an event to the scheduler to notify that this task is now paused.
                        //free execution node even if it is dead
                        resourceManager.freeDownNode(td.getExecuterInformations()
                                                       .getNodeName());
                    } else {
                        failedJob(job, td,
                            "An error has occured due to a node failure and the maximum amout of reRennable property has been reached.",
                            JobState.FAILED);
                        i--;
                        //free execution node even if it is dead
                        resourceManager.freeDownNode(td.getExecuterInformations()
                                                       .getNodeName());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Submit a new job to the scheduler.
     *
     * @param job the job to be scheduled.
     * @throws SchedulerException
     */
    public void submit(InternalJob job) throws SchedulerException {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.STOPPED)) {
            throw new SchedulerException(
                "Scheduler is stopped, cannot submit new job !");
        }
        job.submit();
        // add job to core
        jobs.put(job.getId(), job);
        pendingJobs.add(job);
        //create job result storage
        JobResult jobResult = new JobResultImpl(job.getId(), job.getName());
        //store the job result until user get it
        job.setJobResult(jobResult);
        //create appender for this job
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX +
                job.getId());
        l.setAdditivity(false);
        if (l.getAppender(Log4JTaskLogs.JOB_APPENDER_NAME) == null) {
            BufferedAppender op = new BufferedAppender(Log4JTaskLogs.JOB_APPENDER_NAME,
                    true);
            this.jobLogs.put(job.getId(), op);
            l.addAppender(op);
            // log into file if required
            if (job.getLogFile() != null) {
                try {
                    FileAppender fa = new FileAppender(Log4JTaskLogs.DEFAULT_LOG_LAYOUT,
                            job.getLogFile(), false);
                    op.addSink(fa);
                } catch (IOException e) {
                    logger.warn("[SCHEDULER] Cannot open log file " +
                        job.getLogFile() + " : " + e.getMessage());
                }
            }
        } else {
            throw new RuntimeException("[SCHEDULER] Appender for job " +
                job.getId() + " is already activated");
        }
        //sending event to client
        frontend.newPendingJobEvent(job);
        //and to data base
        AbstractSchedulerDB.getInstance().addJob(job);
        logger.info("[SCHEDULER] New job added containing " +
            job.getTotalNumberOfTasks() + " tasks !");
    }

    /**
     * Failed the given job due to the given task failure.
     *
     * @param job the job to failed.
     * @param td the task who has been the caused of failing.
     * @param errorMsg the error message to send in the task result.
     * @param jobState the type of the failure for this job. (failed/canceled)
     */
    private void failedJob(InternalJob job, InternalTask task, String errorMsg,
        JobState jobState) {
        TaskResult taskResult = null;
        for (InternalTask td : job.getTasks()) {
            if (td.getStatus() == Status.RUNNNING) {
                try {
                    //get the nodes that are used for this descriptor
                    NodeSet nodes = td.getExecuterInformations().getLauncher()
                                      .getNodes();

                    //try to terminate the task
                    try {
                        td.getExecuterInformations().getLauncher().terminate();
                    } catch (Exception e) { /* Tested (nothing to do) */
                    }
                    //free every execution nodes
                    resourceManager.freeNodes(nodes, td.getPostTask());
                } catch (Exception e) {
                    resourceManager.freeNode(td.getExecuterInformations()
                                               .getNode());
                }

                //deleting task result
                if ((jobState == JobState.CANCELLED) &&
                        td.getId().equals(task.getId())) {
                    taskResult = job.getJobResult().getTaskResults()
                                    .get(task.getName());
                }
            }
        }
        //failed the job
        job.failed(task.getId(), jobState);
        //store the exception into jobResult
        // TODO : cdelbe, jlscheef : task is not a final task -> we should not store its result
        // TODO : cdelbe, jlscheef : where is the original exception ... ?
        if (jobState == JobState.FAILED) {
            taskResult = new TaskResultImpl(task.getId(),
                    new Throwable(errorMsg), new SimpleTaskLogs("", errorMsg));
            job.getJobResult().addTaskResult(task.getName(), taskResult);
        } else {
            job.getJobResult().addTaskResult(task.getName(), taskResult);
        }
        AbstractSchedulerDB.getInstance().addTaskResult(taskResult);
        //move the job
        runningJobs.remove(job);
        finishedJobs.add(job);
        // terminate loggers
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX +
                job.getId());
        l.removeAllAppenders();
        //send event to listeners.
        frontend.runningToFinishedJobEvent(job.getJobInfo());
        //create tasks events list
        updateTaskEventsList(job);
        logger.info("[SCHEDULER] Terminated job (failed/Cancelled) " +
            job.getId());
    }

    /**
     * Invoke by a task when it is about to finish.
     * This method can be invoke just a little amount of time before the result arrival.
     * That's why it can block the execution but only for short time.
     *
     * @param taskId the identification of the executed task.
     */
    public void terminate(TaskId taskId) {
        JobId jobId = taskId.getJobId();
        InternalJob job = jobs.get(jobId);

        InternalTask descriptor = job.getHMTasks().get(taskId);
        try {
            //The task is terminated but it's possible to have to
            //wait for the future of the task result (TaskResult).
            //accessing to the taskResult could block current execution but for a little time.
            //it is the time between the end of the task and the arrival of the future from the task.
            //
            //check if the task result future has an error due to node death.
            //if the node has died, a runtimeException is sent instead of the result
            TaskResult res = null;
            res = job.getJobResult().getTaskResults().get(descriptor.getName());
            // unwrap future
            res = (TaskResult) ProFuture.getFutureValue(res);

            // HANDLE DESCIPTORS
            if (res != null) {
                res.setDescriptorClass(descriptor.getResultDescriptor());
            }

            if (res != null) {
                if (ProException.isException(res)) {
                    //in this case, it is a node error.
                    //this is not user exception or usage,
                    //so we restart independently of re-runnable properties
                    logger.info("[SCHEDULER] Node failed on job " + jobId +
                        ", task [ " + taskId + " ]");
                    job.reStartTask(descriptor);
                    //free execution node even if it is dead
                    resourceManager.freeDownNode(descriptor.getExecuterInformations()
                                                           .getNodeName());
                    return;
                }
            }
            logger.info("[SCHEDULER] Terminated task on job " + jobId + " [ " +
                taskId + " ]");
            //if an exception occurred and the user wanted to cancel on exception, cancel the job.
            boolean errorOccured = false;
            try {
                Object resValue = res.value();
                if (descriptor instanceof InternalNativeTask) {
                    // an error occurred if res is not 0
                    errorOccured = ((Integer) resValue) != 0;
                }
            } catch (Throwable e) {
                // An exception occurred during task execution
                errorOccured = true;
            }
            if (errorOccured && job.isCancelOnError()) {
                // TODO jscheef : exception e should be the jobResult... 
                failedJob(job, descriptor,
                    "An error has occured due to a user error caught in the task and user wanted to cancel on error.",
                    JobState.CANCELLED);
                return;
            }

            descriptor = job.terminateTask(taskId);
            //send event
            frontend.runningToFinishedTaskEvent(descriptor.getTaskInfo());
            //and to data base
            AbstractSchedulerDB.getInstance()
                               .setTaskEvent(descriptor.getTaskInfo());
            //store this task result in the job result.
            job.getJobResult().addTaskResult(descriptor.getName(), res);
            //and in data base
            AbstractSchedulerDB.getInstance().addTaskResult(res);

            //if this job is finished (every task are finished)
            if (job.getNumberOfFinishedTask() == job.getTotalNumberOfTasks()) {
                //terminating job
                job.terminate();
                runningJobs.remove(job);
                finishedJobs.add(job);
                logger.info("[SCHEDULER] Terminated job " + jobId);
                // terminate loggers
                Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX +
                        job.getId());
                l.removeAllAppenders(); // appender are closed...
                                        //send event to listeners.

                frontend.runningToFinishedJobEvent(job.getJobInfo());
                //and to data base
                AbstractSchedulerDB.getInstance().setJobEvent(job.getJobInfo());
            }
            //free every execution nodes
            resourceManager.freeNodes(descriptor.getExecuterInformations()
                                                .getLauncher().getNodes(),
                descriptor.getPostTask());
        } catch (NodeException e) {
            //if the getLauncher().getNodes() method throws an exception,
            //just free the execution node.
            try {
                resourceManager.freeNode(NodeFactory.getNode(
                        descriptor.getExecuterInformations().getNodeName()),
                    descriptor.getPostTask());
            } catch (NodeException e1) {
                //the freeNodes has failed, try to get it back as a string (the node may be down)
                resourceManager.freeDownNode(descriptor.getExecuterInformations()
                                                       .getNodeName());
            }
        } catch (NullPointerException eNull) {
            //the task has been killed. Nothing to do anymore with this one.
        }
    }

    /**
     * Return the scheduler current state with the pending, running, finished jobs list.
     *
     * @return the scheduler current state with the pending, running, finished jobs list.
     */
    public SchedulerInitialState<?extends Job> getSchedulerInitialState() {
        SchedulerInitialState<InternalJob> sState = new SchedulerInitialState<InternalJob>();
        sState.setPendingJobs(pendingJobs);
        sState.setRunningJobs(runningJobs);
        sState.setFinishedJobs(finishedJobs);
        sState.setState(state);
        return sState;
    }

    /**
     * Listen for the tasks user log.
     * WARNING : This method is served as immediate service
     * @param jobId the id of the job to listen to.
     * @param hostname the host name where to send the log.
     * @param port the port number on which the log will be sent.
     */
    public void listenLog(JobId jobId, String hostname, int port) {
        BufferedAppender bufferForJobId = this.jobLogs.get(jobId);
        if (bufferForJobId != null) {
            bufferForJobId.addSink(new SocketAppender(hostname, port));
        } else {
            // job has been removed _or_ scheduler has recovered
            // create appender for this job
            InternalJob target = this.jobs.get(jobId);
            if (target != null) {
                Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX +
                        jobId);
                l.setAdditivity(false);
                BufferedAppender op = new BufferedAppender(Log4JTaskLogs.JOB_APPENDER_NAME,
                        true);
                this.jobLogs.put(jobId, op);
                l.addAppender(op);
                op.addSink(new SocketAppender(hostname, port));

                // retreive already stored output of job jobid in task logs in task results
                Collection<TaskResult> allRes = target.getJobResult()
                                                      .getTaskResults().values();
                for (TaskResult tr : allRes) {
                    // if taskResult is not awaited, task is terminated
                    if (!ProFuture.isAwaited(tr)) {
                        l.info(tr.getOuput().getStdoutLogs(false));
                        l.error(tr.getOuput().getStderrLogs(false));
                    }
                }
            }
        }
    }

    /**
     * To get the result of a job.
     *
     * @return the result of a job.
     */
    public JobResult getJobResult(JobId jobId) {
        JobResult result = null;
        InternalJob job = jobs.get(jobId);
        if (job != null) {
            jobs.remove(jobId);
            result = job.getJobResult();
            job.setRemovedTime(System.currentTimeMillis());
            finishedJobs.remove(job);
            //send event to frontend
            frontend.removeFinishedJobEvent(job.getJobInfo());
            //and to data base
            AbstractSchedulerDB.getInstance().setJobEvent(job.getJobInfo());
            BufferedAppender jobLog = this.jobLogs.remove(jobId);
            if (jobLog != null) {
                jobLog.close();
            }
            logger.info("[SCHEDULER] Removed result for job " + jobId);
        }
        return result;
    }

    /**
     * To get the result of a task.
     *
     * @return the result of a task.
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) {
        TaskResult result = null;
        InternalJob job = jobs.get(jobId);
        if (job != null) {
            result = job.getJobResult().getTaskResults().get(taskName);
            if (!ProFuture.isAwaited(result)) {
                logger.info("[SCHEDULER] Get '" + taskName +
                    "' task result for job " + jobId);
            } else {
                return null;
            }
        }
        return result;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#start()
     */
    public BooleanWrapper coreStart() {
        if (state != SchedulerState.STOPPED) {
            return new BooleanWrapper(false);
        }
        state = SchedulerState.STARTED;
        logger.info("[SCHEDULER] Scheduler has just been started !");
        frontend.schedulerStartedEvent();
        return new BooleanWrapper(true);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#stop()
     */
    public BooleanWrapper coreStop() {
        if ((state == SchedulerState.STOPPED) ||
                (state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        state = SchedulerState.STOPPED;
        logger.info(
            "Scheduler has just been stopped, no tasks will be launched until start.");
        frontend.schedulerStoppedEvent();
        return new BooleanWrapper(true);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#pause()
     */
    public BooleanWrapper corePause() {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        if ((state != SchedulerState.PAUSED_IMMEDIATE) &&
                (state != SchedulerState.STARTED)) {
            return new BooleanWrapper(false);
        }
        state = SchedulerState.PAUSED;
        logger.info("[SCHEDULER] Scheduler has just been paused !");
        frontend.schedulerPausedEvent();
        return new BooleanWrapper(true);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#coreImmediatePause()
     */
    public BooleanWrapper coreImmediatePause() {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        if ((state != SchedulerState.PAUSED) &&
                (state != SchedulerState.STARTED)) {
            return new BooleanWrapper(false);
        }
        state = SchedulerState.PAUSED_IMMEDIATE;
        logger.info("[SCHEDULER] Scheduler has just been immediate paused !");
        frontend.schedulerImmediatePausedEvent();
        return new BooleanWrapper(true);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#resume()
     */
    public BooleanWrapper coreResume() {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        if ((state != SchedulerState.PAUSED) &&
                (state != SchedulerState.PAUSED_IMMEDIATE) &&
                (state != SchedulerState.STARTED)) {
            return new BooleanWrapper(false);
        }
        state = SchedulerState.STARTED;
        logger.info("Scheduler has just been resumed !");
        frontend.schedulerResumedEvent();
        return new BooleanWrapper(true);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#shutdown()
     */
    public BooleanWrapper coreShutdown() {
        //TODO of the scheduler is shutting down and a job is paused, what can we do for the job ?
        if ((state == SchedulerState.KILLED) ||
                (state == SchedulerState.SHUTTING_DOWN)) {
            return new BooleanWrapper(false);
        }
        state = SchedulerState.SHUTTING_DOWN;
        logger.info(
            "Scheduler is shutting down, this make take time to finish every jobs !");
        frontend.schedulerShuttingDownEvent();
        return new BooleanWrapper(true);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.core.SchedulerCoreInterface#coreKill()
     */
    public synchronized BooleanWrapper coreKill() {
        if (state == SchedulerState.KILLED) {
            return new BooleanWrapper(false);
        }

        //destroying running active object launcher
        for (InternalJob j : runningJobs) {
            for (InternalTask td : j.getTasks()) {
                if (td.getStatus() == Status.RUNNNING) {
                    try {
                        NodeSet nodes = td.getExecuterInformations()
                                          .getLauncher().getNodes();
                        try {
                            td.getExecuterInformations().getLauncher()
                              .terminate();
                        } catch (Exception e) { /* Tested, nothing to do */
                        }
                        try {
                            resourceManager.freeNodes(nodes, td.getPostTask());
                        } catch (Exception e) {
                            try {
                                // try to get the node back to the IM
                                resourceManager.freeNode(td.getExecuterInformations()
                                                           .getNode());
                            } catch (Exception e1) {
                                resourceManager.freeDownNode(td.getExecuterInformations()
                                                               .getNodeName());
                            }
                        }
                    } catch (Exception e) {
                        //do nothing, the task is already terminated.
                    }
                }
            }
        }
        //cleaning all lists
        jobs.clear();
        pendingJobs.clear();
        runningJobs.clear();
        finishedJobs.clear();
        //finally : shutdown
        state = SchedulerState.KILLED;
        logger.info("[SCHEDULER] Scheduler has just been killed !");
        frontend.schedulerKilledEvent();
        return new BooleanWrapper(true);
    }

    /**
     * Pause the job represented by jobId.
     * This method will finish every running tasks of this job, and then pause the job.
     * The job will have to be resumed in order to finish.
     *
     * @param jobId the job to pause.
     * @return true if success, false otherwise.
     */
    public BooleanWrapper pause(JobId jobId) {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        InternalJob job = jobs.get(jobId);
        if (finishedJobs.contains(job)) {
            return new BooleanWrapper(false);
        }
        boolean change = job.setPaused();
        JobEvent event = job.getJobInfo();
        if (change) {
            logger.info("[SCHEDULER] Job " + jobId + " has just been paused !");
        }
        frontend.jobPausedEvent(event);
        //create tasks events list
        updateTaskEventsList(job);
        return new BooleanWrapper(change);
    }

    /**
     * Resume the job represented by jobId.
     * This method will restart every tasks of this job.
     *
     * @param jobId the job to resume.
     * @return true if success, false otherwise.
     */
    public BooleanWrapper resume(JobId jobId) {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        InternalJob job = jobs.get(jobId);
        if (finishedJobs.contains(job)) {
            return new BooleanWrapper(false);
        }
        boolean change = job.setUnPause();
        JobEvent event = job.getJobInfo();
        if (change) {
            logger.info("[SCHEDULER] Job " + jobId +
                " has just been resumed !");
        }
        frontend.jobResumedEvent(event);
        //create tasks events list
        updateTaskEventsList(job);
        return new BooleanWrapper(change);
    }

    /**
     * kill the job represented by jobId.
     * This method will kill every running tasks of this job, and remove it from the scheduler.
     * The job won't be terminated, it won't have result.
     *
     * @param jobId the job to kill.
     * @return true if success, false otherwise.
     */
    public synchronized BooleanWrapper kill(JobId jobId) {
        if ((state == SchedulerState.SHUTTING_DOWN) ||
                (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }
        InternalJob job = jobs.get(jobId);
        jobs.remove(jobId);
        for (InternalTask td : job.getTasks()) {
            if (td.getStatus() == Status.RUNNNING) {
                try {
                    //get the nodes that are used for this descriptor
                    NodeSet nodes = td.getExecuterInformations().getLauncher()
                                      .getNodes();

                    //try to terminate the task
                    try {
                        td.getExecuterInformations().getLauncher().terminate();
                    } catch (Exception e) { /* Tested (nothing to do) */
                    }
                    //free every execution nodes
                    resourceManager.freeNodes(nodes, td.getPostTask());
                } catch (Exception e) {
                    resourceManager.freeNode(td.getExecuterInformations()
                                               .getNode());
                }
            }
        }
        if (runningJobs.remove(job) || pendingJobs.remove(job) ||
                finishedJobs.remove(job)) {
            ;
        }
        logger.info("[SCHEDULER] Job " + jobId + " has just been killed !");
        frontend.jobKilledEvent(jobId);
        AbstractSchedulerDB.getInstance().removeJob(jobId);
        return new BooleanWrapper(true);
    }

    /**
     * Change the priority of the job represented by jobId.
     *
     * @param jobId the job on which to change the priority.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public void changePriority(JobId jobId, JobPriority priority) {
        InternalJob job = jobs.get(jobId);
        job.setPriority(priority);
        frontend.changeJobPriorityEvent(job.getJobInfo());
        AbstractSchedulerDB.getInstance().setJobEvent(job.getJobInfo());
    }

    /**
     * Rebuild the scheduler after a crash.
     * Get data base instance, connect it and ask if a rebuild is needed.
     * The steps to recover the core are visible below.
     */
    private void recover() {
        //connect to data base
        AbstractSchedulerDB dataBase = AbstractSchedulerDB.getInstance();
        RecoverableState recoverable = dataBase.getRecoverableState();
        if (recoverable == null) {
            logger.info("No recoverable state.");
            return;
        }

        // Recover the scheduler core
        //------------------------------------------------------------------------
        //----------------------    Re-build jobs lists  --------------------------
        //------------------------------------------------------------------------
        logger.info("Re-build jobs lists");
        JobId maxId = JobId.makeJobId("0");
        for (InternalJob job : recoverable.getJobs()) {
            jobs.put(job.getId(), job);
            switch (job.getState()) {
            case PENDING:
                pendingJobs.add(job);
                break;
            case STALLED:
            case RUNNING:
                runningJobs.add(job);
                break;
            case FINISHED:
            case CANCELLED:
            case FAILED:
                finishedJobs.add(job);
                break;
            case PAUSED:
                if ((job.getNumberOfPendingTask() +
                        job.getNumberOfRunningTask() +
                        job.getNumberOfFinishedTask()) == 0) {
                    pendingJobs.add(job);
                } else {
                    runningJobs.add(job);
                }
            }

            //search last JobId
            if (job.getId().compareTo(maxId) > 0) {
                maxId = job.getId();
            }
        }
        //------------------------------------------------------------------------
        //--------------------    Initialize jobId count   ----------------------
        //------------------------------------------------------------------------
        logger.info("Initialize jobId count");
        JobId.setInitialValue(maxId);
        //------------------------------------------------------------------------
        //--------    Re-affect JobEvent/taskEvent to the jobs/tasks   -----------
        //------------------------------------------------------------------------
        logger.info("Re-affect JobEvent/taskEvent to the jobs/tasks");
        for (Entry<JobId, JobEvent> entry : recoverable.getJobEvents().entrySet()) {
            jobs.get(entry.getKey()).update(entry.getValue());
        }
        for (Entry<TaskId, TaskEvent> entry : recoverable.getTaskEvents()
                                                         .entrySet()) {
            jobs.get(entry.getKey().getJobId()).update(entry.getValue());
        }

        //------------------------------------------------------------------------
        //------------------    Re-create task dependences   ---------------------
        //------------------------------------------------------------------------
        logger.info("Re-create task dependences");
        for (InternalJob job : runningJobs) {
            ArrayList<InternalTask> tasksList = new ArrayList<InternalTask>();

            //copy the list with only the finished task.
            for (InternalTask task : job.getTasks()) {
                switch (task.getStatus()) {
                case ABORTED:
                case CANCELLED:
                case FAILED:
                case FINISHED:
                    tasksList.add(task);
                }
            }
            //sort the finished task according to their finish time.
            //to be sure to be in the right tree browsing.
            Collections.sort(tasksList, new FinishTimeComparator());
            //simulate the running execution to recreate the tree.
            for (InternalTask task : tasksList) {
                job.simulateStartAndTerminate(task.getId());
            }
        }

        //------------------------------------------------------------------------
        //--------------    Re-set job results list in each job   ----------------
        //------------------------------------------------------------------------
        logger.info("Re-set job results list in each job");
        for (JobResult result : recoverable.getJobResults()) {
            jobs.get(result.getId()).setJobResult(result);
        }
        // Recover the scheduler front-end
        frontend.recover(jobs);
    }

    /**
     * FinishTimeComparator will compare the internal task on their finished time.
     *
     * @author jlscheef - ProActiveTeam
     * @date 25 oct. 07
     * @version 3.2
     *
     */
    private class FinishTimeComparator implements Comparator<InternalTask> {
        @Override
        public int compare(InternalTask o1, InternalTask o2) {
            return (int) (o1.getFinishedTime() - o2.getFinishedTime());
        }
    }
}
