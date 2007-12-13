/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.node.nodefactory;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;


/**
 * Test the creation of rmi, ibis node whith the factory
 */
public class Test extends FunctionalTest {
    Node rmiNode;
    private String rmiURL;

    @org.junit.Test
    public void action() throws Exception {
        NodeFactory.createNode(rmiURL);
        //        NodeFactory.createNode("ibis://localhost/IBISNode");
        rmiNode = NodeFactory.getNode(rmiURL);
        //ibisNode = NodeFactory.getNode("ibis://localhost/IBISNode");
        assertTrue((rmiNode != null) && NodeFactory.isNodeLocal(rmiNode));
    }

    @Before
    public void initTest() throws Exception {
        String port = PAProperties.PA_RMI_PORT.getValue();
        if (port != null) {
            rmiURL = URIBuilder.buildURI("localhost", "RMINode" + System.currentTimeMillis(),
                    Constants.RMI_PROTOCOL_IDENTIFIER, new Integer(port).intValue()).toString();
        } else {
            rmiURL = URIBuilder.buildURI("localhost", "RMINode" + System.currentTimeMillis(),
                    Constants.RMI_PROTOCOL_IDENTIFIER).toString();
        }
    }

    @After
    public void endTest() throws Exception {
        NodeFactory.killNode(rmiURL);
    }
}
