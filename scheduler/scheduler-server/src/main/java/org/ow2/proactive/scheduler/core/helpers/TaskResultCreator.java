/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 *  * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.core.helpers;

import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class TaskResultCreator {

    private static TaskResultCreator instance = null;

    public TaskResultCreator() {
    }

    public static synchronized TaskResultCreator getInstance() {
        if (instance == null) {
            instance = new TaskResultCreator();
        }
        return instance;
    }

    public TaskResultImpl getTaskResult(SchedulerDBManager dbManager, InternalJob job, InternalTask task)
            throws UnknownTaskException {
        return getTaskResult(dbManager, job, task, null, null);
    }

    public TaskResultImpl getTaskResult(SchedulerDBManager dbManager, InternalJob job, InternalTask task,
                                        Throwable exception, TaskLogs output) throws UnknownTaskException {
        if (task == null) {
            throw new UnknownTaskException();
        }

        TaskResultImpl taskResult;
        JobDescriptor jobDescriptor = job.getJobDescriptor();
        try {
            taskResult = (TaskResultImpl) dbManager.loadTasksResults(job.getId(),
                    Collections.singletonList(task.getId())).get(task.getId());
        } catch (DatabaseManagerException e) {
            taskResult = getEmptyTaskResultWithTaskIdAndExecutionTime(task, exception, output);
        }

        EligibleTaskDescriptor etd = null;

        if (jobDescriptor.getPausedTasks().get(task.getId()) != null) {
            etd = (EligibleTaskDescriptor) jobDescriptor.getPausedTasks().get(task.getId());
        } else if (jobDescriptor.getRunningTasks().get(task.getId()) != null) {
            etd = (EligibleTaskDescriptor) jobDescriptor.getRunningTasks().get(task.getId());
        }

        if (etd != null) {
            taskResult.setPropagatedVariables(getPropagatedVariables(dbManager, etd, job, task));
        }

        return taskResult;

    }

    public TaskResultImpl getEmptyTaskResultWithTaskIdAndExecutionTime(InternalTask task,
                                                                       Throwable exception, TaskLogs output) {
        return new TaskResultImpl(task.getId(), exception,
                output, System.currentTimeMillis() - task.getStartTime());
    }

    private Map<String, byte[]> getPropagatedVariables(SchedulerDBManager dbManager,
                                                       EligibleTaskDescriptor etd, InternalJob job, InternalTask task) throws UnknownTaskException {
        Map<String, byte[]> variables = new HashMap<>();

        int numberOfParentTasks = etd.getParents().size();
        if (job.getType() == JobType.TASKSFLOW) {
            // retrieve from the database the previous task results if available
            if ((numberOfParentTasks > 0) && task.handleResultsArguments()) {
                variables = extractTaskResultsAndMergeIntoMap(dbManager, etd, job);
            } else {
                variables = extractJobVariables(job);
            }
        }
        return variables;
    }

    private Map<String, byte[]> extractJobVariables(InternalJob job) {
        Map<String, byte[]> jobVariables = new HashMap<>();
        // otherwise use the default job variables
        for (Entry<String, String> entry : job.getVariables().entrySet()) {
            jobVariables.put(entry.getKey(), entry.getValue().getBytes());
        }
        return jobVariables;
    }

    private Map<String, byte[]> extractTaskResultsAndMergeIntoMap(SchedulerDBManager dbManager, EligibleTaskDescriptor etd, InternalJob job) {
        Map<String, byte[]> mergedVariables = new HashMap<>();

        int numberOfParentTasks = etd.getParents().size();
        List<TaskId> parentIds = new ArrayList<>(numberOfParentTasks);
        for (int i = 0; i < numberOfParentTasks; i++) {
            parentIds.add(etd.getParents().get(i).getTaskId());
        }
        Map<TaskId, TaskResult> taskResults = dbManager
                .loadTasksResults(
                        job.getId(), parentIds);
        for (TaskResult taskResult : taskResults.values()) {
            if (taskResult.getPropagatedVariables() != null) {
                mergedVariables.putAll(taskResult.getPropagatedVariables());
            }
        }
        return mergedVariables;
    }
}