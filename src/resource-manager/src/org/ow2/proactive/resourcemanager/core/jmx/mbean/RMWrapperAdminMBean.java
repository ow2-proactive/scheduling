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
 * MBean interface representing the attributes and the statistic values to monitor the Resource Manager
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public interface RMWrapperAdminMBean extends RMWrapperAnonymMBean {

    /**
     * It`s the percentage time of inactivity of all the available nodes
     * 
     * @return the current percentage time of nodes inactivity
     */
    public int getTimePercentageOfNodesInactivity();

    /**
     * It`s the percentage time of usage of all the available nodes
     * 
     * @return the current percentage time of nodes usage
     */
    public int getTimePercentageOfNodesUsage();

    /**
     * It`s the percentage time of inactivity of all the available nodes as double
     * 
     * @return the current percentage time of nodes inactivity as double
     */
    public double getTimePercentageOfNodesInactivityAsDouble();

    /**
     * It`s the percentage time of usage of all the available nodes as double
     * 
     * @return the current percentage time of nodes usage as double
     */
    public double getTimePercentageOfNodesUsageAsDouble();
}
