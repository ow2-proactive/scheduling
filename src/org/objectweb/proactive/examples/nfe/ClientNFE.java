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

package org.objectweb.proactive.examples.nfe;


import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.communication.ProActiveCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerCommunicationException;

/**
 * Client to test non functional exception
 */
public class ClientNFE {
	
	protected static Logger logger = Logger.getLogger(ProActive.class.getName());
	
	public static void main(String args[]) {

		ServerNFE server;

		try {
			// checks for the server's URL
			if (args.length == 0) {
				 // There is no url to the server, so create an active server within this VM
			 	server = (ServerNFE) org.objectweb.proactive.ProActive.newActive(ServerNFE.class.getName(), null);
			} else {
				// Lookups the server object
				System.out.println("Using server located on " + args[0]);
				server = (ServerNFE) org.objectweb.proactive.ProActive.lookupActive(ServerNFE.class.getName(), args[0]);
			}
		
			// Try to set a handler for the active object "server"
			//BodyProxy serverProxy = (BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) server).getProxy();
			ProActive.setExceptionHandler(HandlerCommunicationException.class, ProActiveCommunicationException.class, Handler.ID_Body, server);
			//ProActive.setExceptionHandler(HandlerMigrationException.class, ProActiveMigrationException.class, Handler.ID_BODY, serverProxy.getBody());

			// Invokes a remote method on this object to get the message
			server.test("org.objectweb.proactive.core.exceptions.NonFunctionalException");
		
	 	} catch(Exception e) {
		   		System.err.println("Could not reach/create server object");
		   		e.printStackTrace();
		   		System.exit(1);
		}
	}
}
