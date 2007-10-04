/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend;

import java.util.ArrayList;

import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;


/**
 * Inteface designed to monitor the Node Source activity.
 * @author proactive team
 *
 */
public interface NodeSourceInterface {
    // Free nodes
    /**
     * Free nodes are nodes that are available.
     * @return the free nodes.
     */
    public ArrayList<IMNode> getFreeNodes();

    /**
     * @return the number of free nodes.
     */
    public IntWrapper getNbFreeNodes();

    // Busy nodes
    /**
     * Busy nodes are nodes allready in use by someone.
     * @return the busy nodes.
     */
    public ArrayList<IMNode> getBusyNodes();

    /**
     * @return the number of busy nodes.
     */
    public IntWrapper getNbBusyNodes();

    // Down nodes
    /**
     * Down nodes are nodes that no longer respond.
     * @return the down nodes.
     */
    public ArrayList<IMNode> getDownNodes();

    /**
     * @return the number of down nodes.
     */
    public IntWrapper getNbDownNodes();

    // All Nodes
    /**
     * @return all the nodes handled.
     */
    public ArrayList<IMNode> getAllNodes();

    /**
     * @return the number of nodes handled.
     */
    public IntWrapper getNbAllNodes();
}
