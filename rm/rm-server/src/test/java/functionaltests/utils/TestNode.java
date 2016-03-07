/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionaltests.utils;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcess;


public class TestNode {

    private final JVMProcess nodeProcess;

    private final Node node;

    public TestNode(JVMProcess nodeProcess, Node node) {
        this.nodeProcess = nodeProcess;
        this.node = node;
    }

    public JVMProcess getNodeProcess() {
        return nodeProcess;
    }

    public Node getNode() {
        return node;
    }

    public String getNodeURL() {
        return node.getNodeInformation().getURL();
    }

    public void kill() throws InterruptedException {
        nodeProcess.stopProcess();
        nodeProcess.waitFor();
    }

    public void killNode() throws InterruptedException {
        try {
            node.getProActiveRuntime().killNode(node.getNodeInformation().getName());
        } catch (ProActiveException e) {

        }
    }
}
