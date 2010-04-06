/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scheduler.common.AdminMethodsInterface;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface_;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UnknowJobException;
import org.ow2.proactive.scheduler.common.exception.UnknowTaskResultException;
import org.ow2.proactive.scheduler.common.job.JobDescriptor;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.policy.Policy;
import org.ow2.proactive.scheduler.common.task.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskDescriptor;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive.scheduler.core.db.Condition;
import org.ow2.proactive.scheduler.core.db.ConditionComparator;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.RunningProcessException;
import org.ow2.proactive.scheduler.exception.StartProcessException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobWrapper;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.task.ExecutableContainerInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;
import org.ow2.proactive.scripting.ScriptException;
import org.ow2.proactive.threading.CallableWithTimeoutAction;
import org.ow2.proactive.threading.ExecutorServiceTasksInvocator;
import org.ow2.proactive.utils.NodeSet;


/**
 * <i><font size="2" color="#FF0000">** Scheduler core ** </font></i>
 * This is the main active object of the scheduler implementation,
 * it communicates with the entity manager to acquire nodes and with a policy
 * to insert and get jobs from the queue.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerCore implements UserSchedulerInterface_, AdminMethodsInterface,
        TaskTerminateNotification, RunActive {

    /**  */
    private static final long serialVersionUID = 200;
    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** Scheduler main loop time out */
    private static final int SCHEDULER_TIME_OUT = PASchedulerProperties.SCHEDULER_TIME_OUT.getValueAsInt();

    /** Scheduler node ping frequency in second. */
    private static final long SCHEDULER_NODE_PING_FREQUENCY = PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY
            .getValueAsInt() * 1000;

    /** Delay to wait for between getting a job result and removing the job concerned */
    private static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    /** Number of time to retry an active object creation if it fails to create */
    private static final int ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER = 3;

    /** Maximum blocking time for the do task action */
    private static final int DOTASK_ACTION_TIMEOUT = PASchedulerProperties.SCHEDULER_STARTTASK_TIMEOUT
            .getValueAsInt();

    /** MAximum number of thread used for the doTask action */
    private static final int DOTASK_ACTION_THREADNUMBER = PASchedulerProperties.SCHEDULER_STARTTASK_THREADNUMBER
            .getValueAsInt();

    /** Delay to wait for a job is terminated and removing it */
    private static final long SCHEDULER_AUTO_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    /** Implementation of Resource Manager */
    private ResourceManagerProxy resourceManager;

    /** Scheduler front-end. */
    private SchedulerFrontend frontend;

    /** Direct link to the current job to submit. */
    private InternalJobWrapper currentJobToSubmit;

    /** Scheduler current policy */
    private Policy policy;

    /** list of all jobs managed by the scheduler */
    private Map<JobId, InternalJob> jobs;

    /** list of pending jobs among the managed jobs */
    private Vector<InternalJob> pendingJobs;

    /** list of running jobs among the managed jobs */
    private Vector<InternalJob> runningJobs;

    /** list of finished jobs among the managed jobs */
    private Vector<InternalJob> finishedJobs;

    /** Scheduler current status */
    private SchedulerStatus status;

    private ExecutorService threadPool;

    /** Thread that will ping the running nodes */
    private Thread pinger;

    /** Timer used for remove result method (transient because Timer is not serializable) */
    private Timer removeJobTimer;
    /** Timer used for restarting tasks */
    private Timer restartTaskTimer;

    /** Log forwarding service for nodes */
    private LogForwardingService lfs;

    /** Jobs that must be logged into the corresponding appenders */
    private Hashtable<JobId, AsyncAppender> jobsToBeLogged;
    /** jobs that must be logged into a file */
    //TODO cdelbe : file are logged on core side...
    private Hashtable<JobId, FileAppender> jobsToBeLoggedinAFile;
    private static final String FILEAPPENDER_SUFFIX = "_FILE";

    /** Currently running tasks for a given jobId*/
    private Hashtable<JobId, Hashtable<TaskId, TaskLauncher>> currentlyRunningTasks;

    /** ClassLoading */
    // contains taskCLassServer for currently running jobs
    private static Hashtable<JobId, TaskClassServer> classServers = new Hashtable<JobId, TaskClassServer>();
    private static Hashtable<JobId, RemoteObjectExposer<TaskClassServer>> remoteClassServers = new Hashtable<JobId, RemoteObjectExposer<TaskClassServer>>();

    /** Dataspaces Naming service */
    private DataSpaceServiceStarter dataSpaceNSStarter;

    /**
     * Return the task classserver for the job jid.<br>
     * return null if the classServer is undefine for the given jobId.
     * 
     * @param jid the job id 
     * @return the task classserver for the job jid
     */
    public static TaskClassServer getTaskClassServer(JobId jid) {
        return classServers.get(jid);
    }

    /**
     * Create a new taskClassServer for the job jid
     * @param jid the job id
     * @param userClasspathJarFile the contents of the classpath as a serialized jar file
     * @param deflateJar if true, the jar file is deflated in the tmpJarFilesDir
     */
    private static void addTaskClassServer(JobId jid, byte[] userClasspathJarFile, boolean deflateJar)
            throws SchedulerException {
        if (getTaskClassServer(jid) != null) {
            throw new SchedulerException("ClassServer already exists for job " + jid);
        }
        try {
            // create remote task classserver 
            logger_dev.info("Create remote task classServer on job '" + jid + "'");
            TaskClassServer localReference = new TaskClassServer(jid);
            RemoteObjectExposer<TaskClassServer> remoteExposer = new RemoteObjectExposer<TaskClassServer>(
                TaskClassServer.class.getName(), localReference);
            URI uri = RemoteObjectHelper.generateUrl(jid.toString());
            RemoteRemoteObject rro = remoteExposer.createRemoteObject(uri);
            // must activate through local ref to avoid copy of the classpath content !
            logger_dev.info("Active local reference");
            localReference.activate(userClasspathJarFile, deflateJar);
            // store references
            classServers.put(jid, (TaskClassServer) new RemoteObjectAdapter(rro).getObjectProxy());
            remoteClassServers.put(jid, remoteExposer);// stored to be unregistered later
        } catch (FileNotFoundException e) {
            logger_dev.error("", e);
            throw new SchedulerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (IOException e) {
            logger_dev.error("", e);
            throw new SchedulerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (UnknownProtocolException e) {
            logger_dev.error("", e);
            throw new SchedulerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (ProActiveException e) {
            logger_dev.error("", e);
            throw new SchedulerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        }
    }

    /**
     * Remove the taskClassServer for the job jid.
     * Delete the classpath associated in SchedulerCore.tmpJarFilesDir.
     * @return true if a taskClassServer has been removed, false otherwise.
     */
    private static boolean removeTaskClassServer(JobId jid) {
        logger_dev.info("Removing TaskClassServer for Job '" + jid + "'");
        // desactivate tcs
        TaskClassServer tcs = classServers.remove(jid);
        if (tcs != null) {
            tcs.desactivate();
        }
        // unexport remote object
        RemoteObjectExposer<TaskClassServer> roe = remoteClassServers.remove(jid);
        if (roe != null) {
            try {
                logger_dev.info("Unregister remote TaskClassServer for Job '" + jid + "'");
                roe.unregisterAll();
            } catch (ProActiveException e) {
                logger.error("Unable to unregister remote taskClassServer because : " + e);
                logger_dev.error("", e);
            }
        }
        return (tcs != null);
    }

    /**
     * Terminate some job handling at the end of a job
     */
    private void terminateJobHandling(final JobId jid) {
        try {
            final SchedulerCore schedulerStub = (SchedulerCore) PAActiveObject.getStubOnThis();
            //remove loggers
            logger_dev.info("Cleaning loggers for Job '" + jid + "'");
            Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jid);
            l.removeAllAppenders();
            this.jobsToBeLogged.remove(jid);
            this.jobsToBeLoggedinAFile.remove(jid);
            // remove current running tasks
            // TODO cdelbe : When a job can be removed on failure ??
            // Other tasks' logs should remain available...
            this.currentlyRunningTasks.remove(jid);
            removeTaskClassServer(jid);
            //auto remove
            if (SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
                try {
                    //remove job after the given delay
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            schedulerStub.remove(jid);
                        }
                    };
                    removeJobTimer.schedule(tt, SCHEDULER_AUTO_REMOVED_JOB_DELAY);
                } catch (Exception e) {
                    logger_dev.error("", e);
                }
            }
        } catch (Throwable t) {
            logger_dev.warn("", t);
        }
    }

    /**
     * ProActive empty constructor
     */
    public SchedulerCore() {
    }

    /**
     * Create a new scheduler Core with the given arguments.<br>
     * 
     * @param imp the resource manager on which the scheduler will interact.
     * @param frontend a reference to the frontend.
     * @param policyFullName the fully qualified name of the policy to be used.
     */
    public SchedulerCore(ResourceManagerProxy imp, SchedulerFrontend frontend, String policyFullName,
            InternalJobWrapper jobSubmitLink) {
        try {
            this.jobs = new HashMap<JobId, InternalJob>();
            this.pendingJobs = new Vector<InternalJob>();
            this.runningJobs = new Vector<InternalJob>();
            this.finishedJobs = new Vector<InternalJob>();
            this.removeJobTimer = new Timer("RemoveJobTimer");
            this.restartTaskTimer = new Timer("RestartTaskTimer");
            this.status = SchedulerStatus.STOPPED;
            this.jobsToBeLogged = new Hashtable<JobId, AsyncAppender>();
            this.jobsToBeLoggedinAFile = new Hashtable<JobId, FileAppender>();
            this.currentlyRunningTasks = new Hashtable<JobId, Hashtable<TaskId, TaskLauncher>>();
            this.threadPool = Executors.newFixedThreadPool(DOTASK_ACTION_THREADNUMBER);

            this.resourceManager = imp;
            this.frontend = frontend;
            this.currentJobToSubmit = jobSubmitLink;
            //loggers
            String providerClassname = PASchedulerProperties.LOGS_FORWARDING_PROVIDER.getValueAsString();
            if (providerClassname == null || providerClassname.equals("")) {
                logger.error("LogForwardingProvider property is not properly set.");
                throw new RuntimeException("LogForwardingProvider property is not properly set.");
            } else {
                this.lfs = new LogForwardingService(providerClassname);
                this.lfs.initialize();
                logger_dev.info("Initialized log forwarding service at " + this.lfs.getServerURI());
            }
            //starting scheduling policy
            this.policy = (Policy) Class.forName(policyFullName).newInstance();
            logger_dev.info("Instanciated policy : " + policyFullName);
            logger.info("Scheduler Core ready !");
        } catch (InstantiationException e) {
            logger.error("The policy class cannot be found : " + e.getMessage());
            logger_dev.error("", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("The method cannot be accessed " + e.getMessage());
            logger_dev.error("", e);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            logger.error("The class definition cannot be found, it might be due to case sentivity : " +
                e.getMessage());
            logger_dev.error("", e);
            throw new RuntimeException(e);
        } catch (LogForwardingException e) {
            logger.error("Cannot initialize the logs forwarding service due to " + e.getMessage());
            logger_dev.error("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the pinger thread to detect unActivity on nodes.
     */
    private void createPingThread() {
        logger_dev.debug("Creating nodes pinging thread");
        pinger = new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(SCHEDULER_NODE_PING_FREQUENCY);

                        if (runningJobs.size() > 0) {
                            logger_dev.info("Ping deployed nodes (Number of running jobs : " +
                                runningJobs.size() + ")");
                            pingDeployedNodes();
                        }
                    } catch (Exception e) {
                        logger_dev.info("", e);
                    }
                }
            }
        };
        logger_dev.info("Starting nodes pinging thread (ping frequency is : " +
            SCHEDULER_NODE_PING_FREQUENCY + "ms )");
        pinger.start();
    }

    /**
     * - Send every changes through the job state update event.<br>
     * - Clear the task info modify status. It is used to change all status of all tasks
     * with only one request. It has to be cleared after sending events.<br>
     * - Store the changes to the data base.
     *
     * @param currentJob the job where the task info are.
     * @param eventType the type of event to send with the job state updated
     */
    private void updateTaskInfosList(InternalJob currentJob, SchedulerEvent eventType) {
        logger_dev.info("Send multiple changes to front-end for job '" + currentJob.getId() + "' (event='" +
            eventType + "')");
        //send event to listeners.
        try {
            frontend.jobStateUpdated(currentJob.getOwner(), new NotificationData<JobInfo>(eventType,
                currentJob.getJobInfo()));
        } catch (Throwable t) {
            //Just to prevent update method error
        }
        // don't forget to set the task status modify to null
        currentJob.setTaskStatusModify(null);
        // used when a job has failed
        currentJob.setTaskFinishedTimeModify(null);
        // and to database
        DatabaseManager.getInstance().synchronize(currentJob.getJobInfo());
        for (TaskState task : currentJob.getTasks()) {
            DatabaseManager.getInstance().synchronize(task.getTaskInfo());
        }
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {

        try {
            //start Dataspace naming service
            dataSpaceNSStarter = new DataSpaceServiceStarter();
            dataSpaceNSStarter.startNamingService();
            //Start DB and rebuild the scheduler if needed.
            recover();
        } catch (Throwable e) {
            ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE).info("Cannot start Scheduler ", e);
            kill();
            return;
        }

        if (status != SchedulerStatus.KILLED) {

            //listen log as immediate Service.
            //PAActiveObject.setImmediateService("listenLog");
            Service service = new Service(body);

            //used to read the enumerate schedulerStatus in order to know when submit is possible.
            //have to be immediate service
            body.setImmediateService("isSubmitPossible", false);
            //Fix SCHEDULING-337, it could remains a better solution to avoid race condition in DB
            //body.setImmediateService("getTaskResult");
            body.setImmediateService("getJobResult", false);
            body.setImmediateService("getJobState", false);
            logger_dev.debug("Core immediate services : isSubmitPossible, getJobResult, getJobState");

            //set the filter for serveAll method (user action are privileged)
            RequestFilter filter = new MainLoopRequestFilter("submit", "terminate", "listenLog",
                "getSchedulerState");
            createPingThread();

            // default scheduler status is started
            ((SchedulerCore) PAActiveObject.getStubOnThis()).start();

            do {
                service.blockingServeOldest();

                while ((status == SchedulerStatus.STARTED) || (status == SchedulerStatus.PAUSED) ||
                    (status == SchedulerStatus.STOPPED)) {
                    try {
                        //block the loop until a method is invoked and serve it
                        service.blockingServeOldest(SCHEDULER_TIME_OUT);
                        //serve all important methods
                        service.serveAll(filter);
                        //schedule
                        schedule();
                    } catch (Throwable e) {
                        //this point is reached in case of big problem, sometimes unknown
                        logger
                                .error(
                                        "\nSchedulerCore.runActivity(MAIN_LOOP) caught an EXCEPTION - it will not terminate the body !",
                                        e);
                        //trying to check if RM is alive
                        try {
                            logger_dev.error("Check if Resource Manager is alive");
                            resourceManager.isAlive();
                        } catch (Exception rme) {
                            logger_dev.error("Resource Manager seems to be dead", rme);
                            try {
                                //try to shutdown the proxy
                                resourceManager.shutdownProxy();
                            } catch (Exception ev) {
                            }
                            //if failed
                            freeze();
                            //scheduler functionality are reduced until now
                            status = SchedulerStatus.UNLINKED;
                            logger
                                    .fatal("\n*****************************************************************************************************************\n"
                                        + "* Resource Manager is no more available, Scheduler has been paused waiting for a resource manager to be reconnect\n"
                                        + "* Scheduler is in critical state and its functionalities are reduced : \n"
                                        + "* \t-> use the linkResourceManager() method to reconnect a new one.\n"
                                        + "*****************************************************************************************************************");
                            frontend.schedulerStateUpdated(SchedulerEvent.RM_DOWN);
                        }
                    }
                }
            } while ((status != SchedulerStatus.SHUTTING_DOWN) && (status != SchedulerStatus.KILLED));

            logger.info("Scheduler is shutting down...");

            if (pendingJobs.size() + runningJobs.size() > 0) {
                logger_dev.info("Unpause all running and pending jobs !");
                for (InternalJob job : jobs.values()) {
                    //finished jobs cannot be paused, so loop on all jobs
                    if (job.getStatus() == JobStatus.PAUSED) {
                        job.setUnPause();

                        //update events list and send event to the frontend
                        updateTaskInfosList(job, SchedulerEvent.JOB_RESUMED);
                    }
                }

                //terminating jobs...
                logger.info("Terminating jobs...");
            }

            while ((runningJobs.size() + pendingJobs.size()) > 0) {
                try {
                    //block the loop until a method is invoked and serve it
                    service.blockingServeOldest(SCHEDULER_TIME_OUT);
                    service.serveAll(filter);
                    schedule();
                } catch (Exception e) {
                    logger_dev.error("", e);
                }
            }

            //stop the pinger thread.
            pinger.interrupt();
        }

        logger.info("Terminating...");
        //shutdown resource manager proxy
        resourceManager.shutdownProxy();
        logger_dev.info("Resource Manager proxy shutdown");

        if (status == SchedulerStatus.SHUTTING_DOWN) {
            frontend.schedulerStateUpdated(SchedulerEvent.SHUTDOWN);
        }

        //destroying scheduler active objects
        frontend.terminate();
        //closing data base
        logger.debug("Closing Scheduler data base !");
        DatabaseManager.getInstance().close();
        //stop dataspace
        dataSpaceNSStarter.terminateNamingService();
        //terminate this active object
        PAActiveObject.terminateActiveObject(false);
        logger.info("Scheduler is now shutdown !");
        //exit
        System.exit(0);
    }

    /**
     * Schedule computing method
     */
    private void schedule() {
        //Number of time to retry an active object creation before leaving scheduling loop
        int activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;

        //get job Descriptor list with eligible jobs (running and pending)
        ArrayList<JobDescriptor> jobDescriptorList = new ArrayList<JobDescriptor>();

        for (InternalJob j : runningJobs) {
            if (j.getJobDescriptor().getEligibleTasks().size() > 0) {
                jobDescriptorList.add(j.getJobDescriptor());
            }
        }

        //if scheduler is paused it only finishes running jobs
        if (status != SchedulerStatus.PAUSED) {
            for (InternalJob j : pendingJobs) {
                if (j.getJobDescriptor().getEligibleTasks().size() > 0) {
                    jobDescriptorList.add(j.getJobDescriptor());
                }
            }
        }

        if (jobDescriptorList.size() > 0) {
            logger_dev.info("Number of jobs containing tasks to be scheduled : " + jobDescriptorList.size());
        }

        //ask the policy all the tasks to be schedule according to the jobs list.
        Vector<EligibleTaskDescriptor> taskRetrivedFromPolicy = policy.getOrderedTasks(jobDescriptorList);

        if (taskRetrivedFromPolicy == null || taskRetrivedFromPolicy.size() == 0) {
            return;
        }

        if (taskRetrivedFromPolicy.size() > 0) {
            logger_dev.info("Number of tasks ready to be scheduled : " + taskRetrivedFromPolicy.size());
        }

        while (!taskRetrivedFromPolicy.isEmpty()) {
            //number of nodes to ask for
            int nbNodesToAskFor = 0;
            RMState rmState = resourceManager.getRMState();
            policy.RMState = rmState;
            int freeResourcesNb = rmState.getNumberOfFreeResources().intValue();
            logger_dev.info("Number of free resources : " + freeResourcesNb);
            if (freeResourcesNb == 0) {
                break;
            }
            int taskToCheck = 0;
            //select first task to define the selection script ID
            TaskDescriptor taskDescriptor = taskRetrivedFromPolicy.get(taskToCheck);
            InternalJob currentJob = jobs.get(taskDescriptor.getJobId());
            InternalTask internalTask = currentJob.getIHMTasks().get(taskDescriptor.getId());
            InternalTask sentinel = internalTask;
            SchedulingTaskComparator referentComp = new SchedulingTaskComparator(internalTask);
            SchedulingTaskComparator currentComp = referentComp;
            logger_dev.debug("Get the most nodes matching the current selection");
            //if free resources are available and (selection script ID and Node Exclusion) are the same as the first
            while (freeResourcesNb > 0 && referentComp.equals(currentComp)) {
                taskDescriptor = taskRetrivedFromPolicy.get(taskToCheck);
                currentJob = jobs.get(taskDescriptor.getJobId());
                internalTask = currentJob.getIHMTasks().get(taskDescriptor.getId());
                //last task to be launched
                sentinel = internalTask;
                if (internalTask.getNumberOfNodesNeeded() > freeResourcesNb) {
                    //TODO what do we want for multi-nodes task?
                    //Improve scheduling policy to avoid starvation
                    break;
                } else {
                    //update number of nodes to ask to the RM
                    nbNodesToAskFor += internalTask.getNumberOfNodesNeeded();
                    freeResourcesNb -= internalTask.getNumberOfNodesNeeded();
                }
                //get next task
                taskToCheck++;
                //if there is no task anymore, break
                if (taskToCheck >= taskRetrivedFromPolicy.size()) {
                    break;
                }
                {
                    //create next comparator
                    TaskDescriptor td = taskRetrivedFromPolicy.get(taskToCheck);
                    InternalTask it = jobs.get(td.getJobId()).getIHMTasks().get(td.getId());
                    currentComp = new SchedulingTaskComparator(it);
                }
            }

            logger.debug("Number of nodes to ask for : " + nbNodesToAskFor);
            NodeSet nodeSet = null;

            if (nbNodesToAskFor > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Referent task              : " + internalTask.getId());
                    logger.debug("Selection script(s)        : " +
                        ((referentComp.getSsHashCode() == 0) ? "no" : "yes (" + referentComp.getSsHashCode() +
                            ")"));
                    logger.debug("Node(s) exclusion          : " + internalTask.getNodeExclusion());
                }

                try {
                    nodeSet = resourceManager.getAtMostNodes(nbNodesToAskFor, internalTask
                            .getSelectionScripts(), internalTask.getNodeExclusion());
                    //the following line is used to unwrap the future, warning when moving or removing
                    //it may also throw a ScriptException which is a RuntimeException
                    logger.debug("Got " + nodeSet.size() + " node(s)");
                } catch (ScriptException e) {
                    Throwable t = e;
                    while (t.getCause() != null) {
                        t = t.getCause();
                    }
                    logger_dev.info("Selection script throws an exception : " + t);
                    //simulate job starting if needed
                    // set the different informations on job
                    if (currentJob.getStartTime() < 0) {
                        // if it is the first task of this job
                        currentJob.start();
                        pendingJobs.remove(currentJob);
                        runningJobs.add(currentJob);
                        //update tasks events list and send it to front-end
                        updateTaskInfosList(currentJob, SchedulerEvent.JOB_PENDING_TO_RUNNING);
                        logger.info("Job '" + currentJob.getId() + "' started");
                    }
                    //selection script has failed : end the job
                    endJob(currentJob, internalTask, "Selection script has failed : " + t, JobStatus.CANCELED);
                    //leave the method to recreate lists of tasks to start
                    return;
                }

            }

            //remove unschedulable task
            if (nbNodesToAskFor <= 0 || nodeSet.size() == 0) {
                //if RM returns 0 nodes, i.e. no nodes satisfy selection script (or no nodes at all)
                //remove these tasks from the tasks list to Schedule, and then prevent infinite loop :
                //always trying to Schedule in vein these tasks (scheduler Core AO stay blocked on this Schedule loop,
                //and can't treat terminate request asked by ended tasks for example).
                //try again to schedule these tasks on next call to schedule() seems reasonable 
                while (!taskRetrivedFromPolicy.get(0).getId().equals(sentinel.getId())) {
                    taskRetrivedFromPolicy.remove(0);
                }
                taskRetrivedFromPolicy.remove(0);
            }

            Node node = null;

            //start selected tasks
            try {
                while (nodeSet != null && !nodeSet.isEmpty()) {
                    taskDescriptor = taskRetrivedFromPolicy.get(0);
                    currentJob = jobs.get(taskDescriptor.getJobId());
                    internalTask = currentJob.getIHMTasks().get(taskDescriptor.getId());

                    // load and Initialize the executable container
                    DatabaseManager.getInstance().load(internalTask);
                    logger_dev.debug("Load and Initialize the executable container for task '" +
                        internalTask.getId() + "'");

                    // Initialize executable container
                    ExecutableContainerInitializer eci = new ExecutableContainerInitializer();
                    // TCS can be null for non-java task
                    eci.setClassServer(getTaskClassServer(currentJob.getId()));
                    internalTask.getExecutableContainer().init(eci);

                    node = nodeSet.get(0);
                    TaskLauncher launcher = null;

                    //enough nodes to be launched at same time for a communicating task
                    if (nodeSet.size() >= internalTask.getNumberOfNodesNeeded()) {

                        //start dataspace app for this job
                        currentJob.startDataSpaceApplication(dataSpaceNSStarter.getNamingService(),
                                dataSpaceNSStarter.getNamingServiceURL());
                        //create launcher
                        launcher = internalTask.createLauncher(currentJob, node);
                        activeObjectCreationRetryTimeNumber = ACTIVEOBJECT_CREATION_RETRY_TIME_NUMBER;
                        this.currentlyRunningTasks.get(internalTask.getJobId()).put(internalTask.getId(),
                                launcher);
                        nodeSet.remove(0);
                        NodeSet nodes = new NodeSet();

                        for (int i = 0; i < (internalTask.getNumberOfNodesNeeded() - 1); i++) {
                            nodes.add(nodeSet.remove(0));
                        }
                        internalTask.getExecuterInformations().addNodes(nodes);

                        // activate loggers for this task if needed
                        if (this.jobsToBeLogged.containsKey(currentJob.getId()) ||
                            this.jobsToBeLoggedinAFile.containsKey(currentJob.getId())) {
                            launcher.activateLogs(this.lfs.getAppenderProvider());
                        }

                        // Set to empty array to emulate varargs behavior (i.e. not defined is
                        // equivalent to empty array, not null.
                        TaskResult[] params = new TaskResult[0];
                        //if job is TASKSFLOW, preparing the list of parameters for this task.
                        int resultSize = taskDescriptor.getParents().size();
                        if ((currentJob.getType() == JobType.TASKSFLOW) && (resultSize > 0)) {
                            params = new TaskResult[resultSize];
                            for (int i = 0; i < resultSize; i++) {
                                //get parent task number i
                                InternalTask parentTask = currentJob.getIHMTasks().get(
                                        taskDescriptor.getParents().get(i).getId());
                                //set the task result in the arguments array.
                                params[i] = currentJob.getJobResult().getResult(parentTask.getName());
                                //if this result has been unloaded, (extremely rare but possible)
                                if (params[i].getOutput() == null) {
                                    //get the result and load the content from database
                                    DatabaseManager.getInstance().load(params[i]);
                                }
                            }
                        }

                        //set nodes in the executable container
                        internalTask.getExecutableContainer().setNodes(nodes);

                        logger_dev.info("Starting deployment of task '" + internalTask.getName() +
                            "' for job '" + currentJob.getId() + "'");

                        //enqueue next instruction, and execute whole process in the thread-pool controller
                        TimedDoTaskAction tdta = new TimedDoTaskAction(internalTask, launcher,
                            (SchedulerCore) PAActiveObject.getStubOnThis(), params);
                        List<Future<TaskResult>> futurResults = ExecutorServiceTasksInvocator
                                .invokeAllWithTimeoutAction(threadPool, Collections.singletonList(tdta),
                                        DOTASK_ACTION_TIMEOUT);

                        //wait for only one result
                        Future<TaskResult> future = futurResults.get(0);
                        if (future.isDone()) {
                            //if task has finished
                            if (future.get() != null) {
                                //and result is not null
                                ((JobResultImpl) currentJob.getJobResult()).storeFuturResult(internalTask
                                        .getName(), future.get());
                                //mark the task and job (if needed) as started and send events
                                finalizeStarting(currentJob, internalTask, node);
                            } else {
                                //if there was a problem, free nodeSet for multi-nodes task (1)
                                throw new RuntimeException("Free nodes 1");
                            }
                        } else {
                            //if there was a problem, free nodeSet for multi-nodes task (2)
                            throw new RuntimeException("Free nodes 2");
                        }

                    }

                    //if everything were OK (or if the task could not be launched, 
                    //removed this task from the processed task.
                    taskRetrivedFromPolicy.remove(0);
                    //if every task that should be launched have been removed
                    if (internalTask == sentinel) {
                        //get back unused nodes to the RManager
                        if (!nodeSet.isEmpty())
                            resourceManager.freeNodes(nodeSet);
                        //and leave the loop
                        break;
                    }
                }

            } catch (ActiveObjectCreationException e1) {
                //Something goes wrong with the active object creation (createLauncher)
                logger.warn("", e1);
                //so try to get back every remaining nodes to the resource manager
                try {
                    resourceManager.freeNodes(nodeSet);
                } catch (Exception e2) {
                    logger_dev.info("Unable to get back the nodeSet to the RM", e2);
                }
                if (--activeObjectCreationRetryTimeNumber == 0) {
                    return;
                }
            } catch (Exception e1) {
                //if we are here, it is that something append while launching the current task.
                //exception can also come from (1) or (2)
                logger.warn("", e1);
                //so try to get back every remaining nodes to the resource manager
                try {
                    nodeSet.add(node);
                    resourceManager.freeNodes(nodeSet);
                } catch (Exception e2) {
                    logger_dev.info("Unable to get back the nodeSet to the RM", e2);
                }
            }

        }

    }

    /**
     * Finalize the start of the task by mark it as started. Also mark the job if it is not already started.
     *
     * @param job the job that owns the task to be started
     * @param task the task to be started
     * @param node the node on which the task will be started
     */
    private void finalizeStarting(InternalJob job, InternalTask task, Node node) {
        logger.info("Task '" + task.getId() + "' started on " +
            node.getNodeInformation().getVMInformation().getHostName());
        // set the different informations on job
        if (job.getStartTime() < 0) {
            // if it is the first task of this job
            job.start();
            pendingJobs.remove(job);
            runningJobs.add(job);
            //update tasks events list and send it to front-end
            updateTaskInfosList(job, SchedulerEvent.JOB_PENDING_TO_RUNNING);
            logger.info("Job '" + job.getId() + "' started");
        }

        // set the different informations on task
        job.startTask(task);
        // send task event to front-end
        frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
            SchedulerEvent.TASK_PENDING_TO_RUNNING, task.getTaskInfo()));
    }

    /**
     * Ping every nodes on which a task is currently running and repair the task if need.
     */
    private void pingDeployedNodes() {
        logger_dev.info("Search for down nodes !");

        for (int i = 0; i < runningJobs.size(); i++) {
            InternalJob job = runningJobs.get(i);

            for (InternalTask td : job.getITasks()) {
                if (td != null && (td.getStatus() == TaskStatus.RUNNING) &&
                    !PAActiveObject.pingActiveObject(td.getExecuterInformations().getLauncher())) {
                    //check if the task has not been terminated while pinging
                    if (td.getStatus() != TaskStatus.RUNNING) {
                        continue;
                    }
                    logger_dev.info("Node failed on job '" + job.getId() + "', task '" + td.getId() + "'");

                    try {
                        logger_dev.info("Try to free failed node set");
                        //free execution node even if it is dead
                        resourceManager.freeNodes(td.getExecuterInformations().getNodes());
                    } catch (Exception e) {
                        //just save the rest of the method execution
                    }

                    td.decreaseNumberOfExecutionOnFailureLeft();
                    DatabaseManager.getInstance().synchronize(td.getTaskInfo());
                    logger_dev.info("Number of retry on Failure left for the task '" + td.getId() + "' : " +
                        td.getNumberOfExecutionOnFailureLeft());
                    if (td.getNumberOfExecutionOnFailureLeft() > 0) {
                        td.setStatus(TaskStatus.WAITING_ON_FAILURE);
                        job.newWaitingTask();
                        frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                            SchedulerEvent.TASK_WAITING_FOR_RESTART, td.getTaskInfo()));
                        job.reStartTask(td);
                        logger_dev.info("Task '" + td.getId() + "' is waiting to restart");
                    } else {
                        endJob(
                                job,
                                td,
                                "An error has occurred due to a node failure and the maximum amout of retries property has been reached.",
                                JobStatus.FAILED);
                        i--;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Return true if a submit is possible, false if not.
     * 
     * @return true if a submit is possible, false if not.
     */
    public boolean isSubmitPossible() {
        return !((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.STOPPED));
    }

    /**
     * Submit a new job to the scheduler.
     * This method will prepare the new job and get it ready for scheduling.<br>
     * It is not possible to submit the job if the Scheduler is stopped
     *
     * @throws SchedulerException if problem occurs during job preparation
     */
    public void submit() throws SchedulerException {
        InternalJob job = currentJobToSubmit.getJob();
        logger_dev.info("Trying to submit new Job '" + job.getId() + "'");
        // TODO cdelbe : create classserver only when job is running ?
        // create taskClassLoader for this job
        if (job.getEnvironment().getJobClasspath() != null) {
            SchedulerCore.addTaskClassServer(job.getId(), job.getEnvironment().getJobClasspathContent(), job
                    .getEnvironment().containsJarFile());
            // if the classserver creation fails, the submit is aborted
        }

        job.submitAction();

        //create job result storage
        JobResult jobResult = new JobResultImpl(job.getId());
        //store the job result until user get it  (MUST BE SET BEFORE DB STORAGE)
        job.setJobResult(jobResult);

        //Add to data base
        DatabaseManager.getInstance().register(job);

        //If register OK : add job to core
        jobs.put(job.getId(), job);
        pendingJobs.add(job);
        logger_dev.info("New job added to Scheduler lists : '" + job.getId() + "'");

        // create a running task table for this job
        this.currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());

        // create appender for this job if required
        createFileAppender(job, false);

        //unload job environment that potentially contains classpath as byte[]
        DatabaseManager.getInstance().unload(job.getEnvironment());
        //unload heavy object
        for (InternalTask it : job.getITasks()) {
            DatabaseManager.getInstance().unload(it);
        }
        logger_dev.info("JobEnvironment and internalTask unloaded for job '" + job.getId() + "'");
        frontend.jobSubmitted(job);
    }

    /**
     * End the given job due to the given task failure.
     *
     * @param job the job to end.
     * @param task the task who has been the caused of failing. **This argument can be null only if jobStatus is killed**
     * @param errorMsg the error message to send in the task result.
     * @param jobStatus the type of the end for this job. (failed/canceled/killed)
     */
    private void endJob(InternalJob job, InternalTask task, String errorMsg, JobStatus jobStatus) {
        TaskResult taskResult = null;

        if (task != null) {
            logger_dev.info("Job ending request for job '" + job.getId() + "' - cause by task '" +
                task.getId() + "' - status : " + jobStatus);
        } else {
            logger_dev.info("Job ending request for job '" + job.getId() + "' - status : " + jobStatus);
        }

        for (InternalTask td : job.getITasks()) {
            if (td.getStatus() == TaskStatus.RUNNING) {
                //get the nodes that are used for this descriptor
                NodeSet nodes = td.getExecuterInformations().getNodes();

                //try to terminate the task
                try {
                    logger_dev.info("Force terminating task '" + td.getId() + "'");
                    td.getExecuterInformations().getLauncher().terminate();
                } catch (Exception e) { /* (nothing to do) */
                }

                try {
                    //free every execution nodes
                    resourceManager.freeNodes(nodes, td.getCleaningScript());
                } catch (Exception e) {
                    try {
                        // try to get the node back to the RM
                        resourceManager.freeNodes(td.getExecuterInformations().getNodes());
                    } catch (Exception e1) {
                    }
                }

                //If not killed
                if (jobStatus != JobStatus.KILLED) {
                    //deleting tasks results except the one that causes the error
                    if (!td.getId().equals(task.getId())) {
                        job.getJobResult().removeResult(td.getName());
                    }
                    //if canceled, get the result of the canceled task
                    if ((jobStatus == JobStatus.CANCELED) && td.getId().equals(task.getId())) {
                        try {
                            taskResult = job.getJobResult().getResult(task.getName());
                        } catch (RuntimeException e) {
                            //should never append
                            logger_dev.error("", e);
                        }
                    }
                }
            }
        }

        //if job has been killed
        if (jobStatus == JobStatus.KILLED) {
            job.failed(null, jobStatus);
            //the next line will try to remove job from each list.
            //once removed, it won't be removed from remaining list, but we ensure that the job is in only one of the list.
            if (runningJobs.remove(job) || pendingJobs.remove(job)) {
                finishedJobs.add(job);
            }
        } else {
            //if not killed
            job.failed(task.getId(), jobStatus);

            //store the exception into jobResult / To prevent from empty task result (when job canceled), create one
            boolean noResult = (jobStatus == JobStatus.CANCELED && taskResult == null);
            if (jobStatus == JobStatus.FAILED || noResult) {
                taskResult = new TaskResultImpl(task.getId(), new Exception(errorMsg), new SimpleTaskLogs("",
                    errorMsg), -1, null);
                ((JobResultImpl) job.getJobResult()).addTaskResult(task.getName(), taskResult, task
                        .isPreciousResult());
            } else if (jobStatus == JobStatus.CANCELED) {
                taskResult = (TaskResult) PAFuture.getFutureValue(taskResult);
                ((JobResultImpl) job.getJobResult()).addTaskResult(task.getName(), taskResult, task
                        .isPreciousResult());
            }

            //add the result in database
            DatabaseManager.getInstance().update(job.getJobResult());
            //unload the result to improve memory usage
            DatabaseManager.getInstance().unload(taskResult);
            //move the job from running to finished
            runningJobs.remove(job);
            finishedJobs.add(job);

            if (!noResult) {
                //send task event if there was a result
                frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                    SchedulerEvent.TASK_RUNNING_TO_FINISHED, task.getTaskInfo()));
            }
        }

        terminateJobHandling(job.getId());

        //update job and tasks events list and send it to front-end
        updateTaskInfosList(job, SchedulerEvent.JOB_RUNNING_TO_FINISHED);

        logger.info("Job '" + job.getId() + "' terminated (" + jobStatus + ")");
    }

    /**
     * Invoke by a task when it is about to finish.
     * This method can be invoke just a little amount of time before the result arrival.
     * That's why it can block the execution but only for short time.
     *
     * @param taskId the identification of the executed task.
     */
    public void terminate(TaskId taskId) {
        boolean hasBeenReleased = false;
        int nativeIntegerResult = 0;
        JobId jobId = taskId.getJobId();
        logger_dev.info("Received terminate task request for task '" + taskId + "' - job '" + jobId + "'");
        InternalJob job = jobs.get(jobId);

        //if job has been canceled or failed, it is possible that a task has finished just before
        //the failure of the job. In this rare case, the job may not exist anymore.
        if (job == null) {
            logger_dev.info("Job '" + jobId + "' does not exist anymore");
            return;
        }

        InternalTask descriptor = job.getIHMTasks().get(taskId);
        // job might have already been removed if job has failed...
        Hashtable<TaskId, TaskLauncher> runningTasks = this.currentlyRunningTasks.get(jobId);
        if (runningTasks != null) {
            runningTasks.remove(taskId);
        } else {
            logger_dev.error("RunningTasks list was null, This is an abnormal case");
            return;
        }
        try {
            //first unload the executable container that we don't need until next execution (if re-execution)
            DatabaseManager.getInstance().unload(descriptor);
            //The task is terminated but it's possible to have to
            //wait for the future of the task result (TaskResult).
            //accessing to the taskResult could block current execution but for a very little time.
            //it is the time between the end of the task and the arrival of the future from the task.
            //
            //check if the task result future has an error due to node death.
            //if the node has died, a runtimeException is sent instead of the result
            TaskResult tmp = ((JobResultImpl) job.getJobResult()).getAnyResult(descriptor.getName());
            //unwrap future
            TaskResultImpl res = (TaskResultImpl) PAFuture.getFutureValue(tmp);
            logger_dev.info("Task '" + taskId + "' futur result unwrapped");

            updateTaskIdReferences(res, descriptor.getId());

            if (res != null) {
                // HANDLE DESCRIPTORS
                res.setPreviewerClassName(descriptor.getResultPreview());
                res.setJobClasspath(job.getEnvironment().getJobClasspath()); // can be null
                if (PAException.isException(res)) {
                    //in this case, it is a node error. (should never come)
                    //this is not user exception or usage,
                    //so we restart independently of user or admin execution property
                    logger_dev.info("Node failed on job '" + jobId + "', task '" + taskId + "'");
                    //change status and update GUI
                    descriptor.setStatus(TaskStatus.WAITING_ON_FAILURE);
                    job.newWaitingTask();
                    frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                        SchedulerEvent.TASK_WAITING_FOR_RESTART, descriptor.getTaskInfo()));
                    job.reStartTask(descriptor);
                    //update job and task info
                    DatabaseManager.getInstance().startTransaction();
                    DatabaseManager.getInstance().synchronize(job.getJobInfo());
                    DatabaseManager.getInstance().synchronize(descriptor.getTaskInfo());
                    DatabaseManager.getInstance().commitTransaction();
                    //free execution node even if it is dead
                    try {
                        resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes());
                        hasBeenReleased = true;
                    } catch (Exception e) {
                        //save the return
                    }
                    return;
                }
            }

            logger.info("Task '" + taskId + "' on job '" + jobId + "' terminated");

            //Check if an exception or error occurred during task execution...
            boolean errorOccurred = false;
            if (descriptor instanceof InternalNativeTask) {
                logger_dev.debug("Terminated task '" + taskId + "' is a native task");
                try {
                    // try to get the result, res.value can throw an exception,
                    // it means that the process has failed before the end.
                    nativeIntegerResult = ((Integer) res.value());
                    // an error occurred if res is not 0
                    errorOccurred = (nativeIntegerResult != 0);
                } catch (RunningProcessException rpe) {
                    //if res.value throws a RunningProcessException, user is not responsible
                    //change status and update GUI
                    descriptor.setStatus(TaskStatus.WAITING_ON_FAILURE);
                    job.newWaitingTask();
                    frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                        SchedulerEvent.TASK_WAITING_FOR_RESTART, descriptor.getTaskInfo()));
                    job.reStartTask(descriptor);
                    //update job and task info
                    DatabaseManager.getInstance().startTransaction();
                    DatabaseManager.getInstance().synchronize(job.getJobInfo());
                    DatabaseManager.getInstance().synchronize(descriptor.getTaskInfo());
                    DatabaseManager.getInstance().commitTransaction();
                    //free execution node even if it is dead
                    resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes(), descriptor
                            .getCleaningScript());
                    hasBeenReleased = true;
                    return;
                } catch (StartProcessException spe) {
                    //if res.value throws a StartProcessException, it can be due to an IOException thrown by the process
                    //ie:command not found
                    //just note that an error occurred.
                    errorOccurred = true;
                } catch (Throwable e) {
                    //in any other case, note that an error occurred but the user must be informed.
                    errorOccurred = true;
                }
            } else {
                logger_dev.debug("Terminated task '" + taskId + "' is a java task");
                errorOccurred = res.hadException();
            }

            logger_dev
                    .info("Task '" + taskId + "' terminated with" + (errorOccurred ? "" : "out") + " error");

            //if an error occurred
            if (errorOccurred) {
                //the task threw an exception OR the result is an error code (1-255)
                //if the task has to restart
                descriptor.decreaseNumberOfExecutionLeft();
                //check the number of execution left and fail the job if it is cancelOnError
                if (descriptor.getNumberOfExecutionLeft() <= 0 && descriptor.isCancelJobOnError()) {
                    //if no rerun left, failed the job
                    endJob(job, descriptor,
                            "An error occurred in your task and the maximum number of executions has been reached. "
                                + "You also ask to cancel the job in such a situation !", JobStatus.CANCELED);
                    return;
                }
                if (descriptor.getNumberOfExecutionLeft() > 0) {
                    logger_dev.debug("Node Exclusion : restart mode is '" +
                        descriptor.getRestartTaskOnError() + "'");
                    if (descriptor.getRestartTaskOnError().equals(RestartMode.ELSEWHERE)) {
                        //if the task restart ELSEWHERE
                        descriptor.setNodeExclusion(descriptor.getExecuterInformations().getNodes());
                    }
                    try {
                        resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes(), descriptor
                                .getCleaningScript());
                        hasBeenReleased = true;
                    } catch (Exception e) {
                        logger_dev.error("", e);
                        //cannot get back the node, RM take care about that.
                    }
                    //change status and update GUI
                    descriptor.setStatus(TaskStatus.WAITING_ON_ERROR);
                    job.newWaitingTask();

                    //store this task result in the job result.
                    ((JobResultImpl) job.getJobResult()).addTaskResult(descriptor.getName(), res, descriptor
                            .isPreciousResult());
                    //and update database
                    //update job and task info
                    DatabaseManager.getInstance().startTransaction();
                    DatabaseManager.getInstance().synchronize(job.getJobInfo());
                    DatabaseManager.getInstance().synchronize(descriptor.getTaskInfo());
                    DatabaseManager.getInstance().update(job.getJobResult());
                    DatabaseManager.getInstance().commitTransaction();
                    //send event to user
                    frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                        SchedulerEvent.TASK_WAITING_FOR_RESTART, descriptor.getTaskInfo()));

                    //the task is not restarted immediately
                    RestartJobTimerTask jtt = new RestartJobTimerTask(job, descriptor);
                    restartTaskTimer.schedule(jtt, job.getNextWaitingTime(descriptor
                            .getMaxNumberOfExecution() -
                        descriptor.getNumberOfExecutionLeft()));

                    return;
                }
            }

            //store this task result in the job result.
            ((JobResultImpl) job.getJobResult()).addTaskResult(descriptor.getName(), res, descriptor
                    .isPreciousResult());
            logger_dev.info("TaskResult added to job '" + job.getId() + "' - task name is '" +
                descriptor.getName() + "'");
            //to be done before terminating the task, once terminated it is not running anymore..
            TaskDescriptor currentTD = job.getRunningTaskDescriptor(taskId);
            descriptor = job.terminateTask(errorOccurred, taskId);

            //and update database
            DatabaseManager.getInstance().startTransaction();
            DatabaseManager.getInstance().synchronize(job.getJobInfo());
            DatabaseManager.getInstance().synchronize(descriptor.getTaskInfo());
            DatabaseManager.getInstance().update(job.getJobResult());
            DatabaseManager.getInstance().commitTransaction();

            //clean the result to improve memory usage
            if (!job.getJobDescriptor().hasChildren(descriptor.getId())) {
                DatabaseManager.getInstance().unload(res);
            }
            if (currentTD != null) {
                for (TaskDescriptor td : currentTD.getParents()) {
                    if (td.getChildrenCount() == 0) {
                        try {
                            DatabaseManager.getInstance().unload(
                                    job.getJobResult().getResult(td.getId().getReadableName()));
                        } catch (RuntimeException e) {
                            //should never append
                            logger_dev.error("", e);
                        }
                    }
                }
            }
            //send event
            frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                SchedulerEvent.TASK_RUNNING_TO_FINISHED, descriptor.getTaskInfo()));

            //if this job is finished (every task have finished)
            logger_dev.info("Number of finished tasks : " + job.getNumberOfFinishedTasks() +
                " - Number of tasks : " + job.getTotalNumberOfTasks());
            if (job.getNumberOfFinishedTasks() == job.getTotalNumberOfTasks()) {
                //terminating job
                job.terminate();
                runningJobs.remove(job);
                finishedJobs.add(job);
                logger.info("Job '" + jobId + "' terminated");

                terminateJobHandling(job.getId());

                //and to data base
                DatabaseManager.getInstance().synchronize(job.getJobInfo());
                //clean every task result
                for (TaskResult tr : job.getJobResult().getAllResults().values()) {
                    DatabaseManager.getInstance().unload(tr);
                }
                //send event to client
                frontend.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
                    SchedulerEvent.JOB_RUNNING_TO_FINISHED, job.getJobInfo()));
            }

            //free every execution nodes in the finally

        } catch (NullPointerException eNull) {
            logger_dev.error("", eNull);
            //avoid race condition between kill and terminate task
            //the task has been killed. Nothing to do anymore with this one.
            //should never happen
        } finally {
            if (!hasBeenReleased) {
                //free every execution nodes
                resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes(), descriptor
                        .getCleaningScript());
            }
        }
    }

    /**
     * For Hibernate use : a Hibernate session cannot accept two different java objects with the same
     * Hibernate identifier.
     * To avoid this duplicate object (due to serialization),
     * this method will join taskId references in the Job result graph object.
     *
     * @param jobResult the result in which to join cross dependences
     * @param res the current result to check. (avoid searching for any)
     * @param id the taskId reference known by the Scheduler
     */
    private void updateTaskIdReferences(TaskResult res, TaskId id) {
        try {
            logger_dev.info("TaskResult : " + res.getTaskId());
            //find the taskId field
            for (Field f : TaskResultImpl.class.getDeclaredFields()) {
                if (f.getType().equals(TaskId.class)) {
                    f.setAccessible(true);
                    //set to the existing reference
                    f.set(res, id);
                    break;
                }
            }
        } catch (Exception e) {
            logger_dev.error("", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public SchedulerState getSchedulerState() {
        SchedulerStateImpl sState = new SchedulerStateImpl();
        sState.setPendingJobs(convert(pendingJobs));
        sState.setRunningJobs(convert(runningJobs));
        sState.setFinishedJobs(convert(finishedJobs));
        sState.setState(status);
        return sState;
    }

    private Vector<JobState> convert(Vector<InternalJob> jobs) {
        Vector<JobState> jobs2 = new Vector<JobState>();
        for (InternalJob j : jobs) {
            jobs2.add(j);
        }
        return jobs2;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#listenLog(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, AppenderProvider appenderProvider) throws SchedulerException {
        logger_dev.info("listen logs of job '" + jobId + "'");
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId);

        // create the appender to the remote listener
        Appender appender = null;
        try {
            appender = appenderProvider.getAppender();
        } catch (LogForwardingException e1) {
            logger.error("Cannot create an appender for job " + jobId, e1);
            logger_dev.error("", e1);
            throw new SchedulerException("Cannot create an appender for job " + jobId, e1);
        }

        // targeted job
        InternalJob target = this.jobs.get(jobId);

        // get or create appender for the targeted job
        AsyncAppender jobAppender = this.jobsToBeLogged.get(jobId);
        if (jobAppender == null) {
            jobAppender = new AsyncAppender();
            jobAppender.setName(Log4JTaskLogs.JOB_APPENDER_NAME);
            this.jobsToBeLogged.put(jobId, jobAppender);
            l.setAdditivity(false);
            l.addAppender(jobAppender);
        }
        // should add the appender before activating logs on running tasks !
        jobAppender.addAppender(appender);

        // handle finished jobs
        if ((target != null) && this.finishedJobs.contains(target)) {
            logger_dev.info("listen logs of job '" + jobId + "' : job is already finished");
            // for finished tasks, add logs events "manually"
            Collection<TaskResult> allRes = target.getJobResult().getAllResults().values();
            for (TaskResult tr : allRes) {
                this.flushTaskLogs(tr, l, appender);
            }
            // as the job is finished, close appenders
            logger_dev.info("Cleaning loggers for already finished job '" + jobId + "'");
            l.removeAllAppenders(); // close appenders...
            this.jobsToBeLogged.remove(jobId);

            // job is not finished, tasks are running
        } else if ((target != null) && !this.pendingJobs.contains(target)) {
            // this jobs contains running and finished tasks

            // for finished tasks, add logs events "manually"
            Collection<TaskResult> allRes = target.getJobResult().getAllResults().values();
            for (TaskResult tr : allRes) {
                this.flushTaskLogs(tr, l, appender);
            }

            // for running tasks, activate loggers on taskLauncher side
            Hashtable<TaskId, TaskLauncher> curRunning = this.currentlyRunningTasks.get(jobId);
            // for running tasks
            if (curRunning != null) {
                for (TaskId tid : curRunning.keySet()) {
                    try {
                        TaskLauncher tl = curRunning.get(tid);
                        tl.activateLogs(this.lfs.getAppenderProvider());
                    } catch (LogForwardingException e) {
                        logger.error("Cannot create an appender provider for task " + tid, e);
                        logger_dev.error("", e);
                    }
                }
            }
        }
        // nothing to do for pending jobs (bufferFoJobId is not null)
    }

    private void flushTaskLogs(TaskResult tr, Logger l, Appender a) {
        // if taskResult is not awaited, task is terminated
        TaskLogs logs = null;
        // try to look in the DB
        DatabaseManager.getInstance().load(tr);
        logs = tr.getOutput();
        // avoid race condition if any...
        if (logs == null) {
            // the logs has been deleted and stored in the DB during the previous getOutput
            // should not be null now !
            DatabaseManager.getInstance().load(tr);
            logs = tr.getOutput();
        }
        if (logs instanceof Log4JTaskLogs) {
            for (LoggingEvent le : ((Log4JTaskLogs) logs).getAllEvents()) {
                // write into socket appender directly to avoid double lines on other listeners
                a.doAppend(le);
            }
        } else {
            l.info(logs.getStdoutLogs(false));
            l.error(logs.getStderrLogs(false));
        }
    }

    /**
     * This method creates an file appender for a job if required, i.e. if logfile property is set.
     * @param job the job to create the appender for.
     * @param append whether to append or truncate the job's log file.
     */
    private void createFileAppender(InternalJob job, boolean append) {
        if (job.getLogFile() != null) {
            logger_dev.info("Create logger for job '" + job.getId() + "'");
            Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + job.getId());
            l.setAdditivity(false);
            if (l.getAppender(Log4JTaskLogs.JOB_APPENDER_NAME + FILEAPPENDER_SUFFIX) == null) {
                try {
                    FileAppender fa = new FileAppender(Log4JTaskLogs.getTaskLogLayout(), job.getLogFile(),
                        append);
                    fa.setName(Log4JTaskLogs.JOB_APPENDER_NAME + FILEAPPENDER_SUFFIX);
                    l.addAppender(fa);
                    this.jobsToBeLoggedinAFile.put(job.getId(), fa);
                } catch (IOException e) {
                    logger.warn("Cannot open log file " + job.getLogFile() + " : " + e.getMessage());
                }
            }
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) throws SchedulerException {
        final InternalJob job = jobs.get(jobId);
        final SchedulerCore schedulerStub = (SchedulerCore) PAActiveObject.getStubOnThis();

        if (job == null) {
            throw new UnknowJobException("The job " + jobId + " does not exist !");
        }

        logger_dev.info("Trying to get JobResult of job '" + jobId + "'");
        //result = null if not in DB (ie: not yet available)
        JobResult result = null;
        try {
            result = DatabaseManager.getInstance().recover(job.getJobResult().getClass(),
                    new Condition("id", ConditionComparator.EQUALS_TO, job.getJobResult().getJobId())).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new SchedulerException("Cannot retrieve the result of job '" + jobId + "' !", e);
        }

        try {
            if (!job.getJobInfo().isToBeRemoved() && SCHEDULER_REMOVED_JOB_DELAY > 0) {

                //remember that this job is to be removed
                job.setToBeRemoved();
                DatabaseManager.getInstance().synchronize(job.getJobInfo());

                try {
                    //remove job after the given delay
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            schedulerStub.remove(job.getId());
                        }
                    };
                    removeJobTimer.schedule(tt, SCHEDULER_REMOVED_JOB_DELAY);
                    logger_dev.info("Job '" + jobId + "' will be removed in " +
                        (SCHEDULER_REMOVED_JOB_DELAY / 1000) + "sec");
                } catch (Exception e) {
                    logger_dev.error("", e);
                }
            }

            if (result != null) {
                ((JobResultImpl) result).setJobInfo(job.getJobInfo());
            }
            return result;
        } catch (Throwable t) {
            logger.warn("Thrown to user", t);
            throw new SchedulerException(t);
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws SchedulerException {
        logger_dev.info("Trying to get TaskResult of task '" + taskName + "' for job '" + jobId + "'");
        InternalJob job = jobs.get(jobId);

        if (job == null) {
            logger_dev.info("Job '" + jobId + "' does not exist");
            throw new UnknowJobException("The job does not exist !");
        }

        //extract taskResult reference from memory (weak instance)
        //useful to get the task result with the task name
        TaskResult result = ((JobResultImpl) job.getJobResult()).getResult(taskName);
        if (result == null) {
            //the task is unknown
            logger_dev.info("Result of task " + taskName + " does not exist !");
            throw new UnknowTaskResultException("Result of task " + taskName + " does not exist !");
        }
        if (PAFuture.isAwaited(result)) {
            //the result is not yet available
            logger_dev.info("Task '" + taskName + "' is running");
            return null;
        }
        //extract full taskResult from DB
        //use the previous result to get the task Id matching the given name.
        //extract full copy from DB to avoid load, unload operation
        //Hibernate hold every taskResults even the faulty one, can be interesting to have history
        //So get the result of the task is getting the last one.
        List<? extends TaskResult> results = DatabaseManager.getInstance().recover(result.getClass(),
                new Condition("id", ConditionComparator.EQUALS_TO, result.getTaskId()));
        result = results.get(results.size() - 1);

        if ((result != null)) {
            logger_dev.info("Get '" + taskName + "' task result for job '" + jobId + "'");
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) {
        InternalJob job = jobs.get(jobId);

        logger_dev.info("Request to remove job '" + jobId + "'");

        if (job != null && finishedJobs.contains(job)) {
            jobs.remove(jobId);
            job.setRemovedTime(System.currentTimeMillis());
            finishedJobs.remove(job);
            //and to data base
            DatabaseManager.getInstance().synchronize(job.getJobInfo());
            // close log buffer
            AsyncAppender jobLog = this.jobsToBeLogged.remove(jobId);
            if (jobLog != null) {
                jobLog.close();
            }
            FileAppender jobFile = this.jobsToBeLoggedinAFile.remove(jobId);
            if (jobFile != null) {
                jobFile.close();
            }
            //remove from DataBase
            boolean rfdb = PASchedulerProperties.JOB_REMOVE_FROM_DB.getValueAsBoolean();
            logger_dev.info("Remove job '" + jobId + "' also from  dataBase : " + rfdb);
            if (rfdb) {
                DatabaseManager.getInstance().delete(job);
            }
            logger.info("Job " + jobId + " removed !");
            //send event to front-end
            frontend.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
                SchedulerEvent.JOB_REMOVE_FINISHED, job.getJobInfo()));
        } else {
            logger_dev.info("Job '" + jobId + "' has already been removed or is not finished !");
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#start()
     */
    public BooleanWrapper start() {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if (status != SchedulerStatus.STOPPED) {
            return new BooleanWrapper(false);
        }

        status = SchedulerStatus.STARTED;
        logger.info("Scheduler has just been started !");
        frontend.schedulerStateUpdated(SchedulerEvent.STARTED);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#stop()
     */
    public BooleanWrapper stop() {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.STOPPED) || (status == SchedulerStatus.SHUTTING_DOWN) ||
            (status == SchedulerStatus.KILLED)) {
            return new BooleanWrapper(false);
        }

        status = SchedulerStatus.STOPPED;
        logger.info("Scheduler has just been stopped, no tasks will be launched until start.");
        frontend.schedulerStateUpdated(SchedulerEvent.STOPPED);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#pause()
     */
    public BooleanWrapper pause() {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.KILLED)) {
            return new BooleanWrapper(false);
        }

        if ((status != SchedulerStatus.FROZEN) && (status != SchedulerStatus.STARTED)) {
            return new BooleanWrapper(false);
        }

        status = SchedulerStatus.PAUSED;
        logger.info("Scheduler has just been paused !");
        frontend.schedulerStateUpdated(SchedulerEvent.PAUSED);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#freeze()
     */
    public BooleanWrapper freeze() {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.KILLED)) {
            return new BooleanWrapper(false);
        }

        if ((status != SchedulerStatus.PAUSED) && (status != SchedulerStatus.STARTED)) {
            return new BooleanWrapper(false);
        }

        status = SchedulerStatus.FROZEN;
        logger.info("Scheduler has just been frozen !");
        frontend.schedulerStateUpdated(SchedulerEvent.FROZEN);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#resume()
     */
    public BooleanWrapper resume() {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.KILLED)) {
            return new BooleanWrapper(false);
        }

        if ((status != SchedulerStatus.PAUSED) && (status != SchedulerStatus.FROZEN)) {
            return new BooleanWrapper(false);
        }

        status = SchedulerStatus.STARTED;
        logger.info("Scheduler has just been resumed !");
        frontend.schedulerStateUpdated(SchedulerEvent.RESUMED);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#shutdown()
     */
    public BooleanWrapper shutdown() {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.KILLED) || (status == SchedulerStatus.SHUTTING_DOWN)) {
            return new BooleanWrapper(false);
        }

        status = SchedulerStatus.SHUTTING_DOWN;
        logger.info("Scheduler is shutting down, this make take time to finish every jobs !");
        frontend.schedulerStateUpdated(SchedulerEvent.SHUTTING_DOWN);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#kill()
     */
    public synchronized BooleanWrapper kill() {
        if (status == SchedulerStatus.KILLED) {
            return new BooleanWrapper(false);
        }

        logger_dev.info("Killing all running task processes...");
        //destroying running active object launcher
        for (InternalJob j : runningJobs) {
            for (InternalTask td : j.getITasks()) {
                if (td.getStatus() == TaskStatus.RUNNING) {
                    try {
                        NodeSet nodes = td.getExecuterInformations().getNodes();

                        try {
                            td.getExecuterInformations().getLauncher().terminate();
                        } catch (Exception e) {
                            /* Tested, nothing to do */
                            logger_dev.error("", e);
                        }

                        try {
                            resourceManager.freeNodes(nodes, td.getCleaningScript());
                        } catch (Exception e) {
                            try {
                                // try to get the node back to the IM
                                resourceManager.freeNodes(td.getExecuterInformations().getNodes());
                            } catch (Exception e1) {
                            }
                        }
                    } catch (Exception e) {
                        //do nothing, the task is already terminated.
                        logger_dev.error("", e);
                    }
                }
            }
        }

        logger_dev.info("Cleaning all lists...");
        //cleaning all lists
        jobs.clear();
        pendingJobs.clear();
        runningJobs.clear();
        finishedJobs.clear();
        jobsToBeLogged.clear();
        jobsToBeLoggedinAFile.clear();
        currentlyRunningTasks.clear();
        logger_dev.info("Terminating logging service...");
        if (this.lfs != null) {
            try {
                this.lfs.terminate();
            } catch (LogForwardingException e) {
                logger.error("Cannot terminate logging service : " + e.getMessage());
                logger_dev.error("", e);
            }
        }
        //finally : shutdown
        status = SchedulerStatus.KILLED;
        logger.info("Scheduler has just been killed !");
        frontend.schedulerStateUpdated(SchedulerEvent.KILLED);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.KILLED)) {
            return new BooleanWrapper(false);
        }

        InternalJob job = jobs.get(jobId);

        if (finishedJobs.contains(job)) {
            return new BooleanWrapper(false);
        }

        boolean change = job.setPaused();

        if (change) {
            logger.debug("Job " + jobId + " has just been paused !");
        }

        //update tasks events list and send it to front-end
        updateTaskInfosList(job, SchedulerEvent.JOB_PAUSED);

        return new BooleanWrapper(change);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#resume(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.KILLED)) {
            return new BooleanWrapper(false);
        }

        InternalJob job = jobs.get(jobId);

        if (finishedJobs.contains(job)) {
            return new BooleanWrapper(false);
        }

        boolean change = job.setUnPause();

        if (change) {
            logger.debug("Job " + jobId + " has just been resumed !");
        }

        //update tasks events list and send it to front-end
        updateTaskInfosList(job, SchedulerEvent.JOB_RESUMED);

        return new BooleanWrapper(change);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#kill(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public synchronized BooleanWrapper kill(JobId jobId) {
        if (status == SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if (status == SchedulerStatus.KILLED) {
            return new BooleanWrapper(false);
        }

        logger_dev.info("Request sent to kill job '" + jobId + "'");

        InternalJob job = jobs.get(jobId);

        if (job == null || job.getStatus() == JobStatus.KILLED) {
            return new BooleanWrapper(false);
        }

        endJob(job, null, "", JobStatus.KILLED);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#changePriority(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) {
        logger_dev
                .info("Request sent to change priority on job '" + jobId + "' - new priority : " + priority);
        InternalJob job = jobs.get(jobId);
        job.setPriority(priority);
        DatabaseManager.getInstance().synchronize(job.getJobInfo());
        frontend.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
            SchedulerEvent.JOB_CHANGE_PRIORITY, job.getJobInfo()));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.UserSchedulerInterface_#getJobState(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobState getJobState(JobId jobId) throws SchedulerException {
        logger_dev.info("Request sent to get the State of job '" + jobId + "'");
        return jobs.get(jobId);
    }

    /**
     * Change the policy of the scheduler.<br>
     * This method will immediately change the policy and so the whole scheduling process.
     *
     * @param newPolicyFile the new policy file as a string.
     * @return true if the policy has been correctly change, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper changePolicy(Class<? extends Policy> newPolicyFile) throws SchedulerException {
        try {
            policy = newPolicyFile.newInstance();
            frontend.schedulerStateUpdated(SchedulerEvent.POLICY_CHANGED);
            logger_dev.info("New policy changed ! new policy name : " + newPolicyFile.getName());
        } catch (InstantiationException e) {
            logger_dev.error("", e);
            throw new SchedulerException("Exception occurs while instanciating the policy !");
        } catch (IllegalAccessException e) {
            logger_dev.error("", e);
            throw new SchedulerException("Exception occurs while accessing the policy !");
        }

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.AdminMethodsInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        //only if unlink
        if (status != SchedulerStatus.UNLINKED) {
            return new BooleanWrapper(false);
        }
        try {
            ResourceManagerProxy imp = ResourceManagerProxy.getProxy(new URI(rmURL.trim()));
            //re-link the RM
            resourceManager = imp;
            logger
                    .info("New resource manager has been linked to the scheduler.\n\t-> Resume to continue the scheduling.");
            frontend.schedulerStateUpdated(SchedulerEvent.RM_UP);
            //restart the scheduler
            status = SchedulerStatus.STARTED;
            frontend.schedulerStateUpdated(SchedulerEvent.STARTED);
            return new BooleanWrapper(true);
        } catch (Exception e) {
            throw new SchedulerException("Error while connecting the new Resource Manager !", e);
        }
    }

    /**
     * Rebuild the scheduler after a crash.
     * Get data base instance, connect it and ask if a rebuild is needed.
     * The steps to recover the core are visible below.
     */
    private void recover() {
        //Start Hibernate
        logger.info("Starting Hibernate...");
        boolean drop = PASchedulerProperties.SCHEDULER_DB_HIBERNATE_DROPDB.getValueAsBoolean();
        logger.info("Drop DB : " + drop);
        if (drop) {
            DatabaseManager.getInstance().setProperty("hibernate.hbm2ddl.auto", "create");
        }
        DatabaseManager.getInstance().build();
        logger.info("Hibernate successfully started !");

        //create condition of recovering : recover only non-removed job
        //Condition condition = new Condition("jobInfo.removedTime", ConditionComparator.LESS_EQUALS_THAN,(long) 0);
        //list of internal job to recover
        //List<InternalJob> recovering = DatabaseManager.recover(InternalJob.class, condition);
        RecoveringThread rt = new RecoveringThread();
        List<InternalJob> recovering = new ArrayList<InternalJob>();
        try {
            recovering = DatabaseManager.getInstance().recoverAllJobs(rt);
        } finally {
            rt.interrupt();
        }

        if (recovering.size() == 0) {
            logger_dev.info("No Job to recover.");
            frontend.recover(null);
            return;
        }

        // Recover the scheduler core
        //------------------------------------------------------------------------
        //----------------------    Re-build jobs lists  -------------------------
        //------------------------------------------------------------------------
        logger.info("Re-build jobs lists");

        JobId maxId = JobIdImpl.makeJobId("0");

        for (InternalJob job : recovering) {
            jobs.put(job.getId(), job);

            //search last JobId
            if (job.getId().compareTo(maxId) > 0) {
                maxId = job.getId();
            }
        }

        //------------------------------------------------------------------------
        //--------------------    Initialize jobId count   ----------------------
        //------------------------------------------------------------------------
        logger_dev.info("Initialize jobId count");
        JobIdImpl.setInitialValue((JobIdImpl) maxId);

        //------------------------------------------------------------------------
        //-----------    Re-build pending/running/finished lists  ----------------
        //------------------------------------------------------------------------
        logger_dev.info("Re-build jobs pending/running/finished lists");

        for (InternalJob job : jobs.values()) {
            //rebuild job descriptor if needed (needed because not stored in database)
            job.getJobDescriptor();
            switch (job.getStatus()) {
                case PENDING:
                    pendingJobs.add(job);
                    currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());
                    // create appender for this job if required
                    createFileAppender(job, true);
                    // restart classserver if needed
                    if (job.getEnvironment().getJobClasspath() != null) {
                        try {
                            SchedulerCore.addTaskClassServer(job.getId(), job.getEnvironment()
                                    .getJobClasspathContent(), job.getEnvironment().containsJarFile());
                        } catch (SchedulerException e) {
                            // TODO cdelbe : exception handling ?
                            logger_dev.error("", e);
                        }
                    }
                    break;
                case STALLED:
                case RUNNING:
                    //start dataspace app for this job
                    job.startDataSpaceApplication(dataSpaceNSStarter.getNamingService(), dataSpaceNSStarter
                            .getNamingServiceURL());

                    runningJobs.add(job);
                    currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());

                    //reset the finished events in the order they have occurred
                    ArrayList<InternalTask> tasksList = copyAndSort(job.getITasks(), true);

                    for (InternalTask task : tasksList) {
                        job.update(task.getTaskInfo());
                        //if the task was in waiting for restart status, restart it
                        if (task.getStatus() == TaskStatus.WAITING_ON_ERROR ||
                            task.getStatus() == TaskStatus.WAITING_ON_FAILURE) {
                            job.newWaitingTask();
                            job.reStartTask(task);
                        }
                    }
                    // create appender for this job if required
                    createFileAppender(job, true);
                    // restart classServer if needed
                    if (job.getEnvironment().getJobClasspath() != null) {
                        try {
                            SchedulerCore.addTaskClassServer(job.getId(), job.getEnvironment()
                                    .getJobClasspathContent(), job.getEnvironment().containsJarFile());
                        } catch (SchedulerException e) {
                            // TODO cdelbe : exception handling ?
                            logger_dev.error("", e);
                        }
                    }

                    break;
                case FINISHED:
                case CANCELED:
                case FAILED:
                case KILLED:
                    finishedJobs.add(job);
                    break;
                case PAUSED:
                    if ((job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks() + job
                            .getNumberOfFinishedTasks()) == 0) {
                        pendingJobs.add(job);
                    } else {
                        runningJobs.add(job);

                        //reset the finished events in the order they have occurred
                        ArrayList<InternalTask> tasksListP = copyAndSort(job.getITasks(), true);

                        for (InternalTask task : tasksListP) {
                            job.update(task.getTaskInfo());
                        }
                    }
                    // create appender for this job if required
                    createFileAppender(job, true);
                    // restart classserver if needed
                    if (job.getEnvironment().getJobClasspath() != null) {
                        try {
                            SchedulerCore.addTaskClassServer(job.getId(), job.getEnvironment()
                                    .getJobClasspathContent(), job.getEnvironment().containsJarFile());
                        } catch (SchedulerException e) {
                            // TODO cdelbe : exception handling ?
                            logger_dev.error("", e);
                        }
                    }
            }
            //unload job environment once handled
            DatabaseManager.getInstance().unload(job.getEnvironment());
        }

        //------------------------------------------------------------------------
        //------------------    Re-create task dependences   ---------------------
        //------------------------------------------------------------------------
        logger_dev.info("Re-create task dependences");

        for (InternalJob job : runningJobs) {
            ArrayList<InternalTask> tasksList = copyAndSort(job.getITasks(), true);

            //simulate the running execution to recreate the tree.
            for (InternalTask task : tasksList) {
                job.simulateStartAndTerminate(task.getId());
            }

            if ((job.getStatus() == JobStatus.RUNNING) || (job.getStatus() == JobStatus.PAUSED)) {
                //set the status to stalled because the scheduler start in stopped mode.
                if (job.getStatus() == JobStatus.RUNNING) {
                    job.setStatus(JobStatus.STALLED);
                }

                //set the task to pause inside the job if it is paused.
                if (job.getStatus() == JobStatus.PAUSED) {
                    job.setStatus(JobStatus.STALLED);
                    job.setPaused();
                    job.setTaskStatusModify(null);
                }

                //update the count of pending and running task.
                job.setNumberOfPendingTasks(job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks());
                job.setNumberOfRunningTasks(0);
            }
        }

        for (InternalJob job : pendingJobs) {
            //set the task to pause inside the job if it is paused.
            if (job.getStatus() == JobStatus.PAUSED) {
                job.setStatus(JobStatus.STALLED);
                job.setPaused();
                job.setTaskStatusModify(null);
            }
        }

        //------------------------------------------------------------------------
        //---------    Removed non-managed jobs (result has been sent)   ---------
        //----    Set remove waiting time to job where result has been sent   ----
        //------------------------------------------------------------------------
        logger_dev.info("Removing non-managed jobs");
        Iterator<InternalJob> iterJob = jobs.values().iterator();

        final SchedulerCore schedulerStub = (SchedulerCore) PAActiveObject.getStubOnThis();
        while (iterJob.hasNext()) {
            final InternalJob job = iterJob.next();
            //re-set job removed delay (if job result has been sent to user)
            if (SCHEDULER_REMOVED_JOB_DELAY > 0 || SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
                try {
                    //remove job after the given delay
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            schedulerStub.remove(job.getId());
                        }
                    };
                    long toWait = 0;
                    if (job.isToBeRemoved()) {
                        toWait = SCHEDULER_REMOVED_JOB_DELAY * SCHEDULER_AUTO_REMOVED_JOB_DELAY == 0 ? SCHEDULER_REMOVED_JOB_DELAY +
                            SCHEDULER_AUTO_REMOVED_JOB_DELAY
                                : Math.min(SCHEDULER_REMOVED_JOB_DELAY, SCHEDULER_AUTO_REMOVED_JOB_DELAY);
                    } else {
                        toWait = SCHEDULER_AUTO_REMOVED_JOB_DELAY;
                    }
                    if (toWait > 0) {
                        removeJobTimer.schedule(tt, toWait);
                    }
                    logger.debug("Job " + job.getId() + " will be removed in " +
                        (SCHEDULER_REMOVED_JOB_DELAY / 1000) + "sec");
                } catch (Exception e) {
                }
            }
        }

        //------------------------------------------------------------------------
        //----------------------    Sort each list by id   -----------------------
        //------------------------------------------------------------------------

        Comparator<InternalJob> jobComparator = new Comparator<InternalJob>() {
            public int compare(InternalJob o1, InternalJob o2) {
                return o1.getId().compareTo(o2.getId());
            }
        };
        Collections.sort(pendingJobs, jobComparator);
        Collections.sort(runningJobs, jobComparator);
        Collections.sort(finishedJobs, jobComparator);

        //------------------------------------------------------------------------
        //-----------------    Recover the scheduler front-end   -----------------
        //------------------------------------------------------------------------
        logger.debug("Recover the scheduler front-end");

        frontend.recover(jobs);

    }

    /**
     * Make a copy of the given argument with the restriction 'onlyFinished'.
     * Then sort the array according to finished time order.
     *
     * @param tasks the list of internal tasks to copy.
     * @param onlyFinished true if the copy must contains only the finished task,
     *                                                 false to contains every tasks.
     * @return the sorted copy of the given argument.
     */
    private ArrayList<InternalTask> copyAndSort(ArrayList<InternalTask> tasks, boolean onlyFinished) {
        ArrayList<InternalTask> tasksList = new ArrayList<InternalTask>();

        //copy the list with only the finished task.
        for (InternalTask task : tasks) {
            if (onlyFinished) {
                switch (task.getStatus()) {
                    case ABORTED:
                    case FAILED:
                    case FINISHED:
                    case FAULTY:
                        tasksList.add(task);
                }
            } else {
                tasksList.add(task);
            }
        }
        //sort the finished task according to their finish time.
        //to be sure to be in the right tree browsing.
        Collections.sort(tasksList, new FinishTimeComparator());

        return tasksList;
    }

    /**
     * FinishTimeComparator will compare the internal task on their finished time.
     *
     * @author The ProActive Team
     * @date 25 oct. 07
     *
     */
    private static class FinishTimeComparator implements Comparator<InternalTask> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         * @param o1 First InternalTask to be compared.
         * @param o2 Second InternalTask to be compared with the first.
         * @return a negative integer, zero, or a positive integer as the
         * 	       first argument is less than, equal to, or greater than the
         *	       second. 
         */
        public int compare(InternalTask o1, InternalTask o2) {
            return (int) (o1.getFinishedTime() - o2.getFinishedTime());
        }
    }

    /**
     * TimedDoTaskAction is used to start the task execution in parallel.
     *
     * @author The ProActive Team
     * @since ProActive Scheduling 2.0
     */
    class TimedDoTaskAction implements CallableWithTimeoutAction<TaskResult> {

        private AtomicBoolean timeoutCalled = new AtomicBoolean(false);
        private InternalTask task;
        private TaskLauncher launcher;
        private SchedulerCore coreStub;
        private TaskResult[] parameters;

        /**
         * Create a new instance of TimedDoTaskAction
         *
         * @param task the internal task
         * @param launcher the launcher of the task
         * @param coreStub the stub on SchedulerCore
         * @param parameters the parameters to be given to the task
         */
        public TimedDoTaskAction(InternalTask task, TaskLauncher launcher, SchedulerCore coreStub,
                TaskResult[] parameters) {
            this.task = task;
            this.launcher = launcher;
            this.coreStub = coreStub;
            this.parameters = parameters;
        }

        /**
         * {@inheritDoc}
         */
        public TaskResult call() throws Exception {
            try {
                //if a task has been launched
                if (launcher != null) {
                    //try launch the task
                    TaskResult tr = launcher.doTask(coreStub, task.getExecutableContainer(), parameters);
                    //check if timeout occurs
                    if (timeoutCalled.get()) {
                        //return null if timeout occurs (task may have to be restarted later)
                        return null;
                    } else {
                        //return task result if everything was OK
                        return tr;
                    }
                } else {
                    //return null if launcher was null (should never append)
                    return null;
                }
            } catch (Exception e) {
                //return null if something wrong occurs during task deployment
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        public void timeoutAction() {
            timeoutCalled.set(true);
        }

    }

}
