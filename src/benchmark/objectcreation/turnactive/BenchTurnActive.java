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
package benchmark.objectcreation.turnactive;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveBenchmark;
import benchmark.functionscall.FunctionCall;
import benchmark.util.ReifiableObject;



/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchTurnActive extends ProActiveBenchmark {

    /**
     *
     */
    public BenchTurnActive() {
        super(null, "Object Creation with turnActive",
            "Measure time to create an active object with turnActive.");
    }

    /**
     * @param node
     */
    public BenchTurnActive(Node node) {
        super(node, "Object Creation with turnActive",
            "Measure time to create an active object with turnActive.");
    }

    /**
     * @see testsuite.test.Benchmark#action()
     */
    public long action() throws Exception {
        ReifiableObject object = new ReifiableObject();
        String className = ReifiableObject.class.getName();
        Node node = getNode();
        this.timer.start(); for(int i = 0 ; i < FunctionCall.MAX_CALL ; i++) {
        object = (ReifiableObject) ProActive.turnActive(object, node);
        } this.timer.stop();
        return this.timer.getCumulatedTime();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        // nothing to do
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        // nothing to do
    }

    /**
     * @see testsuite.test.AbstractTest#preConditions()
     */
    public boolean preConditions() throws Exception {
        return getNode() != null;
    }
}
