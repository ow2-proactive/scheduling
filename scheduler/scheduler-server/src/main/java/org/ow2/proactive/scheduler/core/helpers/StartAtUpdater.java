package org.ow2.proactive.scheduler.core.helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.policy.ISO8601DateUtil;


public class StartAtUpdater {

    public boolean updateStartAt(InternalJob job, String startAt, SchedulerDBManager dbManager) {

        long scheduledTime = ISO8601DateUtil.toDate(startAt).getTime();

        Set<TaskId> updatedTasks = updateStartAtAndTasksScheduledTime(job, startAt, scheduledTime);

        if (updatedTasks.size() > 0) {
            dbManager.updateTaskSchedulingTime(job, scheduledTime);
        }

        return !updatedTasks.isEmpty();

    }

    private Set<TaskId> updateStartAtAndTasksScheduledTime(InternalJob job, String startAt,
            long scheduledTime) {

        List<InternalTask> internalTasks = job.getITasks();
        Set<TaskId> updatedTasks = new HashSet<>(internalTasks.size());

        if (resetJobGenericInformation(job, startAt)) {

            for (InternalTask td : internalTasks) {
                td.setScheduledTime(scheduledTime);
                updatedTasks.add(td.getId());
                job.getJobDescriptor().updateTaskScheduledTime(td.getId(), scheduledTime);
            }
        }

        return updatedTasks;
    }

    private boolean resetJobGenericInformation(InternalJob job, String startAt) {

        Map<String, String> genericInformation = job.getGenericInformations(true);

        if (iaAValidStartAt(genericInformation, startAt)) {
            genericInformation.put(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT, startAt);
            job.setGenericInformations(genericInformation);
            return true;
        }
        return false;
    }

    private boolean iaAValidStartAt(Map<String, String> genericInformation, String startAt) {
        if (genericInformation.containsKey(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT)) {
            if (genericInformation.get(ExtendedSchedulerPolicy.GENERIC_INFORMATION_KEY_START_AT)
                    .equals(startAt)) {
                return false;
            }
        }
        return true;
    }

}