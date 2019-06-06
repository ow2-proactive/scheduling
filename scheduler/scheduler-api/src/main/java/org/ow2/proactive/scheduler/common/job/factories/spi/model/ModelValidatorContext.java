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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ClassUtils;

import com.google.common.collect.ImmutableSet;


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

    private ModelValidatorContext(Map<String, Serializable> variablesValues) {
        spELVariables = new SpELVariables(variablesValues);
        spelContext = new StandardEvaluationContext(spELVariables);
        spelContext.setTypeLocator(new RestrictedTypeLocator());
    }

    public ModelValidatorContext(Task task) {
        this(task.getVariables().values().stream().collect(HashMap<String, Serializable>::new,
                                                           (m, v) -> m.put(v.getName(), v.getValue()),
                                                           HashMap<String, Serializable>::putAll));

    }

    public ModelValidatorContext(TaskFlowJob job) {
        this(job.getVariables().values().stream().collect(HashMap<String, Serializable>::new,
                                                          (m, v) -> m.put(v.getName(), v.getValue()),
                                                          HashMap<String, Serializable>::putAll));

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

    public static class RestrictedTypeLocator implements TypeLocator {

        // A set of authorized types to prevent security breaches i.e executing maliciously code on the server
        private final Set<String> authorizedTypes = ImmutableSet.of("String",
                                                                    "java.lang.String",
                                                                    "Integer",
                                                                    "java.lang.Integer",
                                                                    "Boolean",
                                                                    "java.lang.Boolean",
                                                                    "Double",
                                                                    "java.lang.Double",
                                                                    "Long",
                                                                    "java.lang.Long",
                                                                    "Float",
                                                                    "java.lang.Float",
                                                                    "Math",
                                                                    "java.lang.Math",
                                                                    "org.codehaus.jackson.map.ObjectMapper",
                                                                    "ObjectMapper",
                                                                    "javax.xml.parsers.DocumentBuilderFactory",
                                                                    "DocumentBuilderFactory",
                                                                    "java.io.StringReader",
                                                                    "StringReader",
                                                                    "org.xml.sax.InputSource",
                                                                    "InputSource",
                                                                    "Date",
                                                                    "java.util.Date",
                                                                    "ImmutableSet",
                                                                    "com.google.common.collect.ImmutableSet",
                                                                    "ImmutableMap",
                                                                    "com.google.common.collect.ImmutableMap",
                                                                    "ImmutableList",
                                                                    "com.google.common.collect.ImmutableList");

        private final ClassLoader classLoader;

        private final List<String> knownPackagePrefixes = new LinkedList<String>();

        public RestrictedTypeLocator() {
            this(ClassUtils.getDefaultClassLoader());
        }

        public RestrictedTypeLocator(ClassLoader classLoader) {
            this.classLoader = classLoader;
            registerImport("java.lang");
            registerImport("com.google.common.collect");
            registerImport("org.codehaus.jackson.map");
            registerImport("javax.xml.parsers");
            registerImport("org.xml.sax.map");
            registerImport("java.io");

        }

        /**
         * Register a new import prefix that will be used when searching for unqualified types.
         * Expected format is something like "java.lang".
         * @param prefix the prefix to register
         */
        public void registerImport(String prefix) {
            this.knownPackagePrefixes.add(prefix);
        }

        /**
         * Remove that specified prefix from this locator's list of imports.
         * @param prefix the prefix to remove
         */
        public void removeImport(String prefix) {
            this.knownPackagePrefixes.remove(prefix);
        }

        /**
         * Return a list of all the import prefixes registered with this StandardTypeLocator.
         * @return a list of registered import prefixes
         */
        public List<String> getImportPrefixes() {
            return Collections.unmodifiableList(this.knownPackagePrefixes);
        }

        /**
         * Find a (possibly unqualified) type reference among a list of authorized types - first using the type name as-is,
         * then trying any registered prefixes if the type name cannot be found.
         * @param typeName the type to locate
         * @return the class object for the type
         * @throws EvaluationException if the type cannot be found
         */
        @Override
        public Class<?> findType(String typeName) throws EvaluationException {
            if (!authorizedTypes.contains(typeName)) {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
            }
            String nameToLookup = typeName;
            try {
                return ClassUtils.forName(nameToLookup, this.classLoader);
            } catch (ClassNotFoundException ey) {
                // try any registered prefixes before giving up
            }
            for (String prefix : this.knownPackagePrefixes) {
                try {
                    nameToLookup = prefix + "." + typeName;
                    return ClassUtils.forName(nameToLookup, this.classLoader);
                } catch (ClassNotFoundException ex) {
                    // might be a different prefix
                }
            }
            throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
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
