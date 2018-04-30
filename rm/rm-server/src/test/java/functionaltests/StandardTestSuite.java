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
package functionaltests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import functionaltests.authentication.AuthenticationTest;
import functionaltests.authentication.ConnectionTest;
import functionaltests.authentication.ConnectionTest2;
import functionaltests.dataspace.DataSpaceNodeConfigurationAgentTest;
import functionaltests.db.NodeHistoryTest;
import functionaltests.db.NodeSourcesTest;
import functionaltests.db.RMDBManagerBufferTest;
import functionaltests.db.RMDBManagerTest;
import functionaltests.execremote.TestExecRemote;
import functionaltests.jmx.RMProxyUserInterfaceTest;
import functionaltests.jmx.ResourceManagerJMXTest;
import functionaltests.jmx.account.AddGetDownRemoveTest;
import functionaltests.jmx.account.AddGetReleaseRemoveTest;
import functionaltests.jmx.account.AddGetRemoveTest;
import functionaltests.jmx.account.AddGetTest;
import functionaltests.monitor.TestRMMonitoring;
import functionaltests.monitor.TestRMNodeMonitoring;
import functionaltests.nodesource.TestLocalInfrastructureRestartDownNodesPolicy;
import functionaltests.nodesource.TestLocalInfrastructureStaticPolicy;
import functionaltests.nodesource.TestLocalInfrastructureTimeSlotPolicy;
import functionaltests.nodesource.deployment.LocalInfrastructureLifecycleTest;
import functionaltests.nodesource.deployment.SSHInfrastructureV2LifecycleTest;
import functionaltests.nodesource.deployment.TestNodeSourceAfterRestart;
import functionaltests.nodestate.TestAddRemoveAll;
import functionaltests.nodestate.TestAdminAddingNodes;
import functionaltests.nodestate.TestConcurrentUsers;
import functionaltests.nodestate.TestNodeEncoding;
import functionaltests.nodestate.TestNodeSourcesActions;
import functionaltests.nodestate.TestNodesStates;
import functionaltests.permissions.TestNSAdminPermissions;
import functionaltests.permissions.TestNSProviderPermissions;
import functionaltests.selectionscript.DynamicSelectionScriptTest;
import functionaltests.selectionscript.SelectionWithNodesExclusionTest;
import functionaltests.selectionscript.SelectionWithSeveralScriptsTest;
import functionaltests.selectionscript.SelectionWithSeveralScriptsTest2;
import functionaltests.selectionscript.StaticSelectionScriptTest;
import functionaltests.selectionscript.UnauthorizedSelectionScriptTest;
import functionaltests.topology.LocalSelectionTest;
import functionaltests.topology.SelectionTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({ AuthenticationTest.class, ConnectionTest.class, ConnectionTest2.class,
                      DataSpaceNodeConfigurationAgentTest.class, NodeHistoryTest.class, NodeSourcesTest.class,
                      RMDBManagerBufferTest.class, RMDBManagerTest.class, TestExecRemote.class,
                      AddGetDownRemoveTest.class, AddGetReleaseRemoveTest.class, AddGetRemoveTest.class,
                      AddGetTest.class, ResourceManagerJMXTest.class, RMProxyUserInterfaceTest.class,
                      TestRMMonitoring.class, TestRMNodeMonitoring.class,
                      TestLocalInfrastructureRestartDownNodesPolicy.class, TestLocalInfrastructureStaticPolicy.class,
                      TestLocalInfrastructureTimeSlotPolicy.class, TestNodeSourceAfterRestart.class,
                      TestAddRemoveAll.class, TestAdminAddingNodes.class, TestConcurrentUsers.class,
                      TestNodeEncoding.class, TestNodeSourcesActions.class, TestNodesStates.class,
                      TestNSAdminPermissions.class, TestNSProviderPermissions.class, DynamicSelectionScriptTest.class,
                      SelectionWithNodesExclusionTest.class, SelectionWithSeveralScriptsTest.class,
                      SelectionWithSeveralScriptsTest2.class, StaticSelectionScriptTest.class,
                      UnauthorizedSelectionScriptTest.class, LocalSelectionTest.class, SelectionTest.class,
                      SSHInfrastructureV2LifecycleTest.class, LocalInfrastructureLifecycleTest.class })

/**
 * @author ActiveEon Team
 * @since 12/01/2018
 */
public class StandardTestSuite {
}
