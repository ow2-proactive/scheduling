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
package nonregressiontest.nfe;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.exceptions.communication.ProActiveCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.HandlerMigrationException;
import org.objectweb.proactive.core.exceptions.migration.ProActiveMigrationException;

import testsuite.test.FunctionalTest;


/**
 * @author agenoud
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest  {
	
	/** An active object on the same VM */
	A sameVM;
	
	/** An active object on a different but local VM */
	A localVM;
	
	/** An active object on a remote VM */
	A remoteVM;
	 
	/**
	 * Constructor for Test.
	 */
	public Test() {
		super("NFE Configuration",
			"Test the configuration of exception handlers on local and remote active objects");
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
	   
		// Every active object must be different from null
		//return ((sameVM != null) && (localVM != null) && (remoteVM != null));
		return true;
   }
	   
	   
	/**
	 * @see testsuite.test.FunctionalTest#action()
	 */
	public void action() throws Exception {
	
		// Get universal body of every active object
		//UniversalBody bodySameVM = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) sameVM).getProxy()).getBody();
		//UniversalBody bodyLocalVM = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) localVM).getProxy()).getBody();
		//UniversalBody bodyRemoteVM = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) remoteVM).getProxy()).getBody();

		// Creation of active objects
		sameVM = (A) ProActive.newActive(A.class.getName(), new Object[]{"sameVM"}, TestNodes.getSameVMNode());
		localVM = (A) ProActive.newActive(A.class.getName(), new Object[]{"localVM"}, TestNodes.getLocalVMNode());
		remoteVM = (A) ProActive.newActive(A.class.getName(), new Object[]{"remoteVM"}, TestNodes.getRemoteVMNode());
		
		// URL
		String sameURL = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) sameVM).getProxy()).getBody().getNodeURL();
		String localURL = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) localVM).getProxy()).getBody().getNodeURL();
		String remoteURL = ((BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) remoteVM).getProxy()).getBody().getNodeURL();
		//System.out.println("*** URL " + TestNodes.getRemoteVMNode().getNodeInformation().getURL());
		//System.out.println("*** OA " + TestNodes.getRemoteVMNode().getActiveObjects(A.class.getName()));
		
		// Add exception handler to the active object on the same VM
		System.out.println("*** SET HANDLER IN SAME VM : " + sameURL);
		ProActive.setExceptionHandler(HandlerCommunicationException.class, ProActiveCommunicationException.class, Handler.ID_Body, sameVM);
		ProActive.setExceptionHandler(HandlerMigrationException.class, ProActiveMigrationException.class, Handler.ID_Body, sameVM); 

		// Add exception handler to the active object on a local VM
		System.out.println("*** SET HANDLER IN LOCAL VM : " + localURL);
		ProActive.setExceptionHandler(HandlerCommunicationException.class, ProActiveCommunicationException.class, Handler.ID_Body, localVM);
		ProActive.setExceptionHandler(HandlerMigrationException.class, ProActiveMigrationException.class, Handler.ID_Body, localVM); 

		// Add exception handler to the active object on a remote VM
		System.out.println("*** SET HANDLER IN REMOTE VM : " + remoteURL);
		ProActive.setExceptionHandler(HandlerCommunicationException.class, ProActiveCommunicationException.class, Handler.ID_Body, remoteVM);
		ProActive.setExceptionHandler(HandlerMigrationException.class, ProActiveMigrationException.class, Handler.ID_Body, remoteVM); 
	}

	/**
	 * @see testsuite.test.AbstractTest#postConditions()
	 */
	public boolean postConditions() throws Exception {
		
		// We create on the fly two non functional exception
		ProActiveMigrationException nfeMig = new ProActiveMigrationException(null); 
		ProActiveCommunicationException nfeCom = new ProActiveCommunicationException(null);
		
		// We check if handlers table contains handlers for both migration and communication exception at active object level 
		/*return (sameVM.protectedFrom(nfeCom) && sameVM.protectedFrom(nfeMig) &&
					 localVM.protectedFrom(nfeCom) && localVM.protectedFrom(nfeMig) &&
					 remoteVM.protectedFrom(nfeCom) && remoteVM.protectedFrom(nfeMig)
					 );
		*/
		return ((ProActive.searchExceptionHandler(nfeCom, sameVM)!=null) && (ProActive.searchExceptionHandler(nfeMig, sameVM)!=null) && 
					(ProActive.searchExceptionHandler(nfeCom, localVM)!=null) && (ProActive.searchExceptionHandler(nfeMig, localVM)!=null) &&
					(ProActive.searchExceptionHandler(nfeCom, remoteVM)!=null) && (ProActive.searchExceptionHandler(nfeMig, remoteVM)!=null));
	}
	
	/**
	 * @see testsuite.test.AbstractTest#endTest()
	 */
	public void endTest() throws Exception {
	}
}
