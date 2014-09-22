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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


/**
 * This class is a representation of the whole scheduler initial jobs list state.<br>
 * It is basically represented by 3 lists of jobs, and its scheduling status.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@XmlRootElement(name = "schedulerstate")
public final class SchedulerStateImpl implements SchedulerState {

    private static final long serialVersionUID = 60L;

    /** Pending jobs */
    private Vector<JobState> pendingJobs = new Vector<JobState>();

    /** Running jobs */
    private Vector<JobState> runningJobs = new Vector<JobState>();

    /** Finished jobs */
    private Vector<JobState> finishedJobs = new Vector<JobState>();

    /** Scheduler status */
    private SchedulerStatus status = SchedulerStatus.STARTED;

    /** List of connected user. */
    private SchedulerUsers sUsers = new SchedulerUsers();

    /**
     * keep a map of all jobs (pending, running finished) to facilitate
     * their updates
     */
    Map<JobId, JobState> jobs = new HashMap<JobId, JobState>();

    /**
     * indicates if the <code>jobs</code> field has already been
     * initialized. Used only when this state is updated through
     * events.
     */
    private boolean initialized = false;

    /**
     * ProActive Empty constructor.
     */
    public SchedulerStateImpl() {
    }

    /**
     * To get the finishedJobs
     *
     * @return the finishedJobs
     */
    public Vector<JobState> getFinishedJobs() {
        return finishedJobs;
    }

    /**
     * To set the finishedJobs
     *
     * @param finishedJobs the finishedJobs to set
     */
    public void setFinishedJobs(Vector<JobState> finishedJobs) {
        this.finishedJobs = finishedJobs;
    }

    /**
     * To get the pendingJobs
     *
     * @return the pendingJobs
     */
    public Vector<JobState> getPendingJobs() {
        return pendingJobs;
    }

    /**
     * To set the pendingJobs
     *
     * @param pendingJobs the pendingJobs to set
     */
    public void setPendingJobs(Vector<JobState> pendingJobs) {
        this.pendingJobs = pendingJobs;
    }

    /**
     * To get the runningJobs
     *
     * @return the runningJobs
     */
    public Vector<JobState> getRunningJobs() {
        return runningJobs;
    }

    /**
     * To set the runningJobs
     *
     * @param runningJobs the runningJobs to set
     */
    public void setRunningJobs(Vector<JobState> runningJobs) {
        this.runningJobs = runningJobs;
    }

    /**
     * @return the status
     */
    @XmlElement(name = "status")
    public SchedulerStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setState(SchedulerStatus status) {
        this.status = status;
    }

    /**
     * Returns the list of connected users.
     *
     * @return the list of connected users.
     */
    public SchedulerUsers getUsers() {
        return sUsers;
    }

    /**
     * Sets the list of connected users to the given users value.
     *
     * @param users the list of connected users to set.
     */
    public void setUsers(SchedulerUsers users) {
        sUsers = users;
    }

    /**
     * Filter the state on the given user name and return a new instance of scheduler state impl
     * After this call, this instance remains the same.
     *
     * @param name username to be filtered
     * @return a new state filtered on job owner name
     */
    SchedulerStateImpl filterOnUser(String name) {
        SchedulerStateImpl ssi = new SchedulerStateImpl();
        ssi.setState(getStatus());
        ssi.setUsers(getUsers());
        //pending
        Vector<JobState> tmp = new Vector<JobState>();
        for (JobState js : getPendingJobs()) {
            if (js.getOwner().equals(name)) {
                tmp.add(js);
            }
        }
        ssi.setPendingJobs(tmp);
        //running
        tmp = new Vector<JobState>();
        for (JobState js : getRunningJobs()) {
            if (js.getOwner().equals(name)) {
                tmp.add(js);
            }
        }
        ssi.setRunningJobs(tmp);
        //finished
        tmp = new Vector<JobState>();
        for (JobState js : getFinishedJobs()) {
            if (js.getOwner().equals(name)) {
                tmp.add(js);
            }
        }
        ssi.setFinishedJobs(tmp);
        return ssi;
    }

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    public void update(SchedulerEvent eventType) {
        switch (eventType) {
            case FROZEN:
                status = SchedulerStatus.FROZEN;
                break;
            case KILLED:
                status = SchedulerStatus.KILLED;
                break;
            case STARTED:
                status = SchedulerStatus.STARTED;
                break;
            case RESUMED:
                status = SchedulerStatus.STARTED;
                break;
            case STOPPED:
                status = SchedulerStatus.STOPPED;
                break;
            case PAUSED:
                status = SchedulerStatus.PAUSED;
                break;
            case SHUTTING_DOWN:
                status = SchedulerStatus.SHUTTING_DOWN;
                break;
            case RM_DOWN:
                status = SchedulerStatus.UNLINKED;
                break;
            case DB_DOWN:
                status = SchedulerStatus.DB_DOWN;
                break;
            case RM_UP:
                break;
        }
    }

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    private void updateJobState(NotificationData<JobState> notification) {
        this.pendingJobs.add(notification.getData());
        this.jobs.put(notification.getData().getId(), notification.getData());
    }

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    private void updateJobInfo(NotificationData<JobInfo> notification) {

        JobState js = jobs.get(notification.getData().getJobId());
        js.update(notification.getData());
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_FINISHED:
                pendingJobs.remove(js);
                finishedJobs.add(js);
                break;
            case JOB_REMOVE_FINISHED:
                finishedJobs.remove(js);
                break;
            case JOB_PENDING_TO_RUNNING:
                pendingJobs.remove(js);
                runningJobs.add(js);
                break;
            case JOB_RUNNING_TO_FINISHED:
                runningJobs.remove(js);
                finishedJobs.add(js);
                break;
        }
    }

    private void updateTaskInfo(NotificationData<TaskInfo> notification) {
        jobs.get(notification.getData().getJobId()).update(notification.getData());
    }

    private void updateUserIdentification(NotificationData<UserIdentification> notification) {
        sUsers.update(notification.getData());
    }

    @SuppressWarnings("unchecked")
    public synchronized void update(NotificationData<?> notification) {

        if (!initialized) {
            for (JobState j : pendingJobs) {
                jobs.put(j.getId(), j);
            }
            for (JobState j : runningJobs) {
                jobs.put(j.getId(), j);
            }
            for (JobState j : finishedJobs) {
                jobs.put(j.getId(), j);
            }
            initialized = true;
        }

        switch (notification.getEventType()) {
            case JOB_CHANGE_PRIORITY:
            case JOB_PAUSED:
            case JOB_PENDING_TO_FINISHED:
            case JOB_RESUMED:
            case JOB_PENDING_TO_RUNNING:
            case JOB_RUNNING_TO_FINISHED:
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                updateJobInfo((NotificationData<JobInfo>) notification);
                break;
            case JOB_REMOVE_FINISHED:
                updateJobInfo((NotificationData<JobInfo>) notification);
                jobs.remove(((NotificationData<JobInfo>) notification).getData().getJobId());
                break;
            case TASK_PENDING_TO_RUNNING:
            case TASK_RUNNING_TO_FINISHED:
            case TASK_WAITING_FOR_RESTART:
                updateTaskInfo((NotificationData<TaskInfo>) notification);
                break;
            case USERS_UPDATE:
                updateUserIdentification((NotificationData<UserIdentification>) notification);
                break;
            case JOB_SUBMITTED:
                updateJobState((NotificationData<JobState>) notification);
                break;
        }
    }

    public synchronized void update(JobState js) {
        pendingJobs.add(js);
        this.jobs.put(js.getId(), js);
    }

}
