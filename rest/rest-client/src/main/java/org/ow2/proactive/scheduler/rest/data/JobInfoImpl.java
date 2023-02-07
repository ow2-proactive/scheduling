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
package org.ow2.proactive.scheduler.rest.data;

import java.util.*;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.job.ExternalEndpoint;


public class JobInfoImpl implements JobInfo {

    private long submittedTime = -1;

    private long startTime = -1;

    private long inErrorTime = -1;

    private long finishedTime = -1;

    private long removedTime = -1;

    private long lastUpdatedTime = -1;

    private long cumulatedCoreTime = 0;

    private int numberOfNodes = 0;

    private int numberOfNodesInParallel = 0;

    private JobId jobId;

    private String jobOwner;

    private String tenant;

    private String projectName;

    private int totalNumberOfTasks = 0;

    private int numberOfPendingTasks = 0;

    private int numberOfRunningTasks = 0;

    private int numberOfFinishedTasks = 0;

    private int numberOfFailedTasks = 0;

    private int numberOfFaultyTasks = 0;

    private int numberOfInErrorTasks = 0;

    private JobPriority jobPriority = JobPriority.NORMAL;

    private JobStatus jobStatus = JobStatus.PENDING;

    private long scheduledTimeForRemoval;

    private boolean toBeRemoved;

    private Map<String, String> genericInformation;

    private Map<String, String> variables;

    private Map<String, JobVariable> detailedVariables;

    private Set<String> signals;

    private Map<String, Map<String, JobVariable>> detailedSignals;

    private Map<String, String> visualizationConnectionStrings;

    private Map<String, String> visualizationIcons;

    private Map<Integer, Boolean> attachedServices;

    private Map<String, ExternalEndpoint> externalEndpointUrls;

    private boolean resultMapPresent;

    private List<String> preciousTasks;

    private Long parentId;

    private int childrenCount;

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    @Override
    public long getFinishedTime() {
        return finishedTime;
    }

    public void setJobId(JobId jobId) {
        this.jobId = jobId;
    }

    @Override
    public JobId getJobId() {
        return jobId;
    }

    public void setJobOwner(String jobOwner) {
        this.jobOwner = jobOwner;
    }

    @Override
    public String getJobOwner() {
        return jobOwner;
    }

    @Override
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    @Override
    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    @Override
    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    @Override
    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    public void setJobPriority(JobPriority jobPriority) {
        this.jobPriority = jobPriority;
    }

    @Override
    public int getNumberOfFailedTasks() {
        return numberOfFailedTasks;
    }

    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        this.numberOfFailedTasks = numberOfFailedTasks;
    }

    @Override
    public int getNumberOfFaultyTasks() {
        return numberOfFaultyTasks;
    }

    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        this.numberOfFaultyTasks = numberOfFaultyTasks;
    }

    @Override
    public int getNumberOfInErrorTasks() {
        return numberOfInErrorTasks;
    }

    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        this.numberOfInErrorTasks = numberOfInErrorTasks;
    }

    @Override
    public JobPriority getPriority() {
        return jobPriority;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    @Override
    public long getRemovedTime() {
        return removedTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public boolean isStarted() {
        return startTime > 0;
    }

    @Override
    public long getInErrorTime() {
        return inErrorTime;
    }

    public void setInErrorTime(long inErrorTime) {
        this.inErrorTime = inErrorTime;
    }

    @Override
    public JobStatus getStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    @Override
    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @Override
    public long getCumulatedCoreTime() {
        return cumulatedCoreTime;
    }

    public void setCumulatedCoreTime(long cumulatedCoreTime) {
        this.cumulatedCoreTime = cumulatedCoreTime;
    }

    @Override
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    @Override
    public int getNumberOfNodesInParallel() {
        return numberOfNodesInParallel;
    }

    public void setNumberOfNodesInParallel(int numberOfNodesInParallel) {
        this.numberOfNodesInParallel = numberOfNodesInParallel;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Override
    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    @Override
    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    public void setToBeRemoved() {
        this.toBeRemoved = true;
    }

    @Override
    public long getScheduledTimeForRemoval() {
        return scheduledTimeForRemoval;
    }

    public void setScheduledTimeForRemoval(long scheduledTimeForRemoval) {
        this.scheduledTimeForRemoval = scheduledTimeForRemoval;
    }

    @Override
    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    @Override
    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public Map<String, JobVariable> getDetailedVariables() {
        return detailedVariables;
    }

    public void setDetailedVariables(Map<String, JobVariable> detailedVariables) {
        this.detailedVariables = detailedVariables;
    }

    @Override
    public Set<String> getSignals() {
        return (signals == null) ? new HashSet<>() : signals;
    }

    @Override
    public void setSignals(Set<String> signals) {
        this.signals = signals;
    }

    @Override
    public Map<String, Map<String, JobVariable>> getDetailedSignals() {
        return (detailedSignals == null) ? new LinkedHashMap<>() : detailedSignals;
    }

    @Override
    public void setDetailedSignals(Map<String, Map<String, JobVariable>> detailedSignals) {
        this.detailedSignals = detailedSignals;
    }

    @Override
    public Map<String, String> getVisualizationConnectionStrings() {
        return visualizationConnectionStrings;
    }

    @Override
    public void setVisualizationConnectionStrings(Map<String, String> visualizationConnectionStrings) {
        this.visualizationConnectionStrings = visualizationConnectionStrings;
    }

    @Override
    public Map<String, String> getVisualizationIcons() {
        return visualizationIcons;
    }

    @Override
    public void setVisualizationIcons(Map<String, String> visualizationIcons) {
        this.visualizationIcons = visualizationIcons;
    }

    @Override
    public Map<Integer, Boolean> getAttachedServices() {
        return attachedServices;
    }

    public void setAttachedServices(Map<Integer, Boolean> attachedServices) {
        this.attachedServices = attachedServices;
    }

    @Override
    public Map<String, ExternalEndpoint> getExternalEndpointUrls() {
        return externalEndpointUrls;
    }

    public void setExternalEndpointUrls(Map<String, ExternalEndpoint> externalEndpointUrls) {
        this.externalEndpointUrls = externalEndpointUrls;
    }

    @Override
    public boolean isResultMapPresent() {
        return resultMapPresent;
    }

    public void setResultMapPresent(boolean resultMapPresent) {
        this.resultMapPresent = resultMapPresent;
    }

    @Override
    public List<String> getPreciousTasks() {
        return preciousTasks;
    }

    public void setPreciousTasks(List<String> preciousTasks) {
        this.preciousTasks = preciousTasks;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }
}
