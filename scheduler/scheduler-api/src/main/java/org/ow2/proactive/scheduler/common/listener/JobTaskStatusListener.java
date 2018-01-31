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
package org.ow2.proactive.scheduler.common.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.*;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskStatus;


public class JobTaskStatusListener implements SchedulerEventListener, Serializable {
    public static final int WAIT_TIME = 1000;

    private List<TimestampedData<JobInfo>> jobEvents = new CopyOnWriteArrayList<>();

    private List<TimestampedData<TaskInfo>> taskEvents = new CopyOnWriteArrayList<>();

    public static JobTaskStatusListener attachListener(Scheduler scheduler) throws Exception {
        JobTaskStatusListener listener = new JobTaskStatusListener();
        listener = PAActiveObject.turnActive(listener);
        scheduler.addEventListener(listener, false);
        return listener;
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notificationData) {
        final JobInfo jobInfo = notificationData.getData();
        final TimestampedData<JobInfo> timestampedData = new TimestampedData<>(jobInfo);
        jobEvents.add(timestampedData);
    }

    @Override
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notificationData) {
        final TaskInfo taskInfo = notificationData.getData();
        final TimestampedData<TaskInfo> timestampedData = new TimestampedData<>(taskInfo);
        taskEvents.add(timestampedData);
    }

    @ImmediateService
    public long blockToGetJobTimestamp(JobId jobId, JobStatus jobStatus) throws Exception {
        return blockToGetJobTimestampInternal(jobId, jobStatus, 0);
    }

    @ImmediateService
    public long blockToGetJobTimestamp(JobId jobId, JobStatus jobStatus, long filter) throws Exception {
        return blockToGetJobTimestampInternal(jobId, jobStatus, filter);
    }

    @ImmediateService
    public long blockToGetTaskTimestamp(JobId jobId, TaskStatus taskStatus) throws Exception {
        return blockToGetTaskTimestampInternal(jobId, taskStatus, 0);

    }

    @ImmediateService
    public long blockToGetTaskTimestamp(JobId jobId, TaskStatus taskStatus, long filter) throws Exception {
        return blockToGetTaskTimestampInternal(jobId, taskStatus, filter);
    }

    @Override
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {

    }

    @Override
    public void jobSubmittedEvent(JobState job) {

    }

    @Override
    public void jobUpdatedFullDataEvent(JobState job) {

    }

    @Override
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {

    }

    @ImmediateService
    public List<TimestampedData<JobInfo>> getJobEvents() {
        return jobEvents;
    }

    @ImmediateService
    public List<TimestampedData<TaskInfo>> getTaskEvents() {
        return taskEvents;
    }

    private long blockToGetTaskTimestampInternal(JobId jobId, TaskStatus taskStatus, long filter) throws Exception {
        Optional<TimestampedData<TaskInfo>> found = searchData(jobId, taskStatus, filter);
        while (!found.isPresent()) {
            Thread.sleep(WAIT_TIME);
            found = searchData(jobId, taskStatus, filter);
        }
        return found.get().getTimestamp();
    }

    private long blockToGetJobTimestampInternal(JobId jobId, JobStatus jobStatus, long filter) throws Exception {
        Optional<TimestampedData<JobInfo>> found = searchData(jobId, jobStatus, filter);
        while (!found.isPresent()) {
            Thread.sleep(WAIT_TIME);
            found = searchData(jobId, jobStatus, filter);
        }
        return found.get().getTimestamp();
    }

    private Optional<TimestampedData<JobInfo>> searchData(JobId jobId, JobStatus jobStatus, long filterTimestamp) {
        for (TimestampedData<JobInfo> event : jobEvents) {
            if (event.getData().getJobId().equals(jobId) && event.getData().getStatus().equals(jobStatus) &&
                event.getTimestamp() > filterTimestamp) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    private Optional<TimestampedData<TaskInfo>> searchData(JobId jobId, TaskStatus taskStatus, long filterTimestamp) {
        for (TimestampedData<TaskInfo> event : taskEvents) {
            if (event.getData().getJobId().equals(jobId) && event.getData().getStatus().equals(taskStatus) &&
                event.getTimestamp() > filterTimestamp) {
                return Optional.of(event);
            }
        }
        return Optional.empty();
    }

    public static class TimestampedData<T> implements Serializable {
        private T data;

        private long timestamp;

        public TimestampedData(T data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() {
            return timestamp;
        }

        public T getData() {
            return data;
        }
    }

}
