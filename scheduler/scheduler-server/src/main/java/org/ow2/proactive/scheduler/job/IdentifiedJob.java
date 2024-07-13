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
package org.ow2.proactive.scheduler.job;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.ow2.proactive.authentication.principals.TenantPrincipal;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * This class represented an authenticate job. It is what the scheduler should
 * be able to managed.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class IdentifiedJob implements Serializable {

    /** Job Identification */
    private JobId jobId;

    /** User identification */
    private UserIdentificationImpl userIdentification;

    /** is this job finished */
    private boolean finished = false;

    /** genericInformation of this job */
    private final Map<String, String> genericInformation;

    /**
     * Identify job constructor with a given job and Identification.
     *
     * @param jobId
     *            a job identification.
     * @param userIdentification
     *            a user identification that should be able to identify the job
     *            user.
     * @param genericInformation
     *            map containing the generic information of the job
     */
    public IdentifiedJob(JobId jobId, UserIdentificationImpl userIdentification,
            Map<String, String> genericInformation) {
        this.jobId = jobId;
        this.userIdentification = userIdentification;
        this.genericInformation = genericInformation;

    }

    /**
     * To get the jobId
     *
     * @return the jobId
     */
    public JobId getJobId() {
        return jobId;
    }

    /**
     * To get the userIdentification
     *
     * @return the userIdentification
     */
    public UserIdentificationImpl getUserIdentification() {
        return userIdentification;
    }

    /**
     * To get the genericInformation
     *
     * @return the genericInformation
     */
    public Map<String, String> getGenericInformation() {
        return genericInformation;
    }

    /**
     * Check if the given user identification can manage this job.
     *
     * @param userId
     *            the user identification to check.
     * @return true if userId has permission to manage this job.
     */
    public boolean hasRight(UserIdentificationImpl userId) {
        if (userIdentification == null) {
            return false;
        }

        String jobOwner = userIdentification.getUsername();
        Set<TenantPrincipal> tenantJobPrincipals = userIdentification.getSubject().getPrincipals(TenantPrincipal.class);
        String jobTenant = tenantJobPrincipals != null &&
                           tenantJobPrincipals.size() == 1 ? tenantJobPrincipals.iterator().next().getName() : "EMPTY";

        String userName = userId.getUsername();

        String userTenant = userId.getTenant() != null ? userId.getTenant() : "EMPTY";

        return jobOwner.equals(userName) && jobTenant.equals(userTenant);
    }

    /**
     * Check if the given user identification has access to this job according to the tenant policy.
     *
     * @param userId
     *            the user identification to check.
     * @return true if userId has permission to access this job.
     */
    public boolean hasTenantAccess(UserIdentificationImpl userId) {
        // check tenant permission
        if (PASchedulerProperties.SCHEDULER_TENANT_FILTER.getValueAsBoolean() && !userId.isAllTenantPermission()) {
            return userIdentification.getTenant() == null || userIdentification.getTenant().equals(userId.getTenant());
        }
        return true;
    }

    /**
     * Return true if the job isFinished, false otherwise.
     *
     * @return the finished status of the job.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Set the finish status of the job.
     *
     * @param finished
     *            the finish status to set.
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return jobId.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdentifiedJob) {
            return jobId.equals(((IdentifiedJob) obj).jobId);
        }

        return false;
    }
}
