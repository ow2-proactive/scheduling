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
package org.ow2.proactive_grid_cloud_portal.scheduler.util;

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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.FlowChecker;
import org.ow2.proactive.scheduler.common.job.factories.FlowError;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;


public class ValidationUtil {

    /**
     * Attributes used for the signal api
     */
    private SynchronizationInternal publicStore;

    private static final String SIGNAL_ORIGINATOR = "scheduler";

    public static JobValidationData validateJobDescriptor(File jobDescFile, Map<String, String> jobVariables,
            Scheduler scheduler, SchedulerSpaceInterface space, String sessionId) {
        return validateJob(jobDescFile.getAbsolutePath(), jobVariables, scheduler, space, sessionId);
    }

    public static JobValidationData validateJob(String jobFilePath, Map<String, String> jobVariables,
            Scheduler scheduler, SchedulerSpaceInterface space, String sessionId) {
        JobValidationData data = new JobValidationData();
        Job job = null;
        try {
            JobFactory factory = JobFactory.getFactory();
            job = factory.createJob(jobFilePath, jobVariables, null, scheduler, space, sessionId);

            if (job instanceof TaskFlowJob) {
                fillUpdatedVariables((TaskFlowJob) job, data);
                validateJob((TaskFlowJob) job, data);
            } else {
                data.setValid(true);
            }
        } catch (JobCreationException e) {
            data.setTaskName(e.getTaskName());
            setJobValidationDataErrorMessage(data, e);
        }
        return data;

    }

    public static void setJobValidationDataErrorMessage(JobValidationData data, JobCreationException e) {
        data.setErrorMessage(e.getMessage());
        data.setStackTrace(getStackTrace(e));
        if (e.getUpdatedVariables() != null) {
            data.setUpdatedVariables(e.getUpdatedVariables());
        }
        if (e.getUpdatedModels() != null) {
            data.setUpdatedModels(e.getUpdatedModels());
        }
        if (e.getUpdatedDescriptions() != null) {
            data.setUpdatedDescriptions(e.getUpdatedDescriptions());
        }
        if (e.getUpdatedGroups() != null) {
            data.setUpdatedGroups(e.getUpdatedGroups());
        }
        if (e.getUpdatedAdvanced() != null) {
            data.setUpdatedAdvanced(e.getUpdatedAdvanced());
        }
        if (e.getUpdatedHidden() != null) {
            data.setUpdatedHidden(e.getUpdatedHidden());
        }
    }

    private static void fillUpdatedVariables(TaskFlowJob job, JobValidationData data) {
        HashMap<String, String> updatedVariables = new HashMap<>();
        HashMap<String, String> updatedModels = new HashMap<>();
        HashMap<String, String> updatedDescriptions = new HashMap<>();
        HashMap<String, String> updatedGroups = new HashMap<>();
        HashMap<String, Boolean> updatedAdvanced = new HashMap<>();
        HashMap<String, Boolean> updatedHidden = new HashMap<>();
        for (JobVariable jobVariable : job.getVariables().values()) {
            updatedVariables.put(jobVariable.getName(), jobVariable.getValue());
            updatedModels.put(jobVariable.getName(), jobVariable.getModel());
            updatedDescriptions.put(jobVariable.getName(), jobVariable.getDescription());
            updatedGroups.put(jobVariable.getName(), jobVariable.getGroup());
            updatedAdvanced.put(jobVariable.getName(), jobVariable.isAdvanced());
            updatedHidden.put(jobVariable.getName(), jobVariable.isHidden());
        }
        for (Task task : job.getTasks()) {
            for (TaskVariable taskVariable : task.getVariables().values()) {
                updatedVariables.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getValue());
                updatedModels.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getModel());
                updatedDescriptions.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getDescription());
                updatedGroups.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getGroup());
                updatedAdvanced.put(task.getName() + ":" + taskVariable.getName(), taskVariable.isAdvanced());
                updatedHidden.put(task.getName() + ":" + taskVariable.getName(), taskVariable.isHidden());
            }
        }
        data.setUpdatedVariables(updatedVariables);
        data.setUpdatedModels(updatedModels);
        data.setUpdatedDescriptions(updatedDescriptions);
        data.setUpdatedGroups(updatedGroups);
        data.setUpdatedAdvanced(updatedAdvanced);
        data.setUpdatedHidden(updatedHidden);
    }

    public static void fillUpdatedVariables(List<JobVariable> jobVariables, JobValidationData data) {
        HashMap<String, String> updatedVariables = new HashMap<>();
        HashMap<String, String> updatedModels = new HashMap<>();
        HashMap<String, String> updatedDescriptions = new HashMap<>();
        HashMap<String, String> updatedGroups = new HashMap<>();
        HashMap<String, Boolean> updatedAdvanced = new HashMap<>();
        HashMap<String, Boolean> updatedHidden = new HashMap<>();
        for (JobVariable jobVariable : jobVariables) {
            updatedVariables.put(jobVariable.getName(), jobVariable.getValue());
            updatedModels.put(jobVariable.getName(), jobVariable.getModel());
            updatedDescriptions.put(jobVariable.getName(), jobVariable.getDescription());
            updatedGroups.put(jobVariable.getName(), jobVariable.getGroup());
            updatedAdvanced.put(jobVariable.getName(), jobVariable.isAdvanced());
            updatedHidden.put(jobVariable.getName(), jobVariable.isHidden());
        }
        data.setUpdatedVariables(updatedVariables);
        data.setUpdatedModels(updatedModels);
        data.setUpdatedDescriptions(updatedDescriptions);
        data.setUpdatedGroups(updatedGroups);
        data.setUpdatedAdvanced(updatedAdvanced);
        data.setUpdatedHidden(updatedHidden);
        data.setValid(true);
    }

    private static void validateJob(TaskFlowJob job, JobValidationData data) {
        ArrayList<Task> tasks = job.getTasks();
        if (tasks.isEmpty()) {
            data.setErrorMessage(String.format("%s must contain at least one task.", job.getName()));
            return;
        }
        FlowError error = FlowChecker.validate(job);
        if (error != null) {
            data.setTaskName(error.getTask());
            data.setErrorMessage(error.getMessage());
            return;
        }
        data.setValid(true);
    }

}
