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

    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("waitForNEvent");
    }

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

    public void waitForNEvent(int nbEvents) throws InterruptedException {
        synchronized (this.nbEventReceived) {
            while ((this.nbEventReceived.getValue() < nbEvents)) {
                this.nbEventReceived.wait();
            }
            this.nbEventReceived.add(-nbEvents);
        }
    }

    public ArrayList<Job> cleanNgetJobSubmittedEvents() {
        ArrayList<Job> toReturn = (ArrayList<Job>) this.jobSubmittedEvents.clone();
        this.jobSubmittedEvents.clear();
        return toReturn;
    }

    public ArrayList<JobEvent> cleanNgetJobPendingToRunningEvents() {
        ArrayList<JobEvent> toReturn = (ArrayList<JobEvent>) this.jobPendingToRunningEvents.clone();
        this.jobPendingToRunningEvents.clear();
        return toReturn;
    }

    public ArrayList<TaskEvent> cleanNgetTaskPendingToRunningEvents() {
        ArrayList<TaskEvent> toReturn = (ArrayList<TaskEvent>) this.taskPendingToRunningEvents.clone();
        this.taskPendingToRunningEvents.clear();
        return toReturn;
    }

    public ArrayList<TaskEvent> cleanNgetTaskRunningToFinishedEvents() {
        ArrayList<TaskEvent> toReturn = (ArrayList<TaskEvent>) this.taskRunningToFinishedEvents.clone();
        this.taskRunningToFinishedEvents.clear();
        return toReturn;
    }

    public ArrayList<JobEvent> cleanNgetjobRunningToFinishedEvents() {
        ArrayList<JobEvent> toReturn = (ArrayList<JobEvent>) this.jobRunningToFinishedEvents.clone();
        this.jobRunningToFinishedEvents.clear();
        return toReturn;
    }

    public ArrayList<JobEvent> cleanNgetjobRemoveFinishedEvents() {
        ArrayList<JobEvent> toReturn = (ArrayList<JobEvent>) this.jobRemoveFinishedEvents.clone();
        this.jobRemoveFinishedEvents.clear();
        return toReturn;
    }

    public void jobPendingToRunningEvent(JobEvent event) {
        jobPendingToRunningEvents.add(event);
    }

    public void jobRemoveFinishedEvent(JobEvent event) {
        jobRemoveFinishedEvents.add(event);
    }

    public void jobRunningToFinishedEvent(JobEvent event) {
        jobRunningToFinishedEvents.add(event);
    }

    public void jobSubmittedEvent(Job job) {
        jobSubmittedEvents.add(job);
    }

    public void taskPendingToRunningEvent(TaskEvent event) {
        taskPendingToRunningEvents.add(event);
        // TODO Auto-generated method stub		
    }

    public void taskRunningToFinishedEvent(TaskEvent event) {
        taskRunningToFinishedEvents.add(event);
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

    public void jobResumedEvent(JobEvent event) {
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

    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub	
    }

    public void schedulerRMUpEvent() {
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

    public void usersUpdate(UserIdentification userIdentification) {
        // TODO Auto-generated method stub	
    }

    public void taskWaitingForRestart(TaskEvent event) {
        // TODO Auto-generated method stub

    }
}
