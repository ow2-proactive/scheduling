/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.account;

import org.ow2.proactive.account.Account;


/**
 * This class represents an account, it contains information about the
 * activity of a Resource Manager user.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public final class RMAccount implements Account {
    String username;
    long usedNodeTime;
    long providedNodeTime;
    int providedNodesCount;

    /**
     * Returns the username of this account. 
     * @return the username of this account
     */
    public String getName() {
        return this.username;
    }

    /**
     * The amount of time in milliseconds other users have spent in using the resources of the current user.
     * @return the used node time in milliseconds
     */
    public long getUsedNodeTime() {
        return this.usedNodeTime;
    }

    /**
     * The amount of time in milliseconds the current user has offered resources to the Resource Manager.
     * @return the provided node time in milliseconds
     */
    public long getProvidedNodeTime() {
        return this.providedNodeTime;
    }

    /**
     * Returns the number of provided nodes.
     * @return the used node time in milliseconds
     */
    public int getProvidedNodesCount() {
        return this.providedNodesCount;
    }
}
