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
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Constant types in the Resource Manager.
 * <p>
 * These interface define names for Resource manager active objects
 * and defines type names for the different node sources types.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface RMConstants {

    /** name of RMCore AO registered in RMI register */
    String NAME_ACTIVE_OBJECT_RMCORE = "RMCORE";

    /** name of RMAdmin AO registered in RMI register */
    String NAME_ACTIVE_OBJECT_RMADMIN = "RMADMIN";

    /** name of RMUser AO registered in RMI register */
    String NAME_ACTIVE_OBJECT_RMUSER = "RMUSER";

    /** name of RMMonitoring AO registered in RMI register */
    String NAME_ACTIVE_OBJECT_RMMONITORING = "RMMONITORING";

    /** name of RMAuthentication AO registered in RMI register */
    String NAME_ACTIVE_OBJECT_RMAUTHENTICATION = "RMAUTHENTICATION";

    /** The default name of the static node source created  at Resource manager Startup */
    String DEFAULT_STATIC_SOURCE_NAME = "Default";

    /** The default name of the local node source created  at Resource manager Startup */
    String DEFAULT_LOCAL_NODES_NODE_SOURCE_NAME = "LocalNodes";

}
