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
