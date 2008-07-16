package jobsubmission;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.task.TaskEvent;


/**
 * SchedulerEventReceiver...
 *
 * @author The ProActive Team
 * @date 2 juil. 08
 * @since ProActive 4.0
 *
 */
public class SchedulerEventReceiver implements SchedulerEventListener, InitActive, RunActive {

    private MutableInteger nbEventReceived = new MutableInteger(0);

    private ArrayList<JobEvent> jobPendingToRunningEvents;

    private ArrayList<JobEvent> jobRunningToFinishedEvents;

    private ArrayList<Job> jobSubmittedEvents;

    private ArrayList<JobEvent> jobRemoveFinishedEvents;

    private ArrayList<TaskEvent> taskPendingToRunningEvents;

    private ArrayList<TaskEvent> taskRunningToFinishedEvents;

    private Vector<String> methodCalls;

    /**
     * ProActive Empty constructor
     */
    public SchedulerEventReceiver() {
        jobPendingToRunningEvents = new ArrayList<JobEvent>();
        jobRunningToFinishedEvents = new ArrayList<JobEvent>();
        jobSubmittedEvents = new ArrayList<Job>();
        jobRemoveFinishedEvents = new ArrayList<JobEvent>();

        taskPendingToRunningEvents = new ArrayList<TaskEvent>();
        taskRunningToFinishedEvents = new ArrayList<TaskEvent>();

        methodCalls = new Vector<String>();
        for (Method method : SchedulerEventListener.class.getMethods()) {
            methodCalls.add(method.getName());
        }
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("waitForNEvent");
    }

    /**
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service s = new Service(body);
        while (body.isActive()) {
            Request r = s.blockingRemoveOldest();
            s.serve(r);
            if (methodCalls.contains(r.getMethodName())) {
                System.out.println(" EventReceived : " + r.getMethodName());
                synchronized (this.nbEventReceived) {
                    this.nbEventReceived.add(1);
                    this.nbEventReceived.notify();
                }
            }
        }
    }

    /**
     * Wait for n events thrown by Scheduler Monitoring component
     *
     * @param nbEvents number of events awaited.
     * @throws InterruptedException
     */
    public void waitForNEvent(int nbEvents) throws InterruptedException {
        synchronized (this.nbEventReceived) {
            while ((this.nbEventReceived.getValue() < nbEvents)) {
                this.nbEventReceived.wait();
            }
            this.nbEventReceived.add(-nbEvents);
        }
    }

    /**
     * Get and remove the eventual 'job submitted' events received by this monitor.
     *
     * @return the eventual 'job submitted' events received by this monitor.
     */
    public ArrayList<Job> cleanNgetJobSubmittedEvents() {
        ArrayList<Job> toReturn = (ArrayList<Job>) this.jobSubmittedEvents.clone();
        this.jobSubmittedEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'Job Pending To Running' events received by this monitor.
     *
     * @return the eventual 'Job Pending To Running' events received by this monitor.
     */
    public ArrayList<JobEvent> cleanNgetJobPendingToRunningEvents() {
        ArrayList<JobEvent> toReturn = (ArrayList<JobEvent>) this.jobPendingToRunningEvents.clone();
        this.jobPendingToRunningEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'Task Pending To Running' events received by this monitor.
     *
     * @return the eventual 'Task Pending To Running' events received by this monitor.
     */
    public ArrayList<TaskEvent> cleanNgetTaskPendingToRunningEvents() {
        ArrayList<TaskEvent> toReturn = (ArrayList<TaskEvent>) this.taskPendingToRunningEvents.clone();
        this.taskPendingToRunningEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'Task Running To Finished' events received by this monitor.
     *
     * @return the eventual 'Task Running To Finished' events received by this monitor.
     */
    public ArrayList<TaskEvent> cleanNgetTaskRunningToFinishedEvents() {
        ArrayList<TaskEvent> toReturn = (ArrayList<TaskEvent>) this.taskRunningToFinishedEvents.clone();
        this.taskRunningToFinishedEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'job submitted' events received by this monitor.
     *
     * @return the eventual 'job submitted' events received by this monitor.
     */
    public ArrayList<JobEvent> cleanNgetjobRunningToFinishedEvents() {
        ArrayList<JobEvent> toReturn = (ArrayList<JobEvent>) this.jobRunningToFinishedEvents.clone();
        this.jobRunningToFinishedEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'job submitted' events received by this monitor.
     *
     * @return the eventual 'job submitted' events received by this monitor.
     */
    public ArrayList<JobEvent> cleanNgetjobRemoveFinishedEvents() {
        ArrayList<JobEvent> toReturn = (ArrayList<JobEvent>) this.jobRemoveFinishedEvents.clone();
        this.jobRemoveFinishedEvents.clear();
        return toReturn;
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobPendingToRunningEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPendingToRunningEvent(JobEvent event) {
        jobPendingToRunningEvents.add(event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobRemoveFinishedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRemoveFinishedEvent(JobEvent event) {
        jobRemoveFinishedEvents.add(event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobRunningToFinishedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobRunningToFinishedEvent(JobEvent event) {
        jobRunningToFinishedEvents.add(event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.Job)
     */
    public void jobSubmittedEvent(Job job) {
        jobSubmittedEvents.add(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskPendingToRunningEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskPendingToRunningEvent(TaskEvent event) {
        taskPendingToRunningEvents.add(event);
        // TODO Auto-generated method stub		
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskRunningToFinishedEvent(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskRunningToFinishedEvent(TaskEvent event) {
        taskRunningToFinishedEvents.add(event);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobChangePriorityEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobChangePriorityEvent(JobEvent event) {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobKilledEvent(org.ow2.proactive.scheduler.common.job.JobId)
     */
    public void jobKilledEvent(JobId jobId) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobPausedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobPausedEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#jobResumedEvent(org.ow2.proactive.scheduler.common.job.JobEvent)
     */
    public void jobResumedEvent(JobEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerFrozenEvent()
     */
    public void schedulerFrozenEvent() {
        // TODO Auto-generated method stub		
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerKilledEvent()
     */
    public void schedulerKilledEvent() {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
        // TODO Auto-generated method stub		
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#schedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#usersUpdate(org.ow2.proactive.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.scheduler.SchedulerEventListener#taskWaitingForRestart(org.ow2.proactive.scheduler.common.task.TaskEvent)
     */
    public void taskWaitingForRestart(TaskEvent event) {
        // TODO Auto-generated method stub

    }
}
