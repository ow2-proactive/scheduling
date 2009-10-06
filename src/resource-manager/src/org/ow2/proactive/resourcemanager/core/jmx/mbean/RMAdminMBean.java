/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This MBean interface exposes all Resource Manager statistics:
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
 * <li>Activity time percentage
 * <li>Inactivity time percentage
 * </ul>
 * <p> 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public interface RMAdminMBean extends RMAnonymMBean {
	
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
     * Returns the nodes activity time percentage.
     * 
     * @return the nodes activity time percentage
     */
    public double getActivityTimePercentage();

    /**
     * Returns the nodes inactivity time percentage.
     * 
     * @return the nodes inactivity time percentage
     */
    public double getInactivityTimePercentage();
}
