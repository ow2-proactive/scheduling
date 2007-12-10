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
package functionalTests.group.exception;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;
import functionalTests.group.A;
import static junit.framework.Assert.assertTrue;

/**
 * do an (a)synchronous call that rise exception
 *
 * @author Laurent Baduel
 */
public class Test extends FunctionalTest {
    private static final long serialVersionUID = -4420144742633751760L;
    private A typedGroup = null;
    private A resultTypedGroup = null;

    @Before
    public void before() throws Exception {
        new TestNodes().action();
    }

    @org.junit.Test
    public void action() throws Exception {
        Object[][] params = {
                { "Agent0" },
                { "Agent1" },
                { "Agent2" }
            };
        Node[] nodes = {
                TestNodes.getSameVMNode(), TestNodes.getLocalVMNode(),
                TestNodes.getRemoteVMNode()
            };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);

        this.resultTypedGroup = this.typedGroup.asynchronousCallException();

        // was the result group created ?
        assertTrue(resultTypedGroup != null);
        // System.err.println(
        //        "the result group containing exception is not build");
        Group group = PAGroup.getGroup(this.typedGroup);
        Group groupOfResult = PAGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        assertTrue(groupOfResult.size() == group.size());
        //    System.err.println(
        //        "the result group containing exception has the correct size");
        boolean exceptionInResultGroup = true;
        for (int i = 0; i < groupOfResult.size(); i++) {
            exceptionInResultGroup &= (groupOfResult.get(i) instanceof Throwable);
        }

        // is the result group containing exceptions ?
        assertTrue(exceptionInResultGroup);
        //        System.err.println(
        //                "the result group doesn't contain (exclusively) exception");

        // has the ExceptionListException the correct size ?
        ExceptionListException el = groupOfResult.getExceptionList();
        assertTrue((el.size() == groupOfResult.size()));
        //        System.err.println(
        //                "the ExceptionListException hasn't the right size");
        A resultOfResultGroup = this.resultTypedGroup.asynchronousCall();
        Group groupOfResultResult = PAGroup.getGroup(resultOfResultGroup);

        // has the result-result group the correct size ?
        assertTrue(groupOfResultResult.size() == groupOfResult.size());
        //            System.err.println(
        //                "the result of a call on a group containing exception hasn't the correct size");
        boolean nullInResultResultGroup = true;
        for (int i = 0; i < groupOfResultResult.size(); i++) {
            nullInResultResultGroup &= (groupOfResultResult.get(i) == null);
        }

        // is the result group containing null ?
        assertTrue(nullInResultResultGroup);
        //            System.err.println(
        //                "the result group of a group containing exception doesn't contain null");

        // are the exceptions deleted ?
        groupOfResult.purgeExceptionAndNull();
        assertTrue(groupOfResult.size() == 0);
        //            System.err.println(
        //                "the exceptions in a group are not correctly (totaly) purged");

        // are the null deleted ?
        groupOfResultResult.purgeExceptionAndNull();
        assertTrue(groupOfResultResult.size() == 0);
        //            System.err.println(
        //                "the null in a group are not correctly (totaly) purged");
    }
}
