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

import static org.junit.Assert.*;
import static org.ow2.proactive_grid_cloud_portal.common.dto.JAASGroup.of;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.common.dto.*;
import org.ow2.tests.ProActiveTestClean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public class JAASParserTest extends ProActiveTestClean {

    File dummyPolicyFile = File.createTempFile("security", "policy");

    public JAASParserTest() throws IOException {
    }

    @Test
    public void writeThenRead() throws IOException {
        JAASConfiguration configuration = new JAASConfiguration();

        JAASGroup group = new JAASGroup();
        JAASRMRoles jaasrmRoles = new JAASRMRoles();
        JAASRMAdvancedRoles jaasrmAdvancedRoles = new JAASRMAdvancedRoles();
        jaasrmRoles.setAdvanced(jaasrmAdvancedRoles);
        JAASSchedulerRoles jaasSchedulerRoles = new JAASSchedulerRoles();
        JAASSchedulerAdvancedRoles jaasSchedulerAdvancedRoles = new JAASSchedulerAdvancedRoles();
        jaasSchedulerRoles.setAdvanced(jaasSchedulerAdvancedRoles);
        JAASGroupAdvancedRoles jaasGroupAdvancedRoles = new JAASGroupAdvancedRoles();
        group.setResourceManager(jaasrmRoles);
        group.setScheduler(jaasSchedulerRoles);
        group.setAdvanced(jaasGroupAdvancedRoles);

        group.setName("mygroup");
        group.setPortalAccess(ImmutableMap.ofEntries(of("rm", true),
                                                     of("scheduler", true),
                                                     of("studio", true),
                                                     of("automation-dashboard", false),
                                                     of("workflow-execution", false),
                                                     of("catalog", false),
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
                                                     of("notification-portal", false)));
        group.setRoleReader(true);
        group.setPcaAdmin(true);
        group.setNotificationAdmin(true);
        jaasSchedulerRoles.setHandleOnlyMyJobs(false);
        jaasSchedulerRoles.setChangeJobPriority(ImmutableMap.of("0",
                                                                false,
                                                                "1",
                                                                true,
                                                                "2",
                                                                true,
                                                                "3",
                                                                true,
                                                                "4",
                                                                false,
                                                                "5",
                                                                false));
        jaasSchedulerAdvancedRoles.setHandleJobsWithGenericInformation("toto");
        jaasrmRoles.setResourceManagerRoles(ImmutableMap.of("basic",
                                                            true,
                                                            "read",
                                                            true,
                                                            "write",
                                                            true,
                                                            "provider",
                                                            false,
                                                            "nsadmin",
                                                            false,
                                                            "admin",
                                                            false));
        jaasSchedulerRoles.setSchedulerRoles(ImmutableMap.of("basic",
                                                             true,
                                                             "read",
                                                             true,
                                                             "write",
                                                             true,
                                                             "admin",
                                                             true));
        group.setUserSpaceRoles(ImmutableMap.of("read", true, "write", false));
        group.setGlobalSpaceRoles(ImmutableMap.of("read", true, "write", true));
        jaasrmAdvancedRoles.setRmMyAccountReader(true);
        jaasrmAdvancedRoles.setRmAllAccountsReader(true);
        jaasSchedulerAdvancedRoles.setSchedulerMyAccountReader(true);
        jaasSchedulerAdvancedRoles.setSchedulerAllAccountsReader(true);
        jaasGroupAdvancedRoles.setDeniedMethodCalls(ImmutableList.of("method1", "method2"));
        configuration.setJaasGroups(ImmutableMap.of("mygroup", group));
        JAASParser parser = new JAASParser();
        parser.writeJAASConfiguration(configuration, dummyPolicyFile);
        String fileContents = FileUtils.readFileToString(dummyPolicyFile);
        System.out.println("Policy file generated:");
        System.out.println(fileContents);
        JAASConfiguration configuration2 = parser.readJAASConfiguration(dummyPolicyFile);
        assertEquals("Configuration should be the same after write+read", configuration, configuration2);
    }

}
