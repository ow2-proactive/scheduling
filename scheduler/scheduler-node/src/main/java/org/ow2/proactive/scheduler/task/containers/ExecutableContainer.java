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

    // node set: not DB managed
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
