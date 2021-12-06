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

import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.spi.JobValidatorService;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;

import com.google.common.base.Strings;


/**
 * TaskFlowJob validator which validates variables against model definition
 */
public class DefaultModelJobValidatorServiceProvider implements JobValidatorService {

    public DefaultModelJobValidatorServiceProvider() {
        // empty
    }

    @Override
    public void validateJob(InputStream jobInputStream) throws JobValidationException {
        // validate any job
    }

    @Override
    public TaskFlowJob validateJob(TaskFlowJob job) throws JobValidationException {
        return validateJob(job, null, null, null);
    }

    @Override
    public TaskFlowJob validateJob(TaskFlowJob job, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) throws JobValidationException {

        ModelValidatorContext context = new ModelValidatorContext(job, scheduler, space, sessionId);

        for (JobVariable jobVariable : job.getVariables().values()) {
            checkVariableFormat(null, jobVariable, context);
            context.updateJobWithContext(job);
        }
        for (Task task : job.getTasks()) {
            context = new ModelValidatorContext(task, scheduler, space, sessionId);
            for (TaskVariable taskVariable : task.getVariables().values()) {
                checkVariableFormat(task, taskVariable, context);
                context.updateTaskWithContext(task);
            }
        }

        return job;
    }

    public void validateVariables(List<JobVariable> variableList, Map<String, Serializable> userValues,
            Scheduler scheduler, SchedulerSpaceInterface space) throws JobValidationException {

        if (variableList == null || variableList.isEmpty() || userValues == null || userValues.isEmpty()) {
            return;
        }
        Map<String, String> models = variableList.stream().collect(Collectors.toMap(JobVariable::getName,
                                                                                    JobVariable::getModel));
        Set<String> groupNames = new LinkedHashSet<>();
        variableList.forEach(e -> {
            if (!Strings.isNullOrEmpty(e.getGroup())) {
                groupNames.add(e.getGroup());
            }
        });

        Map<String, Serializable> variableReplacement = new LinkedHashMap<>();
        Map<String, Serializable> updatedVariables = new LinkedHashMap<>();
        variableList.forEach(jobVariable -> {
            if (userValues.containsKey(jobVariable.getName())) {
                variableReplacement.put(jobVariable.getName(), userValues.get(jobVariable.getName()));
            } else {
                variableReplacement.put(jobVariable.getName(), jobVariable.getValue());
            }
        });
        variableList.forEach(jobVariable -> {
            jobVariable.setValue(userValues.containsKey(jobVariable.getName()) ? VariableSubstitutor.filterAndUpdate((String) userValues.get(jobVariable.getName()),
                                                                                                                     variableReplacement)
                                                                               : VariableSubstitutor.filterAndUpdate(jobVariable.getValue(),
                                                                                                                     variableReplacement));
            updatedVariables.put(jobVariable.getName(), jobVariable.getValue());
        });
        ModelValidatorContext context = new ModelValidatorContext(updatedVariables,
                                                                  models,
                                                                  groupNames,
                                                                  scheduler,
                                                                  space,
                                                                  null);
        for (JobVariable jobVariable : variableList) {
            checkVariableFormat(null, jobVariable, context);
        }
        context.updateJobVariablesWithContext(variableList);
    }

    protected void checkVariableFormat(Task task, JobVariable variable, ModelValidatorContext context)
            throws JobValidationException {
        if (variable.getModel() != null && !variable.getModel().trim().isEmpty()) {
            String model = variable.getModel().trim();
            context.setVariableName(variable.getName());

            try {
                new ModelValidator(model).validate(variable.getValue(), context);
            } catch (Exception e) {
                throw new JobValidationException((task != null ? "Task '" + task.getName() + "': " : "") +
                                                 "Variable '" + variable.getName() + "': Model " + variable.getModel() +
                                                 ": " + e.getMessage(), e);
            }
        }
    }
}
