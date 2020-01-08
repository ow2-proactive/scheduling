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
package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.job.JobInfo;


/**
 * The JobEmailStatus defines the status of the job for email notification.
 * The status of the job could be either aborted (due to an error policy, manual kill by the user,
 * or due to resource failure). or it could finish with errors (when at least one task fails),
 * or finally it could finish without issues in case of normal behavior.
 *
 * @author ActiveEon Team
 * @since 2020-01-07
 */
public class JobEmailStatus {
    private boolean aborted;

    private boolean withErrors;

    private JobInfo jobInfo;

    public JobEmailStatus(JobInfo jobInfo) {
        this.aborted = false;
        this.withErrors = false;
        this.jobInfo = jobInfo;
    }

    /**
     * Verifies if the job has any Failed, In-Error, or Faulty tasks. 'withErrors' attribute is turned to true
     */
    public void checkTasksWithErrors() {
        this.withErrors = jobInfo.getNumberOfFaultyTasks() + jobInfo.getNumberOfInErrorTasks() +
                          jobInfo.getNumberOfFailedTasks() > 0;
    }

    /**
     * Verifies if the job has been aborted with status Cancelled, Failed, or Killed. 'aborted' attribute is turned to true
     */
    public void checkJobAborted() {
        this.aborted = jobInfo.getStatus().toString().matches("Canceled|Failed|Killed");
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean aborted) {
        this.aborted = aborted;
    }

    public boolean isWithErrors() {
        return withErrors;
    }

    public void setWithErrors(boolean withErrors) {
        this.withErrors = withErrors;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }
}
