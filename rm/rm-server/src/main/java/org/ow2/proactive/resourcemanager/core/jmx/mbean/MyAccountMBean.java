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
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

/**
 * MBean interface representing the attributes of an account.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public interface MyAccountMBean {

    /**
     * The amount of time in milliseconds other users have spent in using the resources of the current user.
     * @return the used node time in milliseconds
     */
    long getUsedNodeTime();

    /**
     * The amount of time in milliseconds the current user has offered resources to the Resource Manager.
     * @return the provided node time in milliseconds
     */
    long getProvidedNodeTime();

    /**
     * Returns the number of provided nodes.
     * @return the used node time in milliseconds
     */
    int getProvidedNodesCount();

}
