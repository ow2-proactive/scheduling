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
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * constant types in the Resource Manager.<BR>
 * These interface define names for Resource manager active objects
 * and defines type names for the different node sources types.
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public interface RMConstants {

    /** name of RMCore AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_RMCORE = "RMCORE";

    /** name of RMAdmin AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_RMADMIN = "RMADMIN";

    /** name of RMUser AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_RMUSER = "RMUSER";

    /** name of RMMonitoring AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_RMMONITORING = "RMMONITORING";

    /** constants for {@link PADNodeSource} source type name*/
    public static final String PAD_NODE_SOURCE_TYPE = "PAD_NODE_SOURCE";

    /** constants for {@link org.objectweb.proactive.extra.p2p.scheduler.P2PNodeSource} 
     * source type name*/
    public static final String P2P_NODE_SOURCE_TYPE = "P2P_NODE_SOURCE";

    /** constants for {@link DummyNodeSource} source type name*/
    public static final String DUMMY_NODE_SOURCE_TYPE = "DUMMY_NODE_SOURCE";

    /** The default name of the static node source created 
     * at Resource manager Startup 
     */
    public static final String DEFAULT_STATIC_SOURCE_NAME = "Default";

}
