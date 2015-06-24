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
package org.ow2.proactive.scheduler.task.containers;

import java.io.Serializable;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.utils.NodeSet;


/**
 * An executable container allows to instantiate the actual executable in a lazy manner, i.e.
 * on the worker node that will execute the actual executable.
 *
 * @author The ProActive Team
 */
public abstract class ExecutableContainer implements Serializable {

    // node set : not DB managed
    protected NodeSet nodes;

    protected Credentials credentials;
    private boolean runAsUser;

    /**
     * Set the nodes value to the given nodes value
     *
     * @param nodes the nodes to set
     */
    public void setNodes(NodeSet nodes) {
        this.nodes = nodes;
    }

    /**
     * Return the nodes set for this executable container
     *
     * @return the nodes set for this executable container
     */
    public NodeSet getNodes() {
        return this.nodes;
    }

    /**
     * Get the credentials crypted with launcher public key
     *
     * @return the credentials crypted with launcher public key
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Set the credentials value to the given credentials value
     *
     * @param credentials the credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Return true if user want to run the task under his account id, false otherwise.
     *
     * @return true if user want to run the task under his account id, false otherwise.
     */
    public boolean isRunAsUser() {
        return runAsUser;
    }

    public void setRunAsUser(boolean runAsUser) {
        this.runAsUser = runAsUser;
    }
}
