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
package org.ow2.proactive.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.management.MBeanPermission;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.permissions.*;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.scheduler.core.SchedulerFrontend;
import org.ow2.proactive.scheduler.core.jmx.mbean.AllAccountsMBeanImpl;
import org.ow2.proactive.scheduler.core.jmx.mbean.MyAccountMBeanImpl;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.permissions.*;
import org.ow2.proactive_grid_cloud_portal.common.JAASParserInterface;
import org.ow2.proactive_grid_cloud_portal.common.PortalAccessPermission;
import org.ow2.proactive_grid_cloud_portal.common.dto.*;
import org.ow2.proactive_grid_cloud_portal.dataspace.RestDataspaceImpl;


public class JAASParser implements JAASParserInterface {

    private static final Logger logger = Logger.getLogger(JAASParser.class);

    private static final String I = "    ";

    private static final String P = "permission ";

    private static final String S = "******";

    private static final String NL = "\n";

    private static final String GROUP_ROLE = "grant principal org.ow2.proactive.authentication.principals.GroupNamePrincipal ";

    private static final String GLOBAL_ROLE_BEGIN = "grant {";

    private static final String GLOBAL_ROLE = "//\n" + "// OTHER PERMISSIONS\n" + "//\n" +
                                              "// Allow all actions to subjects without principals above\n" +
                                              "grant {\n" + "    permission java.security.AllPermission;\n" + "};\n";

    private static final String DEFAULT_FILE_ROLE = "    // Granting file reading permission i.e. to read RRD database via JMX\n" +
                                                    "    permission java.io.FilePermission \"<<ALL FILES>>\", \"read\";\n";

    private static final String DEFAULT_MBEAN_ROLE = "    // AuthPermission is requires for those who would like to access any mbean\n" +
                                                     "    permission javax.security.auth.AuthPermission \"getSubject\";\n" +
                                                     "    permission java.lang.RuntimePermission \"setContextClassLoader\";\n" +
                                                     "    permission javax.management.MBeanPermission \"-#-[-]\", \"queryNames\";\n" +
                                                     "    permission javax.management.MBeanPermission \"javax.management.MBeanServerDelegate#-[JMImplementation:type=MBeanServerDelegate]\", \"addNotificationListener\";\n";

    private static final String RM_MY_ACCOUNT_BEAN_ROLE = "    permission javax.management.MBeanPermission \"org.ow2.proactive.resourcemanager.core.jmx.mbean.MyAccountMBeanImpl#*[*:*]\", \"*\";\n" +
                                                          "    permission javax.management.MBeanPermission \"org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBeanImpl#*[*:*]\", \"*\";\n";

    private static final String RM_ALL_ACCOUNTS_BEAN_ROLE = "    permission javax.management.MBeanPermission \"org.ow2.proactive.resourcemanager.core.jmx.mbean.AllAccountsMBeanImpl#*[*:*]\", \"*\";\n" +
                                                            "    permission javax.management.MBeanPermission \"org.ow2.proactive.resourcemanager.core.jmx.mbean.ManagementMBeanImpl#*[*:*]\", \"*\";\n";

    private static final String SCHED_MY_ACCOUNT_BEAN_ROLE = "    permission javax.management.MBeanPermission \"org.ow2.proactive.scheduler.core.jmx.mbean.MyAccountMBeanImpl#*[*:*]\", \"*\";\n" +
                                                             "    permission javax.management.MBeanPermission \"org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBeanImpl#*[*:*]\", \"*\";\n";

    private static final String SCHED_ALL_ACCOUNTS_BEAN_ROLE = "    permission javax.management.MBeanPermission \"org.ow2.proactive.scheduler.core.jmx.mbean.AllAccountsMBeanImpl#*[*:*]\", \"*\";\n" +
                                                               "    permission javax.management.MBeanPermission \"org.ow2.proactive.scheduler.core.jmx.mbean.ManagementMBeanImpl#*[*:*]\", \"*\";\n";

    private static final String DEFAULT_DB_ROLE = "    // API - access to database\n" +
                                                  "    permission java.sql.SQLPermission \"setLog\";\n" +
                                                  "    permission java.sql.SQLPermission \"callAbort\";\n" +
                                                  "    permission java.sql.SQLPermission \"setSyncFactory\";\n" +
                                                  "    permission java.sql.SQLPermission \"setNetworkTimeout\";\n" +
                                                  "    permission java.util.PropertyPermission \"*\", \"read, write\";\n" +
                                                  "    permission java.net.SocketPermission \"*\", \"accept, connect, listen, resolve\";\n";

    private static final String JAAS_FILE_PATH = "config/security.java.policy-server";

    @Override
    public JAASConfiguration readJAASConfiguration() throws IOException {
        return readJAASConfiguration(new File(System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()),
                                              JAAS_FILE_PATH));
    }

    public JAASConfiguration readJAASConfiguration(File jaasFilePath) throws IOException {
        JAASConfiguration answer = new JAASConfiguration();
        List<String> lines = FileUtils.readLines(jaasFilePath, PASchedulerProperties.FILE_ENCODING.getValueAsString());
        Map<String, JAASGroup> jaasGroups = new LinkedHashMap<>();
        int beginGroup = -1;
        int beginGlobalRole = -1;
        String groupName = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith(GLOBAL_ROLE_BEGIN)) {
                beginGlobalRole = i + 1;
            } else if (line.startsWith(GROUP_ROLE)) {
                beginGroup = i + 1;
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                }
                groupName = line.replace(GROUP_ROLE, "").replace("{", "").replace("\"", "").trim();
            } else if (line.startsWith("}")) {
                if (beginGroup < 0 && beginGlobalRole < 0) {
                    throw new IllegalStateException("Invalid end group at line " + i + " without matching begin");
                }
                if (beginGlobalRole >= 0) {
                    beginGlobalRole = -1;
                    continue;
                }
                jaasGroups.put(groupName, readJAASGroup(groupName, lines.subList(beginGroup, i)));
                beginGroup = -1;
                groupName = null;
            }
        }
        answer.setJaasGroups(jaasGroups);
        return answer;
    }

    private JAASGroup readJAASGroup(String name, List<String> lines) {
        JAASGroup answer = new JAASGroup();
        JAASSchedulerRoles jaasSchedulerRoles = new JAASSchedulerRoles();
        JAASSchedulerAdvancedRoles jaasSchedulerAdvancedRoles = new JAASSchedulerAdvancedRoles();
        jaasSchedulerRoles.setAdvanced(jaasSchedulerAdvancedRoles);
        JAASRMRoles jaasrmRoles = new JAASRMRoles();
        JAASRMAdvancedRoles jaasrmAdvancedRoles = new JAASRMAdvancedRoles();
        jaasrmRoles.setAdvanced(jaasrmAdvancedRoles);
        JAASGroupAdvancedRoles jaasGroupAdvancedRoles = new JAASGroupAdvancedRoles();
        answer.setName(name);
        answer.setResourceManager(jaasrmRoles);
        answer.setScheduler(jaasSchedulerRoles);
        answer.setAdvanced(jaasGroupAdvancedRoles);
        List<String> deniedMethodCalls = new ArrayList<>();

        boolean allSchedulerRoles = false;
        boolean allRMRoles = false;
        boolean allUserSpaceRoles = false;
        boolean allGlobalSpaceRoles = false;
        List<String> schedulerRoles = new ArrayList<>();
        List<String> rmRoles = new ArrayList<>();
        List<String> userSpaceRoles = new ArrayList<>();
        List<String> globalSpaceRoles = new ArrayList<>();

        for (String line : lines) {
            String originalLine = line;
            try {
                line = line.trim();
                if (line.startsWith(P)) {
                    // remove comments
                    if (line.contains("//")) {
                        line = line.substring(0, line.indexOf("//"));
                    }
                    line = line.replace(P, "").trim();
                    line = line.substring(0, line.length() - 1).trim(); // remove ; character
                    String permissionName;
                    if (line.contains(" ")) {
                        // permission with parameter
                        permissionName = line.substring(0, line.indexOf(" "));
                    } else {
                        // permission without parameter
                        permissionName = line;
                    }

                    if (PortalAccessPermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        Map<String, Boolean> portalPermissions = new LinkedHashMap<>(answer.getPortalAccess());
                        if ("*".equals(parameter)) {
                            portalPermissions.replaceAll((k, v) -> true);
                        } else {
                            for (String portal : Arrays.asList(parameter.split("\\s*,\\s*"))) {
                                portalPermissions.put(portal, true);
                            }
                        }
                        answer.setPortalAccess(portalPermissions);
                    } else if (RoleAdminPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setRoleAdmin(true);
                    } else if (RoleReaderPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setRoleReader(true);
                    } else if (ManageUsersPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setManageUsersPermission(true);
                    } else if (ChangePasswordPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setChangePasswordPermission(true);
                    } else if (PcaAdminPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setPcaAdmin(true);
                    } else if (NotificationAdminPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setNotificationAdmin(true);
                    } else if (CatalogAllAccessPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setCatalogAdmin(true);
                    } else if (HandleOnlyMyJobsPermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        boolean myJobs = Boolean.parseBoolean(parameter);
                        jaasSchedulerRoles.setHandleOnlyMyJobs(myJobs);
                    } else if (OtherUsersJobReadPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasSchedulerAdvancedRoles.setOtherUsersJobRead(true);
                    } else if (ChangePriorityPermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        Map<String, Boolean> priorityPermission = new LinkedHashMap<>(jaasSchedulerRoles.getChangeJobPriority());
                        for (String priority : Arrays.asList(parameter.split("\\s*,\\s*"))) {
                            priorityPermission.put(priority, true);
                        }
                        jaasSchedulerRoles.setChangeJobPriority(priorityPermission);
                    } else if (HandleJobsWithGenericInformationPermission.class.getCanonicalName()
                                                                               .equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        jaasSchedulerAdvancedRoles.setHandleJobsWithGenericInformation(parameter);
                    } else if (HandleJobsWithBucketNamePermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        jaasSchedulerAdvancedRoles.setHandleJobsWithBucketName(parameter);
                    } else if (HandleJobsWithGroupNamePermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        jaasSchedulerAdvancedRoles.setHandleJobsWithGroupName(parameter);
                    } else if (ConnectToResourceManagerPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasSchedulerAdvancedRoles.setConnectToResourceManager(true);
                    } else if (ChangePolicyPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasSchedulerAdvancedRoles.setChangeSchedulingPolicy(true);
                    } else if (TenantAllAccessPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setTenantAllAccess(true);
                    } else if (JPCanCreateAssociationPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setJopPlannerCanCreateAssociation(true);
                    } else if (JobPlannerAllAccessPermission.class.getCanonicalName().equals(permissionName)) {
                        answer.setJobPlannerAdmin(true);
                    } else if (AllPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasGroupAdvancedRoles.setAllPermissions(true);
                    } else if (ServiceRolePermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        if (parameter.startsWith(SchedulerFrontend.class.getCanonicalName())) {
                            String subParameter = parameter.replace(SchedulerFrontend.class.getCanonicalName() + ".",
                                                                    "")
                                                           .trim();
                            if ("*".equals(subParameter)) {
                                allSchedulerRoles = true;
                            } else {
                                schedulerRoles.add(subParameter);
                            }
                        } else if (parameter.startsWith(RMCore.class.getCanonicalName())) {
                            String subParameter = parameter.replace(RMCore.class.getCanonicalName() + ".", "").trim();
                            if ("*".equals(subParameter)) {
                                allRMRoles = true;
                            } else {
                                rmRoles.add(subParameter);
                            }
                        } else if (parameter.startsWith(RestDataspaceImpl.class.getCanonicalName() + ".user")) {
                            String subParameter = parameter.replace(RestDataspaceImpl.class.getCanonicalName() +
                                                                    ".user.", "")
                                                           .trim();
                            if ("*".equals(subParameter)) {
                                allUserSpaceRoles = true;
                            } else {
                                userSpaceRoles.add(subParameter);
                            }
                        } else if (parameter.startsWith(RestDataspaceImpl.class.getCanonicalName() + ".global")) {
                            String subParameter = parameter.replace(RestDataspaceImpl.class.getCanonicalName() +
                                                                    ".global.", "")
                                                           .trim();
                            if ("*".equals(subParameter)) {
                                allGlobalSpaceRoles = true;
                            } else {
                                globalSpaceRoles.add(subParameter);
                            }
                        }
                    } else if (RMCoreAllPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasrmRoles.setResourceManagerGlobalAdmin(true);
                    } else if (NSAdminPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasrmRoles.setNodeSourceAdmin(true);
                    } else if (NodeUserAllPermission.class.getCanonicalName().equals(permissionName)) {
                        jaasrmAdvancedRoles.setNodeFullAccess(true);
                    } else if (MBeanPermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim();
                        if (parameter.contains(MyAccountMBeanImpl.class.getCanonicalName())) {
                            jaasSchedulerAdvancedRoles.setSchedulerMyAccountReader(true);
                        } else if (parameter.contains(org.ow2.proactive.resourcemanager.core.jmx.mbean.MyAccountMBeanImpl.class.getCanonicalName())) {
                            jaasrmAdvancedRoles.setRmMyAccountReader(true);
                        } else if (parameter.contains(AllAccountsMBeanImpl.class.getCanonicalName())) {
                            jaasSchedulerAdvancedRoles.setSchedulerAllAccountsReader(true);
                        } else if (parameter.contains(org.ow2.proactive.resourcemanager.core.jmx.mbean.AllAccountsMBeanImpl.class.getCanonicalName())) {
                            jaasrmAdvancedRoles.setRmAllAccountsReader(true);
                        }
                    } else if (DeniedMethodCallPermission.class.getCanonicalName().equals(permissionName)) {
                        String parameter = line.replace(permissionName, "").trim().replace("\"", "").trim();
                        deniedMethodCalls.add(parameter);
                    }
                }
            } catch (Exception e) {
                logger.error("Error when reading line '" + originalLine + "': " + e.getMessage());
            }
        }

        Map<String, Boolean> rmPermissions = new LinkedHashMap<>(jaasrmRoles.getResourceManagerRoles());
        if (allRMRoles) {
            rmPermissions.replaceAll((k, v) -> true);
        } else {
            for (String role : rmRoles) {
                rmPermissions.put(role, true);
            }
        }
        jaasrmRoles.setResourceManagerRoles(rmPermissions);

        Map<String, Boolean> schedPermissions = new LinkedHashMap<>(jaasSchedulerRoles.getSchedulerRoles());
        if (allSchedulerRoles) {
            schedPermissions.replaceAll((k, v) -> true);
        } else {
            for (String role : schedulerRoles) {
                schedPermissions.put(role, true);
            }
        }
        jaasSchedulerRoles.setSchedulerRoles(schedPermissions);

        Map<String, Boolean> userSpacePermissions = new LinkedHashMap<>(answer.getUserSpaceRoles());
        if (allUserSpaceRoles) {
            userSpacePermissions.replaceAll((k, v) -> true);
        } else {
            for (String role : userSpaceRoles) {
                userSpacePermissions.put(role, true);
            }
        }
        answer.setUserSpaceRoles(userSpacePermissions);

        Map<String, Boolean> globalSpacePermissions = new LinkedHashMap<>(answer.getGlobalSpaceRoles());
        if (allGlobalSpaceRoles) {
            globalSpacePermissions.replaceAll((k, v) -> true);
        } else {
            for (String role : globalSpaceRoles) {
                globalSpacePermissions.put(role, true);
            }
        }
        answer.setGlobalSpaceRoles(globalSpacePermissions);

        jaasGroupAdvancedRoles.setDeniedMethodCalls(deniedMethodCalls);

        return answer;
    }

    @Override
    public void writeJAASConfiguration(JAASConfiguration configuration) throws IOException {
        writeJAASConfiguration(configuration,
                               new File(System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()),
                                        JAAS_FILE_PATH));
    }

    public void writeJAASConfiguration(JAASConfiguration configuration, File jaasFilePath) throws IOException {
        try {
            SecurityPolicyLoader.lock.lockInterruptibly();
            FileUtils.write(jaasFilePath,
                            writeJAASConfigurationToString(configuration),
                            PASchedulerProperties.FILE_ENCODING.getValueAsString());
            SecurityPolicyLoader.reloadPolicy();
        } catch (InterruptedException e) {
            logger.warn("writeJAASConfiguration interrupted", e);
        } finally {
            SecurityPolicyLoader.lock.unlock();
        }
    }

    private String writeJAASConfigurationToString(JAASConfiguration configuration) {
        StringBuilder answer = new StringBuilder();
        for (JAASGroup group : configuration.getJaasGroups().values()) {
            answer.append(writeJAASGroup(group));
            answer.append(NL + NL);
        }
        answer.append(GLOBAL_ROLE);
        return answer.toString();
    }

    private String writeJAASGroup(JAASGroup group) {
        StringBuilder answer = new StringBuilder();
        answer.append(GROUP_ROLE + "\"" + group.getName() + "\" {" + NL);
        answer.append(I + "// ****** Global permissions ******" + NL);
        boolean isAllPortalPermission = group.getPortalAccess().entrySet().stream().allMatch(e -> e.getValue());
        boolean noPortalPermission = group.getPortalAccess().entrySet().stream().noneMatch(e -> e.getValue());
        if (!noPortalPermission) {
            String portalPermissionString = group.getPortalAccess()
                                                 .entrySet()
                                                 .stream()
                                                 .filter(e -> e.getValue())
                                                 .map(e -> e.getKey())
                                                 .reduce("", (st, e) -> st + (!st.isEmpty() ? "," : "") + e);
            answer.append(I + P + PortalAccessPermission.class.getCanonicalName() + " \"" +
                          (isAllPortalPermission ? "*" : portalPermissionString) + "\";" + NL);
        }

        if (group.isRoleAdmin()) {
            answer.append(I + P + RoleAdminPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isRoleReader()) {
            answer.append(I + P + RoleReaderPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isManageUsersPermission()) {
            answer.append(I + P + ManageUsersPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isChangePasswordPermission()) {
            answer.append(I + P + ChangePasswordPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isTenantAllAccess()) {
            answer.append(I + P + TenantAllAccessPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isPcaAdmin()) {
            answer.append(I + P + PcaAdminPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isNotificationAdmin()) {
            answer.append(I + P + NotificationAdminPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isJopPlannerCanCreateAssociation()) {
            answer.append(I + P + JPCanCreateAssociationPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isJobPlannerAdmin()) {
            answer.append(I + P + JobPlannerAllAccessPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.isCatalogAdmin()) {
            answer.append(I + P + CatalogAllAccessPermission.class.getCanonicalName() + ";" + NL);
        }
        if (group.getAdvanced().isAllPermissions()) {
            answer.append(I + P + AllPermission.class.getCanonicalName() + ";" + NL);
        }
        answer.append(NL);
        answer.append(I + "// ****** Scheduler permissions ******" + NL);
        if (group.getScheduler().getHandleOnlyMyJobs() != null) {
            if (group.getScheduler().getHandleOnlyMyJobs()) {
                answer.append(I + P + HandleOnlyMyJobsPermission.class.getCanonicalName() + " \"true\";" + NL);
            } else {
                answer.append(I + P + HandleOnlyMyJobsPermission.class.getCanonicalName() + " \"false\";" + NL);
            }
        }

        if (group.getScheduler().getAdvanced().isOtherUsersJobRead()) {
            answer.append(I + P + OtherUsersJobReadPermission.class.getCanonicalName() + ";" + NL);
        }

        if (group.getScheduler().getChangeJobPriority() != null &&
            group.getScheduler().getChangeJobPriority().values().stream().anyMatch(e -> e.booleanValue())) {
            answer.append(I + P + ChangePriorityPermission.class.getCanonicalName() + " \"" + group.getScheduler()
                                                                                                   .getChangeJobPriority()
                                                                                                   .entrySet()
                                                                                                   .stream()
                                                                                                   .filter(e -> e.getValue())
                                                                                                   .map(e -> e.getKey())
                                                                                                   .reduce("",
                                                                                                           (st, e) -> st +
                                                                                                                      (!st.isEmpty() ? ","
                                                                                                                                     : "") +
                                                                                                                      e) +
                          "\";" + NL);
        }

        if (group.getScheduler().getAdvanced().getHandleJobsWithGenericInformation() != null) {
            answer.append(I + P + HandleJobsWithGenericInformationPermission.class.getCanonicalName() + " \"" +
                          group.getScheduler().getAdvanced().getHandleJobsWithGenericInformation() + "\";" + NL);
        }

        if (group.getScheduler().getAdvanced().getHandleJobsWithBucketName() != null) {
            answer.append(I + P + HandleJobsWithBucketNamePermission.class.getCanonicalName() + " \"" +
                          group.getScheduler().getAdvanced().getHandleJobsWithBucketName() + "\";" + NL);
        }

        if (group.getScheduler().getAdvanced().getHandleJobsWithGroupName() != null) {
            answer.append(I + P + HandleJobsWithGroupNamePermission.class.getCanonicalName() + " \"" +
                          group.getScheduler().getAdvanced().getHandleJobsWithGroupName() + "\";" + NL);
        }

        if (group.getScheduler().getAdvanced().isConnectToResourceManager()) {
            answer.append(I + P + ConnectToResourceManagerPermission.class.getCanonicalName() + ";" + NL);
        }

        if (group.getScheduler().getAdvanced().isChangeSchedulingPolicy()) {
            answer.append(I + P + ChangePolicyPermission.class.getCanonicalName() + ";" + NL);
        }

        if (group.getScheduler() != null &&
            group.getScheduler().getSchedulerRoles().values().stream().anyMatch(e -> e.booleanValue())) {
            if (group.getScheduler().getSchedulerRoles().values().stream().allMatch(e -> e.booleanValue())) {
                answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                              SchedulerFrontend.class.getCanonicalName() + ".*\";" + NL);
            } else {
                group.getScheduler()
                     .getSchedulerRoles()
                     .entrySet()
                     .stream()
                     .filter(e -> e.getValue())
                     .forEach(e -> answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                                                 SchedulerFrontend.class.getCanonicalName() + "." + e.getKey() + "\";" +
                                                 NL));
            }
        }

        answer.append(NL);
        answer.append(I + "// ****** Resource Manager permissions ******" + NL);

        if (group.getResourceManager().isResourceManagerGlobalAdmin()) {
            answer.append(I + P + RMCoreAllPermission.class.getCanonicalName() + ";" + NL);
        }

        if (group.getResourceManager().isNodeSourceAdmin()) {
            answer.append(I + P + NSAdminPermission.class.getCanonicalName() + ";" + NL);
        }

        if (group.getResourceManager().getAdvanced().isNodeFullAccess()) {
            answer.append(I + P + NodeUserAllPermission.class.getCanonicalName() + ";" + NL);
        }

        answer.append(NL);

        if (group.getResourceManager().getResourceManagerRoles() != null &&
            group.getResourceManager().getResourceManagerRoles().values().stream().anyMatch(e -> e.booleanValue())) {
            if (group.getResourceManager()
                     .getResourceManagerRoles()
                     .values()
                     .stream()
                     .allMatch(e -> e.booleanValue())) {
                answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                              RMCore.class.getCanonicalName() + ".*\";" + NL);
            } else {
                group.getResourceManager()
                     .getResourceManagerRoles()
                     .entrySet()
                     .stream()
                     .filter(e -> e.getValue())
                     .forEach(e -> answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                                                 RMCore.class.getCanonicalName() + "." + e.getKey() + "\";" + NL));
            }
        }

        answer.append(NL);
        answer.append(I + "// ****** User and Global Space permissions ******" + NL);

        if (group.getUserSpaceRoles() != null &&
            group.getUserSpaceRoles().values().stream().anyMatch(e -> e.booleanValue())) {
            if (group.getUserSpaceRoles().values().stream().allMatch(e -> e.booleanValue())) {
                answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                              RestDataspaceImpl.class.getCanonicalName() + ".user.*\";" + NL);
            } else {
                group.getUserSpaceRoles()
                     .entrySet()
                     .stream()
                     .filter(e -> e.getValue())
                     .forEach(e -> answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                                                 RestDataspaceImpl.class.getCanonicalName() + ".user." + e.getKey() +
                                                 "\";" + NL));
            }
        }

        answer.append(NL);

        if (group.getGlobalSpaceRoles() != null &&
            group.getGlobalSpaceRoles().values().stream().anyMatch(e -> e.booleanValue())) {
            if (group.getGlobalSpaceRoles().values().stream().allMatch(e -> e.booleanValue())) {
                answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                              RestDataspaceImpl.class.getCanonicalName() + ".global.*\";" + NL);
            } else {
                group.getGlobalSpaceRoles()
                     .entrySet()
                     .stream()
                     .filter(e -> e.getValue())
                     .forEach(e -> answer.append(I + P + ServiceRolePermission.class.getCanonicalName() + " \"" +
                                                 RestDataspaceImpl.class.getCanonicalName() + ".global." + e.getKey() +
                                                 "\";" + NL));
            }
        }

        if (!group.getAdvanced().getDeniedMethodCalls().isEmpty()) {
            answer.append(NL);
            answer.append(I + "// ****** Advanced permissions ******" + NL);

            for (String deniedMethod : group.getAdvanced().getDeniedMethodCalls()) {
                answer.append(I + P + DeniedMethodCallPermission.class.getCanonicalName() + " \"" + deniedMethod +
                              "\";" + NL);
            }
        }

        answer.append(NL);
        answer.append(I + "// ****** JMX permissions ******" + NL);

        answer.append(DEFAULT_MBEAN_ROLE);
        if (group.getScheduler().getAdvanced().isSchedulerMyAccountReader()) {
            answer.append(SCHED_MY_ACCOUNT_BEAN_ROLE);
        }
        if (group.getScheduler().getAdvanced().isSchedulerAllAccountsReader()) {
            answer.append(SCHED_ALL_ACCOUNTS_BEAN_ROLE);
        }
        if (group.getResourceManager().getAdvanced().isRmMyAccountReader()) {
            answer.append(RM_MY_ACCOUNT_BEAN_ROLE);
        }
        if (group.getResourceManager().getAdvanced().isRmAllAccountsReader()) {
            answer.append(RM_ALL_ACCOUNTS_BEAN_ROLE);
        }

        answer.append(NL);
        answer.append(I + "// ****** Default permissions ******" + NL);

        answer.append(DEFAULT_FILE_ROLE);

        answer.append(NL);

        answer.append(DEFAULT_DB_ROLE);

        answer.append(NL);

        answer.append("};" + NL);
        return answer.toString();
    }
}
