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
package org.ow2.proactive.scheduler.common.job;

import static org.ow2.proactive.scheduler.common.util.LogFormatter.addIndent;
import static org.ow2.proactive.scheduler.common.util.LogFormatter.line;
import static org.ow2.proactive.scheduler.common.util.LogFormatter.lineWithQuotes;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.joda.time.format.ISODateTimeFormat;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;
import org.ow2.proactive.scheduler.common.task.OnTaskError;


/**
 * Definition of a job for the user.
 * You can create a job by using this class. Job will be used to set some properties,
 * and give it the different tasks to run.
 * <p>
 * Here's a definition of the different parts of a job:<br>
 * {@link #setName(String)} will be used to identified the job.<br>
 * {@link #setDescription(String)} to set a short description of your job.<br>
 * {@link #setPriority(JobPriority)} to set the priority for the job, see {@link JobPriority} for more details.<br>
 * {@link #setOnTaskError(OnTaskError)} to set a predefined action when an exception occurred in at least one of the task.<br>
 * <p>
 * Once the job created, you can submit it to the scheduler using the UserSchedulerInterface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class Job extends CommonAttribute {

    private static final Logger LOGGER = Logger.getLogger(Job.class);

    public static final String JOB_DDL = "JOB_DDL";

    public static final String JOB_EXEC_TIME = "JOB_EXEC_TIME";

    /**
     * Name of the job
     */
    protected String name = SchedulerConstants.JOB_DEFAULT_NAME;

    /**
     * Short description of this job
     */
    protected String description = "No description";

    /**
     * Project name for this job
     */
    protected String projectName = "Not Assigned";

    /**
     * Job priority
     */
    protected JobPriority priority = JobPriority.NORMAL;

    protected String inputSpace = null;

    protected String outputSpace = null;

    protected String globalSpace = null;

    protected String userSpace = null;

    /**
     * A map to holds job descriptor variables
     */
    protected Map<String, JobVariable> variables = Collections.synchronizedMap(new LinkedHashMap());

    /**
     * ProActive Empty Constructor
     */
    public Job() {
    }

    /**
     * To get the type of this job
     *
     * @return the type of this job
     */
    public abstract JobType getType();

    /**
     * To get the id
     *
     * @return the id
     */
    public abstract JobId getId();

    /**
     * To get the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * To set a short description for the job.
     *
     * @param description the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * To get the name of the job.
     *
     * @return the name of the job.
     */
    public String getName() {
        return name;
    }

    /**
     * To set the name of the job.
     *
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * To get the priority of the job.
     *
     * @return the priority of the job.
     */
    public JobPriority getPriority() {
        return priority;
    }

    /**
     * To set the priority of the job. (Default is 'NORMAL')
     *
     * @param priority the priority to set.
     */
    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    /**
     * Returns the project Name.
     *
     * @return the project Name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the project Name to the given projectName value.
     *
     * @param projectName the project Name to set.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Get the input Space
     *
     * @return the input Space
     */
    public String getInputSpace() {
        return inputSpace;
    }

    /**
     * Set the input Space value to the given inputSpace value
     *
     * @param inputSpace the input Space to set
     */
    public void setInputSpace(String inputSpace) {
        this.inputSpace = inputSpace;
    }

    /**
     * Get the output Space
     *
     * @return the output Space
     */
    public String getOutputSpace() {
        return outputSpace;
    }

    /**
     * Set the output Space value to the given outputSpace value
     *
     * @param outputSpace the outputDataSpaceURL to set
     */
    public void setOutputSpace(String outputSpace) {
        this.outputSpace = outputSpace;
    }

    public String getGlobalSpace() {
        return globalSpace;
    }

    /**
     * Set the global Space value to the given GLOBAL space string value
     *
     * @param globalSpace the globalDataSpaceURL to set
     */
    public void setGlobalSpace(String globalSpace) {
        this.globalSpace = globalSpace;
    }

    public String getUserSpace() {
        return userSpace;
    }

    /**
     * Set the USER space value to the given USER space string value
     *
     * @param userSpace the userDataSpaceURL to set
     */
    public void setUserSpace(String userSpace) {
        this.userSpace = userSpace;
    }

    /**
     * Sets the variable map for this job.
     *
     * @param variables the variables map
     */
    public void setVariables(Map<String, JobVariable> variables) {
        verifyVariableMap(variables);
        this.variables = Collections.synchronizedMap(new LinkedHashMap(variables));
    }

    public static void verifyVariableMap(Map<String, ? extends JobVariable> variables) {
        for (Map.Entry<String, ? extends JobVariable> entry : variables.entrySet()) {
            if (!entry.getKey().equals(entry.getValue().getName())) {
                throw new IllegalArgumentException("Variables map entry key (" + entry.getKey() +
                                                   ") is different from variable name (" + entry.getValue().getName() +
                                                   ")");
            }
        }
    }

    /**
     * Returns the variable map of this job.
     *
     * @return a variable map
     */
    public Map<String, JobVariable> getVariables() {
        return this.variables;
    }

    /**
     * Returns a map containing the variable names and their values.
     *
     * @return a variable map
     */
    public Map<String, String> getVariablesAsReplacementMap() {
        HashMap<String, String> replacementVariables = new LinkedHashMap<>(variables.size());
        for (JobVariable variable : variables.values()) {
            replacementVariables.put(variable.getName(), variable.getValue());
        }
        return replacementVariables;
    }

    @Override
    public String toString() {
        return name;
    }

    public String display() {
        return "Job '" + name + "' : " + System.lineSeparator() +
               addIndent(Stream.of(lineWithQuotes("Description", description),
                                   lineWithQuotes("ProjectName", projectName),
                                   line("onTaskError", onTaskError),
                                   line("restartTaskOnError", restartTaskOnError),
                                   line("maxNumberOfExecution",
                                        maxNumberOfExecution,
                                        () -> maxNumberOfExecution.getValue().getIntegerValue()),
                                   line("genericInformation", genericInformation),
                                   line("Priority", priority),
                                   lineWithQuotes("InputSpace", inputSpace),
                                   lineWithQuotes("OutputSpace", outputSpace),
                                   lineWithQuotes("GlobalSpace", globalSpace),
                                   lineWithQuotes("UserSpace", userSpace),
                                   line("Variables", variables))
                               .filter(s -> !s.isEmpty())
                               .collect(Collectors.joining(System.lineSeparator())));
    }

    /**
     * Deadline can be as absolute or relative (from the job started time)
     * Absolute deadline can be set as GI vairable `JOB_DDL` in ISO8601 date format
     * without milliseconds, e.g. 2018-08-14T08:40:30+02:00.
     * Relative deadline can be set as GI vairable `JOB_DDL` in the following format
     * +HH:MM:SS, e.g. `+02:30:00`, or as +MM:SS, e.g. `+15:30`, or as +SS, e.g. `+40`.
     * @return job deadline if it exists
     */
    public Optional<JobDeadline> getJobDeadline() {
        if (genericInformation.containsKey(JOB_DDL)) {
            final String strJobDeadline = genericInformation.get(Job.JOB_DDL);
            try {
                if (strJobDeadline.startsWith("+")) { // should be relative deadline
                    return Optional.of(new JobDeadline(JobDeadline.parseDuration(strJobDeadline.substring(1)).get()));
                } else { // should be absolute deadline
                    return Optional.of(new JobDeadline(ISODateTimeFormat.dateTimeNoMillis()
                                                                        .parseDateTime(strJobDeadline)
                                                                        .toDate()));
                }
            } catch (Exception e) {
                LOGGER.warn("Imposible to parse JOB_DDL GI variable as ISO8601 date: " + strJobDeadline);
            }
        }

        return Optional.empty();
    }

    public Optional<Duration> getJobExpectedExecutionTime() {
        if (genericInformation.containsKey(JOB_EXEC_TIME)) {
            final String strJobExecTime = genericInformation.get(Job.JOB_EXEC_TIME);
            try {
                return Optional.of(JobDeadline.parseDuration(strJobExecTime).get());
            } catch (Exception e) {
                LOGGER.warn("Imposible to parse JOB_EXEC_TIME GI variable as `HH:MM:SS`: " + strJobExecTime);
            }
        }
        return Optional.empty();
    }

}
