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
package benchmark.objectcreation.newactive;


import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveBenchmark;
import benchmark.util.ReifiableObject;


/**
 * @author Alexandre di Costanzo
 *
 */
public class BenchNewActive extends ProActiveBenchmark {

	private ReifiableObject object = null;
	
    /**
     *
     */
    public BenchNewActive() {
        super(null, "Object Creation with newActive",
            "Measure time to create an active object with newActive.");
    }

    /**
     * @param node
     */
    public BenchNewActive(Node node) {
        super(node, "Object Creation with newActive",
            "Measure time to create an active object with newActive.");
    }

    /**
     * @see testsuite.test.Benchmark#action()
     */
    public long action() throws Exception {
        String className = ReifiableObject.class.getName();
        Node node = getNode();
        this.timer.start();
        object = (ReifiableObject) ProActive.newActive(className, null, node);
        this.timer.stop();
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
    
	public boolean postConditions() throws Exception {
		Object[] activeObjects = getNode().getActiveObjects();
		return (object != null) && (activeObjects != null) &&
		(activeObjects.length == 1) && activeObjects[0].equals(object);
	}
}
