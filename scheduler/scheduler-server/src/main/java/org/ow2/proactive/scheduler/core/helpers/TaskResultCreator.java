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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.job.JobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class TaskResultCreator {

    public static TaskResultImpl getTaskResult(SchedulerDBManager dbManager, InternalJob job, InternalTask task) {
        if (task == null){
            return null;
        }
        
        TaskResultImpl taskResult;
        JobDescriptor jobDescriptor = job.getJobDescriptor();
        if (jobDescriptor.getRunningTasks().get(task.getId()) != null){
            taskResult = (TaskResultImpl) dbManager.loadTasksResults(job.getId(), Collections.singletonList(task.getId()))
            .get(task.getId());
        }
        else {
            //TODO check whether second argument should be "" or null
            taskResult =  getEmptyTaskResultWithTaskIdAndExecutionTime(task);
            if (jobDescriptor.getPausedTasks().get(task.getId()) != null){
                taskResult.setPropagatedVariables(getPropagatedVariables(dbManager, job, task));
            }
        }
        
        return taskResult;
    }
    
    public static TaskResultImpl getEmptyTaskResultWithTaskIdAndExecutionTime(InternalTask task){
        return new TaskResultImpl(task.getId(), "",
                null, System.currentTimeMillis() - task.getStartTime());
    }
    
    private static Map<String, byte[]> getPropagatedVariables(SchedulerDBManager dbManager, InternalJob job, 
            InternalTask task){
        Map<String, byte[]> variables = new HashMap<>();
        
        JobDescriptor jobDescriptor = job.getJobDescriptor();

        EligibleTaskDescriptor etd = (EligibleTaskDescriptor) jobDescriptor.getPausedTasks().get(task.getId());
        int resultSize = etd.getParents().size();
        if (job.getType() == JobType.TASKSFLOW) {
            // retrieve from the database the previous task results if available
            if ((resultSize > 0) && task.handleResultsArguments()) {
                List<TaskId> parentIds = new ArrayList<>(resultSize);
                for (int i = 0; i < resultSize; i++) {
                    parentIds.add(etd.getParents().get(i).getTaskId());
                }
                Map<TaskId, TaskResult> taskResults = dbManager
                        .loadTasksResults(
                                job.getId(), parentIds);
                for (TaskResult taskResult : taskResults.values()) {
                    if (taskResult.getPropagatedVariables() != null) {
                        variables.putAll(taskResult.getPropagatedVariables());
                    }
                }
            } else {
                // otherwise use the default job variables
                for (Entry<String, String> entry : job.getVariables().entrySet()){
                    variables.put(entry.getKey(), entry.getValue().getBytes());
                }
            }
        }
        return variables;
    }
}