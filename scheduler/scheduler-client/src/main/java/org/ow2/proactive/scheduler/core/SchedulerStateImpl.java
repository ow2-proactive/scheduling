/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
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
public final class SchedulerStateImpl<T extends JobState> implements SchedulerState<T> {

    /** Pending jobs */
    private Set<T> pendingJobs = Collections.synchronizedSet(new LinkedHashSet<T>());

    /** Running jobs */
    private Set<T> runningJobs = Collections.synchronizedSet(new LinkedHashSet<T>());

    /** Finished jobs */
    private Map<JobId, T> finishedJobs = Collections.synchronizedMap(new HashMap<JobId, T>());

    /** Scheduler status */
    private SchedulerStatus status = SchedulerStatus.STARTED;

    /** List of connected user. */
    private SchedulerUsers sUsers = new SchedulerUsers();

    private static final Logger logger = Logger.getLogger(SchedulerStateImpl.class);

    /**
     * keep a map of all jobs (pending, running finished) to facilitate
     * their updates
     */
    Map<JobId, T> jobs = new HashMap<>();

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
    public Vector<T> getFinishedJobs() {
        return new Vector(finishedJobs.values());
    }

    /**
     * To set the finishedJobs
     *
     * @param finishedJobs the finishedJobs to set
     */
    public void setFinishedJobs(Vector<T> finishedJobs) {
        this.finishedJobs = Collections.synchronizedMap(finishedJobs.stream()
                                                                    .collect(Collectors.toMap(JobState::getId,
                                                                                              Function.identity(),
                                                                                              (x1, x2) -> x2)));
    }

    /**
     * To get the pendingJobs
     *
     * @return the pendingJobs
     */
    public Vector<T> getPendingJobs() {
        return new Vector(pendingJobs);
    }

    /**
     * To set the pendingJobs
     *
     * @param pendingJobs the pendingJobs to set
     */
    public void setPendingJobs(Vector<T> pendingJobs) {
        this.pendingJobs = Collections.synchronizedSet(new LinkedHashSet<>(pendingJobs));
    }

    /**
     * To get the runningJobs
     *
     * @return the runningJobs
     */
    public Vector<T> getRunningJobs() {
        return new Vector(runningJobs);
    }

    /**
     * To set the runningJobs
     *
     * @param runningJobs the runningJobs to set
     */
    public void setRunningJobs(Vector<T> runningJobs) {
        this.runningJobs = Collections.synchronizedSet(new LinkedHashSet<>(runningJobs));
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
     * Returns the total number of jobs.
     *
     * @return the total number of jobs.
     */
    @Override
    public int getTotalNbJobs() {
        return jobs.size();
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
    public SchedulerStateImpl filterOnUser(String name) {
        SchedulerStateImpl ssi = new SchedulerStateImpl();
        ssi.setState(getStatus());
        ssi.setUsers(getUsers());
        //pending
        Vector<T> tmp = new Vector<>();
        for (T js : getPendingJobs()) {
            if (js.getOwner().equals(name)) {
                tmp.add(js);
            }
        }
        ssi.setPendingJobs(tmp);
        //running
        tmp = new Vector<>();
        for (T js : getRunningJobs()) {
            if (js.getOwner().equals(name)) {
                tmp.add(js);
            }
        }
        ssi.setRunningJobs(tmp);
        //finished
        tmp = new Vector<>();
        for (T js : getFinishedJobs()) {
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
    private void updateJobState(NotificationData<T> notification) {
        this.pendingJobs.add(notification.getData());
        this.jobs.put(notification.getData().getId(), notification.getData());
    }

    /**
     * Updates the scheduler state given the event passed as a parameter
     */
    private void updateJobInfo(NotificationData<JobInfo> notification) {

        T js = jobs.get(notification.getData().getJobId());
        js.update(notification.getData());
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_FINISHED:
                pendingToFinished(js);
                break;
            case JOB_REMOVE_FINISHED:
                removeFinished(js);
                logger.info("HOUSEKEEPING SchedulerStateImpl removed " + js.getId());
                break;
            case JOB_PENDING_TO_RUNNING:
                pendingToRunning(js);
                break;
            case JOB_RUNNING_TO_FINISHED:
                runningToFinished(js);
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
            for (T j : pendingJobs) {
                jobs.put(j.getId(), j);
            }
            for (T j : runningJobs) {
                jobs.put(j.getId(), j);
            }
            for (T j : finishedJobs.values()) {
                jobs.put(j.getId(), j);
            }
            initialized = true;
        }

        switch (notification.getEventType()) {
            case JOB_CHANGE_PRIORITY:
            case JOB_PAUSED:
            case JOB_PENDING_TO_FINISHED:
            case JOB_RESUMED:
            case JOB_RESTARTED_FROM_ERROR:
            case JOB_PENDING_TO_RUNNING:
            case JOB_RUNNING_TO_FINISHED:
            case TASK_REPLICATED:
            case TASK_SKIPPED:
                updateJobInfo((NotificationData<JobInfo>) notification);
                break;
            case JOB_REMOVE_FINISHED:
                updateJobInfo((NotificationData<JobInfo>) notification);
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
                updateJobState((NotificationData<T>) notification);
                break;
        }
    }

    public synchronized void update(T js) {
        pendingJobs.add(js);
        this.jobs.put(js.getId(), js);
    }

    public synchronized void pendingToRunning(T js) {
        pendingJobs.remove(js);
        runningJobs.add(js);
    }

    public synchronized void pendingToFinished(T js) {
        pendingJobs.remove(js);
        finishedJobs.put(js.getId(), js);
    }

    public synchronized void runningToFinished(T js) {
        runningJobs.remove(js);
        finishedJobs.put(js.getId(), js);
    }

    public synchronized void removeFinished(T js) {
        finishedJobs.remove(js.getId());
        jobs.remove(js.getId());
    }

    public synchronized void removeFinished(JobId jobId) {
        finishedJobs.remove(jobId);
        jobs.remove(jobId);
    }

}
