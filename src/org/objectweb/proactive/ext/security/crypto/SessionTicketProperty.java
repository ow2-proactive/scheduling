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
package org.objectweb.proactive.ext.security.crypto;
import java.io.*;

/**
 *  This class is used as an attribute of the SessionTicket class
 *
 * @author     Vincent RIBAILLIER
 * @created    July 19, 2001
 */
public class SessionTicketProperty implements Serializable {
	public long ta;
	// indicates an expiracyTime
	public long ra;
	// random number to detect replay and impersonation
	public String B;
	// addressee
	public Object encrypted_key;
	// Encrypted session key with B public Key

	/**
	 *  Constructor for the SessionTicketProperty object
	 *
	 * @since
	 */
	public SessionTicketProperty() {
	}

}

