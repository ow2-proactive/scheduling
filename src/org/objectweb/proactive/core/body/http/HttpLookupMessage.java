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
package org.objectweb.proactive.core.body.http;

import org.objectweb.proactive.core.body.UniversalBody;


/**
 * This classes represents a HTTPMessage. When processed, this message performs a lookup thanks to the urn.
 * @author vlegrand
 * @see HttpMessage
 */
public class HttpLookupMessage implements HttpMessage {

	private String urn;
	private Object returnedObject;  
	 
	/**
	 * Constructs an HTTP Message
	 * @param urn The urn of the Object (it can be an active object or a runtime).
	 */
	public HttpLookupMessage (String urn) {    
		this.urn = urn;
	}

	/**
	 * Performs the lookup 
	 * @return The Object associated with the urn
	 */
	public Object processMessage() {
		if (this.urn != null) {
			UniversalBody ub = RemoteBodyAdapter.getBodyFromUrn(urn);
			if (ub != null)		
				this.returnedObject = ub;
		/*	else {	// urn body is not found in http we search in rmi 	
				try {
					this.returnedObject = org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter.lookup(urn);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (this.returnedObject == null) // urn body is no found
					this.returnedObject = ProActiveXMLUtils.NO_SUCH_OBJECT;	
			}	*/	
	
		this.urn = null;
		return this;
		}
		else { 
			return this.returnedObject;
		}
	}

}
