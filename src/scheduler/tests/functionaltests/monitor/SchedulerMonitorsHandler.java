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
package functionaltests.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.TimeoutAccounter;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * this class provide waiting methods on different Scheduler events
 * A thread can ask to wait for specific events, related to jobs, tasks
 * and Scheduler state. If event has already occurred,
 * waiting methods return immediately, otherwise, waiting is performed.
 * It provides waiting methods with a timeout too.
 *
 * waitForEvent**() methods, this object act as Producer-consumer mechanism ;
 * a Scheduler produce events that are memorized by this object,
 * and waiting methods waitForEvent**() are as consumer of these event.
 * It means that an event asked to be waited by a call to waitForEvent**() methods, is removed
 * after its occurrence. On the contrary, an event is kept till a waitForEvent**() for this event
 * has been called.
 *
 * waitForTerminatedJob() method dosen't act has other waitForEvent**() Methods.
 * This method deduce a job finished from current Scheduler's job states and received event.
 * This method can also be used for testing for job submission with killing and restarting
 * Scheduler.
 *
 * @author ProActive team
 *
 */
public class SchedulerMonitorsHandler {

    /**
     * Event concerning SchedulerState (started, killed, etc)
     * and not yet checked by a waiter.
     */
    private List<SchedulerEvent> schedulerStateEvents;

    /**
     * Jobs Events received and not yet checked by a waiter.
     * (not yet 'consumed')
     */
    private HashMap<JobId, List<JobEventMonitor>> jobsEvents;

    /**
     * Tasks events received and not yet checked by a waiter.
     * (not yet 'consumed')
     */
    private HashMap<TaskId, List<TaskEventMonitor>> tasksEvents;

    /**
     * Awaited event from Scheduler ;
     * a list of monitors, used for synchronization with threads that have called waitForEvent**()
     * methods. These monitors are notified only if corresponding event is thrown by Scheduler
     */
    private ArrayList<EventMonitor> eventsMonitors = null;

    /**
     * List of terminated jobs.
     *
     */
    private List<JobId> finishedJobs;

    /**
     * Constructor
     * @param userInterface
     */
    public SchedulerMonitorsHandler() {
        jobsEvents = new HashMap<JobId, List<JobEventMonitor>>();
        tasksEvents = new HashMap<TaskId, List<TaskEventMonitor>>();
        schedulerStateEvents = new ArrayList<SchedulerEvent>();
        finishedJobs = new ArrayList<JobId>();
        eventsMonitors = new ArrayList<EventMonitor>();
    }

    /**
     *
     * @param state
     */
    public void init(SchedulerState state) {
        synchronized (this) {
            for (JobState j : (Vector<JobState>) state.getFinishedJobs()) {
                this.finishedJobs.add(j.getId());
            }
        }
    }

    //---------------------------------------------------------------//
    // events waiting methods
    //---------------------------------------------------------------//

    /**
     * Wait for event : submission of Job to Scheduler.
     * @param id JobId to wait for submission
     * @param timeout max waiting time in milliseconds.
     * @return A Job representing submitted job
     * @throws ProActiveTimeoutException if timeout has expired
     */
    public JobState waitForEventJobSubmitted(JobId id, long timeout) throws ProActiveTimeoutException {
        JobEventMonitor monitor = null;
        synchronized (this) {
            monitor = removeJobEvent(id, SchedulerEvent.JOB_SUBMITTED);
            if (monitor != null) {
                //event occurred, remove it and return associated Job object
                return monitor.getJobState();
            }
            monitor = (JobEventMonitor) getMonitor(new JobEventMonitor(SchedulerEvent.JOB_SUBMITTED, id));
        }
        waitWithMonitor(monitor, timeout);
        return monitor.getJobState();
    }

    /**
     * Wait for an event related to a job.
     * Warning : this method must not be called to wait for a job submitted event (because
     * associated object for this event is different). should use for this event
     * @{link SchedulerMonitorsHandler.waitForEventJobSubmitted(Id id, long timeout)}
     *
     * @param event SchedulerEvent to wait for.
     * @param id JobId for which event is awaited.
     * @param timeout max waiting time in milliseconds.
     * @return JobEvent associated to event (thrown by scheduler)
     * @throws ProActiveTimeoutException
     */
    public JobInfo waitForEventJob(SchedulerEvent event, JobId id, long timeout)
            throws ProActiveTimeoutException {
        JobEventMonitor monitor = null;
        synchronized (this) {
            monitor = removeJobEvent(id, event);
            if (monitor != null) {
                //event occurred, remove it and return associated Job object
                return monitor.getJobInfo();
            }
            monitor = (JobEventMonitor) getMonitor(new JobEventMonitor(event, id));
        }
        waitWithMonitor(monitor, timeout);
        return monitor.getJobInfo();
    }

    /**
     * Wait for an event related to a task.
     * @param event SchedulerEvent to wait for ; an event related to Task
     * @param jobId task's corresponding jobId
     * @param taskName for which event is awaited
     * @param timeout max waiting time in milliseconds.
     * @return TaskEvent associated to event (thrown by scheduler)
     * @throws ProActiveTimeoutException
     */
    public TaskInfo waitForEventTask(SchedulerEvent event, JobId jobId, String taskName, long timeout)
            throws ProActiveTimeoutException {
        TaskEventMonitor monitor = null;
        synchronized (this) {
            monitor = removeTaskEvent(jobId, taskName, event);
            if (monitor != null) {
                //job is already finished, no need to wait
                return monitor.getTaskInfo();
            }
            monitor = (TaskEventMonitor) getMonitor(new TaskEventMonitor(event, jobId, taskName));
        }
        waitWithMonitor(monitor, timeout);
        return monitor.getTaskInfo();
    }

    /**
     * Wait for an event regarding Scheduler state : started, resumed, stopped...
     * @param event awaited event.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public void waitForEventSchedulerState(SchedulerEvent event, long timeout)
            throws ProActiveTimeoutException {
        EventMonitor monitor = null;
        synchronized (this) {
            if (schedulerStateEvents.contains(event)) {
                schedulerStateEvents.remove(event);
                return;
            }
            monitor = getMonitor(new EventMonitor(event));
        }
        waitWithMonitor(monitor, timeout);
    }

    /**
     * Wait for job a Job finished.
     * This method act differently from other waitForEvent**() methods ;
     * it doesn't wait strictly Job finished event. if job is already
     * on Scheduler's finished jobs list, then methods returns.
     * Otherwise, a wait for is performed.
     * This method corresponds to the running to finished transition
     *
     * @param id JobId representing the job awaited to be finished.
     * @param timeout in milliseconds
     * @throws ProActiveTimeoutException if timeout is reached
     */
    public void waitForFinishedJob(JobId id, long timeout) throws ProActiveTimeoutException {
        EventMonitor monitor = null;
        synchronized (this) {
            if (this.finishedJobs.contains(id)) {
                return;
            } else {
                monitor = getMonitor(new JobEventMonitor(SchedulerEvent.JOB_RUNNING_TO_FINISHED, id));
            }
        }
        waitWithMonitor(monitor, timeout);
    }

    //---------------------------------------------------------------//
    //private methods
    // these methods MUST be called from a synchronized(this) block
    //---------------------------------------------------------------//

    /**
     * Add a job event to the list of occurred events (memorize it).
     * @param event type of event occurred.
     * @param jEvent associated JobEvent object to event occurred.
     */
    private void addJobEvent(SchedulerEvent event, JobInfo jInfo) {
        if (!jobsEvents.containsKey(jInfo.getJobId())) {
            List<JobEventMonitor> list = new ArrayList<JobEventMonitor>();
            jobsEvents.put(jInfo.getJobId(), list);
        }
        jobsEvents.get(jInfo.getJobId()).add(new JobEventMonitor(event, jInfo));
    }

    /**
     * Add a job Event to the list of occurred events (memorize it).
     * Here object associated to Event is a Job and not a Job event because some
     * event (as Job submitted event have a Job object associated).
     * @param event type of event occurred.
     * @param job associated Job object to the event
     */
    private void addJobEvent(SchedulerEvent event, JobState jState) {
        if (!jobsEvents.containsKey(jState.getId())) {
            List<JobEventMonitor> list = new ArrayList<JobEventMonitor>();
            jobsEvents.put(jState.getId(), list);
        }
        jobsEvents.get(jState.getId()).add(new JobEventMonitor(event, jState));
    }

    /**
     * Add a Task Event to the list of occurred events (memorize it).
     * @param event type of task event
     * @param tEvent TaskEvent object associated to Event
     */
    private void addTaskEvent(SchedulerEvent event, TaskInfo tInfo) {
        if (!tasksEvents.containsKey(tInfo.getTaskId())) {
            List<TaskEventMonitor> list = new ArrayList<TaskEventMonitor>();
            tasksEvents.put(tInfo.getTaskId(), list);
        }
        tasksEvents.get(tInfo.getTaskId()).add(new TaskEventMonitor(event, tInfo));
    }

    /**
     * Remove, if exist a JobEventMonitor to the list of memorized job events
     * @param id JobId object representing a specific job to look for.
     * @param event type to look for
     * @return removed JobEventMonitor, or null if not exists.
     */
    private JobEventMonitor removeJobEvent(JobId id, SchedulerEvent event) {
        JobEventMonitor tmp = new JobEventMonitor(event, id);
        if (jobsEvents.containsKey(id) && jobsEvents.get(id).contains(tmp)) {
            return jobsEvents.get(id).remove((jobsEvents.get(id).indexOf(tmp)));
        } else
            return null;
    }

    /**
     * Remove, if exist a TaskEventMonitor to the list of memorized tasks events
     * @param id
     * @param taskName
     * @param event
     * @return
     */
    private TaskEventMonitor removeTaskEvent(JobId id, String taskName, SchedulerEvent event) {
        TaskEventMonitor tmp = new TaskEventMonitor(event, id, taskName);
        for (Entry<TaskId, List<TaskEventMonitor>> entry : this.tasksEvents.entrySet()) {
            if (entry.getKey().getJobId().equals(id) && entry.getKey().getReadableName().equals(taskName) &&
                entry.getValue().contains(tmp)) {
                return entry.getValue().remove(entry.getValue().indexOf(tmp));
            }
        }
        return null;
    }

    /**
     * Notify threads, if any that wait for a specific event
     *
     * @param monitor TaskEventMonitor object to look and notify if found
     * @return true if a monitor has been found and notification has been performed, false otherwise
     */
    private boolean lookAndNotifyMonitor(EventMonitor monitor) {
        EventMonitor monitorToNotify = this.getAndRemoveMonitor(monitor);
        if (monitorToNotify != null) {
            synchronized (monitorToNotify) {
                //monitor exists, maybe created by a waiter that has reached timeout
                //so check if it has been timeouted
                if (!monitorToNotify.isTimeouted()) {
                    notifyMonitor(monitorToNotify);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Notify threads, if any, that are waiting for a specific Job event. Set Job or JobEvent
     * (thrown by Scheduler) to associated JobEventMonitor
     * @param monitor
     * @return
     */
    private boolean lookAndNotifyMonitor(JobEventMonitor monitor) {
        JobEventMonitor monitorToNotify = (JobEventMonitor) getAndRemoveMonitor(monitor);
        if (monitorToNotify != null) {
            synchronized (monitorToNotify) {
                //monitor exists, maybe created by a waiter that has reached timeout
                //so check if it has been timeouted
                if (!monitorToNotify.isTimeouted()) {
                    //set JobEvent (if any) to monitor to notify
                    monitorToNotify.setJobInfo(monitor.getJobInfo());
                    //set Job (if any) to monitor to notify
                    monitorToNotify.setJobState(monitor.getJobState());
                    notifyMonitor(monitorToNotify);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Notify threads, if any, that are waiting for a specific task event. Set TaskEvent
     * (thrown by Scheduler) to associated TaskEventMonitor.
     * @param monitor
     * @return if monitors has been notified, false otherwise
     */
    private boolean lookAndNotifyMonitor(TaskEventMonitor monitor) {
        TaskEventMonitor monitorToNotify = (TaskEventMonitor) getAndRemoveMonitor(monitor);
        if (monitorToNotify != null) {
            synchronized (monitorToNotify) {
                //monitor exists, maybe created by a waiter that has reached timeout
                //so check if it has been timeout
                if (!monitorToNotify.isTimeouted()) {
                    monitorToNotify.setTaskInfo(monitor.getTaskInfo());
                    notifyMonitor(monitorToNotify);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return an EventMonitor that is currently used to wait for an event, or null if
     * this event is awaited by no one.
     * @param monitor EventMonitor Object representing Event to look for.
     * @return an eventMonitor object to notify, or null.
     */
    private EventMonitor getAndRemoveMonitor(EventMonitor monitor) {
        if (eventsMonitors.contains(monitor)) {
            return eventsMonitors.remove(eventsMonitors.indexOf(monitor));
        }
        return null;
    }

    /**
     * Returns a monitor used to wait for a specific event that hasn't yet occurred.
     * If there is not yet a monitor for this event, EventMonitor passed in parameter is
     * used as Monitor object for this event.
     * @param monitor representing event to wait for.
     * @return an EventMonitorJob to use as waiting Monitor.
     */
    private EventMonitor getMonitor(EventMonitor monitor) {
        if (!eventsMonitors.contains(monitor)) {
            eventsMonitors.add(monitor);
            return monitor;
        } else {
            return eventsMonitors.get(eventsMonitors.indexOf(monitor));
        }
    }

    /**
     * Notify an EventMonitor object, i.e resume threads that have perform
     * a wait on EventMonitor object passed in parameter.
     * @param monitorToNotify EventMonitor to notify.
     */
    private void notifyMonitor(EventMonitor monitorToNotify) {
        //System.out.println("===========================================");
        //System.out.println("NOTIFYING FOR EVENT : " + monitorToNotify.getWaitedEvent());
        //System.out.println("===========================================");
        synchronized (monitorToNotify) {
            monitorToNotify.setEventOccured();
            monitorToNotify.notify();
        }
    }

    //---------------------------------------------------------------//
    //private methods
    // these methods MUST NOT be called from a synchronized(this) block
    //---------------------------------------------------------------//
    private void waitWithMonitor(EventMonitor monitor, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter counter = TimeoutAccounter.getAccounter(timeout);
        synchronized (monitor) {
            monitor.setTimeouted(false);
            while (!counter.isTimeoutElapsed()) {
                if (monitor.eventOccured())
                    return;
                try {
                    //System.out.println("I AM WAITING FOR EVENT : " + monitor.getWaitedEvent() + " during " +
                    //    counter.getRemainingTimeout());
                    monitor.wait(counter.getRemainingTimeout());
                } catch (InterruptedException e) {
                    //spurious wake-up, nothing to do
                    e.printStackTrace();
                }
            }
            monitor.setTimeouted(true);
        }
        throw new ProActiveTimeoutException("timeout elapsed");
    }

    //---------------------------------------------------------------//
    //Method called by SchedulerEventListener
    //---------------------------------------------------------------//
    /**
     * Memorize or notify a waiter for a new job event received
     *
     * @param event Job event type
     * @param jInfo event's associated JobInfo object
     */
    public void handleJobEvent(SchedulerEvent event, JobInfo jInfo) {
        synchronized (this) {
            if (event.equals(SchedulerEvent.JOB_RUNNING_TO_FINISHED) ||
                event.equals(SchedulerEvent.JOB_PENDING_TO_FINISHED)) {
                this.finishedJobs.add(jInfo.getJobId());
            }
            if (!lookAndNotifyMonitor(new JobEventMonitor(event, jInfo))) {
                //no monitor notified, memorize event.
                addJobEvent(event, jInfo);
            }
        }
    }

    /**
     * Memorize or notify a waiter for a new job event received
     *
     * @param event Job event type
     * @param jState event's associated state object
     */
    public void handleJobEvent(SchedulerEvent event, JobState jState) {
        synchronized (this) {
            if (event.equals(SchedulerEvent.JOB_RUNNING_TO_FINISHED) ||
                event.equals(SchedulerEvent.JOB_PENDING_TO_FINISHED)) {
                this.finishedJobs.add(jState.getId());
            }
            if (!lookAndNotifyMonitor(new JobEventMonitor(event, jState))) {
                //no monitor notified, memorize event.
                addJobEvent(event, jState);
            }
        }
    }

    /**
     * Memorize or notify a waiter for a new task event received
     * @param event task event type
     * @param tInfo event's associated Job object
     */
    public void handleTaskEvent(SchedulerEvent event, TaskInfo tInfo) {
        synchronized (this) {
            if (!lookAndNotifyMonitor(new TaskEventMonitor(event, tInfo))) {
                //no monitor notified, memorize event.
                addTaskEvent(event, tInfo);
            }
        }
    }

    /**
     *
     * @param event
     */
    public void handleSchedulerStateEvent(SchedulerEvent event) {
        synchronized (this) {
            if (!lookAndNotifyMonitor(new EventMonitor(event))) {
                schedulerStateEvents.add(event);
            }
        }
    }
}