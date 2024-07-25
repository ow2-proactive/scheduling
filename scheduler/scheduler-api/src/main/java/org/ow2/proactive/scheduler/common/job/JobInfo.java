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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.job.ExternalEndpoint;


/**
 * JobInfo provides information about the Job it is linked with.
 * <br>
 * These information and only them are able to change inside the Job.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface JobInfo extends Serializable {

    /**
     * Returns an identifier that uniquely identifies the Job
     * within a Scheduler instance.
     *
     * @return an identifier that uniquely identifies the Job
     * within a Scheduler instance.
     */
    JobId getJobId();

    /**
     * Returns the name of the Job owner.
     *
     * @return the name of the Job owner (the one who submitted the Job).
     */
    String getJobOwner();

    /**
     * Return the tenant associated with the job owner
     *
     * @return the tenant associated with the job owner or null if no tenant is associated
     */
    String getTenant();

    /**
     * Return the domain associated with the job owner
     *
     * @return the domain associated with the job owner or null if no tenant is associated
     */
    String getDomain();

    /**
     * Returns the project name associated with this job
     * @return project name
     */
    String getProjectName();

    /**
     * Returns the bucket name associated with this job
     * @return bucket name
     */
    String getBucketName();

    /**
     * Returns the label associated with this job
     * @return label
     */
    String getLabel();

    /**
     * Returns the portal name via the job was submitted
     * @return portal name
     */
    String getSubmissionMode();

    /**
     * Returns the time at which the Job has finished.
     *
     * @return the time at which the Job has finished
     * (i.e. all tasks have finished because of a normal or abnormal termination).
     */
    long getFinishedTime();

    /**
     * Returns the time at which the Job has been marked as removed.
     *
     * @return the time at which the Job has been marked as removed.
     */
    long getRemovedTime();

    /**
     * Returns the time at which the Job has started.
     *
     * @return the time at which the Job has started.
     */
    long getStartTime();

    /**
     * @return true if job is already started
     */
    boolean isStarted();

    /**
     * Returns the time at which a Job was seen as in-error
     * for the last time.
     *
     * @return the time at which a Job was seen as in-error
     * for the last time. The default value is {@code -1}.
     */
    long getInErrorTime();

    /**
     * Returns the time at which the Job was submitted.
     *
     * @return the time at which the Job was submitted.
     */
    long getSubmittedTime();

    /**
     * Returns the time at which the last updated happened on the Job.
     *
     * @return the time in long format.
     */
    long getLastUpdatedTime();

    /**
     * Returns the cumulated time consumed by task executions for this job
     * @return sum of all time consumed by each task execution
     */
    long getCumulatedCoreTime();

    /**
     * Returns the number of nodes used by the job since the beginning of its execution
     * @return number of nodes used by the job
     */
    int getNumberOfNodes();

    /**
     * Returns the number of nodes in parallel used by the job since the beginning of its execution
     * @return number of nodes in parallel used by the job
     */
    int getNumberOfNodesInParallel();

    /**
     * Returns the number of tasks managed by the Job.
     *
     * @return the number of tasks managed by the Job.
     * The number should correspond to the sum of tasks which
     * are grouped in pending, running and finished.
     */
    int getTotalNumberOfTasks();

    /**
     * Returns the number of tasks managed by the Job
     * which are finished.
     *
     * @return the number of tasks managed by the Job
     * which are finished.
     */
    int getNumberOfFinishedTasks();

    /**
     * Returns the number of tasks managed by the Job
     * which are pending.
     *
     * @return the number of tasks managed by the Job
     * which are pending.
     */
    int getNumberOfPendingTasks();

    /**
     * Returns the number of tasks managed by the Job
     * which are running.
     *
     * @return the number of tasks managed by the Job
     * which are running.
     */
    int getNumberOfRunningTasks();

    /**
     * Returns the number of tasks managed by the Job
     * that are in a failed state due to a resource failure.
     *
     * @return the number of tasks managed by the Job
     * that are in a failed state due to a resource failure.
     */
    int getNumberOfFailedTasks();

    /**
     * Returns the number of tasks managed by the Job
     * that are in a faulty state.
     *
     * @return the number of tasks managed by the Job
     * that are in a faulty state due to a task fault
     * (exception or return code).
     */
    int getNumberOfFaultyTasks();

    /**
     * Returns the number of tasks managed by the Job
     * that are in an in-error state.
     *
     * @return the number of tasks managed by the Job
     * that are in an in-error state because of faulty
     * tasks that were configured to suspend on error.
     */
    int getNumberOfInErrorTasks();

    /**
     * Returns the priority of the Job.
     *
     * @return the priority of the Job.
     */
    JobPriority getPriority();

    /**
     * Return the status of the job.
     *
     * @return the status of the job.
     */
    JobStatus getStatus();

    /**
     * Returns a boolean that indicates whether the job
     * is marked for removal or not.
     *
     * @return a boolean that indicates whether the job
     * is marked for removal or not.
     */
    boolean isToBeRemoved();

    /**
     * Returns the scheduled time for removal.
     * If none is set, returns 0.
     *
     * @return the time at which the Job is to be removed.
     */
    long getScheduledTimeForRemoval();

    /**
     * Returns the generic information Map
     * @return generic information Map
     */
    Map<String, String> getGenericInformation();

    /**
     * Returns the variables Map
     * @return variables Map
     */
    Map<String, String> getVariables();

    /**
     * Returns the variables Map with full definition
     * @return variables Map
     */
    Map<String, JobVariable> getDetailedVariables();

    /**
     * Return a map of visualization connection strings
     * key: task name
     * value: connection string
     * @return a map of connection strings
     */
    Map<String, String> getVisualizationConnectionStrings();

    /**
     * Sets the visualization connection string map
     * @param connectionStrings
     */
    void setVisualizationConnectionStrings(Map<String, String> connectionStrings);

    /**
     * Return a map of visualization icons
     * key: task name
     * value: icon associated with the task
     * @return a map of task icons
     */
    Map<String, String> getVisualizationIcons();

    /**
     * Sets the visualization icons
     * @param visualizationIcons
     */
    void setVisualizationIcons(Map<String, String> visualizationIcons);

    /**
     * Returns a map of attached PSA services
     * {ServiceId, EnableActions}
     * @return a map of psa service
     */
    Map<Integer, Boolean> getAttachedServices();

    /**
     * Returns a set of external endpoint urls
     * @return a set of external endpoint urls
     */
    Map<String, ExternalEndpoint> getExternalEndpointUrls();

    /**
     * Returns the set of signals used by the job
     * @return signals set
     */
    Set<String> getSignals();

    /**
     * Sets the set of job signals
     */
    void setSignals(Set<String> signals);

    /**
     * Returns the map of signals and input variables
     * @return signals map
     */
    Map<String, Map<String, JobVariable>> getDetailedSignals();

    /**
     * Sets the map of job signals and input variables
     */
    void setDetailedSignals(Map<String, Map<String, JobVariable>> detailedSignals);

    /**
     * Return the list of tasks names with precious results
     * @return list of task names
     */
    List<String> getPreciousTasks();

    /**
     * Return true if a non-empty result map is attached to this job
     * @return non-empty result map test
     */
    boolean isResultMapPresent();

    /**
     * Return the parent id of the job
     * @return parent id of the job, or null if the job has no parent
     */
    Long getParentId();

    /**
     * Return the scheduled time of the job, defined by the generic information START_AT
     * @return scheduled time of the job of the job, or null it is not defined
     */
    Long getStartAt();

    /**
     * Return the number of children jobs
     * @return number of children jobs
     */
    int getChildrenCount();
}
