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
package org.ow2.proactive.scheduler.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerInitialState;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerState;
import org.ow2.proactive.scheduler.common.scheduler.Stats;
import org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface;
import org.ow2.proactive.resourcemanager.common.scripting.SelectionScript;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.core.db.AbstractSchedulerDB;
import org.ow2.proactive.scheduler.core.db.RecoverableState;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.DataBaseNotFoundException;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobDescriptor;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.job.TaskDescriptor;
import org.ow2.proactive.scheduler.policy.PolicyInterface;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.ProActiveTaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalNativeTask;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.classloading.TaskClassServer;
import org.ow2.proactive.scheduler.util.logforwarder.BufferedAppender;
import org.ow2.proactive.scheduler.util.logforwarder.SimpleLoggerServer;
import org.ow2.proactive.scheduler.util.SchedulerLoggers;


/**
 * <i><font size="-1" color="#FF0000">** Scheduler core ** </font></i>
 * This is the main active object of the scheduler implementation,
 * it communicates with the entity manager to acquire nodes and with a policy
 * to insert and get jobs from the queue.
 *
 * @author The ProActive Team
 * @version 3.9, Jun 27, 2007
 * @since ProActive 3.9
 */
public class SchedulerCore implements UserDeepInterface, AdminMethodsInterface, RunActive {

    /** Scheduler logger */
    public static final Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CORE);

    /** Scheduler main loop time out */
    private static final int SCHEDULER_TIME_OUT = PASchedulerProperties.SCHEDULER_TIME_OUT.getValueAsInt();

    /** Scheduler node ping frequency in ms. */
    private static final int SCHEDULER_NODE_PING_FREQUENCY = PASchedulerProperties.SCHEDULER_NODE_PING_FREQUENCY
            .getValueAsInt();

    /** Delay to wait for between getting a job result and removing the job concerned */
    private static final int SCHEDULER_REMOVED_JOB_DELAY = PASchedulerProperties.SCHEDULER_REMOVED_JOB_DELAY
            .getValueAsInt();

    /** Host name of the scheduler for logger system. */
    private String host = null;

    /** Selected port for connection logger system */
    private int port;

    /** Implementation of Resource Manager */
    private ResourceManagerProxy resourceManager;

    /** Scheduler front-end. */
    private SchedulerFrontend frontend;

    /** Scheduler current policy */
    private PolicyInterface policy;

    /** Path to the database configuration file */
    private String dataBaseConfigFile;

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

    /** Jobs that must be logged into the corresponding appender */
    private Hashtable<JobId, BufferedAppender> jobsToBeLogged = new Hashtable<JobId, BufferedAppender>();

    /** Currently running tasks for a given jobId*/
    private Hashtable<JobId, Hashtable<TaskId, TaskLauncher>> currentlyRunningTasks = new Hashtable<JobId, Hashtable<TaskId, TaskLauncher>>();

    /** ClassLoading */

    // temp directory for unjaring classpath
    private static final String tmpJarFilesDir = PASchedulerProperties.SCHEDULER_TMPDIR.getValueAsString();

    // contains taskCLassServer for currently running jobs
    private static Hashtable<JobId, TaskClassServer> classServers = new Hashtable<JobId, TaskClassServer>();

    /**
     * Return the task classserver for the job jid
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
     */
    private static void addTaskClassServer(JobId jid, byte[] userClasspathJarFile) throws SchedulerException {
        if (getTaskClassServer(jid) != null) {
            throw new SchedulerException("ClassServer alredy exists for job " + jid);
        }
        try {
            // create file
            File jarFile = new File(tmpJarFilesDir + jid.toString() + ".jar");
            FileOutputStream fos = new FileOutputStream(jarFile);
            fos.write(userClasspathJarFile);
            fos.flush();
            fos.close();
            // create server
            // add a remote ref to tcs
            RemoteObjectExposer<TaskClassServer> roe = new RemoteObjectExposer<TaskClassServer>(
                TaskClassServer.class.getName(), new TaskClassServer(jarFile.getAbsolutePath()));
            URI uri = RemoteObjectHelper.generateUrl(jid.toString());
            InternalRemoteRemoteObject rro = roe.createRemoteObject(uri);
            classServers.put(jid, (TaskClassServer) rro.getObjectProxy());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new SchedulerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (UnknownProtocolException e) {
            e.printStackTrace();
            throw new SchedulerException("Unable to create class server for job " + jid + " because " +
                e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
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
        File jarFile = new File(tmpJarFilesDir + jid.toString() + ".jar");
        if (jarFile.exists()) {
            jarFile.delete();
        }
        return (classServers.remove(jid) != null);
    }

    /**
     * ProActive empty constructor
     */
    public SchedulerCore() {
    }

    /**
     * Create a new scheduler Core with the given arguments.<br>
     * 
     * @param configFile the file that contains the description of the database.
     * @param imp the resource manager on which the scheduler will interact.
     * @param frontend a reference to the frontend.
     * @param policyFullName the fully qualified name of the policy to be used.
     */
    public SchedulerCore(String configFile, ResourceManagerProxy imp, SchedulerFrontend frontend,
            String policyFullName) {
        try {
            this.dataBaseConfigFile = configFile;
            this.resourceManager = imp;
            this.frontend = frontend;
            //logger
            host = ProActiveInet.getInstance().getInetAddress().getHostName();

            try {
                // redirect event only into JobLogs
                SimpleLoggerServer slf = SimpleLoggerServer.createLoggerServer();
                this.port = slf.getPort();
            } catch (IOException e) {
                logger.error("Cannot create logger server : " + e.getMessage());
                throw new RuntimeException(e);
            }

            this.policy = (PolicyInterface) Class.forName(policyFullName).newInstance();
            logger.info("Scheduler Core ready !");
        } catch (InstantiationException e) {
            logger.error("The policy class cannot be found : " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("The method cannot be accessed " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            logger.error("The class definition cannot be found, it might be due to case sentivity : " +
                e.getMessage());
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
            TaskEvent ev = currentJob.getHMTasks().get(id).getTaskInfo();

            if (ev.getStatus() != TaskState.RUNNING) {
                events.add(ev);
            }
        }

        // don't forget to set the task status modify to null
        currentJob.setTaskStatusModify(null);
        // used when a job has failed
        currentJob.setTaskFinishedTimeModify(null);
        // and to database
        AbstractSchedulerDB.getInstance().setJobAndTasksEvents(currentJob.getJobInfo(), events);
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        //Rebuild the scheduler if a crash has occurred.
        recover();

        //listen log as immediate Service.
        //PAActiveObject.setImmediateService("listenLog");
        Service service = new Service(body);

        //used to read the enumerate schedulerState in order to know when submit is possible.
        //have to be immediate service
        body.setImmediateService("isSubmitPossible");

        //set the filter for serveAll method (user action are privileged)
        RequestFilter filter = new MainLoopRequestFilter("submit", "terminate", "listenLog",
            "getSchedulerInitialState");
        createPingThread();

        // default scheduler state is started
        ((SchedulerCore) PAActiveObject.getStubOnThis()).start();

        do {
            service.blockingServeOldest();

            while ((state == SchedulerState.STARTED) || (state == SchedulerState.PAUSED)) {
                try {
                    service.serveAll(filter);
                    schedule();
                    //block the loop until a method is invoked and serve it
                    service.blockingServeOldest(SCHEDULER_TIME_OUT);
                } catch (Exception e) {
                    //this point is reached in case of big problem, sometimes unknown
                    logger
                            .warn("\nSchedulerCore.runActivity(MAIN_LOOP) caught an EXCEPTION - it will not terminate the body !");
                    e.printStackTrace();
                    //trying to check if RM is dead
                    try {
                        resourceManager.echo().stringValue();
                    } catch (Exception rme) {
                        resourceManager.shutdownProxy();
                        //if failed
                        freeze();
                        //scheduler functionality are reduced until now 
                        state = SchedulerState.UNLINKED;
                        logger
                                .warn("******************************\n"
                                    + "Resource Manager is no more available, Scheduler has been paused waiting for a resource manager to be reconnect\n"
                                    + "Scheduler is in critical state and its functionality are reduced : \n"
                                    + "\t-> use the linkResourceManager methode to reconnect a new one.\n"
                                    + "******************************");
                        frontend.schedulerRMDownEvent();
                    }
                    //other checks ?
                    //...
                }
            }
        } while ((state != SchedulerState.SHUTTING_DOWN) && (state != SchedulerState.KILLED));

        logger.info("[SCHEDULER] Scheduler is shutting down...");

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
                logger.info("SchedulerCore.runActivity(SHUTTING_DOWN)");
                e.printStackTrace();
            }
        }

        //stop the pinger thread.
        pinger.interrupt();
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
        PAActiveObject.terminateActiveObject(false);
        logger.info("[SCHEDULER] Scheduler is now shutdown !");
        //exit
        System.exit(0);
    }

    /**
     * Schedule computing method
     */
    private void schedule() {
        //get job Descriptor list with eligible jobs (running and pending)
        ArrayList<JobDescriptor> jobDescriptorList = new ArrayList<JobDescriptor>();

        for (InternalJob j : runningJobs) {
            jobDescriptorList.add(j.getJobDescriptor());
        }

        //if scheduler is paused it only finishes running jobs
        if (state != SchedulerState.PAUSED) {
            for (InternalJob j : pendingJobs) {
                jobDescriptorList.add(j.getJobDescriptor());
            }
        }

        //ask the policy all the tasks to be schedule according to the jobs list.
        Vector<? extends TaskDescriptor> taskRetrivedFromPolicy = policy.getOrderedTasks(jobDescriptorList);

        while (!taskRetrivedFromPolicy.isEmpty()) {
            int nbNodesToAskFor = 0;
            RMState rmState = resourceManager.getRMState();
            policy.RMState = rmState;
            int freeResourcesNb = rmState.getNumberOfFreeResources().intValue();
            if (freeResourcesNb == 0) {
                break;
            }
            int taskToCheck = 0;
            //select first task to define the selection script ID
            TaskDescriptor taskDescriptor = taskRetrivedFromPolicy.get(taskToCheck);
            InternalJob currentJob = jobs.get(taskDescriptor.getJobId());
            InternalTask internalTask = currentJob.getHMTasks().get(taskDescriptor.getId());
            InternalTask sentinel = internalTask;
            SelectionScript ss = internalTask.getSelectionScript();
            NodeSet ns = internalTask.getNodeExclusion();
            //if free resources are available and (selection script ID and Node Exclusion) are the same as the first
            while (freeResourcesNb > 0 &&
                (ss == internalTask.getSelectionScript() || (ss != null && ss.equals(internalTask
                        .getSelectionScript()))) &&
                (ns == internalTask.getNodeExclusion() || (ns != null && ns.equals(internalTask
                        .getNodeExclusion())))) {
                //last task to be launched
                sentinel = internalTask;
                if (internalTask.getNumberOfNodesNeeded() > freeResourcesNb) {
                    //TODO what do we want for proActive job?
                    //Wait until enough resources are free or <<- chosen for the moment
                    //get the node until number of needed resources is reached?
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
                taskDescriptor = taskRetrivedFromPolicy.get(taskToCheck);
                currentJob = jobs.get(taskDescriptor.getJobId());
                internalTask = currentJob.getHMTasks().get(taskDescriptor.getId());
            }

            NodeSet nodeSet = null;

            if (nbNodesToAskFor > 0) {
                logger.info("[SCHEDULING] Asking for " + nbNodesToAskFor + " node(s) with" +
                    ((ss == null) ? "out " : " ") + "verif script");

                //nodeSet = resourceManager.getAtMostNodes(nbNodesToAskFor, ss);
                nodeSet = resourceManager.getAtMostNodes(nbNodesToAskFor, ss, ns);

                logger.info("[SCHEDULING] Got " + nodeSet.size() + " node(s)");
            }
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

            try {
                while (nodeSet != null && !nodeSet.isEmpty()) {
                    taskDescriptor = taskRetrivedFromPolicy.get(0);
                    currentJob = jobs.get(taskDescriptor.getJobId());
                    internalTask = currentJob.getHMTasks().get(taskDescriptor.getId());

                    // Initialize the executable container
                    internalTask.getExecutableContainer().init(currentJob, internalTask);

                    node = nodeSet.get(0);
                    TaskLauncher launcher = null;

                    //if the job is a ProActive job and if all nodes can be launched at the same time
                    if ((currentJob.getType() == JobType.PROACTIVE) &&
                        (nodeSet.size() >= internalTask.getNumberOfNodesNeeded())) {
                        nodeSet.remove(0);
                        launcher = internalTask.createLauncher(node);
                        this.currentlyRunningTasks.get(internalTask.getJobId()).put(internalTask.getId(),
                                launcher);
                        NodeSet nodes = new NodeSet();

                        for (int i = 0; i < (internalTask.getNumberOfNodesNeeded() - 1); i++) {
                            nodes.add(nodeSet.remove(0));
                        }
                        internalTask.getExecuterInformations().addNodes(nodes);

                        // activate loggers for this task if needed
                        if (this.jobsToBeLogged.containsKey(currentJob.getId())) {
                            launcher.activateLogs(host, port);
                        }
                        currentJob.getJobResult().addTaskResult(
                                internalTask.getName(),
                                ((ProActiveTaskLauncher) launcher).doTask((SchedulerCore) PAActiveObject
                                        .getStubOnThis(), (JavaExecutableContainer) internalTask
                                        .getExecutableContainer(), nodes), internalTask.isPreciousResult());
                    } else if (currentJob.getType() != JobType.PROACTIVE) {
                        nodeSet.remove(0);
                        launcher = internalTask.createLauncher(node);
                        this.currentlyRunningTasks.get(internalTask.getJobId()).put(internalTask.getId(),
                                launcher);
                        // activate loggers for this task if needed
                        if (this.jobsToBeLogged.containsKey(currentJob.getId())) {
                            launcher.activateLogs(host, port);
                        }

                        //if job is TASKSFLOW, preparing the list of parameters for this task.
                        int resultSize = taskDescriptor.getParents().size();
                        if ((currentJob.getType() == JobType.TASKSFLOW) && (resultSize > 0)) {
                            TaskResult[] params = new TaskResult[resultSize];

                            for (int i = 0; i < resultSize; i++) {
                                //get parent task number i
                                InternalTask parentTask = currentJob.getHMTasks().get(
                                        taskDescriptor.getParents().get(i).getId());
                                //set the task result in the arguments array.
                                params[i] = currentJob.getJobResult().getAllResults().get(
                                        parentTask.getName());
                                //if this result has been clean, (extremely rare but possible).
                                if (params[i].getOuput() == null) {
                                    //get the result from database
                                    params[i] = AbstractSchedulerDB.getInstance().getTaskResult(
                                            parentTask.getId());
                                }
                            }
                            currentJob.getJobResult().addTaskResult(
                                    internalTask.getName(),
                                    launcher.doTask((SchedulerCore) PAActiveObject.getStubOnThis(),
                                            internalTask.getExecutableContainer(), params),
                                    internalTask.isPreciousResult());
                        } else {
                            currentJob.getJobResult().addTaskResult(
                                    internalTask.getName(),
                                    launcher.doTask((SchedulerCore) PAActiveObject.getStubOnThis(),
                                            internalTask.getExecutableContainer()),
                                    internalTask.isPreciousResult());
                        }
                    }

                    //if a task has been launched
                    if (launcher != null) {
                        logger.info("[SCHEDULER] New task started on " +
                            node.getNodeInformation().getVMInformation().getHostName() + " [ " +
                            internalTask.getId() + " ]");

                        // set the different informations on job
                        if (currentJob.getStartTime() == -1) {
                            // if it is the first task of this job
                            currentJob.start();
                            pendingJobs.remove(currentJob);
                            runningJobs.add(currentJob);
                            // send job event to front-end
                            frontend.jobPendingToRunningEvent(currentJob.getJobInfo());
                            //create tasks events list
                            updateTaskEventsList(currentJob);
                        }

                        // set the different informations on task
                        currentJob.startTask(internalTask, node.getNodeInformation().getVMInformation()
                                .getHostName());
                        // send task event to front-end
                        frontend.taskPendingToRunningEvent(internalTask.getTaskInfo());

                        //no need to set this state in database
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

            } catch (Exception e1) {
                e1.printStackTrace();
                //if we are here, it is that something append while launching the current task.
                logger.warn("Current node has failed due to node failure : " + node);
                //so get back the node to the resource manager
                resourceManager.freeDownNode(internalTask.getExecuterInformations().getNodeName());
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
                if ((td.getStatus() == TaskState.RUNNING) &&
                    !PAActiveObject.pingActiveObject(td.getExecuterInformations().getLauncher())) {
                    logger.info("[SCHEDULER] Node failed on job " + job.getId() + ", task [ " + td.getId() +
                        " ]");

                    //free execution node even if it is dead
                    resourceManager.freeDownNode(td.getExecuterInformations().getNodeName());

                    if (td.getRerunnableLeft() > 0) {
                        td.setRerunnableLeft(td.getRerunnableLeft() - 1);
                        td.setStatus(TaskState.WAITING);
                        frontend.taskWaitingForRestart(td.getTaskInfo());
                        job.reStartTask(td);
                        //TODO if the job is paused, send an event to the scheduler to notify that this task is now paused.
                    } else {
                        endJob(
                                job,
                                td,
                                "An error has occurred due to a node failure and the maximum amout of retries property has been reached.",
                                JobState.FAILED);
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
        return !((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.STOPPED));
    }

    /**
     * Submit a new job to the scheduler.
     *
     * @param job the job to be scheduled.
     * @throws SchedulerException
     */
    public void submit(InternalJob job) throws SchedulerException {

        // TODO cdelbe : create classserver only when job is running ?
        // create taskClassLoader for this job
        if (job.getEnv().getJobClasspath() != null) {
            SchedulerCore.addTaskClassServer(job.getId(), job.getEnv().getJobClasspathContent());
            // if the classserver creation fails, the submit is aborted
        }

        job.submit();
        // add job to core
        jobs.put(job.getId(), job);
        pendingJobs.add(job);

        //create job result storage
        JobResult jobResult = new JobResultImpl(job.getId());
        //store the job result until user get it
        job.setJobResult(jobResult);

        // create a running task table for this job
        this.currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());

        //create appender for this job if required 
        if (job.getLogFile() != null) {
            Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + job.getId());
            l.setAdditivity(false);

            if (l.getAppender(Log4JTaskLogs.JOB_APPENDER_NAME) == null) {
                BufferedAppender op = new BufferedAppender(Log4JTaskLogs.JOB_APPENDER_NAME, true);
                this.jobsToBeLogged.put(job.getId(), op);
                l.addAppender(op);

                try {
                    FileAppender fa = new FileAppender(Log4JTaskLogs.DEFAULT_LOG_LAYOUT, job.getLogFile(),
                        false);
                    op.addSink(fa);
                } catch (IOException e) {
                    logger.warn("[SCHEDULER] Cannot open log file " + job.getLogFile() + " : " +
                        e.getMessage());
                }
            }
        }

        //sending event to client
        frontend.jobSubmittedEvent(job);
        //and to data base
        AbstractSchedulerDB.getInstance().addJob(job);
        logger.info("[SCHEDULER] New job added (" + job.getName() + ") containing " +
            job.getTotalNumberOfTasks() + " tasks !");
    }

    /**
     * End the given job due to the given task failure.
     *
     * @param job the job to end.
     * @param td the task who has been the caused of failing.
     * @param errorMsg the error message to send in the task result.
     * @param jobState the type of the end for this job. (failed/canceled)
     */
    private void endJob(InternalJob job, InternalTask task, String errorMsg, JobState jobState) {
        TaskResult taskResult = null;

        for (InternalTask td : job.getTasks()) {
            if (td.getStatus() == TaskState.RUNNING) {
                //get the nodes that are used for this descriptor
                NodeSet nodes = td.getExecuterInformations().getNodes();

                //try to terminate the task
                try {
                    td.getExecuterInformations().getLauncher().terminate();
                } catch (Exception e) { /* Tested (nothing to do) */
                }

                //free every execution nodes
                resourceManager.freeNodes(nodes, td.getPostScript());

                //deleting tasks results except the one that causes the failure
                if (!td.getId().equals(task.getId())) {
                    job.getJobResult().removeResult(td.getName());
                }
                //if canceled, get the result of the canceled task
                if ((jobState == JobState.CANCELLED) && td.getId().equals(task.getId())) {
                    taskResult = job.getJobResult().getAllResults().get(task.getName());
                }
            }
        }

        taskResult = (TaskResult) PAFuture.getFutureValue(taskResult);
        //failed the job
        job.failed(task.getId(), jobState);

        //store the exception into jobResult
        if (jobState == JobState.FAILED) {
            taskResult = new TaskResultImpl(task.getId(), new Throwable(errorMsg), new SimpleTaskLogs("",
                errorMsg));
            job.getJobResult().addTaskResult(task.getName(), taskResult, task.isPreciousResult());
        } else {
            job.getJobResult().addTaskResult(task.getName(), taskResult, task.isPreciousResult());
        }

        //add the result in database
        AbstractSchedulerDB.getInstance().addTaskResult(taskResult);
        //clean the result to improve memory usage
        ((TaskResultImpl) taskResult).clean();
        //move the job
        runningJobs.remove(job);
        finishedJobs.add(job);

        // terminate loggers
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + job.getId());
        l.removeAllAppenders();
        this.jobsToBeLogged.remove(job.getId());

        // remove current running tasks
        // TODO cdelbe : When a job can be removed on failure ??
        // Other tasks' logs should remain available...
        this.currentlyRunningTasks.remove(job.getId());

        // Remove taskClassServer
        removeTaskClassServer(job.getId());

        //send event to listeners.
        frontend.jobRunningToFinishedEvent(job.getJobInfo());
        //create tasks events list
        updateTaskEventsList(job);
        logger.info("[SCHEDULER] Terminated job " + job.getId() + " (failed/Canceled) ");
    }

    /**
     * Invoke by a task when it is about to finish.
     * This method can be invoke just a little amount of time before the result arrival.
     * That's why it can block the execution but only for short time.
     *
     * @param taskId the identification of the executed task.
     */
    public void terminate(TaskId taskId) {
        int nativeIntegerResult = 0;
        JobId jobId = taskId.getJobId();
        InternalJob job = jobs.get(jobId);

        //if job has been canceled or failed, it is possible that a task has finished just before
        //the failure of the job. In this rare case, the job may not exist anymore.
        if (job == null) {
            return;
        }

        InternalTask descriptor = job.getHMTasks().get(taskId);
        // job might have already been removed if job has failed...
        Hashtable<TaskId, TaskLauncher> runningTasks = this.currentlyRunningTasks.get(jobId);
        if (runningTasks != null) {
            runningTasks.remove(taskId);
        } else {
            return;
        }
        try {
            //The task is terminated but it's possible to have to
            //wait for the future of the task result (TaskResult).
            //accessing to the taskResult could block current execution but for a very little time.
            //it is the time between the end of the task and the arrival of the future from the task.
            //
            //check if the task result future has an error due to node death.
            //if the node has died, a runtimeException is sent instead of the result
            TaskResult res = null;
            res = job.getJobResult().getAllResults().get(descriptor.getName());
            // unwrap future
            res = (TaskResult) PAFuture.getFutureValue(res);

            if (res != null) {
                // HANDLE DESCIPTORS
                res.setPreviewerClassName(descriptor.getResultPreview());
                res.setJobClasspath(job.getEnv().getJobClasspath()); // can be null
                if (PAException.isException(res)) {
                    //in this case, it is a node error.
                    //this is not user exception or usage,
                    //so we restart independently of re-runnable properties
                    logger.info("[SCHEDULER] Node failed on job " + jobId + ", task [ " + taskId + " ]");
                    //change status and update GUI
                    descriptor.setStatus(TaskState.WAITING);
                    frontend.taskWaitingForRestart(descriptor.getTaskInfo());
                    job.reStartTask(descriptor);
                    //free execution node even if it is dead
                    resourceManager.freeDownNode(descriptor.getExecuterInformations().getNodeName());
                    return;
                }
            }

            logger.info("[SCHEDULER] Terminated task on job " + jobId + " [ " + taskId + " ]");

            //if an exception occurred and the user wanted to cancel on exception, cancel the job.
            boolean errorOccured = false;
            try {
                if (descriptor instanceof InternalNativeTask) {
                    nativeIntegerResult = ((Integer) res.value());
                    // an error occurred if res is not 0
                    errorOccured = (nativeIntegerResult != 0);
                    if (nativeIntegerResult == -1) {
                        //in this case, the user is not responsible
                        //change status and update GUI
                        descriptor.setStatus(TaskState.WAITING);
                        frontend.taskWaitingForRestart(descriptor.getTaskInfo());
                        job.reStartTask(descriptor);
                        //free execution node even if it is dead
                        resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes(), descriptor
                                .getPostScript());
                        return;
                    }
                }
            } catch (Throwable e) {
                // An exception occurred during task execution (res.value() throws it)
                errorOccured = true;
            }

            //if an error occurred
            if (errorOccured) {
                //if job is cancelOnError and the task has not to restart
                if (job.isCancelOnError() && descriptor.getRestartOnError() == RestartMode.NOWHERE) {
                    endJob(
                            job,
                            descriptor,
                            "An error has occured due to a user error caught in the task and user wanted to cancel on error.",
                            JobState.CANCELLED);
                    return;
                }
                //if the task threw an exception OR is native and the result is an error code (1-255)
                if (res.hadException() ||
                    ((descriptor instanceof InternalNativeTask) && nativeIntegerResult > 0)) {
                    //if the task has to restart
                    if (descriptor.getRestartOnError() != RestartMode.NOWHERE) {
                        //check the number of reruns left
                        if (descriptor.getRerunnableLeft() <= 0) {
                            //if no rerun left, failed the job
                            endJob(
                                    job,
                                    descriptor,
                                    "An error occurred in your task and the maximum amout of retries property has been reached.",
                                    JobState.FAILED);
                            return;
                        }
                        if (descriptor.getRestartOnError() == RestartMode.ELSEWHERE) {
                            //if the task restart ELSEWHERE
                            descriptor.setNodeExclusion(descriptor.getExecuterInformations().getNodes());
                        }
                        resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes(), descriptor
                                .getPostScript());
                        //change status and update GUI
                        descriptor.setStatus(TaskState.WAITING);
                        frontend.taskWaitingForRestart(descriptor.getTaskInfo());

                        descriptor.setRerunnableLeft(descriptor.getRerunnableLeft() - 1);
                        RestartJobTimerTask jtt = new RestartJobTimerTask(job, descriptor);
                        new Timer().schedule(jtt, job.getNextWaitingTime());

                        //TODO if the job is paused, send an event to the scheduler to notify that this task is now paused.
                        return;
                    }
                }
            }

            //to be done before terminating the task, once terminated it is not running anymore..
            TaskDescriptor currentTD = job.getRunningTaskDescriptor(taskId);
            descriptor = job.terminateTask(taskId);
            //send event
            frontend.taskRunningToFinishedEvent(descriptor.getTaskInfo());
            //store this task result in the job result.
            job.getJobResult().addTaskResult(descriptor.getName(), res, descriptor.isPreciousResult());
            //and to data base
            AbstractSchedulerDB.getInstance().setTaskEvent(descriptor.getTaskInfo());
            AbstractSchedulerDB.getInstance().addTaskResult(res);

            //clean the result to improve memory usage
            if (!job.getJobDescriptor().hasChildren(descriptor.getId())) {
                ((TaskResultImpl) res).clean();
            }
            for (TaskDescriptor td : currentTD.getParents()) {
                if (td.getChildrenCount() == 0) {
                    ((TaskResultImpl) job.getJobResult().getAllResults().get(td.getId().getReadableName()))
                            .clean();
                }
            }

            //if this job is finished (every task are finished)
            if (job.getNumberOfFinishedTask() == job.getTotalNumberOfTasks()) {
                //terminating job
                job.terminate();
                runningJobs.remove(job);
                finishedJobs.add(job);
                logger.info("[SCHEDULER] Terminated job " + jobId);

                // terminate loggers
                Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + job.getId());
                l.removeAllAppenders(); // appender are closed...
                //send event to listeners.

                this.jobsToBeLogged.remove(jobId);
                // TODO cdelbe : race condition with GUI ? Logger are removed before
                // job is removed from GUI ...
                this.currentlyRunningTasks.remove(jobId);

                // Remove taskClassServer
                removeTaskClassServer(job.getId());

                frontend.jobRunningToFinishedEvent(job.getJobInfo());
                //and to data base
                AbstractSchedulerDB.getInstance().setJobEvent(job.getJobInfo());
                //clean every task result
                for (TaskResult tr : job.getJobResult().getAllResults().values()) {
                    ((TaskResultImpl) tr).clean();
                }
            }

            //free every execution nodes
            resourceManager.freeNodes(descriptor.getExecuterInformations().getNodes(), descriptor
                    .getPostScript());
        } catch (NullPointerException eNull) {
            //the task has been killed. Nothing to do anymore with this one.
        }
    }

    /**
     * Return the scheduler current state with the pending, running, finished jobs list.
     *
     * @return the scheduler current state with the pending, running, finished jobs list.
     */
    public SchedulerInitialState<? extends Job> getSchedulerInitialState() {
        SchedulerInitialState<InternalJob> sState = new SchedulerInitialState<InternalJob>();
        sState.setPendingJobs(pendingJobs);
        sState.setRunningJobs(runningJobs);
        sState.setFinishedJobs(finishedJobs);
        sState.setState(state);
        return sState;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#listenLog(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String, int)
     */
    public void listenLog(JobId jobId, String hostname, int port) {
        logger.info("[SCHEDULER] listen logs of job[" + jobId + "]");
        BufferedAppender bufferForJobId = this.jobsToBeLogged.get(jobId);
        Logger l = null;
        if (bufferForJobId == null) {
            // can be not null if a log file has been defined for this job
            // or created by previous call to listenLog
            bufferForJobId = new BufferedAppender(Log4JTaskLogs.JOB_APPENDER_NAME, true);
            this.jobsToBeLogged.put(jobId, bufferForJobId);
            l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + jobId);
            l.setAdditivity(false);
            l.addAppender(bufferForJobId);

            InternalJob target = this.jobs.get(jobId);
            if ((target != null) && !this.pendingJobs.contains(target)) {
                // this jobs contains running and finished tasks
                // for running tasks, activate loggers on taskLauncher side
                Hashtable<TaskId, TaskLauncher> curRunning = this.currentlyRunningTasks.get(jobId);

                // for running tasks
                if (curRunning != null) {
                    Collection<TaskLauncher> runningTasks = curRunning.values();
                    for (TaskLauncher tl : runningTasks) {
                        tl.activateLogs(host, port);
                    }
                }

                // for finished tasks, add logs events "manually"
                Collection<TaskResult> allRes = target.getJobResult().getAllResults().values();
                for (TaskResult tr : allRes) {
                    // if taskResult is not awaited, task is terminated
                    if (!PAFuture.isAwaited(tr)) {
                        // mmm, well... It's only half-generic for the moment,
                        // but I don't despair...
                        //Log4JTaskLogs logs = (Log4JTaskLogs) (tr.getOuput());
                        Log4JTaskLogs logs = (Log4JTaskLogs) (AbstractSchedulerDB.getInstance()
                                .getTaskResult(tr.getTaskId()).getOuput());
                        for (LoggingEvent le : logs.getAllEvents()) {
                            bufferForJobId.doAppend(le);
                        }
                    }
                }
            }
        }
        // connect to client side logger 
        // TODO should connect to socket before flushing finished logs
        bufferForJobId.addSink(new SocketAppender(hostname, port));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#getJobResult(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public JobResult getJobResult(JobId jobId) {
        JobResult result = null;
        final InternalJob job = jobs.get(jobId);

        if (job != null) {
            result = AbstractSchedulerDB.getInstance().getJobResult(job.getId());
            logger.info("[SCHEDULER] GetJobResult of job[" + jobId + "]");
        }

        try {
            //remove job after the given delay
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    remove(job.getId());
                }
            };
            new Timer().schedule(tt, SCHEDULER_REMOVED_JOB_DELAY);
            logger.info("[SCHEDULER] Job " + jobId + " will be removed in " +
                (SCHEDULER_REMOVED_JOB_DELAY / 1000) + "sec");
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#remove(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void remove(JobId jobId) {
        InternalJob job = jobs.get(jobId);

        if (job != null) {
            jobs.remove(jobId);
            job.setRemovedTime(System.currentTimeMillis());
            finishedJobs.remove(job);
            //send event to front-end
            frontend.jobRemoveFinishedEvent(job.getJobInfo());
            //and to data base
            AbstractSchedulerDB.getInstance().setJobEvent(job.getJobInfo());
            // close log buffer
            BufferedAppender jobLog = this.jobsToBeLogged.remove(jobId);
            if (jobLog != null) {
                jobLog.close();
            }
            logger.info("[SCHEDULER] job " + jobId + " removed !");
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#getTaskResult(org.ow2.proactive.scheduler.common.job.JobId, java.lang.String)
     */
    public TaskResult getTaskResult(JobId jobId, String taskName) {
        logger.info("[SCHEDULER] trying to getTaskResult of task [" + taskName + "] for job[" + jobId + "]");
        TaskResult result = null;
        InternalJob job = jobs.get(jobId);

        if (job != null) {
            //extract taskResult reference from memory (weak instance) 
            //useful to get the task result with the task name
            result = job.getJobResult().getAllResults().get(taskName);
            if (PAFuture.isAwaited(result)) {
                //the result is not yet available
                return null;
            }
            //extract full taskResult from DB
            //use the previous result to get the task Id matching the given name.
            result = AbstractSchedulerDB.getInstance().getTaskResult(result.getTaskId());

            if ((result != null)) {
                logger.info("[SCHEDULER] Get '" + taskName + "' task result for job " + jobId);
            }
        }

        return result;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#start()
     */
    public BooleanWrapper start() {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if (state != SchedulerState.STOPPED) {
            return new BooleanWrapper(false);
        }

        state = SchedulerState.STARTED;
        logger.info("[SCHEDULER] Scheduler has just been started !");
        frontend.schedulerStartedEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#stop()
     */
    public BooleanWrapper stop() {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.STOPPED) || (state == SchedulerState.SHUTTING_DOWN) ||
            (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }

        state = SchedulerState.STOPPED;
        logger.info("Scheduler has just been stopped, no tasks will be launched until start.");
        frontend.schedulerStoppedEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#pause()
     */
    public BooleanWrapper pause() {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }

        if ((state != SchedulerState.FROZEN) && (state != SchedulerState.STARTED)) {
            return new BooleanWrapper(false);
        }

        state = SchedulerState.PAUSED;
        logger.info("[SCHEDULER] Scheduler has just been paused !");
        frontend.schedulerPausedEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#freeze()
     */
    public BooleanWrapper freeze() {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }

        if ((state != SchedulerState.PAUSED) && (state != SchedulerState.STARTED)) {
            return new BooleanWrapper(false);
        }

        state = SchedulerState.FROZEN;
        logger.info("[SCHEDULER] Scheduler has just been frozen !");
        frontend.schedulerFrozenEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#resume()
     */
    public BooleanWrapper resume() {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }

        if ((state != SchedulerState.PAUSED) && (state != SchedulerState.FROZEN) &&
            (state != SchedulerState.STARTED)) {
            return new BooleanWrapper(false);
        }

        state = SchedulerState.STARTED;
        logger.info("Scheduler has just been resumed !");
        frontend.schedulerResumedEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#shutdown()
     */
    public BooleanWrapper shutdown() {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.KILLED) || (state == SchedulerState.SHUTTING_DOWN)) {
            return new BooleanWrapper(false);
        }

        state = SchedulerState.SHUTTING_DOWN;
        logger.info("Scheduler is shutting down, this make take time to finish every jobs !");
        frontend.schedulerShuttingDownEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#kill()
     */
    public synchronized BooleanWrapper kill() {
        if (state == SchedulerState.KILLED) {
            return new BooleanWrapper(false);
        }

        //destroying running active object launcher
        for (InternalJob j : runningJobs) {
            for (InternalTask td : j.getTasks()) {
                if (td.getStatus() == TaskState.RUNNING) {
                    try {
                        NodeSet nodes = td.getExecuterInformations().getNodes();

                        try {
                            td.getExecuterInformations().getLauncher().terminate();
                        } catch (Exception e) { /* Tested, nothing to do */
                        }

                        try {
                            resourceManager.freeNodes(nodes, td.getPostScript());
                        } catch (Exception e) {
                            try {
                                // try to get the node back to the IM
                                resourceManager.freeNodes(td.getExecuterInformations().getNodes());
                            } catch (Exception e1) {
                                resourceManager.freeDownNode(td.getExecuterInformations().getNodeName());
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
        jobsToBeLogged.clear();
        currentlyRunningTasks.clear();
        //finally : shutdown
        state = SchedulerState.KILLED;
        logger.info("[SCHEDULER] Scheduler has just been killed !");
        frontend.schedulerKilledEvent();

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#pause(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper pause(JobId jobId) {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.KILLED)) {
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
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#resume(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public BooleanWrapper resume(JobId jobId) {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }

        InternalJob job = jobs.get(jobId);

        if (finishedJobs.contains(job)) {
            return new BooleanWrapper(false);
        }

        boolean change = job.setUnPause();
        JobEvent event = job.getJobInfo();

        if (change) {
            logger.info("[SCHEDULER] Job " + jobId + " has just been resumed !");
        }

        frontend.jobResumedEvent(event);
        //create tasks events list
        updateTaskEventsList(job);

        return new BooleanWrapper(change);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#kill(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public synchronized BooleanWrapper kill(JobId jobId) {
        if (state == SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }

        if ((state == SchedulerState.SHUTTING_DOWN) || (state == SchedulerState.KILLED)) {
            return new BooleanWrapper(false);
        }

        InternalJob job = jobs.get(jobId);
        jobs.remove(jobId);

        for (InternalTask td : job.getTasks()) {
            if (td.getStatus() == TaskState.RUNNING) {
                try {
                    //get the nodes that are used for this descriptor
                    NodeSet nodes = td.getExecuterInformations().getNodes();

                    //try to terminate the task
                    try {
                        td.getExecuterInformations().getLauncher().terminate();
                    } catch (Exception e) { /* Tested (nothing to do) */
                    }

                    //free every execution nodes
                    resourceManager.freeNodes(nodes, td.getPostScript());
                } catch (Exception e) {
                    resourceManager.freeNodes(td.getExecuterInformations().getNodes());
                }
            }
        }

        if (runningJobs.remove(job) || pendingJobs.remove(job) || finishedJobs.remove(job)) {
            ;
        }

        // terminate loggers
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + job.getId());
        l.removeAllAppenders();
        this.jobsToBeLogged.remove(job.getId());

        // remove current running tasks
        this.currentlyRunningTasks.remove(job.getId());

        logger.info("[SCHEDULER] Job " + jobId + " has just been killed !");
        frontend.jobKilledEvent(jobId);
        AbstractSchedulerDB.getInstance().removeJob(jobId);

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.UserDeepInterface#changePriority(org.ow2.proactive.scheduler.common.job.JobId, org.ow2.proactive.scheduler.common.job.JobPriority)
     */
    public void changePriority(JobId jobId, JobPriority priority) {
        InternalJob job = jobs.get(jobId);
        job.setPriority(priority);
        frontend.jobChangePriorityEvent(job.getJobInfo());
        AbstractSchedulerDB.getInstance().setJobEvent(job.getJobInfo());
    }

    /**
     * Change the policy of the scheduler.<br>
     * This method will immediately change the policy and so the whole scheduling process.
     *
     * @param newPolicyFile the new policy file as a string.
     * @return true if the policy has been correctly change, false if not.
     * @throws SchedulerException (can be due to insufficient permission)
     */
    public BooleanWrapper changePolicy(Class<? extends PolicyInterface> newPolicyFile)
            throws SchedulerException {
        try {
            policy = newPolicyFile.newInstance();
        } catch (InstantiationException e) {
            throw new SchedulerException("Exception occurs while instanciating the policy !");
        } catch (IllegalAccessException e) {
            throw new SchedulerException("Exception occurs while accessing the policy !");
        }

        return new BooleanWrapper(true);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.AdminMethodsInterface#linkResourceManager(java.lang.String)
     */
    public BooleanWrapper linkResourceManager(String rmURL) throws SchedulerException {
        //only if unlink
        if (state != SchedulerState.UNLINKED) {
            return new BooleanWrapper(false);
        }
        try {
            ResourceManagerProxy imp = ResourceManagerProxy.getProxy(new URI(rmURL.trim()));
            //re-link the RM
            resourceManager = imp;
            state = SchedulerState.FROZEN;
            logger
                    .info("New resource manager has been linked to the scheduler.\n\t-> Resume to continue the scheduling.");
            frontend.schedulerRMUpEvent();
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
        //connect to data base
        AbstractSchedulerDB dataBase;
        try {
            dataBase = AbstractSchedulerDB.getInstance(dataBaseConfigFile);
        } catch (DataBaseNotFoundException e) {
            //if the database doesn't exist
            logger.info("[SCHEDULER] *********  ERROR ********** " + e.getMessage());
            kill();
            return;
        }

        RecoverableState recoverable = dataBase.getRecoverableState();

        if (recoverable == null) {
            logger.info("[SCHEDULER-RECOVERY-SYSTEM] No recoverable state.");
            frontend.recover(null);

            return;
        }

        // Recover the scheduler core
        //------------------------------------------------------------------------
        //----------------------    Re-build jobs lists  --------------------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Re-build jobs lists");

        JobId maxId = JobId.makeJobId("0");

        for (InternalJob job : recoverable.getJobs()) {
            jobs.put(job.getId(), job);

            //search last JobId
            if (job.getId().compareTo(maxId) > 0) {
                maxId = job.getId();
            }
        }

        //------------------------------------------------------------------------
        //--------------------    Initialize jobId count   ----------------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Initialize jobId count");
        JobId.setInitialValue(maxId);
        //------------------------------------------------------------------------
        //--------    Re-affect JobEvent/taskEvent to the jobs/tasks   -----------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Re-affect JobEvent/taskEvent to the jobs/tasks");

        for (Entry<TaskId, TaskEvent> entry : recoverable.getTaskEvents().entrySet()) {
            try {
                jobs.get(entry.getKey().getJobId()).update(entry.getValue());
            } catch (NullPointerException e) {
                //do nothing, the job has not to be managed anymore
                //the job stays in database until someone removed it (1)
                //its job result can be get until previous removing (1).
            }
        }

        for (Entry<JobId, JobEvent> entry : recoverable.getJobEvents().entrySet()) {
            try {
                jobs.get(entry.getKey()).update(entry.getValue());
            } catch (NullPointerException e) {
                //same thing
            }
        }

        //------------------------------------------------------------------------
        //-----------    Re-build pending/running/finished lists  ----------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Re-build jobs lists");

        for (InternalJob job : jobs.values()) {
            switch (job.getState()) {
                case PENDING:
                    pendingJobs.add(job);
                    currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());
                    // restart classserver if needed
                    if (job.getEnv().getJobClasspath() != null) {
                        try {
                            SchedulerCore.addTaskClassServer(job.getId(), job.getEnv()
                                    .getJobClasspathContent());
                        } catch (SchedulerException e) {
                            // TODO cdelbe : exception handling ?
                            e.printStackTrace();
                        }
                    }
                    break;
                case STALLED:
                case RUNNING:
                    runningJobs.add(job);
                    currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());

                    //reset the finished events in the order they they have occurred
                    ArrayList<InternalTask> tasksList = copyAndSort(job.getTasks(), true);

                    for (InternalTask task : tasksList) {
                        job.update(task.getTaskInfo());
                    }

                    // restart classserver if needed
                    if (job.getEnv().getJobClasspath() != null) {
                        try {
                            SchedulerCore.addTaskClassServer(job.getId(), job.getEnv()
                                    .getJobClasspathContent());
                        } catch (SchedulerException e) {
                            // TODO cdelbe : exception handling ?
                            e.printStackTrace();
                        }
                    }

                    break;
                case FINISHED:
                case CANCELLED:
                case FAILED:
                    finishedJobs.add(job);
                    break;
                case PAUSED:
                    if ((job.getNumberOfPendingTask() + job.getNumberOfRunningTask() + job
                            .getNumberOfFinishedTask()) == 0) {
                        pendingJobs.add(job);
                    } else {
                        runningJobs.add(job);

                        //reset the finished events in the order they have occurred
                        ArrayList<InternalTask> tasksListP = copyAndSort(job.getTasks(), true);

                        for (InternalTask task : tasksListP) {
                            job.update(task.getTaskInfo());
                        }
                    }
                    // restart classserver if needed
                    if (job.getEnv().getJobClasspath() != null) {
                        try {
                            SchedulerCore.addTaskClassServer(job.getId(), job.getEnv()
                                    .getJobClasspathContent());
                        } catch (SchedulerException e) {
                            // TODO cdelbe : exception handling ?
                            e.printStackTrace();
                        }
                    }
            }
        }

        //------------------------------------------------------------------------
        //------------------    Re-create task dependences   ---------------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Re-create task dependences");

        for (InternalJob job : runningJobs) {
            ArrayList<InternalTask> tasksList = copyAndSort(job.getTasks(), true);

            //simulate the running execution to recreate the tree.
            for (InternalTask task : tasksList) {
                job.simulateStartAndTerminate(task.getId());
            }

            if ((job.getState() == JobState.RUNNING) || (job.getState() == JobState.PAUSED)) {
                //set the state to stalled because the scheduler start in stopped mode.
                if (job.getState() == JobState.RUNNING) {
                    job.setState(JobState.STALLED);
                }

                //set the task to pause inside the job if it is paused.
                if (job.getState() == JobState.PAUSED) {
                    job.setState(JobState.STALLED);
                    job.setPaused();
                    job.setTaskStatusModify(null);
                }

                //update the count of pending and running task.
                job.setNumberOfPendingTasks(job.getNumberOfPendingTask() + job.getNumberOfRunningTask());
                job.setNumberOfRunningTasks(0);
            }
        }

        for (InternalJob job : pendingJobs) {
            //set the task to pause inside the job if it is paused.
            if (job.getState() == JobState.PAUSED) {
                job.setState(JobState.STALLED);
                job.setPaused();
                job.setTaskStatusModify(null);
            }
        }

        //------------------------------------------------------------------------
        //--------------    Re-set job results list in each job   ----------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Re-set job results list in each job");

        for (JobResult result : recoverable.getJobResults()) {
            try {
                jobs.get(result.getId()).setJobResult(result);
            } catch (NullPointerException e) {
                //same thing
            }
        }

        //------------------------------------------------------------------------
        //-------------    Re-set jobEvent reference to all task   ---------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Re-set Job event reference to all task");

        for (InternalJob job : jobs.values()) {
            JobEvent event = job.getJobInfo();

            //for each tasks, set the same reference to the jobEvent.
            for (InternalTask tasks : job.getTasks()) {
                tasks.setJobInfo(event);
            }
        }

        //------------------------------------------------------------------------
        //---------    Removed non-managed jobs (result has been sent)   ---------
        //------------------------------------------------------------------------
        Iterator<InternalJob> iterJob = jobs.values().iterator();

        while (iterJob.hasNext()) {
            InternalJob job = iterJob.next();

            if (job.getRemovedTime() > 0) {
                //        		jobs.remove(job.getId());
                iterJob.remove();
                finishedJobs.remove(job);
            }
        }

        //------------------------------------------------------------------------
        //-----------------    Recover the scheduler front-end   -----------------
        //------------------------------------------------------------------------
        logger.info("[SCHEDULER-RECOVERY-SYSTEM] Recover the scheduler front-end");

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
                    case CANCELED:
                    case FAILED:
                    case FINISHED:
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
     * @version 3.2
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

    //UNUSED OVERRIDED METHOD
    //here to keep consistency in the architecture
    /**
     * UNUSED OVERRIDED METHOD
     *
     * @param sel /
     * @param events /
     * @return /
     * @throws SchedulerException /
     */
    public SchedulerInitialState<? extends Job> addSchedulerEventListener(
            SchedulerEventListener<? extends Job> sel, SchedulerEvent... events) throws SchedulerException {
        return null;
    }

    /**
     * UNUSED OVERRIDED METHOD
     *
     * @throws SchedulerException /
     */
    public void disconnect() throws SchedulerException {
    }

    /**
     * UNUSED OVERRIDED METHOD
     *
     * @return /
     * @throws SchedulerException /
     */
    public Stats getStats() throws SchedulerException {
        return null;
    }

    /**
     * UNUSED OVERRIDED METHOD
     *
     * @param job /
     * @return /
     * @throws SchedulerException /
     */
    public JobId submit(Job job) throws SchedulerException {
        return null;
    }
}
