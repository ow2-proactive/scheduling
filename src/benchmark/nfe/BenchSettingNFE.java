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
package benchmark.nfe;


import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.communication.ProActiveCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerCommunicationException;
import org.objectweb.proactive.core.node.Node;

import testsuite.test.ProActiveBenchmark;


/**
 * @author Alexandre Genoud
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BenchSettingNFE extends ProActiveBenchmark {

	private int set_iter = 0;
	private A a = null;
	
    /**
     *
     */
    public BenchSettingNFE() {
        super(null, "NFE Mechanism Configuration",
            "Bench time to attach handlers to active object.");
    }

    /**
     * @param node
     */
    public BenchSettingNFE(Node node, int set_iter) {
		super(node, "NFE Mechanism Configuration",
			"Bench time to attach handlers to active object.");
		this.set_iter =set_iter;
     }

    /**
     * @see testsuite.test.Benchmark#action()
     */
    public long action() throws Exception {
		// System.out.println("NFE Bench on " + getNode() + " with " + set_iter + " iterations");
		int iter_tmp = set_iter;
    	this.timer.start();
		//UniversalBody body = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) a).getProxy()).getBody();
    	while (iter_tmp != 0) {
			//System.out.print("Active objects protected from ProActiveCommunicationException is " + a.protectedFrom(new ProActiveCommunicationException()));
			ProActive.setExceptionHandler(HandlerCommunicationException.class, ProActiveCommunicationException.class, Handler.ID_Body, a);
			//System.out.print(" - " + a.protectedFrom(new ProActiveCommunicationException()));
			ProActive.unsetExceptionHandler(ProActiveCommunicationException.class, Handler.ID_Body, a);
			//System.out.println(" - " + a.protectedFrom(new ProActiveCommunicationException()));
			iter_tmp = iter_tmp - 1;
    	}
        this.timer.stop();
        return this.timer.getCumulatedTime();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
		// Create an active object on the given node and get the body
		Node node = getNode();
		a = (A) org.objectweb.proactive.ProActive.newActive(A.class.getName(), null, node);
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
        return (getNode() != null);
    }
    
	public boolean postConditions() throws Exception {
		return true;
	}
}
