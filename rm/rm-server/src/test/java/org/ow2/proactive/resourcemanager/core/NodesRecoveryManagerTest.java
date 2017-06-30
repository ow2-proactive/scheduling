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
package org.ow2.proactive.resourcemanager.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import com.google.common.base.Function;
import com.google.common.collect.Maps;


/**
 * @author ActiveEon Team
 * @since 29/06/17
 */
public class NodesRecoveryManagerTest {

    @Mock
    private RMCore rmCore;

    private NodesRecoveryManager nodesRecoveryManager;

    private NodesLockRestorationManager nodesLockRestorationManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        nodesLockRestorationManager = null;

        nodesRecoveryManager = new NodesRecoveryManager(rmCore);
        nodesRecoveryManager = spy(nodesRecoveryManager);

        nodesLockRestorationManager = null;

        doReturn(new Function<RMCore, NodesLockRestorationManager>() {
            @Override
            public NodesLockRestorationManager apply(RMCore rmCore) {
                nodesLockRestorationManager = new NodesLockRestorationManager(rmCore);
                nodesLockRestorationManager = spy(nodesLockRestorationManager);

                doReturn(Maps.newHashMap()).when(nodesLockRestorationManager).findNodesLockedOnPreviousRun();

                return nodesLockRestorationManager;
            }
        }).when(nodesRecoveryManager).getNodesLockRestorationManagerBuilder();

        initialize();
    }

    private void initialize() {
        nodesRecoveryManager.initNodesRestorationManager();
    }

    @Test
    public void testInitNodesLockRestorationManagerDisabled() {
        PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION.updateProperty("false");
        initialize();

        assertThat(nodesLockRestorationManager).isNotNull();
        assertThat(nodesLockRestorationManager.isInitialized()).isFalse();

        verify(nodesLockRestorationManager, never()).initialize();
    }

    @Test
    public void testInitNodesLockRestorationManagerEnabled() {
        PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION.updateProperty("true");
        initialize();

        assertThat(nodesLockRestorationManager).isNotNull();
        assertThat(nodesLockRestorationManager.isInitialized()).isTrue();

        verify(nodesLockRestorationManager).initialize();
    }

}
