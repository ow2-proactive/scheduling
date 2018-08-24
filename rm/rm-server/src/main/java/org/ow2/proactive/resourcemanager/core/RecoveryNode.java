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
package org.ow2.proactive.resourcemanager.core;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;


/**
 * Almost empty implementation of the {@link Node} interface for the special
 * case in which the resource manager recovery mechanism has to handle down
 * nodes. Indeed, as down nodes are not reachable, it is impossible to retrieve
 * more information than what has been saved in database for these nodes,
 * specifically its name and URL. Note that we do not want to store {@link Node}
 * information in database neither because most of it is not {@link Serializable}
 * Nevertheless, even down nodes need to be recovered as fully instantiated
 * {@link Node} because they are handled as this in lifecycle policies, in which
 * mostly the node's URL is used to ensure proper handling.
 */
public class RecoveryNode implements Node, Serializable {

    private String name;

    private String url;

    public RecoveryNode(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public NodeInformation getNodeInformation() {
        return new NodeInformation() {
            @Override
            public String getName() {
                return RecoveryNode.this.name;
            }

            @Override
            public String getURL() {
                return RecoveryNode.this.url;
            }

            @Override
            public VMInformation getVMInformation() {
                return null;
            }
        };
    }

    @Override
    public VMInformation getVMInformation() {
        return null;
    }

    @Override
    public ProActiveRuntime getProActiveRuntime() {
        return null;
    }

    @Override
    public Object[] getActiveObjects() throws NodeException, ActiveObjectCreationException {
        return new Object[0];
    }

    @Override
    public Object[] getActiveObjects(String s) throws NodeException, ActiveObjectCreationException {
        return new Object[0];
    }

    @Override
    public int getNumberOfActiveObjects() throws NodeException {
        return 0;
    }

    @Override
    public void killAllActiveObjects() throws NodeException, IOException {

    }

    @Override
    public Object setProperty(String s, String s1) throws ProActiveException {
        return null;
    }

    @Override
    public String getProperty(String s) throws ProActiveException {
        return "";
    }

    @Override
    public String getThreadDump() throws ProActiveException {
        return "";
    }

}
