/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties;


/**
 * constant types in the Resource Manager.<BR>
 * These interface define names for Resource manager active objects
 * and defines type names for the different node sources types.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
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

    /** name of RMAuthentication AO registered in RMI register */
    public static final String NAME_ACTIVE_OBJECT_RMAUTHENTICATION = "RMAUTHENTICATION";

    /** The default name of the static node source created  at Resource manager Startup */
    public static final String DEFAULT_STATIC_SOURCE_NAME = "Default";

    /** 
     * The default jmx Connector Server url for the resource manager, is specified the port to use for exchanging objects 
     * (the first one) and the port where the RMI registry is reachable (the second port) so that a firewall
     * should not block the requests to the JMX connector 
     */
    public static final String DEFAULT_JMX_CONNECTOR_URL = "service:jmx:rmi://localhost:" +
        PAProperties.PA_RMI_PORT.getValue() + "/jndi/rmi://localhost:" + PAProperties.PA_RMI_PORT.getValue() +
        "/";

    /** The default jmx Connector Server name for the resource manager */
    public static final String DEFAULT_JMX_CONNECTOR_NAME = "JMXRMAgent";

}
