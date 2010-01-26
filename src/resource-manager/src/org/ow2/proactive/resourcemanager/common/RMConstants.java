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
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;


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

}
