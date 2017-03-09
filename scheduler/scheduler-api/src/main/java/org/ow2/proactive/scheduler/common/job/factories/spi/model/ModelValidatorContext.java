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
import java.util.LinkedHashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * This class contains a context used by SPEL Language to let the language access all variables of a job
 */
public class ModelValidatorContext {

    private final StandardEvaluationContext spelContext;

    // container for job and task variables
    private SpELVariables spELVariables;

    public ModelValidatorContext(StandardEvaluationContext context) {
        this.spelContext = context;

    }

    public ModelValidatorContext(Task task) {
        Map<String, Serializable> taskVariablesValues = new LinkedHashMap<>();

        for (TaskVariable taskVariable : task.getVariables().values()) {
            taskVariablesValues.put(taskVariable.getName(), taskVariable.getValue());
        }

        spELVariables = new SpELVariables(taskVariablesValues);
        spelContext = new StandardEvaluationContext(spELVariables);
    }

    public ModelValidatorContext(TaskFlowJob job) {

        Map<String, Serializable> jobVariablesValues = new LinkedHashMap<>();

        for (JobVariable jobVariable : job.getVariables().values()) {
            jobVariablesValues.put(jobVariable.getName(), jobVariable.getValue());
        }

        spELVariables = new SpELVariables(jobVariablesValues);
        spelContext = new StandardEvaluationContext(spELVariables);
    }

    public StandardEvaluationContext getSpELContext() {
        return spelContext;
    }

    public SpELVariables getSpELVariables() {
        return spELVariables;
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
