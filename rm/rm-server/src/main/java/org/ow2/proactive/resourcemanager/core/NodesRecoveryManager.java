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

import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.RM_NODES_LOCK_RESTORATION;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;

import com.google.common.base.Function;


/**
 * This classe handles the recovery of node sources and of nodes when the RM is restarted.
 *
 * @author ActiveEon Team
 * @since 26/06/17
 */
public class NodesRecoveryManager {

    private static final Logger logger = Logger.getLogger(NodesRecoveryManager.class);

    private RMCore resourceManager;

    private NodesLockRestorationManager nodesLockRestorationManager;

    public NodesRecoveryManager(RMCore rmCore) {
        resourceManager = rmCore;
    }

    public void initialize() {
        initNodesRestorationManager();
    }

    void initNodesRestorationManager() {
        nodesLockRestorationManager = getNodesLockRestorationManagerBuilder().apply(resourceManager);

        if (RM_NODES_LOCK_RESTORATION.getValueAsBoolean()) {
            nodesLockRestorationManager.initialize();
        } else {
            logger.info("Nodes lock restoration is disabled");
        }
    }

    Function<RMCore, NodesLockRestorationManager> getNodesLockRestorationManagerBuilder() {
        return new Function<RMCore, NodesLockRestorationManager>() {
            @Override
            public NodesLockRestorationManager apply(RMCore rmCore) {
                return new NodesLockRestorationManager(rmCore);
            }
        };
    }

    public void restoreLocks(RMNode rmNode) {
        nodesLockRestorationManager.handle(rmNode);
    }

}
