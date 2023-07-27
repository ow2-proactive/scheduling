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
package org.ow2.proactive.authentication;

import java.io.Serializable;
import java.util.Set;


/**
 * A class representing a user information
 *
 * @author ActiveEon Team
 * @since 28/07/2017
 */
public class UserData implements Serializable {

    public UserData() {
        super();
    }

    private String userName;

    private Set<String> groups;

    private String tenant;

    private String domain;

    private boolean filterByTenant;

    private boolean allTenantPermission;

    private boolean allJobPlannerPermission;

    private boolean handleOnlyMyJobsPermission;

    private boolean otherUsersJobReadPermission;

    public boolean isAllJobPlannerPermission() {
        return allJobPlannerPermission;
    }

    public void setAllJobPlannerPermission(boolean allJobPlannerPermission) {
        this.allJobPlannerPermission = allJobPlannerPermission;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isFilterByTenant() {
        return filterByTenant;
    }

    public void setFilterByTenant(boolean filterByTenant) {
        this.filterByTenant = filterByTenant;
    }

    public boolean isAllTenantPermission() {
        return allTenantPermission;
    }

    public void setAllTenantPermission(boolean allTenantPermission) {
        this.allTenantPermission = allTenantPermission;
    }

    public boolean isHandleOnlyMyJobsPermission() {
        return handleOnlyMyJobsPermission;
    }

    public void setHandleOnlyMyJobsPermission(boolean handleOnlyMyJobsPermission) {
        this.handleOnlyMyJobsPermission = handleOnlyMyJobsPermission;
    }

    public boolean isOtherUsersJobReadPermission() {
        return otherUsersJobReadPermission;
    }

    public void setOtherUsersJobReadPermission(boolean otherUsersJobReadPermission) {
        this.otherUsersJobReadPermission = otherUsersJobReadPermission;
    }
}
