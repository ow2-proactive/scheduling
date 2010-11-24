/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

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
    public String getStatus();

    /**
     * Returns the current number of available nodes.
     * @return the current number of available nodes
     */
    public int getAvailableNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#CONFIGURING} state.
     * @return the current number of nodes in {@link NodeState#CONFIGURING} state.
     */
    public int getConfiguringNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#DEPLOYING} state.
     * @return the current number of nodes in {@link NodeState#DEPLOYING} state.
     */
    public int getDeployingNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#LOST} state.
     * @return the current number of nodes in {@link NodeState#LOST} state.
     */
    public int getLostNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#FREE} state.
     * @return the current number of free nodes
     */
    public int getFreeNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#BUSY} state.
     * @return the current number of busy nodes
     */
    public int getBusyNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#TO_BE_RELEASED} state.
     * @return the current number of busy nodes
     */
    public int getToBeReleasedNodesCount();

    /**
     * Returns the current number of nodes in {@link NodeState#DOWN} state.
     * @return the current number of down nodes
     */
    public int getDownNodesCount();

    /**
     * Returns the maximum number of nodes in {@link NodeState#CONFIGURING} state.
     *
     * @return the maximum number of nodes in {@link NodeState#CONFIGURING} state.
     */
    public int getMaxConfiguringNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#DEPLOYING} state.
     *
     * @return the maximum number of nodes in {@link NodeState#DEPLOYING} state.
     */
    public int getMaxDeployingNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#LOST} state.
     *
     * @return the maximum number of nodes in {@link NodeState#LOST} state.
     */
    public int getMaxLostNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#FREE} state.
     *
     * @return the maximum number of free nodes
     */
    public int getMaxFreeNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#BUSY} state.
     *
     * @return the maximum number of busy nodes
     */
    public int getMaxBusyNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#TO_BE_RELEASED} state.
     *
     * @return the maximum number of busy nodes
     */
    public int getMaxToBeReleasedNodes();

    /**
     * Returns the maximum number of nodes in {@link NodeState#FREE} state.
     *
     * @return the maximum number of down nodes
     */
    public int getMaxDownNodes();

    /**
     * Returns the average activity percentage.
     *
     * @return the average activity percentage
     */
    public double getAverageActivity();

    /**
     * Returns the average inactivity percentage.
     *
     * @return the average inactivity percentage
     */
    public double getAverageInactivity();

}