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

import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;


@XmlRootElement
public class JAASGroup {

    private String name;

    public static Map.Entry of(String key, Boolean value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    private Map<String, Boolean> portalAccess = ImmutableMap.ofEntries(of("rm", false),
                                                                       of("scheduler", false),
                                                                       of("studio", false),
                                                                       of("automation-dashboard", false),
                                                                       of("workflow-execution", false),
                                                                       of("catalog-portal", false),
                                                                       of("health-dashboard", false),
                                                                       of("job-analytics", false),
                                                                       of("job-gantt", false),
                                                                       of("node-gantt", false),
                                                                       of("job-planner-calendar-def", false),
                                                                       of("job-planner-calendar-def-workflows", false),
                                                                       of("job-planner-execution-planning", false),
                                                                       of("job-planner-gantt-chart", false),
                                                                       of("event-orchestration", false),
                                                                       of("service-automation", false),
                                                                       of("notification-portal", false),
                                                                       of("user-management", false));

    private boolean roleAdmin = false;

    private boolean roleReader = false;

    private boolean manageUsersPermission = false;

    private boolean changePasswordPermission = false;

    private boolean pcaAdmin = false;

    private boolean notificationAdmin = false;

    private boolean tenantAllAccess = false;

    private boolean jopPlannerCanCreateAssociation = false;

    private boolean jobPlannerAdmin = false;

    private boolean catalogAdmin = false;

    private JAASRMRoles resourceManager = new JAASRMRoles();

    private JAASSchedulerRoles scheduler = new JAASSchedulerRoles();

    private Map<String, Boolean> userSpaceRoles = ImmutableMap.of("read", false, "write", false);

    private Map<String, Boolean> globalSpaceRoles = ImmutableMap.of("read", false, "write", false);

    private JAASGroupAdvancedRoles advanced = new JAASGroupAdvancedRoles();

    public JAASGroup() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getPortalAccess() {
        return portalAccess;
    }

    public void setPortalAccess(Map<String, Boolean> portalAccess) {
        this.portalAccess = portalAccess;
    }

    public boolean isRoleAdmin() {
        return roleAdmin;
    }

    public void setRoleAdmin(boolean roleAdmin) {
        this.roleAdmin = roleAdmin;
    }

    public boolean isRoleReader() {
        return roleReader;
    }

    public void setRoleReader(boolean roleReader) {
        this.roleReader = roleReader;
    }

    public boolean isManageUsersPermission() {
        return manageUsersPermission;
    }

    public void setManageUsersPermission(boolean manageUsersPermission) {
        this.manageUsersPermission = manageUsersPermission;
    }

    public boolean isChangePasswordPermission() {
        return changePasswordPermission;
    }

    public void setChangePasswordPermission(boolean changePasswordPermission) {
        this.changePasswordPermission = changePasswordPermission;
    }

    public boolean isPcaAdmin() {
        return pcaAdmin;
    }

    public void setPcaAdmin(boolean pcaAdmin) {
        this.pcaAdmin = pcaAdmin;
    }

    public boolean isNotificationAdmin() {
        return notificationAdmin;
    }

    public void setNotificationAdmin(boolean notificationAdmin) {
        this.notificationAdmin = notificationAdmin;
    }

    public boolean isTenantAllAccess() {
        return tenantAllAccess;
    }

    public void setTenantAllAccess(boolean tenantAllAccess) {
        this.tenantAllAccess = tenantAllAccess;
    }

    public boolean isJopPlannerCanCreateAssociation() {
        return jopPlannerCanCreateAssociation;
    }

    public void setJopPlannerCanCreateAssociation(boolean jopPlannerCanCreateAssociation) {
        this.jopPlannerCanCreateAssociation = jopPlannerCanCreateAssociation;
    }

    public boolean isJobPlannerAdmin() {
        return jobPlannerAdmin;
    }

    public void setJobPlannerAdmin(boolean jobPlannerAdmin) {
        this.jobPlannerAdmin = jobPlannerAdmin;
    }

    public boolean isCatalogAdmin() {
        return catalogAdmin;
    }

    public void setCatalogAdmin(boolean catalogAdmin) {
        this.catalogAdmin = catalogAdmin;
    }

    public JAASRMRoles getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(JAASRMRoles resourceManager) {
        this.resourceManager = resourceManager;
    }

    public JAASSchedulerRoles getScheduler() {
        return scheduler;
    }

    public void setScheduler(JAASSchedulerRoles scheduler) {
        this.scheduler = scheduler;
    }

    public Map<String, Boolean> getUserSpaceRoles() {
        return userSpaceRoles;
    }

    public void setUserSpaceRoles(Map<String, Boolean> userSpaceRoles) {
        this.userSpaceRoles = userSpaceRoles;
    }

    public Map<String, Boolean> getGlobalSpaceRoles() {
        return globalSpaceRoles;
    }

    public void setGlobalSpaceRoles(Map<String, Boolean> globalSpaceRoles) {
        this.globalSpaceRoles = globalSpaceRoles;
    }

    public JAASGroupAdvancedRoles getAdvanced() {
        return advanced;
    }

    public void setAdvanced(JAASGroupAdvancedRoles advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JAASGroup jaasGroup = (JAASGroup) o;
        return roleAdmin == jaasGroup.roleAdmin && roleReader == jaasGroup.roleReader &&
               pcaAdmin == jaasGroup.pcaAdmin && notificationAdmin == jaasGroup.notificationAdmin &&
               tenantAllAccess == jaasGroup.tenantAllAccess &&
               jopPlannerCanCreateAssociation == jaasGroup.jopPlannerCanCreateAssociation &&
               jobPlannerAdmin == jaasGroup.jobPlannerAdmin && catalogAdmin == jaasGroup.catalogAdmin &&
               name.equals(jaasGroup.name) && portalAccess.equals(jaasGroup.portalAccess) &&
               resourceManager.equals(jaasGroup.resourceManager) && scheduler.equals(jaasGroup.scheduler) &&
               userSpaceRoles.equals(jaasGroup.userSpaceRoles) && globalSpaceRoles.equals(jaasGroup.globalSpaceRoles) &&
               advanced.equals(jaasGroup.advanced);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,
                            portalAccess,
                            roleAdmin,
                            roleReader,
                            pcaAdmin,
                            notificationAdmin,
                            tenantAllAccess,
                            jopPlannerCanCreateAssociation,
                            jobPlannerAdmin,
                            catalogAdmin,
                            resourceManager,
                            scheduler,
                            userSpaceRoles,
                            globalSpaceRoles,
                            advanced);
    }

    @Override
    public String toString() {
        return "JAASGroup{" + "name='" + name + '\'' + ", portalAccess=" + portalAccess + ", roleAdmin=" + roleAdmin +
               ", roleReader=" + roleReader + ", pcaAdmin=" + pcaAdmin + ", notificationAdmin=" + notificationAdmin +
               ", tenantAllAccess=" + tenantAllAccess + ", jopPlannerCanCreateAssociation=" +
               jopPlannerCanCreateAssociation + ", jobPlannerAdmin=" + jobPlannerAdmin + ", catalogAdmin=" +
               catalogAdmin + ", resourceManager=" + resourceManager + ", scheduler=" + scheduler +
               ", userSpaceRoles=" + userSpaceRoles + ", globalSpaceRoles=" + globalSpaceRoles + ", advanced=" +
               advanced + '}';
    }

}
