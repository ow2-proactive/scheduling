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
package org.ow2.proactive.resourcemanager.db;

import static com.google.common.truth.Truth.assertThat;

import java.security.Permission;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * @author ActiveEon Team
 * @since 02/10/17
 */
public class RMNodeDataTest {

    private String name = "name";

    private String url = "url";

    @Mock
    private Client owner;

    @Mock
    private Client provider;

    @Mock
    private Permission permission;

    private NodeState state = NodeState.TO_BE_REMOVED;

    private long stateChangeTime = 1L;

    private String hostname = "hostname";

    private String[] jmxUrls = new String[] { "jmxUrl1", "jmxUrl2" };

    private String jvmName = "jvmName";

    private boolean locked = false;

    @Mock
    private Client lockedBy;

    private long lockTime = 1L;

    private String commandLine = "commandLine";

    private String description = "description";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBuilderSetsFields() {
        RMNodeData nodeData = new RMNodeData.Builder().name(name)
                                                      .nodeUrl(url)
                                                      .owner(owner)
                                                      .provider(provider)
                                                      .userPermission(permission)
                                                      .state(state)
                                                      .stateChangeTime(stateChangeTime)
                                                      .hostname(hostname)
                                                      .jmxUrls(jmxUrls)
                                                      .jvmName(jvmName)
                                                      .locked(locked)
                                                      .lockedBy(lockedBy)
                                                      .lockTime(lockTime)
                                                      .commandLine(commandLine)
                                                      .description(description)
                                                      .build();

        assertThat(nodeData.getName()).isEqualTo(name);
        assertThat(nodeData.getNodeUrl()).isEqualTo(url);
        assertThat(nodeData.getOwner()).isEqualTo(owner);
        assertThat(nodeData.getProvider()).isEqualTo(provider);
        assertThat(nodeData.getUserPermission()).isEqualTo(permission);
        assertThat(nodeData.getState()).isEqualTo(state);
        assertThat(nodeData.getStateChangeTime()).isEqualTo(stateChangeTime);
        assertThat(nodeData.getHostname()).isEqualTo(hostname);
        assertThat(nodeData.getJmxUrls()).isEqualTo(jmxUrls);
        assertThat(nodeData.getJvmName()).isEqualTo(jvmName);
        assertThat(nodeData.getLocked()).isEqualTo(locked);
        assertThat(nodeData.getLockedBy()).isEqualTo(lockedBy);
        assertThat(nodeData.getLockTime()).isEqualTo(lockTime);
        assertThat(nodeData.getCommandLine()).isEqualTo(commandLine);
        assertThat(nodeData.getDescription()).isEqualTo(description);

    }

    @Test
    public void testEqualNodeDataHaveSameHashcode() {
        RMNodeData nodeData1 = new RMNodeData.Builder().name(name)
                                                       .nodeUrl(url)
                                                       .owner(owner)
                                                       .provider(provider)
                                                       .state(NodeState.DEPLOYING)
                                                       .stateChangeTime(1L)
                                                       .hostname(hostname)
                                                       .jvmName(jvmName)
                                                       .build();
        RMNodeData nodeData2 = new RMNodeData.Builder().name(name)
                                                       .nodeUrl(url)
                                                       .owner(owner)
                                                       .provider(provider)
                                                       .state(NodeState.DEPLOYING)
                                                       .stateChangeTime(1L)
                                                       .hostname(hostname)
                                                       .jvmName(jvmName)
                                                       .build();
        assertThat(nodeData1).isEqualTo(nodeData2);
        assertThat(nodeData1.equals(nodeData2)).isTrue();
        assertThat(nodeData1.hashCode()).isEqualTo(nodeData2.hashCode());
    }

    @Test
    public void testIfHashCodeIsDifferentEqualsIsFalse() {
        RMNodeData nodeData1 = new RMNodeData.Builder().name(name + "1")
                                                       .nodeUrl(url + "1")
                                                       .owner(owner)
                                                       .provider(provider)
                                                       .state(NodeState.DEPLOYING)
                                                       .stateChangeTime(1L)
                                                       .hostname(hostname)
                                                       .jvmName(jvmName)
                                                       .build();
        RMNodeData nodeData2 = new RMNodeData.Builder().name(name + "2")
                                                       .nodeUrl(url + "2")
                                                       .owner(owner)
                                                       .provider(provider)
                                                       .state(NodeState.DEPLOYING)
                                                       .stateChangeTime(1L)
                                                       .hostname(hostname)
                                                       .jvmName(jvmName)
                                                       .build();
        assertThat(nodeData1).isNotEqualTo(nodeData2);
        assertThat(nodeData1.equals(nodeData2)).isFalse();
        assertThat(nodeData1.hashCode()).isNotEqualTo(nodeData2.hashCode());
    }
}
