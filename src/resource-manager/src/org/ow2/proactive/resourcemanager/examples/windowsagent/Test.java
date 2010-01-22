/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.examples.windowsagent;

//@snippet-start ProActiveWindowsAgent
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;


public class Test {

    public static void main(String[] args) {

        try {

            // Note that the port is 1100 and not 1099 like those used by the
            // resource manager. The initial port is incremented to ensure that
            // each runtime uses a unique port.
            // Thus, this port corresponds to the port of the first runtime.
            // If a second runtime has been launched, it would use the port 1101,
            // and so on and so forth...

            final Node n = NodeFactory.getNode("rmi://192.168.1.62:1100/toto");

            System.out.println("Nb of active objects on the remote node: " + n.getNumberOfActiveObjects() +
                " local runtime hashcode " + Runtime.getRuntime().hashCode());

            final Test stubOnTest = (Test) PAActiveObject.newActive(Test.class.getName(), null, n);

            final String receivedMessage = stubOnTest.getMessage();

            System.out.println("Nb of active objects on the remote node: " + n.getNumberOfActiveObjects() +
                " received message: " + receivedMessage);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Test() {
    }

    public String getMessage() {
        return "A message from " + Runtime.getRuntime().hashCode();
    }

}
//@snippet-end ProActiveWindowsAgent
