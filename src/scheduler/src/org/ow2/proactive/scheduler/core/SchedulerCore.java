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
package org.ow2.proactive.scheduler.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.db.Condition;
import org.ow2.proactive.db.ConditionComparator;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerCoreMethods;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.ClassServerException;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.TaskPreemptedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
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
import org.ow2.proactive.scheduler.core.annotation.RunActivityFiltered;
import org.ow2.proactive.scheduler.core.db.DatabaseManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxyCreationException;
import org.ow2.proactive.scheduler.core.rmproxies.UserRMProxy;
import org.ow2.proactive.scheduler.descriptor.TaskDescriptor;
import org.ow2.proactive.scheduler.exception.RunningProcessException;
import org.ow2.proactive.scheduler.exception.StartProcessException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobWrapper;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.policy.Policy;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scheduler.util.classloading.JobClasspathManager;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;
import org.ow2.proactive.scheduler.util.classloading.JobClasspathManager.JobClasspathEntry;
import org.ow2.proactive.utils.NodeSet;


/**
 * <i><font size="2" color="#FF0000">** Scheduler core ** </font></i>
 * This is the main active object of the scheduler implementation,
 * it communicates with the resources manager to acquire nodes and with a policy
 * to insert and get jobs from the queue.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SchedulerCore implements SchedulerCoreMethods, TaskTerminateNotification, RunActive {

    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);

    /** Number of threads used to call TaskLauncher.terminate() */
    private static final int TERMINATE_THREAD_NUMBER = PASchedulerProperties.SCHEDULER_STARTTASK_THREADNUMBER
            .getValueAsInt();

    /** Scheduler main loop time out */
    private static final int SCHEDULER_TIME_OUT = PASchedulerProperties.SCHEDULER_TIME_OUT.getValueAsInt();

    /** Ratio between number of task start and task termination in a single scheduling loop */
    private static final int SCHEDULER_START_TERM_RATIO = PASchedulerProperties.SCHEDULER_START_TERMINATE_RATIO
            .getValueAsInt();

    /** Max number of terminate request that can be served consecutively */
    private static final int MAX_TERM_SERVICE = 21;

    /**
     * Scheduler node ping frequency in second.
     * Ping will in fact call a method to the launcher to get the task progress state
     */
    private static final long SCHEDULER_NODE_PING_FREQUENCY = PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY
            .getValueAsInt() * 1000;
    /**
     * Scheduler node ping frequency in second.
     * Ping will in fact call a method to the launcher to get the task progress state
     */
    private static final int SCHEDULER_TASK_PROGRESS_NBTHREAD = PASchedulerProperties.SCHEDULER_TASK_PROGRESS_NBTHREAD
            .getValueAsInt();

    /** Delay to wait for between getting a job result and removing the job concerned */
    private static final long SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    /** Delay to wait for a job is terminated and removing it */
    private static final long SCHEDULER_AUTO_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_AUTOMATIC_REMOVED_JOB_DELAY
            .getValueAsInt() * 1000;

    /** Direct link to the current job to submit. */
    private InternalJobWrapper currentJobToSubmit;

    /** Implementation of Resource Manager proxies */
    RMProxiesManager rmProxiesManager;

    /** Scheduler front-end. */
    SchedulerFrontend frontend;

    /** Scheduler current policy */
    Policy policy;

    /** list of all jobs managed by the scheduler */
    Map<JobId, InternalJob> jobs;

    /** list of pending jobs among the managed jobs */
    Vector<InternalJob> pendingJobs;

    /** list of running jobs among the managed jobs */
    Vector<InternalJob> runningJobs;

    /** Scheduler current status */
    SchedulerStatus status;

    /** list of finished jobs among the managed jobs */
    private Vector<InternalJob> finishedJobs;

    private SchedulingMethod schedulingMethod;

    /** Thread that will ping the running nodes */
    private Thread pinger;
    /** Thread Pool used to get task progress status */
    private ExecutorService threadPool;

    /** Timer used for remove result method (transient because Timer is not serializable) */
    private Timer removeJobTimer;
    /** Timer used for restarting tasks */
    private Timer restartTaskTimer;

    /** Log forwarding service for nodes */
    LogForwardingService lfs;

    /** Jobs that must be logged into the corresponding appenders */
    Hashtable<JobId, AsyncAppender> jobsToBeLogged;

    /** Currently running tasks for a given jobId*/
    ConcurrentHashMap<JobId, Hashtable<TaskId, TaskLauncher>> currentlyRunningTasks;

    /** Currently running task previous progress percent */
    ConcurrentHashMap<TaskId, Integer> previousTaskProgress;

    /** ClassLoading */
    // contains taskCLassServer for currently running jobs
    protected Hashtable<JobId, TaskClassServer> classServers;
    protected Hashtable<JobId, RemoteObjectExposer<TaskClassServer>> remoteClassServers;
    // handle job classpathes in DB
    protected JobClasspathManager jobClasspathManager;

    /** Dataspaces Naming service */
    DataSpaceServiceStarter dataSpaceNSStarter;

    /**
     * Return the task classserver for the job jid.<br>
     * return null if the classServer is undefine for the given jobId.
     * 
     * @param jid the job id 
     * @return the task classserver for the job jid
     */
    public TaskClassServer getTaskClassServer(JobId jid) {
        return classServers.get(jid);
    }

    /**
     * Create a new taskClassServer for the job jid
     * @param jid the job id
     * @param userClasspathJarFile the contents of the classpath as a serialized jar file
     * @param deflateJar if true, the jar file is deflated in the tmpJarFilesDir
     * @throws ClassServerException if something goes wrong during task class server creation
     */
    protected void addTaskClassServer(JobId jid, byte[] userClasspathJarFile, boolean deflateJar)
            throws ClassServerException {
        if (getTaskClassServer(jid) != null) {
            throw new ClassServerException("ClassServer already exists for job " + jid);
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
            throw new ClassServerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (IOException e) {
            logger_dev.error("", e);
            throw new ClassServerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (UnknownProtocolException e) {
            logger_dev.error("", e);
            throw new ClassServerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (ProActiveException e) {
            logger_dev.error("", e);
            throw new ClassServerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        }
    }

    /**
     * Remove the taskClassServer for the job jid.
     * Delete the classpath associated in SchedulerCore.tmpJarFilesDir.
     * @return true if a taskClassServer has been removed, false otherwise.
     */
    protected boolean removeTaskClassServer(JobId jid) {
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
     * Exit the scheduler and the JVM displaying stacktrace cause in logger
     * and message on standard output.<br/>
     * If the given message is null, the message of the throwable will be displayed.<br/>
     * If the given throwable is null, message will be displayed and logged.<br/>
     * If both are null, displayed and logged message will be something like 'Unknown reason'.
     *
     * @param t the throwable that causes the JVM to stop
     * @param message a human readable message for this cause
     */
    static void exitFailure(Throwable t, String message) {
        if (t == null && message == null) {
            message = "Unknown reason";
        } else {
            if (message == null) {
                message = t.getMessage();
            }
        }
        if (t == null) {
            logger.fatal("**** " + message + "****");
        } else {
            logger.fatal(message, t);
        }
        System.out.println("Scheduling JVM has exited - Reason : " + message);
        PALifeCycle.exitFailure();
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
            this.currentlyRunningTasks.remove(jid);
            removeTaskClassServer(jid);
            //auto remove
            if (SCHEDULER_AUTO_REMOVED_JOB_DELAY > 0) {
                try {
                    //remove job after the given delay
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            schedulerStub.removeJob(jid);
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
     * @param rmp the resource manager on which the scheduler will interact.
     * @param frontend a reference to the frontend.
     * @param policyFullName the fully qualified name of the policy to be used.
     */
    public SchedulerCore(URI rmURL, SchedulerFrontend frontend, String policyFullName,
            InternalJobWrapper jobSubmitLink) {
        try {
            // create classloading tools
            this.classServers = new Hashtable<JobId, TaskClassServer>();
            this.remoteClassServers = new Hashtable<JobId, RemoteObjectExposer<TaskClassServer>>();
            this.jobClasspathManager = new JobClasspathManager();

            //try connect to RM with Scheduler user
            this.rmProxiesManager = RMProxiesManager.getRMProxiesManager(rmURL);
            this.rmProxiesManager.getSchedulerRMProxy();//-> used to connect RM
            //init
            this.jobs = new HashMap<JobId, InternalJob>();
            this.pendingJobs = new Vector<InternalJob>();
            this.runningJobs = new Vector<InternalJob>();
            this.finishedJobs = new Vector<InternalJob>();
            this.removeJobTimer = new Timer("RemoveJobTimer");
            this.restartTaskTimer = new Timer("RestartTaskTimer");
            this.status = SchedulerStatus.STOPPED;
            this.jobsToBeLogged = new Hashtable<JobId, AsyncAppender>();
            this.currentlyRunningTasks = new ConcurrentHashMap<JobId, Hashtable<TaskId, TaskLauncher>>();
            this.previousTaskProgress = new ConcurrentHashMap<TaskId, Integer>();
            this.threadPoolForTerminateTL = Executors.newFixedThreadPool(TERMINATE_THREAD_NUMBER,
                    new NamedThreadFactory("TaskLauncher_Terminate"));
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
            this.policy.setCore(this);
            if (!this.policy.reloadConfig()) {
                throw new RuntimeException("Scheduling policy cannot be started, see log file for details.");
            }
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
        } catch (RMException e) {
            logger.error("Cannot instanciate RM proxies Manager due to " + e.getMessage());
            logger_dev.error("", e);
            throw new RuntimeException(e);
        } catch (RMProxyCreationException e) {
            logger.error("Cannot create Scheduler RM proxy due to " + e.getMessage());
            logger_dev.error("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the pinger thread to detect unActivity on nodes.
     */
    private void createPingThread() {
        logger_dev.debug("Creating nodes pinging thread");
        threadPool = Executors.newFixedThreadPool(SCHEDULER_TASK_PROGRESS_NBTHREAD, new NamedThreadFactory(
            "Scheduling_GetTaskProgress"));
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
                    } catch (InterruptedException e) {
                        //miam -> shutdown scheduler
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
    void updateTaskInfosList(InternalJob currentJob, SchedulerEvent eventType) {
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
        // also replicated tasks
        currentJob.setReplicatedTasksModify(null);
        currentJob.setLoopedTasksModify(null);
        currentJob.setSkippedTasksModify(null);
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
        //start Dataspace naming service
        try {
            dataSpaceNSStarter = new DataSpaceServiceStarter();
            dataSpaceNSStarter.startNamingService();
        } catch (Throwable e) {
            ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE).info("Cannot start Scheduler :", e);
            //terminate this active object
            try {
                PAActiveObject.terminateActiveObject(false);
            } catch (ProActiveRuntimeException pare) {
            }
            //exit
            PALifeCycle.exitSuccess();
            return;
        }
        //Start DB and rebuild the scheduler if needed.
        try {
            recover();
        } catch (Throwable e) {
            ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE).info("Cannot start Scheduler :", e);
            kill();
        }

        if (status != SchedulerStatus.KILLED) {

            Service service = new Service(body);
            // immediate services are set with @ImmediateService annotation
            if (logger_dev.isDebugEnabled()) {
                String tmp = "Core immediate services :";
                for (Method m : SchedulerCore.class.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(ImmediateService.class)) {
                        tmp += " " + m.getName();
                    }
                }
                logger_dev.info(tmp);
            }

            //set the filter for serveAll method (user action are privileged)
            //Filtered methods must be annoted with @RunActivityFiltered
            RequestFilter terminateFilter = new MainLoopRequestFilter("internal");
            RequestFilter incomingRequestsFilter = new MainLoopRequestFilter("external");

            createPingThread();

            //create scheduling method
            schedulingMethod = new SchedulingMethodImpl(this);

            //default scheduler status will be started
            start();

            do {
                // number of task started by the previous scheduling loop
                int numberOfTaskStarted = 1;
                while ((status == SchedulerStatus.STARTED) || (status == SchedulerStatus.PAUSED) ||
                    (status == SchedulerStatus.STOPPED)) {
                    try {
                        // block the loop until a method is invoked and serve it
                        // while some task are started loop as faster as possible
                        service.blockingServeOldest(numberOfTaskStarted != 0 ? 1 : SCHEDULER_TIME_OUT);
                        if (logger_dev.isTraceEnabled()) {
                            logger_dev.trace("[PROF] Timout is = " +
                                (numberOfTaskStarted != 0 ? 1 : SCHEDULER_TIME_OUT));
                        }
                        //serve all incoming methods
                        service.serveAll(incomingRequestsFilter);

                        //schedule
                        long startSch = System.currentTimeMillis();
                        numberOfTaskStarted = schedulingMethod.schedule();
                        if (logger_dev.isTraceEnabled()) {
                            logger_dev.trace("[PROF] Scheduling time = " +
                                (System.currentTimeMillis() - startSch) + " for " + numberOfTaskStarted);
                        }
                        // serve internal request
                        long startTerm = System.currentTimeMillis();
                        int maxTermServices = 0;
                        if (numberOfTaskStarted != 0) {
                            final int stratio = numberOfTaskStarted / SCHEDULER_START_TERM_RATIO;
                            maxTermServices = stratio < 1 ? 1 : stratio;
                        } else {
                            maxTermServices = MAX_TERM_SERVICE;
                        }
                        int termServicesCounter = 0;
                        while (service.hasRequestToServe(terminateFilter) &&
                            (termServicesCounter < maxTermServices)) {
                            service.serveOldest(terminateFilter);
                            termServicesCounter++;
                        }
                        if (logger_dev.isTraceEnabled()) {
                            logger_dev.trace("[PROF] Terminate served = " + termServicesCounter + " in " +
                                (System.currentTimeMillis() - startTerm));
                        }

                    } catch (Exception e) {
                        //this point is reached in case of unknown problems
                        logger
                                .error(
                                        "\nSchedulerCore.runActivity(MAIN_LOOP) caught an EXCEPTION - it will not terminate the body !",
                                        e);
                        //trying to check if RM is alive
                        try {
                            logger_dev.error("Check if Resource Manager is alive");
                            //call isActive and wait recursively on the two futures (this -> proxy -> RM)
                            PAFuture.waitFor(rmProxiesManager.getSchedulerRMProxy().isActive(), true);
                        } catch (Exception rme) {
                            logger_dev.error("Resource Manager seems to be dead", rme);
                            //try to terminate every proxies
                            rmProxiesManager.terminateSchedulerRMProxy();
                            rmProxiesManager.terminateAllUsersRMProxies();
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
                    } catch (Error e) {
                        //this point is reached in case of big problem, sometimes unknown
                        logger.error("\nSchedulerCore.runActivity(MAIN_LOOP) caught an ERROR !", e);
                        //Terminate proxy and disconnecting RM
                        logger_dev.error("Resource Manager will be disconnected");
                        rmProxiesManager.terminateSchedulerRMProxy();
                        rmProxiesManager.terminateAllUsersRMProxies();
                        //if failed
                        freeze();
                        //scheduler functionality are reduced until now
                        status = SchedulerStatus.UNLINKED;
                    }
                }

                // allows frozen->other state transition
                service.blockingServeOldest(SCHEDULER_TIME_OUT);

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
            int numberOfTaskStarted = 1;
            while ((runningJobs.size() + pendingJobs.size()) > 0) {
                try {
                    // same loop as main loop
                    service.blockingServeOldest(numberOfTaskStarted != 0 ? 1 : SCHEDULER_TIME_OUT);
                    service.serveAll(incomingRequestsFilter);
                    numberOfTaskStarted = schedulingMethod.schedule();
                    int maxTermServices = 0;
                    if (numberOfTaskStarted != 0) {
                        final int stratio = numberOfTaskStarted / SCHEDULER_START_TERM_RATIO;
                        maxTermServices = stratio < 1 ? 1 : stratio;
                    } else {
                        maxTermServices = MAX_TERM_SERVICE;
                    }
                    int termServicesCounter = 0;
                    while (service.hasRequestToServe(terminateFilter) &&
                        (termServicesCounter < maxTermServices)) {
                        service.serveOldest(terminateFilter);
                        termServicesCounter++;
                    }
                } catch (Exception e) {
                    logger_dev.error("", e);
                }
            }

            //stop the pinger thread.
            pinger.interrupt();
        }

        logger.info("Terminating...");
        //try to disconnect every proxies
        rmProxiesManager.terminateSchedulerRMProxy();
        rmProxiesManager.terminateAllUsersRMProxies();
        logger_dev.info("Resource Manager proxies shutdown");

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
        threadPoolForTerminateTL.shutdownNow();
        //terminate this active object
        try {
            PAActiveObject.terminateActiveObject(false);
        } catch (ProActiveRuntimeException e) {
        }
        logger.info("Scheduler is now shutdown !");
        //exit
        PALifeCycle.exitSuccess();
    }

    /**
     * Ping every nodes on which a task is currently running and repair the task if need.
     */
    private void pingDeployedNodes() {
        logger_dev.info("Search for down nodes !");

        for (int i = 0; i < runningJobs.size(); i++) {
            final InternalJob job = runningJobs.get(i);
            //use thread pool to ping every tasks of a job
            threadPool.submit(new Runnable() {
                public void run() {
                    for (InternalTask td : job.getITasks()) {
                        if (td != null && (td.getStatus() == TaskStatus.RUNNING)) {
                            try {
                                Integer progress = td.getExecuterInformations().getLauncher().getProgress();
                                td.setProgress(progress);//(1)
                                //get previous if set
                                Integer previousProgress = previousTaskProgress.get(td.getId());
                                if (!progress.equals(previousProgress)) {
                                    //if previousProgress==null or previousProgress != progress -> update
                                    frontend.taskStateUpdated(job.getOwner(), new NotificationData<TaskInfo>(
                                        SchedulerEvent.TASK_PROGRESS, td.getTaskInfo()));
                                }
                            } catch (IllegalArgumentException iae) {
                                //thrown by (1)
                                //avoid setting bad value, no event if bad
                            } catch (NullPointerException e) {
                                //do nothing - should not happened, but avoid restart if execInfo or launcher is null
                            } catch (Throwable t) {
                                //check if the task has not been terminated while pinging
                                if (currentlyRunningTasks.get(job.getId()).remove(td.getId()) == null) {
                                    continue;
                                }

                                logger_dev.info("Node failed on job '" + job.getId() + "', task '" +
                                    td.getId() + "'");

                                try {
                                    logger_dev.info("Try to free failed node set");
                                    //free execution node even if it is dead
                                    rmProxiesManager.getUserRMProxy(job).releaseNodes(
                                            td.getExecuterInformations().getNodes());
                                } catch (Exception e) {
                                    //just save the rest of the method execution
                                }

                                //remove previous read
                                previousTaskProgress.remove(td.getId());
                                //manage restart
                                td.decreaseNumberOfExecutionOnFailureLeft();
                                DatabaseManager.getInstance().synchronize(td.getTaskInfo());
                                logger_dev.info("Number of retry on Failure left for the task '" +
                                    td.getId() + "' : " + td.getNumberOfExecutionOnFailureLeft());
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
                                    break;
                                }
                            }
                        }

                    }
                }
            });
        }
    }

    /**
     * Return true if a submit is possible, false if not.
     * 
     * @return true if a submit is possible, false if not.
     */
    @ImmediateService
    public boolean isSubmitPossible() {
        return !((status == SchedulerStatus.SHUTTING_DOWN) || (status == SchedulerStatus.STOPPED));
    }

    /**
     * Submit a new job to the scheduler.
     * This method will prepare the new job and get it ready for scheduling.<br>
     * It is not possible to submit the job if the Scheduler is stopped
     * This method must be synchronous !
     */
    @RunActivityFiltered(id = "external")
    public boolean submit() {
        InternalJob job = currentJobToSubmit.getJob();
        logger_dev.info("Trying to submit new Job '" + job.getId() + "'");

        // store jobclasspath content if it is set and unknown
        if (job.getEnvironment().getJobClasspath() != null) {
            // store jobclasspath in db if unknown
            final long crc = job.getEnvironment().getJobClasspathCRC();
            if (!jobClasspathManager.contains(crc)) {
                jobClasspathManager.put(crc, job.getEnvironment().clearJobClasspathContent(), job
                        .getEnvironment().containsJarFile());
            }
        }

        // TODO cdelbe : create classserver only when job is running ?
        // create taskClassLoader for this job
        createTaskClassServer(job);

        job.submitAction();

        //create job result storage
        JobResult jobResult = new JobResultImpl(job);
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

        //unload job environment that potentially contains classpath as byte[]
        DatabaseManager.getInstance().unload(job.getEnvironment());
        //unload heavy object
        for (InternalTask it : job.getITasks()) {
            DatabaseManager.getInstance().unload(it);
        }
        logger_dev.info("JobEnvironment and internalTask unloaded for job '" + job.getId() + "'");
        frontend.jobSubmitted(job);
        return true;
    }

    /**
     * End the given job due to the given task failure.
     *
     * @param job the job to end.
     * @param task the task who has been the caused of failing. **This argument can be null only if jobStatus is killed**
     * @param errorMsg the error message to send in the task result.
     * @param jobStatus the type of the end for this job. (failed/canceled/killed)
     */
    void endJob(InternalJob job, InternalTask task, String errorMsg, JobStatus jobStatus) {

        // job can be already ended (SCHEDULING-700)
        JobStatus currentStatus = job.getStatus();
        if (currentStatus == JobStatus.CANCELED || currentStatus == JobStatus.FAILED ||
            currentStatus == JobStatus.KILLED || currentStatus == JobStatus.FINISHED) {
            logger_dev.info("Job ending request for already ended job '" + job.getId() + "'");
            // job is already ended nothing to do
            return;
        }

        TaskResult taskResult = null;

        if (task != null) {
            logger_dev.info("Job ending request for job '" + job.getId() + "' - cause by task '" +
                task.getId() + "' - status : " + jobStatus);
        } else {
            logger_dev.info("Job ending request for job '" + job.getId() + "' - status : " + jobStatus);
        }

        for (InternalTask td : job.getITasks()) {
            if (td.getStatus() == TaskStatus.RUNNING) {
                //remove previous read progress
                previousTaskProgress.remove(td.getId());

                //get the nodes that are used for this descriptor
                NodeSet nodes = td.getExecuterInformations().getNodes();

                //try to terminate the task
                try {
                    logger_dev.info("Force terminating task '" + td.getId() + "'");
                    td.getExecuterInformations().getLauncher().terminate(false);
                } catch (Exception e) { /* (nothing to do) */
                }

                try {
                    //free every execution nodes
                    ((UserRMProxy) rmProxiesManager.getUserRMProxy(job)).releaseNodes(nodes, td
                            .getCleaningScript());
                } catch (Throwable e) {
                    logger_dev.debug("", e);
                    //we did our best
                }

                //If not killed
                if (jobStatus != JobStatus.KILLED) {
                    //deleting tasks results except the one that causes the error
                    if (!td.getId().equals(task.getId())) {
                        try {
                            job.getJobResult().removeResult(td.getName());
                        } catch (UnknownTaskException ute) {
                            //should never happen
                            logger_dev.error("", ute);
                        }
                    }
                    //if canceled, get the result of the canceled task
                    if ((jobStatus == JobStatus.CANCELED) && td.getId().equals(task.getId())) {
                        try {
                            taskResult = job.getJobResult().getResult(task.getName());
                        } catch (UnknownTaskException ute) {
                            //should never append
                            logger_dev.error("", ute);
                        }
                    }
                }

            }
        }

        boolean pj = false;
        //if job has been killed
        if (jobStatus == JobStatus.KILLED) {
            job.failed(null, jobStatus);
            //the next line will try to remove job from each list.
            //once removed, it won't be removed from remaining list, but we ensure that the job is in only one of the list.
            if (runningJobs.remove(job) || (pj = pendingJobs.remove(job))) {
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
        updateTaskInfosList(job, pj ? SchedulerEvent.JOB_PENDING_TO_FINISHED
                : SchedulerEvent.JOB_RUNNING_TO_FINISHED);

        logger.info("Job '" + job.getId() + "' terminated (" + jobStatus + ")");
    }

    private ExecutorService threadPoolForTerminateTL;

    /**
     * Invoke by a task when it is about to finish.
     * This method can be invoke just a little amount of time before the result arrival.
     * That's why it can block the execution but only for short time.
     *
     * This method is marked as 'internal' because it is called only from node.
     *
     * @param taskId the identification of the executed task.
     */
    @RunActivityFiltered(id = "internal")
    public void terminate(final TaskId taskId) {
        terminate(taskId, new TerminateOptions());
    }

    /**
     * This method is the generic method to terminate task.<br/>
     * This method is private and internal access only, must be non-actif call.<br/>
     *
     * Can be called through other active object calls and must be called by service thread.
     * (no immediate service)
     *
     * @param taskId the identification of the executed task.
     * @param terminateOptions the way the task must be terminated
     */
    private void terminate(final TaskId taskId, final TerminateOptions terminateOptions) {
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

        //get the internal task
        InternalTask descriptor = job.getIHMTasks().get(taskId);
        //remove previous read
        previousTaskProgress.remove(descriptor.getId());

        final TaskLauncher taskLauncher;
        synchronized (currentlyRunningTasks) {
            // job might have already been removed if job has failed...
            Hashtable<TaskId, TaskLauncher> runningTasks = this.currentlyRunningTasks.get(jobId);
            if (runningTasks != null) {
                if ((taskLauncher = runningTasks.remove(taskId)) == null) {
                    //This case is checked to avoid race condition when starting a task.
                    //The doTask(...) action could have been performed while the starter thread has considered it
                    //as timed out. In this particular case, this terminate(taskId) method could have been called anyway.
                    //We must not consider this call !
                    logger_dev.info("This taskId represents a non running task");
                    return;
                }
            } else {
                logger_dev.warn("RunningTasks list was null, it could be due to a second call to terminate");
                return;
            }
        }

        try {
            //first unload the executable container that we don't need until next execution (if re-execution)
            DatabaseManager.getInstance().unload(descriptor);
            TaskResultImpl res;
            if (terminateOptions.isNormalTermination()) {
                //The task is terminated but it's possible to have to
                //wait for the future of the task result (TaskResult).
                //accessing to the taskResult could block current execution but for a very little time.
                //it is the time between the end of the task and the arrival of the future from the task.
                //
                //check if the task result future has an error due to node death.
                //if the node has died, a runtimeException is sent instead of the result
                TaskResult tmp = ((JobResultImpl) job.getJobResult()).getAnyResult(descriptor.getName());
                //unwrap future
                res = (TaskResultImpl) PAFuture.getFutureValue(tmp);
                logger_dev.info("Task '" + taskId + "' futur result unwrapped");
            } else {
                if (terminateOptions.isPreempt()) {
                    //create fake task result with preempt exception
                    res = new TaskResultImpl(taskId, new TaskPreemptedException("Preempted by admin"),
                        new SimpleTaskLogs("", "Preempted by admin"), System.currentTimeMillis() -
                            descriptor.getStartTime());
                } else {
                    //create fake task result with restart exception
                    res = new TaskResultImpl(taskId, new TaskAbortedException("Aborted by user"),
                        new SimpleTaskLogs("", "Aborted by user"), System.currentTimeMillis() -
                            descriptor.getStartTime());
                }
            }
            // at this point, the TaskLauncher can be terminated
            threadPoolForTerminateTL.submit(new Runnable() {
                public void run() {
                    try {
                        taskLauncher.terminate(terminateOptions.isNormalTermination());
                    } catch (Throwable t) {
                        logger_dev.info("Cannot terminate task launcher for task '" + taskId + "'", t);
                    }
                }
            });

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
                        rmProxiesManager.getUserRMProxy(job).releaseNodes(
                                descriptor.getExecuterInformations().getNodes());
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
                    try {
                        ((UserRMProxy) rmProxiesManager.getUserRMProxy(job)).releaseNodes(descriptor
                                .getExecuterInformations().getNodes(), descriptor.getCleaningScript());
                    } catch (RMProxyCreationException rmpce) {
                        logger_dev.debug("", rmpce);
                        //we did our best - should not be thrown here
                    }
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
                if (terminateOptions.isNormalRestart()) {
                    descriptor.decreaseNumberOfExecutionLeft();
                }
                //check the number of execution left and fail the job if it is cancelOnError
                if (descriptor.isCancelJobOnError()) {
                    if (descriptor.getNumberOfExecutionLeft() <= 0) {
                        //if no rerun left, failed the job
                        endJob(job, descriptor,
                                "An error occurred in your task and the maximum number of executions has been reached. "
                                    + "You also ask to cancel the job in such a situation !",
                                JobStatus.CANCELED);
                        return;
                    } else if (terminateOptions.isKilled()) {
                        //task is killed, so cancel the job
                        endJob(job, descriptor, "The task has been manually killed. "
                            + "You also ask to cancel the job in such a situation !", JobStatus.CANCELED);
                        return;
                    }
                }
                if (descriptor.getNumberOfExecutionLeft() > 0 && !terminateOptions.isKilled()) {
                    logger_dev.debug("Node Exclusion : restart mode is '" +
                        descriptor.getRestartTaskOnError() + "'");
                    if (descriptor.getRestartTaskOnError().equals(RestartMode.ELSEWHERE)) {
                        //if the task restart ELSEWHERE
                        descriptor.setNodeExclusion(descriptor.getExecuterInformations().getNodes());
                    }
                    try {
                        ((UserRMProxy) rmProxiesManager.getUserRMProxy(job)).releaseNodes(descriptor
                                .getExecuterInformations().getNodes(), descriptor.getCleaningScript());
                        hasBeenReleased = true;
                    } catch (Exception e) {
                        logger_dev.error("", e);
                        //cannot get back the node, RM take care about that.
                    }
                    //change status and update GUI
                    if (terminateOptions.isPreempt()) {
                        descriptor.setStatus(TaskStatus.PENDING);
                    } else {
                        descriptor.setStatus(TaskStatus.WAITING_ON_ERROR);
                    }
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
                    long nextWaitingTime;
                    if (terminateOptions.hasDelay()) {
                        nextWaitingTime = terminateOptions.getDelay() * 1000;
                    } else {
                        nextWaitingTime = job.getNextWaitingTime(descriptor.getMaxNumberOfExecution() -
                            descriptor.getNumberOfExecutionLeft());
                    }
                    restartTaskTimer.schedule(jtt, nextWaitingTime);

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
            descriptor = job.terminateTask(errorOccurred, taskId, frontend, res.getAction());

            //and update database
            try {
                DatabaseManager.getInstance().startTransaction();
                DatabaseManager.getInstance().synchronize(job.getJobInfo());
                DatabaseManager.getInstance().synchronize(descriptor.getTaskInfo());
                DatabaseManager.getInstance().update(job.getJobResult());
                DatabaseManager.getInstance().commitTransaction();
            } catch (DatabaseManagerException e) {

            }

            //clean the result to improve memory usage
            if (!job.getJobDescriptor().hasChildren(descriptor.getId())) {
                DatabaseManager.getInstance().unload(res);
            }
            if (currentTD != null) {
                for (TaskDescriptor td : currentTD.getParents()) {
                    if (td.getChildrenCount() == 0) {
                        try {
                            DatabaseManager.getInstance().unload(
                                    job.getJobResult().getResult(td.getTaskId().getReadableName()));
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
            if (job.isFinished()) {
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
        } catch (UnknownTaskException ute) {
            //should never happen : taskName is unknown
            logger_dev.error("", ute);
        } catch (NullPointerException eNull) {
            logger_dev.error("", eNull);
            //avoid race condition between kill and terminate task
            //the task has been killed. Nothing to do anymore with this one.
            //should never happen
        } finally {
            if (!hasBeenReleased) {
                //free every execution nodes
                try {
                    ((UserRMProxy) rmProxiesManager.getUserRMProxy(job)).releaseNodes(descriptor
                            .getExecuterInformations().getNodes(), descriptor.getCleaningScript());
                } catch (RMProxyCreationException e) {
                    logger_dev.debug("", e);
                    //we did our best > should not be thrown
                }
            }
            if (job.isFinished()) {
                //terminate user proxy if needed
                terminateUserProxy(job);
            }
        }
    }

    /**
     * Terminate the RM proxy of the owner of the job
     * if there is no more running and pending jobs of this user
     *
     * @param job the job that belongs to the user the proxy must be terminated
     */
    private void terminateUserProxy(InternalJob job) {
        for (InternalJob j : pendingJobs) {
            if (j.getOwner().equals(job.getOwner())) {
                return;
            }
        }
        for (InternalJob j : runningJobs) {
            if (j.getOwner().equals(job.getOwner())) {
                return;
            }
        }
        //if not found in pending and running, terminate proxy
        rmProxiesManager.terminateUserRMProxy(job);
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
    @RunActivityFiltered(id = "external")
    public void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws UnknownJobException {
        logger_dev.info("listen logs of job '" + jobId + "'");
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId);

        // create the appender to the remote listener
        Appender appender = null;
        try {
            appender = appenderProvider.getAppender();
        } catch (LogForwardingException e1) {
            logger.error("Cannot create an appender for job " + jobId, e1);
            logger_dev.error("", e1);
            throw new InternalException("Cannot create an appender for job " + jobId, e1);
        }

        // targeted job
        InternalJob target = this.jobs.get(jobId);

        if (target == null) {
            throw new UnknownJobException(jobId);
        }

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
        if (this.finishedJobs.contains(target)) {
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
        } else if (!this.pendingJobs.contains(target)) {
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
     * Create a taskclassserver for this job if a jobclasspath is set
     */
    private void createTaskClassServer(InternalJob job) {
        // restart classserver if needed
        try {
            if (job.getEnvironment().getJobClasspath() != null) {
                JobClasspathEntry jce = jobClasspathManager.get(job.getEnvironment().getJobClasspathCRC());
                if (jce == null) {
                    throw new ClassServerException("No classpath content is available for job " +
                        job.getJobInfo().getJobId());
                }
                this.addTaskClassServer(job.getId(), jce.classpathContent, jce.containsJarFiles);
            }
        } catch (ClassServerException e) {
            throw new IllegalStateException("Cannot create TaskClassServer for job " +
                job.getJobInfo().getJobId(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @ImmediateService
    public JobResult getJobResult(JobId jobId) throws UnknownJobException {
        final InternalJob job = jobs.get(jobId);
        final SchedulerCore schedulerStub = (SchedulerCore) PAActiveObject.getStubOnThis();

        if (job == null) {
            throw new UnknownJobException(jobId);
        }

        logger_dev.info("Trying to get JobResult of job '" + jobId + "'");
        //result = null if not in DB (ie: not yet available)
        JobResult result = null;
        try {
            result = DatabaseManager.getInstance().recover(job.getJobResult().getClass(),
                    new Condition("id", ConditionComparator.EQUALS_TO, job.getJobResult().getJobId())).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new InternalException("Cannot retrieve the result of job '" + jobId + "' !", e);
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
                            schedulerStub.removeJob(job.getId());
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
            throw new InternalException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) throws UnknownJobException,
            UnknownTaskException {
        logger_dev.info("Trying to get TaskResult of task '" + taskName + "' for job '" + jobId + "'");
        InternalJob job = jobs.get(jobId);

        if (job == null) {
            logger_dev.info("Job '" + jobId + "' does not exist");
            throw new UnknownJobException(jobId);
        }

        //extract taskResult reference from memory (weak instance)
        //useful to get the task result with the task name
        TaskResult result = ((JobResultImpl) job.getJobResult()).getResult(taskName);
        if (result == null) {
            //the task is not finished : means not yet started or running
            logger_dev.info("Task " + taskName + " is not finished");
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
     * {@inheritDoc}
     */
    public boolean removeJob(JobId jobId) {
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
            return true;
        } else {
            logger_dev.info("Job '" + jobId + "' has already been removed or is not finished !");
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean start() {
        if (status == SchedulerStatus.UNLINKED) {
            return false;
        }

        if (status != SchedulerStatus.STOPPED) {
            return false;
        }

        status = SchedulerStatus.STARTED;
        logger.info("Scheduler has just been started !");
        frontend.schedulerStateUpdated(SchedulerEvent.STARTED);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean stop() {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.STOPPED ||
            status == SchedulerStatus.SHUTTING_DOWN || status == SchedulerStatus.KILLED) {
            return false;
        }

        status = SchedulerStatus.STOPPED;
        logger.info("Scheduler has just been stopped, no tasks will be launched until start.");
        frontend.schedulerStateUpdated(SchedulerEvent.STOPPED);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean pause() {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.SHUTTING_DOWN ||
            status == SchedulerStatus.KILLED) {
            return false;
        }

        if ((status != SchedulerStatus.FROZEN) && (status != SchedulerStatus.STARTED)) {
            return false;
        }

        status = SchedulerStatus.PAUSED;
        logger.info("Scheduler has just been paused !");
        frontend.schedulerStateUpdated(SchedulerEvent.PAUSED);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean freeze() {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.SHUTTING_DOWN ||
            status == SchedulerStatus.KILLED) {
            return false;
        }

        if ((status != SchedulerStatus.PAUSED) && (status != SchedulerStatus.STARTED)) {
            return false;
        }

        status = SchedulerStatus.FROZEN;
        logger.info("Scheduler has just been frozen !");
        frontend.schedulerStateUpdated(SchedulerEvent.FROZEN);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean resume() {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.SHUTTING_DOWN ||
            status == SchedulerStatus.KILLED) {
            return false;
        }

        if ((status != SchedulerStatus.PAUSED) && (status != SchedulerStatus.FROZEN)) {
            return false;
        }

        status = SchedulerStatus.STARTED;
        logger.info("Scheduler has just been resumed !");
        frontend.schedulerStateUpdated(SchedulerEvent.RESUMED);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean shutdown() {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.KILLED ||
            status == SchedulerStatus.SHUTTING_DOWN) {
            return false;
        }

        status = SchedulerStatus.SHUTTING_DOWN;
        logger.info("Scheduler is shutting down, this make take time to finish every jobs !");
        frontend.schedulerStateUpdated(SchedulerEvent.SHUTTING_DOWN);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean kill() {
        if (status == SchedulerStatus.KILLED) {
            return false;
        }

        logger_dev.info("Killing all running task processes...");
        //destroying running active object launcher
        for (InternalJob j : runningJobs) {
            for (InternalTask td : j.getITasks()) {
                if (td.getStatus() == TaskStatus.RUNNING) {
                    try {
                        NodeSet nodes = td.getExecuterInformations().getNodes();

                        try {
                            td.getExecuterInformations().getLauncher().terminate(false);
                        } catch (Exception e) {
                            /* Tested, nothing to do */
                            logger_dev.error("", e);
                        }

                        try {
                            ((UserRMProxy) rmProxiesManager.getUserRMProxy(j)).releaseNodes(nodes, td
                                    .getCleaningScript());
                        } catch (Throwable e) {
                            logger_dev.debug("", e);
                            //we did our best - should not be thrown here
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

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean pauseJob(JobId jobId) {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.SHUTTING_DOWN ||
            status == SchedulerStatus.KILLED) {
            return false;
        }

        InternalJob job = jobs.get(jobId);

        if (finishedJobs.contains(job)) {
            return false;
        }

        boolean change = job.setPaused();

        if (change) {
            logger.debug("Job " + jobId + " has just been paused !");
        }

        //update tasks events list and send it to front-end
        updateTaskInfosList(job, SchedulerEvent.JOB_PAUSED);

        return change;
    }

    /**
     * {@inheritDoc}
     */
    public boolean resumeJob(JobId jobId) {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.SHUTTING_DOWN ||
            status == SchedulerStatus.KILLED) {
            return false;
        }

        InternalJob job = jobs.get(jobId);

        if (finishedJobs.contains(job)) {
            return false;
        }

        boolean change = job.setUnPause();

        if (change) {
            logger.debug("Job " + jobId + " has just been resumed !");
        }

        //update tasks events list and send it to front-end
        updateTaskInfosList(job, SchedulerEvent.JOB_RESUMED);

        return change;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean killJob(JobId jobId) {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.KILLED) {
            return false;
        }

        logger_dev.info("Request sent to kill job '" + jobId + "'");

        InternalJob job = jobs.get(jobId);

        if (job == null || job.getStatus() == JobStatus.KILLED) {
            return false;
        }

        endJob(job, null, "", JobStatus.KILLED);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @RunActivityFiltered(id = "external")
    public boolean restartTask(JobId jobId, String taskName, int restartDelay) throws UnknownJobException,
            UnknownTaskException {
        return killOrRestartTask(jobId, taskName,
                new TerminateOptions(TerminateOptions.RESTART, restartDelay));
    }

    /**
     * {@inheritDoc}
     */
    @RunActivityFiltered(id = "external")
    public boolean preemptTask(JobId jobId, String taskName, int restartDelay) throws UnknownJobException,
            UnknownTaskException {
        return killOrRestartTask(jobId, taskName,
                new TerminateOptions(TerminateOptions.PREEMPT, restartDelay));
    }

    /**
     * {@inheritDoc}
     */
    @RunActivityFiltered(id = "external")
    public boolean killTask(JobId jobId, String taskName) throws UnknownJobException, UnknownTaskException {
        return killOrRestartTask(jobId, taskName, new TerminateOptions(TerminateOptions.KILL));
    }

    /**
     * Kill, restart or preempt task factorisation
     */
    private boolean killOrRestartTask(JobId jobId, String taskName, TerminateOptions options)
            throws UnknownJobException, UnknownTaskException {
        if (status == SchedulerStatus.UNLINKED || status == SchedulerStatus.KILLED) {
            return false;
        }

        logger_dev.info("Trying to kill task  '" + taskName + "' for job '" + jobId + "'");
        InternalJob job = jobs.get(jobId);

        if (job == null) {
            logger_dev.info("Job '" + jobId + "' does not exist");
            throw new UnknownJobException(jobId);
        }

        InternalTask task = job.getTask(taskName);

        if (task.getStatus() == TaskStatus.RUNNING) {
            this.terminate(task.getId(), options);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void changeJobPriority(JobId jobId, JobPriority priority) {
        logger_dev
                .info("Request sent to change priority on job '" + jobId + "' - new priority : " + priority);
        InternalJob job = jobs.get(jobId);
        job.setPriority(priority);
        DatabaseManager.getInstance().synchronize(job.getJobInfo());
        frontend.jobStateUpdated(job.getOwner(), new NotificationData<JobInfo>(
            SchedulerEvent.JOB_CHANGE_PRIORITY, job.getJobInfo()));
    }

    /**
     * {@inheritDoc}
     */
    public boolean changePolicy(String newPolicyClassName) {
        try {
            if (status == SchedulerStatus.KILLED || status == SchedulerStatus.SHUTTING_DOWN) {
                logger.warn("Policy can only be changed when Scheduler is up, current state : " + status);
                return false;
            }
            //TODO class loading ? (for now, class must be in scheduler classpath or addons)
            Policy newPolicy = (Policy) Class.forName(newPolicyClassName).newInstance();
            newPolicy.setCore(this);
            if (!newPolicy.reloadConfig()) {
                return false;
            }
            //if success, change current policy 
            policy = newPolicy;
            frontend.schedulerStateUpdated(SchedulerEvent.POLICY_CHANGED);
            logger_dev.info("Policy changed ! new policy name : " + newPolicyClassName);
            return true;
        } catch (InstantiationException e) {
            logger_dev.error("", e);
            throw new InternalException("Exception occurs while instanciating the policy !", e);
        } catch (IllegalAccessException e) {
            logger_dev.error("", e);
            throw new InternalException("Exception occurs while accessing the policy !", e);
        } catch (ClassNotFoundException e) {
            logger_dev.error("", e);
            throw new InternalException("Exception occurs while loading the policy class !", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean linkResourceManager(String rmURL) {
        //only if unlink
        if (status != SchedulerStatus.UNLINKED) {
            return false;
        }
        try {
            //re-link the RM
            rmProxiesManager = RMProxiesManager.rebindRMProxiesManager(new URI(rmURL.trim()));
            rmProxiesManager.getSchedulerRMProxy();
            logger
                    .info("New resource manager has been linked to the scheduler.\n\t-> Resume to continue the scheduling.");
            frontend.schedulerStateUpdated(SchedulerEvent.RM_UP);
            //restart the scheduler
            status = SchedulerStatus.STARTED;
            frontend.schedulerStateUpdated(SchedulerEvent.STARTED);
            return true;
        } catch (Exception e) {
            throw new InternalException("Error while connecting the new Resource Manager !", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean reloadPolicyConfiguration() {
        if (status == SchedulerStatus.KILLED || status == SchedulerStatus.SHUTTING_DOWN) {
            logger.warn("Policy configuration can only be reloaded when Scheduler is up, current state : " +
                status);
            return false;
        }
        return policy.reloadConfig();
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
            frontend.recover(new SchedulerStateImpl());
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
                    // restart classserver if needed
                    createTaskClassServer(job);
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
                    // restart classServer if needed
                    createTaskClassServer(job);

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
                    // restart classserver if needed
                    createTaskClassServer(job);
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
                            schedulerStub.removeJob(job.getId());
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

        SchedulerStateImpl sState = new SchedulerStateImpl();
        sState.setPendingJobs(convert(pendingJobs));
        sState.setRunningJobs(convert(runningJobs));
        sState.setFinishedJobs(convert(finishedJobs));
        //status is started by default
        frontend.recover(sState);

    }

    private Vector<JobState> convert(Vector<InternalJob> jobs) {
        Vector<JobState> jobs2 = new Vector<JobState>();
        for (InternalJob j : jobs) {
            jobs2.add(j);
        }
        return jobs2;
    }

    /**
     * Make a copy of the given argument with the restriction 'onlyFinished'.
     * As no task could be running after recover, this method also move task from RUNNING status to PENDING one.
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
                    case SKIPPED:
                        tasksList.add(task);
                }
            } else {
                tasksList.add(task);
            }
            //if task was running, put it in pending status
            if (task.getStatus() == TaskStatus.RUNNING) {
                task.setStatus(TaskStatus.PENDING);
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

}
