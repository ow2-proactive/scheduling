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
package testsuite.test;

import org.objectweb.proactive.core.node.Node;

import testsuite.manager.ProActiveFuncTestManager;

import java.io.Serializable;


/**
 * @author adicosta
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ProActiveFunctionalTest extends FunctionalTest
    implements Serializable, InterfaceProActiveTest {
    private Node node = null;

    public ProActiveFunctionalTest() {
    }

    /**
     *
     */
    public ProActiveFunctionalTest(Node node) {
        super("Remote Functional AbstractTest",
            "This test is executed in remote host.");
        this.node = node;
    }

    /**
     * @param logger
     * @param name
     */
    public ProActiveFunctionalTest(Node node, String name) {
        super(name, "This test is executed in remote host.");
        this.node = node;
    }
    
    /**
     * @param name
     * @param description
     */
    public ProActiveFunctionalTest(String name, String description) {
        super(name, description);
    }

    /**
     * @param name
     * @param description
     */
    public ProActiveFunctionalTest(Node node, String name, String description) {
        super(name, description);
        this.node = node;
    }

    public void killVM() {
        if (logger.isDebugEnabled()) {
            logger.debug("kill VM");
        }
        System.exit(0);
    }

    /**
     * @return
     */
    public Node getNode() {
        return node;
    }

    /**
     * @param node
     */
    public void setNode(Node node) {
        this.node = node;
    }

    public Node getSameVMNode() {
        return ((ProActiveFuncTestManager) manager).getSameVMNode();
    }

    public Node getLocalVMNode() {
        return ((ProActiveFuncTestManager) manager).getLocalVMNode();
    }

    public Node getRemoteVMNode() {
        return ((ProActiveFuncTestManager) manager).getRemoteVMNode();
    }
}
