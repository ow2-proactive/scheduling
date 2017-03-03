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
package org.ow2.proactive_grid_cloud_portal.scheduler;

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

import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.FlowChecker;
import org.ow2.proactive.scheduler.common.job.factories.FlowError;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobValidationData;


public class ValidationUtil {

    private ValidationUtil() {
    }

    public static JobValidationData validateJobDescriptor(File jobDescFile) {
        return validateJob(jobDescFile.getAbsolutePath());
    }

    private static JobValidationData validateJob(String jobFilePath) {
        JobValidationData data = new JobValidationData();
        try {
            JobFactory factory = JobFactory.getFactory();
            Job job = factory.createJob(jobFilePath);

            if (job instanceof TaskFlowJob) {
                validateJob((TaskFlowJob) job, data);
                fillUpdatedVariables((TaskFlowJob) job, data);
            } else {
                data.setValid(true);
            }
        } catch (JobCreationException e) {
            data.setTaskName(e.getTaskName());
            data.setErrorMessage(e.getMessage());
            data.setStackTrace(getStackTrace(e));
        }
        return data;

    }

    private static void fillUpdatedVariables(TaskFlowJob job, JobValidationData data) {
        HashMap<String, String> updatedVariables = new HashMap<>();
        for (JobVariable jobVariable : job.getVariables().values()) {
            updatedVariables.put(jobVariable.getName(), jobVariable.getValue());
        }
        for (Task task : job.getTasks()) {
            for (TaskVariable taskVariable : task.getVariables().values()) {
                updatedVariables.put(task.getName() + ":" + taskVariable.getName(), taskVariable.getValue());
            }
        }
        data.setUpdatedVariables(updatedVariables);
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
