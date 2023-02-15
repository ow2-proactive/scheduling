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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.job.ExternalEndpoint;


public class JobInfoData implements java.io.Serializable {

    private long startTime;

    private long inErrorTime;

    private long finishedTime;

    private long submittedTime;

    private long removedTime;

    private JobStatusData status;

    private JobIdData jobId;

    private int totalNumberOfTasks;

    private int numberOfPendingTasks;

    private int numberOfRunningTasks;

    private int numberOfFinishedTasks;

    private int numberOfFailedTasks;

    private int numberOfFaultyTasks;

    private int numberOfInErrorTasks;

    private JobPriorityData priority;

    private String jobOwner;

    private String tenant;

    private String projectName;

    private String bucketName;

    private boolean toBeRemoved = false;

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

    private Long parentId = null;

    private int childrenCount = 0;

    private long cumulatedCoreTime = 0;

    private int numberOfNodes = 0;

    private int numberOfNodesInParallel = 0;

    public void setToBeRemoved() {
        toBeRemoved = true;
    }

    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    public long getRemovedTime() {
        return removedTime;
    }

    public void setRemovedTime(long removedTime) {
        this.removedTime = removedTime;
    }

    public long getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(long submittedTime) {
        this.submittedTime = submittedTime;
    }

    public int getNumberOfPendingTasks() {
        return numberOfPendingTasks;
    }

    public void setNumberOfPendingTasks(int numberOfPendingTasks) {
        this.numberOfPendingTasks = numberOfPendingTasks;
    }

    public int getNumberOfRunningTasks() {
        return numberOfRunningTasks;
    }

    public void setNumberOfRunningTasks(int numberOfRunningTasks) {
        this.numberOfRunningTasks = numberOfRunningTasks;
    }

    public int getNumberOfFinishedTasks() {
        return numberOfFinishedTasks;
    }

    public void setNumberOfFinishedTasks(int numberOfFinishedTasks) {
        this.numberOfFinishedTasks = numberOfFinishedTasks;
    }

    public int getNumberOfFailedTasks() {
        return numberOfFailedTasks;
    }

    public void setNumberOfFailedTasks(int numberOfFailedTasks) {
        this.numberOfFailedTasks = numberOfFailedTasks;
    }

    public int getNumberOfFaultyTasks() {
        return numberOfFaultyTasks;
    }

    public void setNumberOfFaultyTasks(int numberOfFaultyTasks) {
        this.numberOfFaultyTasks = numberOfFaultyTasks;
    }

    public int getNumberOfInErrorTasks() {
        return numberOfInErrorTasks;
    }

    public void setNumberOfInErrorTasks(int numberOfInErrorTasks) {
        this.numberOfInErrorTasks = numberOfInErrorTasks;
    }

    public String getJobOwner() {
        return jobOwner;
    }

    public void setJobOwner(String jobOwner) {
        this.jobOwner = jobOwner;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public JobIdData getJobId() {
        return jobId;
    }

    public void setJobId(JobIdData jobId) {
        this.jobId = jobId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getInErrorTime() {
        return inErrorTime;
    }

    public void setInErrorTime(long inErrorTime) {
        this.inErrorTime = inErrorTime;
    }

    public long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public JobStatusData getStatus() {
        return status;
    }

    public void setStatus(JobStatusData status) {
        this.status = status;
    }

    public int getTotalNumberOfTasks() {
        return totalNumberOfTasks;
    }

    public void setTotalNumberOfTasks(int totalNumberOfTasks) {
        this.totalNumberOfTasks = totalNumberOfTasks;
    }

    public JobPriorityData getPriority() {
        return priority;
    }

    public void setPriority(JobPriorityData priority) {
        this.priority = priority;
    }

    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public Map<String, JobVariable> getDetailedVariables() {
        return detailedVariables;
    }

    public void setDetailedVariables(Map<String, JobVariable> detailedVariables) {
        this.detailedVariables = detailedVariables;
    }

    public Set<String> getSignals() {
        return signals;
    }

    public void setSignals(Set<String> signals) {
        this.signals = signals;
    }

    public Map<String, Map<String, JobVariable>> getDetailedSignals() {
        return detailedSignals;
    }

    public void setDetailedSignals(Map<String, Map<String, JobVariable>> detailedSignals) {
        this.detailedSignals = detailedSignals;
    }

    public Map<String, String> getVisualizationConnectionStrings() {
        return visualizationConnectionStrings;
    }

    public void setVisualizationConnectionStrings(Map<String, String> visualizationConnectionStrings) {
        this.visualizationConnectionStrings = visualizationConnectionStrings;
    }

    public Map<String, String> getVisualizationIcons() {
        return visualizationIcons;
    }

    public void setVisualizationIcons(Map<String, String> visualizationIcons) {
        this.visualizationIcons = visualizationIcons;
    }

    public Map<Integer, Boolean> getAttachedServices() {
        return attachedServices;
    }

    public void setAttachedServices(Map<Integer, Boolean> attachedServices) {
        this.attachedServices = attachedServices;
    }

    public Map<String, ExternalEndpoint> getExternalEndpointUrls() {
        return externalEndpointUrls;
    }

    public void setExternalEndpointUrls(Map<String, ExternalEndpoint> externalEndpointUrls) {
        this.externalEndpointUrls = externalEndpointUrls;
    }

    public boolean isResultMapPresent() {
        return resultMapPresent;
    }

    public void setResultMapPresent(boolean resultMapPresent) {
        this.resultMapPresent = resultMapPresent;
    }

    public List<String> getPreciousTasks() {
        return preciousTasks;
    }

    public void setPreciousTasks(List<String> preciousTasks) {
        this.preciousTasks = preciousTasks;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public long getCumulatedCoreTime() {
        return cumulatedCoreTime;
    }

    public void setCumulatedCoreTime(long cumulatedCoreTime) {
        this.cumulatedCoreTime = cumulatedCoreTime;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getNumberOfNodesInParallel() {
        return numberOfNodesInParallel;
    }

    public void setNumberOfNodesInParallel(int numberOfNodesInParallel) {
        this.numberOfNodesInParallel = numberOfNodesInParallel;
    }

    @Override
    public String toString() {
        return "JobInfoData{ " + "startTime=" + startTime + ", finishedTime=" + finishedTime + ", submittedTime=" +
               submittedTime + ", removedTime=" + removedTime + ", status=" + status + ", jobId=" + jobId +
               ", parentId=" + parentId + ", childrenCount=" + childrenCount + ", totalNumberOfTasks=" +
               totalNumberOfTasks + ", numberOfPendingTasks=" + numberOfPendingTasks + ", numberOfRunningTasks=" +
               numberOfRunningTasks + ", numberOfFinishedTasks=" + numberOfFinishedTasks + ", numberOfFailedTasks=" +
               numberOfFailedTasks + ", numberOfFaultyTasks=" + numberOfFaultyTasks + ", numberOfInErrorTasks=" +
               numberOfInErrorTasks + ", priority=" + priority + ", jobOwner='" + jobOwner + "', projectName='" +
               projectName + "', bucketName='" + bucketName + "', toBeRemoved=" + toBeRemoved +
               ", genericInformation=" + genericInformation + ", variables=" + variables + ", signals=" + signals +
               ", detailedSignals=" + detailedSignals + ", attachedServices=" + attachedServices +
               ", externalEndpointUrls=" + externalEndpointUrls + ", cumulatedCoreTime=" + cumulatedCoreTime +
               ", numberOfNodes=" + numberOfNodes + ", numberOfNodesInParallel=" + numberOfNodesInParallel + " }";
    }

}
