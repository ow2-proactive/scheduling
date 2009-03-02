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
package functionnaltests;

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
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


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

    private ArrayList<JobInfo> jobPendingToRunningEvents;

    private ArrayList<JobInfo> jobRunningToFinishedEvents;

    private ArrayList<Job> jobSubmittedEvents;

    private ArrayList<JobInfo> jobRemoveFinishedEvents;

    private ArrayList<TaskInfo> taskPendingToRunningEvents;

    private ArrayList<TaskInfo> taskRunningToFinishedEvents;

    private ArrayList<TaskInfo> taskWaitingForRestartEvents;

    private Vector<String> methodCalls;

    private ArrayList<SchedulerEvent> miscEvents;

    /**
     * ProActive Empty constructor
     */
    public SchedulerEventReceiver() {
        jobPendingToRunningEvents = new ArrayList<JobInfo>();
        jobRunningToFinishedEvents = new ArrayList<JobInfo>();
        jobSubmittedEvents = new ArrayList<Job>();
        jobRemoveFinishedEvents = new ArrayList<JobInfo>();
        taskWaitingForRestartEvents = new ArrayList<TaskInfo>();
        taskPendingToRunningEvents = new ArrayList<TaskInfo>();
        taskRunningToFinishedEvents = new ArrayList<TaskInfo>();
        miscEvents = new ArrayList<SchedulerEvent>();

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
    public ArrayList<JobInfo> cleanNgetJobPendingToRunningEvents() {
        ArrayList<JobInfo> toReturn = (ArrayList<JobInfo>) this.jobPendingToRunningEvents.clone();
        this.jobPendingToRunningEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'Task Pending To Running' events received by this monitor.
     *
     * @return the eventual 'Task Pending To Running' events received by this monitor.
     */
    public ArrayList<TaskInfo> cleanNgetTaskPendingToRunningEvents() {
        ArrayList<TaskInfo> toReturn = (ArrayList<TaskInfo>) this.taskPendingToRunningEvents.clone();
        this.taskPendingToRunningEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'Task Running To Finished' events received by this monitor.
     *
     * @return the eventual 'Task Running To Finished' events received by this monitor.
     */
    public ArrayList<TaskInfo> cleanNgetTaskRunningToFinishedEvents() {
        ArrayList<TaskInfo> toReturn = (ArrayList<TaskInfo>) this.taskRunningToFinishedEvents.clone();
        this.taskRunningToFinishedEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'job submitted' events received by this monitor.
     *
     * @return the eventual 'job submitted' events received by this monitor.
     */
    public ArrayList<JobInfo> cleanNgetjobRunningToFinishedEvents() {
        ArrayList<JobInfo> toReturn = (ArrayList<JobInfo>) this.jobRunningToFinishedEvents.clone();
        this.jobRunningToFinishedEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'job submitted' events received by this monitor.
     *
     * @return the eventual 'job submitted' events received by this monitor.
     */
    public ArrayList<JobInfo> cleanNgetjobRemoveFinishedEvents() {
        ArrayList<JobInfo> toReturn = (ArrayList<JobInfo>) this.jobRemoveFinishedEvents.clone();
        this.jobRemoveFinishedEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'job submitted' events received by this monitor.
     *
     * @return the eventual 'job submitted' events received by this monitor.
     */
    public ArrayList<TaskInfo> cleanNgetTaskWaitingForRestartEvents() {
        ArrayList<TaskInfo> toReturn = (ArrayList<TaskInfo>) this.taskWaitingForRestartEvents.clone();
        this.taskWaitingForRestartEvents.clear();
        return toReturn;
    }

    /**
     * Get and remove the eventual 'misc' events received by this monitor.
     *
     * @return the eventual 'misc' events received by this monitor.
     */
    public boolean checkLastMiscEvents(SchedulerEvent eventType) {
        return eventType.equals(miscEvents.remove(0));
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobPendingToRunningEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobPendingToRunningEvent(JobInfo info) {
        jobPendingToRunningEvents.add(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobRemoveFinishedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobRemoveFinishedEvent(JobInfo info) {
        jobRemoveFinishedEvents.add(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobRunningToFinishedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobRunningToFinishedEvent(JobInfo info) {
        jobRunningToFinishedEvents.add(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.Job)
     */
    public void jobSubmittedEvent(Job job) {
        jobSubmittedEvents.add(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskPendingToRunningEvent(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public void taskPendingToRunningEvent(TaskInfo info) {
        taskPendingToRunningEvents.add(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskRunningToFinishedEvent(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public void taskRunningToFinishedEvent(TaskInfo info) {
        taskRunningToFinishedEvents.add(info);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobChangePriorityEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobChangePriorityEvent(JobInfo info) {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobPausedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobPausedEvent(JobInfo info) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobResumedEvent(org.ow2.proactive.scheduler.common.job.JobInfo)
     */
    public void jobResumedEvent(JobInfo info) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerFrozenEvent()
     */
    public void schedulerFrozenEvent() {
        miscEvents.add(SchedulerEvent.FROZEN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerKilledEvent()
     */
    public void schedulerKilledEvent() {
        miscEvents.add(SchedulerEvent.KILLED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerPausedEvent()
     */
    public void schedulerPausedEvent() {
        miscEvents.add(SchedulerEvent.PAUSED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerResumedEvent()
     */
    public void schedulerResumedEvent() {
        miscEvents.add(SchedulerEvent.RESUMED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        miscEvents.add(SchedulerEvent.SHUTDOWN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        miscEvents.add(SchedulerEvent.SHUTTING_DOWN);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        miscEvents.add(SchedulerEvent.STARTED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        miscEvents.add(SchedulerEvent.STOPPED);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdate(org.ow2.proactive.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
        // TODO Auto-generated method stub	
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskWaitingForRestart(org.ow2.proactive.scheduler.common.task.TaskInfo)
     */
    public void taskWaitingForRestart(TaskInfo event) {
        taskWaitingForRestartEvents.add(event);
    }

    public void schedulerPolicyChangedEvent(String newPolicyName) {
        // TODO Auto-generated method stub

    }
}
