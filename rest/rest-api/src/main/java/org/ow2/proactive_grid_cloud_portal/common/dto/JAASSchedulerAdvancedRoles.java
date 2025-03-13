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
package org.ow2.proactive_grid_cloud_portal.common.dto;

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class JAASSchedulerAdvancedRoles {
    boolean otherUsersJobRead = false;

    String handleJobsWithGenericInformation = null;

    String handleJobsWithBucketName = null;

    String handleJobsWithGroupName = null;

    boolean connectToResourceManager = false;

    boolean changeSchedulingPolicy = false;

    boolean schedulerMyAccountReader = false;

    boolean schedulerAllAccountsReader = false;

    public JAASSchedulerAdvancedRoles() {
    }

    public boolean isOtherUsersJobRead() {
        return otherUsersJobRead;
    }

    public void setOtherUsersJobRead(boolean otherUsersJobRead) {
        this.otherUsersJobRead = otherUsersJobRead;
    }

    public String getHandleJobsWithGenericInformation() {
        return handleJobsWithGenericInformation;
    }

    public void setHandleJobsWithGenericInformation(String handleJobsWithGenericInformation) {
        this.handleJobsWithGenericInformation = handleJobsWithGenericInformation;
    }

    public String getHandleJobsWithBucketName() {
        return handleJobsWithBucketName;
    }

    public void setHandleJobsWithBucketName(String handleJobsWithBucketName) {
        this.handleJobsWithBucketName = handleJobsWithBucketName;
    }

    public String getHandleJobsWithGroupName() {
        return handleJobsWithGroupName;
    }

    public void setHandleJobsWithGroupName(String handleJobsWithGroupName) {
        this.handleJobsWithGroupName = handleJobsWithGroupName;
    }

    public boolean isConnectToResourceManager() {
        return connectToResourceManager;
    }

    public void setConnectToResourceManager(boolean connectToResourceManager) {
        this.connectToResourceManager = connectToResourceManager;
    }

    public boolean isChangeSchedulingPolicy() {
        return changeSchedulingPolicy;
    }

    public void setChangeSchedulingPolicy(boolean changeSchedulingPolicy) {
        this.changeSchedulingPolicy = changeSchedulingPolicy;
    }

    public boolean isSchedulerMyAccountReader() {
        return schedulerMyAccountReader;
    }

    public void setSchedulerMyAccountReader(boolean schedulerMyAccountReader) {
        this.schedulerMyAccountReader = schedulerMyAccountReader;
    }

    public boolean isSchedulerAllAccountsReader() {
        return schedulerAllAccountsReader;
    }

    public void setSchedulerAllAccountsReader(boolean schedulerAllAccountsReader) {
        this.schedulerAllAccountsReader = schedulerAllAccountsReader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JAASSchedulerAdvancedRoles that = (JAASSchedulerAdvancedRoles) o;
        return otherUsersJobRead == that.otherUsersJobRead &&
               connectToResourceManager == that.connectToResourceManager &&
               changeSchedulingPolicy == that.changeSchedulingPolicy &&
               schedulerMyAccountReader == that.schedulerMyAccountReader &&
               schedulerAllAccountsReader == that.schedulerAllAccountsReader &&
               Objects.equals(handleJobsWithGenericInformation, that.handleJobsWithGenericInformation) &&
               Objects.equals(handleJobsWithBucketName, that.handleJobsWithBucketName) &&
               Objects.equals(handleJobsWithGroupName, that.handleJobsWithGroupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(otherUsersJobRead,
                            handleJobsWithGenericInformation,
                            handleJobsWithBucketName,
                            handleJobsWithGroupName,
                            connectToResourceManager,
                            changeSchedulingPolicy,
                            schedulerMyAccountReader,
                            schedulerAllAccountsReader);
    }

    @Override
    public String toString() {
        return "JAASSchedulerAdvancedRoles{" + "otherUsersJobRead=" + otherUsersJobRead +
               ", handleJobsWithGenericInformation='" + handleJobsWithGenericInformation + '\'' +
               ", handleJobsWithBucketName='" + handleJobsWithBucketName + '\'' + ", handleJobsWithGroupName='" +
               handleJobsWithGroupName + '\'' + ", connectToResourceManager=" + connectToResourceManager +
               ", changeSchedulingPolicy=" + changeSchedulingPolicy + ", schedulerMyAccountReader=" +
               schedulerMyAccountReader + ", schedulerAllAccountsReader=" + schedulerAllAccountsReader + '}';
    }
}
