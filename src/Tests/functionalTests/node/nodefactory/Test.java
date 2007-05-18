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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.node.nodefactory;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test the creation of rmi, ibis node whith the factory
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = -303170046021003591L;
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
        String port = ProActiveConfiguration.getInstance()
                                            .getProperty("proactive.rmi.port");
        if (port != null) {
            rmiURL = UrlBuilder.buildUrl("localhost",
                    "RMINode" + System.currentTimeMillis(),
                    Constants.RMI_PROTOCOL_IDENTIFIER,
                    new Integer(port).intValue());
        } else {
            rmiURL = UrlBuilder.buildUrl("localhost",
                    "RMINode" + System.currentTimeMillis(),
                    Constants.RMI_PROTOCOL_IDENTIFIER);
        }
    }

    @After
    public void endTest() throws Exception {
        NodeFactory.killNode(rmiURL);
    }
}
