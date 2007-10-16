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
package functionalTests.activeobject.migration.loopmigration;

import org.junit.Before;
import org.objectweb.proactive.api.ProActiveObject;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;
import static junit.framework.Assert.assertFalse;

/**
 * Test AO loop migration
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = 38511988116531130L;
    String node1;
    String node2;
    A a;

    @Before
    public void Before() throws Exception {
        new TestNodes().action();
    }

    @org.junit.Test
    public void action() throws Exception {
        node1 = TestNodes.getSameVMNode().getNodeInformation().getURL();
        node2 = TestNodes.getLocalVMNode().getNodeInformation().getURL();
        a = (A) ProActiveObject.newActive(A.class.getName(),
                new Object[] { node1, node2 }, node1);

        assertFalse(a.isException());
    }
}
