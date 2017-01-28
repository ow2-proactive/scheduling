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
package org.ow2.proactive.scheduler.common.job.factories.spi.model;

import java.io.File;

import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.spi.JobValidatorService;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;

import com.google.common.base.Strings;


/**
 * TaskFlowJob validator which validates variables against model definition
 */
public class DefaultModelJobValidatorServiceProvider implements JobValidatorService {

    public DefaultModelJobValidatorServiceProvider() {
        // empty
    }

    @Override
    public void validateJob(File jobFile) throws JobValidationException {
        // validate any job
    }

    @Override
    public void validateJob(TaskFlowJob job) throws JobValidationException {
        for (Task task : job.getTasks()) {
            for (TaskVariable variable : task.getVariables().values()) {
                checkVariableFormat(task, variable);
            }
        }
    }

    protected void checkVariableFormat(Task task, TaskVariable variable) throws JobValidationException {
        if (!Strings.isNullOrEmpty(variable.getModel())) {
            String model = variable.getModel().trim();

            try {
                new ModelValidator(model).validate(variable.getValue());
            } catch (Exception e) {
                throw new JobValidationException("Task '" + task.getName() + "': Variable '" + variable.getName() +
                                                 "': Model " + variable.getModel() + ": " + e.getMessage(), e);
            }
        }
    }
}
