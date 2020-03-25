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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.utils.RestrictedMethodResolver;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.utils.RestrictedPropertyAccessor;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.utils.RestrictedTypeLocator;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * This class contains a context used by SPEL Language to let the language access all variables of a job
 */
public class ModelValidatorContext {

    private final StandardEvaluationContext spelContext;

    private final Scheduler scheduler;

    private final SchedulerSpaceInterface space;

    private String variableName;

    // container for job and task variables
    private SpELVariables spELVariables;

    public ModelValidatorContext(StandardEvaluationContext context, Scheduler scheduler,
            SchedulerSpaceInterface space) {
        this.spelContext = context;
        this.scheduler = scheduler;
        this.space = space;
    }

    public ModelValidatorContext(Map<String, Serializable> variablesValues, Scheduler scheduler,
            SchedulerSpaceInterface space) {
        spELVariables = new SpELVariables(variablesValues);
        spelContext = new StandardEvaluationContext(spELVariables);
        spelContext.setTypeLocator(new RestrictedTypeLocator());
        spelContext.setMethodResolvers(Collections.singletonList(new RestrictedMethodResolver()));
        spelContext.addPropertyAccessor(new RestrictedPropertyAccessor());
        this.scheduler = scheduler;
        this.space = space;
    }

    public ModelValidatorContext(Task task, Scheduler scheduler, SchedulerSpaceInterface space) {
        this(task.getVariables().values().stream().collect(HashMap<String, Serializable>::new,
                                                           (m, v) -> m.put(v.getName(), v.getValue()),
                                                           HashMap<String, Serializable>::putAll),
             scheduler,
             space);

    }

    public ModelValidatorContext(TaskFlowJob job, Scheduler scheduler, SchedulerSpaceInterface space) {
        this(job.getVariables().values().stream().collect(HashMap<String, Serializable>::new,
                                                          (m, v) -> m.put(v.getName(), v.getValue()),
                                                          HashMap<String, Serializable>::putAll),
             scheduler,
             space);

    }

    public StandardEvaluationContext getSpELContext() {
        return spelContext;
    }

    public SpELVariables getSpELVariables() {
        return spELVariables;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public SchedulerSpaceInterface getSpace() {
        return space;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * updates the given job with the current context
     */
    public void updateJobWithContext(TaskFlowJob job) {
        for (JobVariable jobVariable : job.getVariables().values()) {
            jobVariable.setValue(spELVariables.getVariables().get(jobVariable.getName()).toString());
        }
    }

    /**
     * updates the given task with the current context
     */
    public void updateTaskWithContext(Task task) {
        for (TaskVariable taskVariable : task.getVariables().values()) {
            taskVariable.setValue(spELVariables.getVariables().get(taskVariable.getName()).toString());
        }
    }

    public class SpELVariables {

        private Map<String, Serializable> variables;

        public SpELVariables(Map<String, Serializable> variables) {
            this.variables = variables;
        }

        public Map<String, Serializable> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Serializable> variables) {
            this.variables = variables;
        }
    }
}
