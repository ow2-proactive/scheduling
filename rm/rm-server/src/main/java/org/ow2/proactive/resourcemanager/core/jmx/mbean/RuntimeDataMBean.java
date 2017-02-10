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
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import java.io.IOException;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.NodeState;


/**
 * This MBean interface exposes runtime Resource Manager statistics:
 * <p>
 * <ul>
 * <li>The Resource Manager status
 * <li>Available nodes count
 * <li>Free nodes count
 * <li>Busy nodes count
 * <li>To be released nodes count
 * <li>Down nodes count
 * <li>Maximum free nodes
 * <li>Maximum busy nodes
 * <li>Maximum to be released nodes
 * <li>Maximum down nodes
 * <li>Average activity percentage
 * <li>Average inactivity percentage
 * </ul>
 * <p>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public interface RuntimeDataMBean {

    /**
     * Returns the current status of the resource manager.
     * @return the current status of the resource manager
     */
    String getStatus();

    /**
     * Returns the current number of available nodes.
     * @return the current number of available nodes
     */
    int getAvailableNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#CONFIGURING} state.
     * @return the current number of nodes in {@link NodeState#CONFIGURING} state.
     */
    int getConfiguringNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#DEPLOYING} state.
     * @return the current number of nodes in {@link NodeState#DEPLOYING} state.
     */
    int getDeployingNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#LOST} state.
     * @return the current number of nodes in {@link NodeState#LOST} state.
     */
    int getLostNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#FREE} state.
     * @return the current number of free nodes
     */
    int getFreeNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#BUSY} state.
     * @return the current number of busy nodes
     */
    int getBusyNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#TO_BE_REMOVED} state.
     * @return the current number of busy nodes
     */
    int getToBeReleasedNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#DOWN} state.
     * @return the current number of down nodes
     */
    int getDownNodesCount();

    /**
     * Returns the maximum number of nodes in {@link NodeState#CONFIGURING} state.
     *
     * @return the maximum number of nodes in {@link NodeState#CONFIGURING} state.
     */
    int getMaxConfiguringNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#DEPLOYING} state.
     *
     * @return the maximum number of nodes in {@link NodeState#DEPLOYING} state.
     */
    int getMaxDeployingNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#LOST} state.
     *
     * @return the maximum number of nodes in {@link NodeState#LOST} state.
     */
    int getMaxLostNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#FREE} state.
     *
     * @return the maximum number of free nodes
     */
    int getMaxFreeNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#BUSY} state.
     *
     * @return the maximum number of busy nodes
     */
    int getMaxBusyNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#TO_BE_REMOVED} state.
     *
     * @return the maximum number of busy nodes
     */
    int getMaxToBeReleasedNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#FREE} state.
     *
     * @return the maximum number of down nodes
     */
    int getMaxDownNodes();

    /**
     * Returns the average activity percentage.
     *
     * @return the average activity percentage
     */
    double getAverageActivity();

    /**
     * Returns the average inactivity percentage.
     *
     * @return the average inactivity percentage
     */
    double getAverageInactivity();

    /**
     * Sends the statistics accumulated in the RRD data base
     *
     * @return data base file converted to bytes
     * @throws IOException when data base cannot be read
     */
    byte[] getStatisticHistory() throws IOException;

}
