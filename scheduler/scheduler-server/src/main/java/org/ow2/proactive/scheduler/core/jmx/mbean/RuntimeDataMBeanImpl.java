/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scheduler.core.jmx.mbean;

import java.io.IOException;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.ow2.proactive.jmx.Chronological;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.utils.Tools;


/**
 * Implementation of the SchedulerRuntimeMBean interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class RuntimeDataMBeanImpl extends StandardMBean implements RuntimeDataMBean {

    private final SchedulerDBManager dbManager;

    private final SchedulerUsers schedulerClients;

    /** Current Scheduler status typed as scheduler event */
    private volatile SchedulerEvent schedulerStatus;

    public RuntimeDataMBeanImpl(SchedulerDBManager dbManager) throws NotCompliantMBeanException {
        super(RuntimeDataMBean.class);
        this.schedulerClients = new SchedulerUsers();
        this.dbManager = dbManager;
    }

    public void usersUpdatedEvent(final NotificationData<UserIdentification> notificationData) {
        synchronized (schedulerClients) {
            this.schedulerClients.update(notificationData.getData());
        }
    }

    /**
     * Methods for dispatching events
     *
     * Call the MBean event for the related Scheduler Updated event type
     *
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     * @param eventType the type of the received event
     */
    public void schedulerStateUpdatedEvent(final SchedulerEvent eventType) {
        this.schedulerStatus = eventType;
    }

    // ATTRIBUTES TO CONTROL

    /**
     * Returns the connected clients count
     * @return current number of connected users
     */
    @Chronological
    public int getConnectedUsersCount() {
        synchronized (schedulerClients) {
            return this.schedulerClients.getUsersCount();
        }
    }

    /**
     * @return current number of finished jobs
     */
    @Chronological
    public int getFinishedJobsCount() {
        return (int) dbManager.getFinishedJobsCount();
    }

    /**
     * @return current number of pending jobs
     */
    public int getPendingJobsCount() {
        return (int) dbManager.getPendingJobsCount();
    }

    /**
     * @return current number of running jobs
     */
    public int getRunningJobsCount() {
        return (int) dbManager.getRunningJobsCount();
    }

    /**
     * @return current number of jobs submitted to the Scheduler
     */
    public int getTotalJobsCount() {
        return (int) dbManager.getTotalJobsCount();
    }

    /**
     * @return current number of pending tasks
     */
    public int getPendingTasksCount() {
        return (int) dbManager.getPendingTasksCount();
    }

    /**
     * @return current number of finished tasks
     */
    public int getFinishedTasksCount() {
        return (int) dbManager.getFinishedTasksCount();
    }

    /**
     * @return current number of running tasks
     */
    public int getRunningTasksCount() {
        return (int) dbManager.getRunningTasksCount();
    }

    /**
     * @return current number of tasks submitted to the Scheduler
     */
    public int getTotalTasksCount() {
        return (int) dbManager.getTotalTasksCount();
    }

    /**
     * @return current status of the Scheduler as String
     */
    public String getStatus() {
        return this.schedulerStatus.toString();
    }

    /**
     * Getter methods for the KPI values
     *
     * @return current mean job pending time as integer
     */
    @Chronological
    public int getMeanJobPendingTime() {
        return (int) dbManager.getMeanJobPendingTime();
    }

    /**
     * @return current mean job execution time as integer
     */
    @Chronological
    public int getMeanJobExecutionTime() {
        return (int) dbManager.getMeanJobExecutionTime();
    }

    /**
     * @return current mean job submitting period as integer
     */
    @Chronological
    public int getJobSubmittingPeriod() {
        return (int) dbManager.getMeanJobSubmittingPeriod();
    }

    // UTILITY METHODS

    /**
     * Getter methods for KPI values as String
     *
     * @return current mean job pending time as formatted duration
     */
    public String getFormattedMeanJobPendingTime() {
        return Tools.getFormattedDuration(0, getMeanJobPendingTime());
    }

    /**
     * @return current mean job execution time as formatted duration
     */
    public String getFormattedMeanJobExecutionTime() {
        return Tools.getFormattedDuration(0, getMeanJobExecutionTime());
    }

    /**
     * @return current mean job submitting period as formatted duration
     */
    public String getFormattedJobSubmittingPeriod() {
        return Tools.getFormattedDuration(0, getJobSubmittingPeriod());
    }

    // MBEAN OPERATIONS

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the pending time for the given job
     */
    public long getJobPendingTime(final String jobId) {
        return dbManager.getJobPendingTime(jobId);
    }

    /**
     * This method gives the running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the running time for the given job
     */
    public long getJobRunningTime(final String jobId) {
        return dbManager.getJobRunningTime(jobId);
    }

    /**
     * This method gives the mean task pending time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task pending time for the given job
     */
    public long getMeanTaskPendingTime(final String jobId) {
        return (long) dbManager.getMeanTaskPendingTime(jobId);
    }

    /**
     * This method gives the mean task running time for a given Job
     *
     * @param jobId, the id of the Job to check
     * @return a representation as long of the duration of the mean task running time for the given job
     */
    public long getMeanTaskRunningTime(final String jobId) {
        return (long) dbManager.getMeanTaskRunningTime(jobId);
    }

    /**
     * This method gives the total number of nodes used by a given Job
     *
     * @param jobId, the id of the Job to check
     * @return the total number of nodes used by the given job
     */
    public int getTotalNumberOfNodesUsed(final String jobId) {
        return dbManager.getTotalNumberOfHostsUsed(jobId);
    }

    // UTILITY OPERATIONS

    /**
     * This method represents a possible Operation to Invoke on the MBean.
     * It gives the pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation of the duration of the pending time for the given job.
     */
    public String getFormattedJobPendingTime(final String jobId) {
        return Tools.getFormattedDuration(0, getJobPendingTime(jobId));
    }

    /**
     * This method gives the running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation of the duration of the running time for the given job
     */
    public String getFormattedJobRunningTime(final String jobId) {
        return Tools.getFormattedDuration(0, getJobRunningTime(jobId));
    }

    /**
     * This method gives the mean task pending time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation of the duration of the mean task pending time for the given job
     */
    public String getFormattedMeanTaskPendingTime(final String jobId) {
        return Tools.getFormattedDuration(0, getMeanTaskPendingTime(jobId));
    }

    /**
     * This method gives the mean task running time for a given Job as String
     *
     * @param jobId, the id of the Job to check
     * @return a formatted representation as long of the duration of the mean task running time for the given job.
     */
    public String getFormattedMeanTaskRunningTime(final String jobId) {
        return Tools.getFormattedDuration(0, getMeanTaskRunningTime(jobId));
    }

    /**
     * Sends the statistics accumulated in the RRD data base
     *
     * @return data base file converted to bytes
     * @throws IOException when data base cannot be read
     */
    public byte[] getStatisticHistory() throws IOException {
        return SchedulerJMXHelper.getInstance().getDataStore().getBytes();
    }
}
