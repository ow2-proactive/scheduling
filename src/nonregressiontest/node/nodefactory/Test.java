/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package nonregressiontest.node.nodefactory;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;

import testsuite.test.FunctionalTest;


/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest {
    Node rmiNode;
    Node jiniNode;
    private String rmiURL;
    private String jiniURL = "jini://localhost/JININode" + System.currentTimeMillis();

    //Node ibisNode;

    /**
     * Constructor for Test.
     */
    public Test() {
        super("NodeFactory",
            "Test the creation of rmi, jini, ibis node whith the factory");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        NodeFactory.createNode(rmiURL);
        NodeFactory.createNode(jiniURL);
        //NodeFactory.createNode("ibis://localhost/IBISNode");
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    	String port = System.getProperty("proactive.rmi.port");
    	if (port != null) rmiURL = UrlBuilder.buildUrl("localhost","RMINode", "rmi:", new Integer(port).intValue());
    	else rmiURL = UrlBuilder.buildUrl("localhost","RMINode", "rmi:");
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    	NodeFactory.killNode(rmiURL);
		NodeFactory.killNode(jiniURL);
    }

    public boolean postConditions() throws Exception {
        rmiNode = NodeFactory.getNode(rmiURL);
        jiniNode = NodeFactory.getNode(jiniURL);
        //ibisNode = NodeFactory.getNode("ibis://localhost/IBISNode");
        return ((rmiNode != null) && (jiniNode != null) &&
        NodeFactory.isNodeLocal(rmiNode));
    }
}
