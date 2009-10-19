//============================================================================
// Name        : ProActive Embarrassingly Parallel Framework 
// Author      : Emil Salageanu, ActiveEon team
// Version     : 0.1
// Copyright   : Copyright ActiveEon 2008-2009, Tous Droits Réservés (All Rights Reserved)
// Description : Framework for building distribution layers for native applications
//================================================================================

package org.ow2.proactive.scheduler.ext.filessplitmerge.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.ext.filessplitmerge.exceptions.ExceptionToStringHelper;
import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;
import org.ow2.proactive.scheduler.ext.filessplitmerge.schedulertools.SchedulerProxyUserInterface;


/**
 * 		This (active) object subscribes as a listener to the Scheduler (using
 * {@link SchedulerProxyUserInterface}) and keeps its own view of the Scheduler.
 * Each time an event occurs (sent by the Scheduler) this object updates its
 * view of the scheduler and send a notification to its own listeners.<p>
 *  Objects that are interested in notifications from the scheduler should
 * subscribe as listeners to this object <p>
 * 		This object should be used in order to obtain information about the Scheduler state, the job running on the Scheduler, etc ...
 * 
 * @author esalagea
 * 
 */
public class InternalSchedulerEventListener extends Observable implements SchedulerEventListener {

    private static InternalSchedulerEventListener localView = null;

    // The shared instance view as an active object
    private static InternalSchedulerEventListener activeView = null;

    // jobs
    private Map<String, JobState> jobs = null;

    // jobs id
    private Vector<String> pendingJobsIds = null;
    private Vector<String> runningJobsIds = null;
    private Vector<String> finishedJobsIds = null;
    private boolean connected = false;

    // private UserSchedulerInterface uiScheduler;

    // -------------Constructor, active and passive view and init methods
    // ---------//
    // ----------------------------------------------------------------------------//

    /**
     * This constructor is not to be used by users of the class. It's only to be
     * used by the ProActive Framework <p> 
     *  Please use {@link #getActiveAndLocalReferences()} to obtain references to an object of this type
     */
    public InternalSchedulerEventListener() {

    }

    private static InternalSchedulerEventListener getLocalView() {
        if (localView == null) {
            localView = new InternalSchedulerEventListener();
        }
        return localView;
    }

    private static InternalSchedulerEventListener getActiveView() {
        if (activeView == null) {
            turnActive();
        }
        return activeView;
    }

    /**
     * creates the (java) object if it has not been created creates the active
     * object if it has not yet been created subscribe a stub on this listener
     * on the scheduler if it has not already been subscribed
     * 
     * @return a 2 values vector: the first value is the local reference, the
     *         second value is the active reference
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static final InternalSchedulerEventListener[] getActiveAndLocalReferences()
            throws ActiveObjectCreationException, NodeException {
        // System.out
        // .println("InternalSchedulerEventListener.getActiveAndLocalReferences()");
        final InternalSchedulerEventListener[] res = new InternalSchedulerEventListener[2];
        res[0] = InternalSchedulerEventListener.getLocalView();
        res[1] = InternalSchedulerEventListener.getActiveView();
        if (!res[1].isConnected().booleanValue()) {
            // System.out
            // .println("InternalSchedulerEventListener.getActiveAndLocalReferences()
            // -> not connected, calling init ");
            res[1].init();
        }
        // System.out.println("connected:
        // "+res[1].isConnected().booleanValue());
        return res;
    }

    private static InternalSchedulerEventListener turnActive() {
        try {
            activeView = (InternalSchedulerEventListener) PAActiveObject.turnActive(getLocalView());
            return activeView;
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  This method should be called on the active reference of this object
     *  
     */
    public boolean init() {

        SchedulerProxyUserInterface proxyUserInterface = null;
        try {
            proxyUserInterface = SchedulerProxyUserInterface.getActiveInstance();
        } catch (ActiveObjectCreationException e1) {
            LoggerManager.getInstane().error(
                    "ProActive exception occured while initializing gold listener. ", e1);
            return false;
        } catch (NodeException e1) {
            LoggerManager.getInstane().error(
                    "ProActive exception occured while initializing gold listener. ", e1);
            return false;
        }

        SchedulerState state = null;

        if ((proxyUserInterface == null) || (!proxyUserInterface.isConnected().booleanValue())) {
            LoggerManager.getLogger().warn(
                    "InternalSchedulerEventListener.init()-> userSchedulerInerface not connected.");
            LoggerManager.getInstane().error("Could not initiate Scheduler Event Listener. ");
            return false;
        }

        try {

            //TODO: put true for the second argument: listen only to my events
            //... and test 
            state = proxyUserInterface.addSchedulerEventListener((SchedulerEventListener) PAActiveObject
                    .getStubOnThis(), false);

            // LoggerManager.getInstane().info("The scheduler current state was
            // gathered: ");

            // state =
            // uiScheduler.addSchedulerEventListener((SchedulerEventListener)
            // PAActiveObject.getStubOnThis());
            connected = true;
        } catch (SchedulerException e) {
            LoggerManager.getLogger().error(ExceptionToStringHelper.getStackTrace(e));
        }

        if (state == null) { // addSchedulerEventListener failed
            connected = false;
            LoggerManager.getLogger().info(
                    "InternalSchedulerEventListener.init() -> could not get initial state from scheduler.");
            LoggerManager.getInstane().error("The scheduler state is inconsistent.  ");
            return false;
        }

        SchedulerStatus schedulerState = state.getStatus();
        switch (schedulerState) {
            case SHUTTING_DOWN:
                schedulerTerminatedEvent("SHUT DOWN");
                break;
            case KILLED:
                schedulerTerminatedEvent("KILLED");
                break;

            // case PAUSED:
            // break;
            // case FROZEN:
            // break;
            // case SHUTTING_DOWN:
            // break;
            // case STARTED:
            // break;
            // case STOPPED:
            // break;
        }

        jobs = Collections.synchronizedMap(new HashMap<String, JobState>());
        pendingJobsIds = new Vector<String>();
        runningJobsIds = new Vector<String>();
        finishedJobsIds = new Vector<String>();

        Vector<JobState> tmp = state.getPendingJobs();
        for (JobState job : tmp) {
            jobs.put(job.getId().value(), job);
            pendingJobsIds.add(job.getId().value());
        }

        tmp = state.getRunningJobs();
        for (JobState job : tmp) {
            jobs.put(job.getId().value(), job);
            runningJobsIds.add(job.getId().value());
        }

        tmp = state.getFinishedJobs();
        for (JobState job : tmp) {
            jobs.put(job.getId().value(), job);
            finishedJobsIds.add(job.getId().value());
        }

        // LoggerManager.getInstane().info("Pending jobs:
        // "+pendingJobsIds.size()+" | Running jobs: "+runningJobsIds.size()+" |
        // Finished jobs: "+finishedJobsIds.size());
        // LoggerManager.getInstane().info("GoldSchedulerEventlistener->init \n
        // Pending jobs: "+pendingJobsIds.toString()+" \n Running jobs:
        // "+runningJobsIds.toString()+" Finished jobs:
        // "+finishedJobsIds.toString()+" \n Jobs "+jobs.toString());

        // String allJobs = new ListAllJobsCmd("","").execute().getOutput();
        // LoggerManager.getInstane().info("The application has synchronnized
        // with the Scheduler. Jobs on the Scheduler are:\n"+allJobs);
        // System.out.println("The application has synchronnized with the
        // Scheduler. Jobs on the Scheduler are:\n"+allJobs);

        return true;

    }

    // ----------------------SchedulerEventListener methods------------------ //
    // -------------------------------------------------------------------- //

    //@Override
    public void jobSubmittedEvent(JobState job) {

        //		 System.out
        //		 .println("InternalSchedulerEventListener.jobSubmittedEvent()");

        LoggerManager.getInstane().debug(
                "InternalSchedulereventListener -> jobSubmittedEvent() " + job.getId());

        JobState internalJobViewOfJob = job;

        // add job to the global jobs map
        jobs.put(job.getId().value(), internalJobViewOfJob);
        // add job to the pending jobs list
        if (!pendingJobsIds.add(job.getId().value())) {
            throw new IllegalStateException("can't add the job (id = " + job.getId().value() +
                ") to the pendingJobsIds list !");
        }
        // call method on listeners

        this.setChanged();

        notifyObservers(new InternalEvent(EventType.jobSubmitted, job));

        // System.out.println("----------------------------------InternalSchedulerEventListener.jobSubmittedEvent()");
    }

    private void jobPendingToRunningEvent(JobInfo event) {
        // System.out.println("SchedulerData.jobPendingToRunningEvent()");
        final JobId jobId = event.getJobId();
        final JobState job = getJobById(jobId.value());

        job.update(event);
        // call method on listeners
        // removePendingJobInfoInternal(jobId);
        // remove job from the pending jobs list
        if (!pendingJobsIds.remove(jobId.value())) {
            throw new IllegalStateException("can't remove the job (id = " + jobId +
                ") from the pendingJobsIds list !");
        }

        // add job to running jobs list
        if (!runningJobsIds.add(jobId.value())) {
            throw new IllegalStateException("can't add the job (id = " + jobId +
                ") from the runningJobsIds list !");
        }

        this.setChanged();
        notifyObservers(new InternalEvent(EventType.jobPendingToRunning, job));
    }

    private void jobRunningToFinishedEvent(JobInfo event) {

        // System.out
        // .println("InternalSchedulerEventListener.jobRunningToFinishedEvent()");

        final JobId jobId = event.getJobId();
        final JobState job = getJobById(jobId.value());
        job.update(event);

        LoggerManager.getInstane().debug("SchedulerData.jobRunningToFinishedEvent()-> " + jobId);
        // call method on listeners
        // removeRunningJobInfoInternal(jobId);

        // remove job from the running jobs list
        if (!runningJobsIds.remove(jobId.value())) {
            throw new IllegalStateException("can't remove the job (id = " + jobId +
                ") from the runningJobsIds list !");
        }

        // add job to finished jobs list
        if (!finishedJobsIds.add(jobId.value())) {
            throw new IllegalStateException("can't add the job (id = " + jobId +
                ") from the finishedJobsIds list !");
        }

        this.setChanged();
        notifyObservers(new InternalEvent(EventType.jobRunningToFinishedEvent, job));
    }

    private void jobRemoveFinishedEvent(JobInfo event) {
        final JobId jobId = event.getJobId();

        // call method on listeners
        // removeFinishedJobEventInternal(jobId);

        // remove job from the jobs map
        if (jobs.remove(jobId.value()) == null) {
            throw new IllegalStateException("can't remove the job (id = " + jobId + ") from the jobs map !");
        }

        // remove job from the finished jobs list
        if (!finishedJobsIds.remove(jobId.value())) {
            throw new IllegalStateException("can't remove the job (id = " + jobId +
                ") from the finishedJobsIds list !");
        }
    }

    // private void jobKilledEvent(JobId aJobId) {
    // final JobId jobId = aJobId;
    //
    // JobState killedJob = this.getJobById(aJobId.value());
    //
    // Vector<String> list = null;
    //
    // if (pendingJobsIds.contains(jobId.value())) {
    // list = pendingJobsIds;
    // // removePendingJobEventInternal(jobId);
    // } else if (runningJobsIds.contains(jobId.value())) {
    // list = runningJobsIds;
    // // removeRunningJobEventInternal(jobId);
    // } else if (finishedJobsIds.contains(jobId.value())) {
    // list = finishedJobsIds;
    // // removeFinishedJobEventInternal(jobId);
    // }
    //
    // // remove job from the jobs map
    // if (jobs.remove(jobId.value()) == null) {
    // throw new IllegalStateException("can't remove the job (id = "
    // + jobId + ") from the jobs map !");
    // }
    //
    //		
    // // remove job from the specified jobs list
    // if (!list.remove(jobId.value())) {
    // throw new IllegalStateException("can't remove the job (id = "
    // + jobId + ") from the list !");
    // }
    //
    // this.setChanged();
    // notifyObservers(new InternalEvent(EventType.jobKilledEvent, killedJob));
    //
    // }

    private void schedulerTerminatedEvent(String state) {
        this.connected = false;
        LoggerManager.getInstane().info("The scheduler state  has changed to: " + state);
    }

    public static void clearInstances() {
        localView = null;
        activeView = null;
    }

    // ---- Methods for obtaining information on the Scheduler State --------//
    // ----------------------------------------------------------------------//

    public List<JobState> getRunningJobs() {
        LinkedList<JobState> jobs = new LinkedList<JobState>();
        Iterator<String> it = this.runningJobsIds.iterator();
        while (it.hasNext()) {
            String id = it.next();
            JobState ijob = this.getJobById(id);
            jobs.add(ijob);
        }

        return jobs;
    }

    public List<JobState> getPendingJobs() {
        LinkedList<JobState> jobs = new LinkedList<JobState>();
        Iterator<String> it = this.pendingJobsIds.iterator();
        while (it.hasNext()) {
            String id = it.next();
            JobState ijob = this.getJobById(id);
            jobs.add(ijob);
        }

        return jobs;
    }

    public List<JobState> getFinishedJobs() {
        LinkedList<JobState> jobs = new LinkedList<JobState>();
        Iterator<String> it = this.finishedJobsIds.iterator();
        while (it.hasNext()) {
            String id = it.next();
            JobState ijob = this.getJobById(id);
            jobs.add(ijob);
        }

        return jobs;
    }

    // ----------------------Other------------------ ------------------------//
    // -------------------------------------------------------------------- //

    public synchronized JobState getJobById(String id) {

        // System.out.println("Pending jobs: "+pendingJobsIds.toString());
        // System.out.println("Running jobs: "+runningJobsIds.toString());
        // System.out.println("Finished jobs: "+finishedJobsIds.toString());
        // System.out.println("All jobs: "+jobs.toString());
        //    	

        // LoggerManager.getInstane().info("GoldSchedulerEventlistener->getJobById("+id+")
        // \n Pending jobs: "+pendingJobsIds.toString()+" \n Running jobs:
        // "+runningJobsIds.toString()+" \n Finished jobs:
        // "+finishedJobsIds.toString()+" \n Jobs "+jobs.toString());

        JobState res = jobs.get(id);
        if (res == null)
            throw new IllegalArgumentException("there are no jobs with the id : " + id);
        return res;
    }

    public synchronized List<JobState> getJobsByIds(List<JobId> ids) {
        List<JobState> res = new ArrayList<JobState>();
        for (JobId id : ids)
            res.add(jobs.get(id));
        return res;
    }

    public BooleanWrapper isConnected() {
        return new BooleanWrapper(connected);
    }

    public static void disconnect() {
        if (!localView.connected) {
            LoggerManager.getInstane().warning(
                    "Could not disconnect GoldListener. Listener is not connected. ");
            return;
        }
        localView.connected = false;

        // UserSchedulerInterface userSchedulerInterface=null;
        // try {
        // userSchedulerInterface =
        // SchedulerProxyUserInterface.getActiveInstance();
        // //userSchedulerInterface.removeSchedulerEventListener();
        // } catch (ActiveObjectCreationException e1) {
        // LoggerManager.getInstane().warning("ProActive exception occured while
        // disconnecting gold listener. ",e1);
        // } catch (NodeException e1) {
        // LoggerManager.getInstane().warning("ProActive exception occured while
        // disconnecting gold listener. ",e1);
        // }
        // catch (SchedulerException e) {
        // LoggerManager.getInstane().warning("ProActive exception occured while
        // disconnecting gold listener. ",e);
        // }

        // PAActiveObject.terminateActiveObject(InternalSchedulerEventListener.activeView,false);
        // InternalSchedulerEventListener.localView=null;
        // InternalSchedulerEventListener.activeView=null;

        // LoggerManager.getLogger().info("InternalSchedulerEventListener has
        // been disonnected. ");
    }

    //@Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> arg0) {

        JobInfo data = arg0.getData();

        switch (arg0.getEventType()) {
            case JOB_PENDING_TO_RUNNING:
                jobPendingToRunningEvent(data);
                break;
            case JOB_RUNNING_TO_FINISHED:
                jobRunningToFinishedEvent(data);
                break;
            case JOB_RESUMED:
            case JOB_CHANGE_PRIORITY:
            case JOB_PAUSED:
                final JobState job = getJobById(data.getJobId().value());
                job.update(data);
                break;
            case JOB_REMOVE_FINISHED:
                jobRemoveFinishedEvent(data);
                break;
        }

    }

    //@Override
    public void schedulerStateUpdatedEvent(SchedulerEvent arg0) {
        switch (arg0) {
            case SHUTDOWN:
            case KILLED:
                this.schedulerTerminatedEvent(arg0.toString());
                break;
        }

    }

    //@Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> arg0) {
        switch (arg0.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                JobId jobId = arg0.getData().getJobId();
                getJobById(jobId.value()).update(arg0.getData());
                break;
        }
    }

    //@Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> arg0) {
        // TODO Auto-generated method stub

    }

}
