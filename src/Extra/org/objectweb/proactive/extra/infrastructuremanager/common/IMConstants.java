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
package org.objectweb.proactive.extra.infrastructuremanager.common;

import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DummyNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;


/**
 * constant types in the Infrastructure Manager.<BR>
 * These interface define names for Infrastructure manager active objects
 * and defines type names for the different node sources types.
 *
 * @author ProActive team
 *
 */
public interface IMConstants {

    /** Name of the ProActive node which contains
     * infrastructure Manager active objects (AO)*/
    public static final String NAME_NODE_IM = "IMNODE";

    /** name of IMCore AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_IMCORE = "IMCORE";

    /** name of IMAdmin AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_IMADMIN = "IMADMIN";

    /** name of IMUser AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_IMUSER = "IMUSER";

    /** name of IMMonitoring AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_IMMONITORING = "IMMONITORING";

    /** constants for {@link PADNodeSource} source type name*/
    public static final String PAD_NODE_SOURCE_TYPE = "PAD_NODE_SOURCE";

    /** constants for {@link P2PNodeSource} source type name*/
    public static final String P2P_NODE_SOURCE_TYPE = "P2P_NODE_SOURCE";

    /** constants for {@link DummyNodeSource} source type name*/
    public static final String DUMMY_NODE_SOURCE_TYPE = "DUMMY_NODE_SOURCE";
}
