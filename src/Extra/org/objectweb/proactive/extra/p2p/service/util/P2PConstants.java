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
package org.objectweb.proactive.extra.p2p.service.util;

/**
 * Java system properties name and P2P constants.
 *
 * @author The ProActive Team
 *
 * Created on Jan 13, 2005
 */
public interface P2PConstants {

    /**
     * <code>P2P_NODE_NAME</code>: name of the p2pServiceNode where the P2P
     *  Service is running.
     */
    public static final String P2P_NODE_NAME = "P2PNode";

    /**
     * <code>SHARED_NODE_NAME</code>: name of the default shared node.
     */
    public static final String SHARED_NODE_NAME = "SharedNode";

    /**
     * <code>MAX_NODE</code>: special number for asking nodes every times.
     */
    public static final int MAX_NODE = -1;

    /**
     * <code>VN_NAME</code>: name of the virtual node.
     */
    public static final String VN_NAME = "SharedNodes";

    // -------------------------------------------------------------------------
    // IC2D P2P Property
    // -------------------------------------------------------------------------

    /** Enable or not the P2P Node monitoring with IC2D.
     * This property takes a Java boolean value.*/
    public static final String HIDE_P2PNODE_MONITORING = "proactive.ic2d.hidep2pnode";
}
