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
package nonregressiontest.nfe.servicecrash;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.mop.MethodCall;

import testsuite.test.FunctionalTest;


/**
 * @author agenoud
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest {

	// logger for NFE mechanism
	public static Logger loggerNFE = Logger.getLogger("NFE");	
	
    /** An active object on the same VM */
    A sameVM;

    /** An active object on a different but local VM */
    A localVM;

    /** An active object on a remote VM */
    A remoteVM;

	/**
	 * Result from the remote method call
	 */
	Integer resultServeRequest = null;
	
    /**
     * Constructor for Test.
     */
    public Test() {
        super("NFE Service Failed TEST",
            "Test that service exception are correctly signaled to caller and callee");
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
    * @see testsuite.test.AbstractTest#preConditions()
    */
    public boolean preConditions() throws Exception {
        return true;
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {

        remoteVM = (A) ProActive.newActive(A.class.getName(),
                new Object[] { "remoteVM" }, TestNodes.getRemoteVMNode());
		
		// Construct a fake method call to serveRequest to create a service exception
		Object[] param = {new Integer(0)};
		MethodCall methodCall = new MethodCall(A.class.getMethod("serveRequestTest", null), param);
		try {
			resultServeRequest = (Integer) ((org.objectweb.proactive.core.mop.StubObject) remoteVM).getProxy().reify(methodCall);
			//remoteVM.serveRequestTest();
		} catch (NonFunctionalException e) {
			resultServeRequest = remoteVM.serveRequestTest();
			//System.out.println("ServeException");
		} catch (Throwable t) {
			//System.out.println(t);
			loggerNFE.warn("*** ERROR when calling erroneous methodcall " + t.getMessage());
			resultServeRequest = new Integer(-1);
			//System.out.println("Throwable");
		}
    }

    /**
     * @see testsuite.test.AbstractTest#postConditions()
     */
    public boolean postConditions() throws Exception {
    	return (resultServeRequest.intValue() == 0); 
	}

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }
}
