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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

import com.google.common.base.Strings;


/**
 * This class contains a context used by SPEL Language to let the language access all variables of a job
 */
public class ModelValidatorContext {

    private final StandardEvaluationContext spelContext;

    private final Scheduler scheduler;

    private final SchedulerSpaceInterface space;

    private final String sessionId;

    private String variableName;

    // container for job and task variables
    private SpELVariables spELVariables;

    public ModelValidatorContext(StandardEvaluationContext context, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) {
        this.spelContext = context;
        this.scheduler = scheduler;
        this.space = space;
        this.sessionId = sessionId;
    }

    public ModelValidatorContext(Map<String, Serializable> variablesValues, Map<String, String> models,
            Set<String> groupNames, Scheduler scheduler, SchedulerSpaceInterface space, String sessionId) {
        spELVariables = new SpELVariables(variablesValues, models, groupNames);
        spelContext = new StandardEvaluationContext(spELVariables);
        spelContext.setTypeLocator(new RestrictedTypeLocator());
        spelContext.setMethodResolvers(Collections.singletonList(new RestrictedMethodResolver()));
        spelContext.addPropertyAccessor(new RestrictedPropertyAccessor());
        this.scheduler = scheduler;
        this.space = space;
        this.sessionId = sessionId;
    }

    public ModelValidatorContext(Task task, Scheduler scheduler, SchedulerSpaceInterface space, String sessionId) {
        this(task.getVariables().values().stream().collect(LinkedHashMap<String, Serializable>::new,
                                                           (m, v) -> m.put(v.getName(), v.getValue()),
                                                           LinkedHashMap<String, Serializable>::putAll),
             task.getVariables().values().stream().collect(LinkedHashMap<String, String>::new,
                                                           (m, v) -> m.put(v.getName(), v.getModel()),
                                                           LinkedHashMap<String, String>::putAll),
             getGroups(task.getVariables()),
             scheduler,
             space,
             sessionId);

    }

    public static Set<String> getGroups(Map<String, ? extends JobVariable> variables) {
        Set<String> groups = new LinkedHashSet<>();
        if (variables != null) {
            for (JobVariable variable : variables.values()) {
                String groupName = variable.getGroup();
                if (!Strings.isNullOrEmpty(groupName)) {
                    groups.add(groupName);
                }
            }
        }
        return groups;
    }

    public ModelValidatorContext(TaskFlowJob job, Scheduler scheduler, SchedulerSpaceInterface space,
            String sessionId) {
        this(job.getVariables().values().stream().collect(LinkedHashMap<String, Serializable>::new,
                                                          (m, v) -> m.put(v.getName(), v.getValue()),
                                                          LinkedHashMap<String, Serializable>::putAll),
             job.getVariables().values().stream().collect(LinkedHashMap<String, String>::new,
                                                          (m, v) -> m.put(v.getName(), v.getModel()),
                                                          LinkedHashMap<String, String>::putAll),
             getGroups(job.getVariables()),
             scheduler,
             space,
             sessionId);

    }

    public ModelValidatorContext(StandardEvaluationContext context) {
        this(context, null, null, null);
    }

    public ModelValidatorContext(Map<String, Serializable> variablesValues, Map<String, String> models,
            Set<String> groupNames) {
        this(variablesValues, models, groupNames, null, null, null);
    }

    public ModelValidatorContext(Task task) {
        this(task, null, null, null);
    }

    public ModelValidatorContext(TaskFlowJob job) {
        this(job, null, null, null);
    }

    public StandardEvaluationContext getSpELContext() {
        return spelContext;
    }

    public SpELVariables getSpELVariables() {
        return spELVariables;
    }

    public void setSpELVariables(SpELVariables spELVariables) {
        this.spELVariables = spELVariables;
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

    public String getSessionId() {
        return sessionId;
    }

    /**
     * updates the given job with the current context
     */
    public void updateJobWithContext(TaskFlowJob job) {
        for (JobVariable jobVariable : job.getVariables().values()) {
            jobVariable.setValue(spELVariables.getVariables().get(jobVariable.getName()).toString());
            jobVariable.setModel(spELVariables.getModels().get(jobVariable.getName()));
            String groupName = jobVariable.getGroup();
            if (!Strings.isNullOrEmpty(groupName)) {
                Boolean hiddenGroupStatus = spELVariables.getHiddenGroups().get(groupName);
                if (hiddenGroupStatus != null) {
                    jobVariable.setHidden(hiddenGroupStatus);
                }
            }
            Boolean hiddenVariableStatus = spELVariables.getHiddenVariables().get(jobVariable.getName());
            if (hiddenVariableStatus != null) {
                jobVariable.setHidden(hiddenVariableStatus);
            }
        }
    }

    /**
     * updates the given task with the current context
     */
    public void updateTaskWithContext(Task task) {
        for (TaskVariable taskVariable : task.getVariables().values()) {
            taskVariable.setValue(spELVariables.getVariables().get(taskVariable.getName()).toString());
            taskVariable.setModel(spELVariables.getModels().get(taskVariable.getName()));
            String groupName = taskVariable.getGroup();
            if (!Strings.isNullOrEmpty(groupName)) {
                Boolean hiddenGroupStatus = spELVariables.getHiddenGroups().get(groupName);
                if (hiddenGroupStatus != null) {
                    taskVariable.setHidden(hiddenGroupStatus);
                }
            }
            Boolean hiddenVariableStatus = spELVariables.getHiddenVariables().get(taskVariable.getName());
            if (hiddenVariableStatus != null) {
                taskVariable.setHidden(hiddenVariableStatus);
            }
        }
    }

    public static class SpELVariables {

        /**
         * Can be used to access or modify variables
         */
        private Map<String, Serializable> variables;

        /**
         * Can be used to access or modify variables models
         */
        private Map<String, String> models;

        /**
         * Can be used to show/hide variables
         */
        private Map<String, Boolean> hiddenVariables;

        /**
         * Can be used to hide/unhide groups of variables
         */
        private Map<String, Boolean> hiddenGroups;

        /**
         * A temporary object which can be used in SPEL expressions
         */
        private Object temp;

        /**
         * A temporary map which can be used in SPEL expressions
         */
        private Map<String, Object> tempMap;

        /**
         * The 'valid' variable can be defined to set the result of the validation (instead of returning a boolean expression)
         */
        private Boolean valid;

        public SpELVariables(Map<String, Serializable> variables, Map<String, String> models, Set<String> groupsNames) {
            this.variables = variables;
            this.models = models;
            this.tempMap = new LinkedHashMap<>();
            this.hiddenVariables = new LinkedHashMap<>();
            if (variables != null) {
                for (String variableName : variables.keySet()) {
                    this.hiddenVariables.put(variableName, null);
                }
            }
            this.hiddenGroups = new LinkedHashMap<>();
            if (groupsNames != null) {
                for (String groupName : groupsNames) {
                    this.hiddenGroups.put(groupName, null);
                }
            }
        }

        /**
         * Takes any expression and return true
         * Registered as a SPEL function
         */
        public static boolean t(Object expression) {
            return true;
        }

        /**
         * Takes any expression and return false
         * Registered as a SPEL function
         */
        public static boolean f(Object expression) {
            return false;
        }

        /**
         * Takes any expression and return an empty string
         * This allows to sequence actions with the + operator
         * Registered as a SPEL function
         */
        public static String s(Object expression) {
            return "";
        }

        public Map<String, Serializable> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Serializable> variables) {
            this.variables = variables;
        }

        public Map<String, String> getModels() {
            return models;
        }

        public void setModels(Map<String, String> models) {
            this.models = models;
        }

        public Map<String, Boolean> getHiddenVariables() {
            return hiddenVariables;
        }

        public Map<String, Boolean> getHiddenGroups() {
            return hiddenGroups;
        }

        public Object getTemp() {
            return temp;
        }

        public void setTemp(Object temp) {
            this.temp = temp;
        }

        public Map<String, Object> getTempMap() {
            return tempMap;
        }

        public void setTempMap(Map<String, Object> tempMap) {
            this.tempMap = tempMap;
        }

        public Boolean getValid() {
            return valid;
        }

        public void setValid(Boolean valid) {
            this.valid = valid;
        }

        /**
         * Hide the given variable.
         * @param variableName variable to hide
         * @return true if the variable exists. Returning true allows easy usage of the function in SPEL expressions.
         * {@code Example: hideVar('var1') && showVar('var2')}
         */
        public boolean hideVar(String variableName) {
            if (hiddenVariables.containsKey(variableName)) {
                hiddenVariables.put(variableName, true);
                return true;
            } else {
                return false;
            }
        }

        /**
         * Show the given variable.
         * @param variableName variable to show
         * @return true if the variable exists. Returning true allows easy usage of the function in SPEL expressions.
         * {@code Example: showVar('var1') && hideVar('var2')}
         */
        public boolean showVar(String variableName) {
            if (hiddenVariables.containsKey(variableName)) {
                hiddenVariables.put(variableName, false);
                return true;
            } else {
                return false;
            }
        }

        /**
         * Hide the given group of variables.
         * @param groupName name of the variable group
         * @return true if the variable group exists. Returning true allows easy usage of the function in SPEL expressions.
         * {@code Example: hideGroup('group1') && showGroup('group2')}
         */
        public boolean hideGroup(String groupName) {
            if (hiddenGroups.containsKey(groupName)) {
                hiddenGroups.put(groupName, true);
                return true;
            } else {
                return false;
            }
        }

        /**
         * Show the given group of variables.
         * @param groupName name of the variable group
         * @return true if the variable group exists. Returning true allows easy usage of the function in SPEL expressions.
         * {@code Example: hideGroup('group1') && showGroup('group2')}
         */
        public boolean showGroup(String groupName) {
            if (hiddenGroups.containsKey(groupName)) {
                hiddenGroups.put(groupName, false);
                return true;
            } else {
                return false;
            }
        }
    }
}
