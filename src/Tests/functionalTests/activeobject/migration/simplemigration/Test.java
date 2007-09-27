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
package functionalTests.activeobject.migration.simplemigration;

import org.junit.Before;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;
import static junit.framework.Assert.assertTrue;

/**
 * Test AO simple migration
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = -7610928539081956490L;
    A a;
    Node sameVmNode;
    Node localVmNode;

    @Before
    public void Before() throws Exception {
        new TestNodes().action();
    }

    @org.junit.Test
    public void action() throws Exception {
        sameVmNode = TestNodes.getSameVMNode();
        if (sameVmNode == null) {
            new TestNodes().action();
            sameVmNode = TestNodes.getSameVMNode();
        }

        localVmNode = TestNodes.getLocalVMNode();
        a = (A) ProActiveObject.newActive(A.class.getName(), new Object[] { "toto" },
                sameVmNode);
        a.moveTo(localVmNode);

        assertTrue(a.getName().equals("toto"));
        assertTrue(a.getNodeUrl()
                    .equals(localVmNode.getNodeInformation().getURL()));
    }
}
