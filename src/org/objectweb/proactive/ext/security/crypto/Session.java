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
import java.security.Key;

/**
 * This class contains the basic informations of a communication session.
 *
 * @author     Vincent RIBAILLIER
 * <br>created    July 19, 2001
 */
public class Session {
	private long id;
	private PublicCertificate otherPublicCertificate;
	private Key sessionKey;
	private long lastrandom = 0;


	/**
	 *  Constructor for the Session object
	 *
	 * @since
	 */
	public Session() {
	}


	/**
	 *  Constructor for the Session object
	 *
	 * @param  session The session
	 * @since
	 */
	public Session(Session session) {
		id = session.id;
		otherPublicCertificate = session.otherPublicCertificate;
		sessionKey = session.sessionKey;
		lastrandom = session.lastrandom;
	}


	/**
	 *  Constructor for the Session object
	 *
	 * @param  id The id of the session
	 * @since
	 */
	public Session(long id) {
		this.id = id;
	}

	/**
	 * 
	 *
	 * @param  lastrandom  The last_random long required in the authentifcation protocol
	 * @since
	 */
	public void set_lastrandom(long lastrandom) {
		this.lastrandom = lastrandom;
	}


	/**
	 * 
	 *
	 * @param  sessionKey  The symmetric key used by the session for confidential communications
	 * @since
	 */
	public void set_sessionKey(Key sessionKey) {
		this.sessionKey = sessionKey;
	}

	/**
	 * 
	 *
	 * @param  otherPublicCertificate  The publicCertificate of the other communicating entity
	 * @since
	 */
	public void set_otherPublicCertificate(PublicCertificate otherPublicCertificate) {
		this.otherPublicCertificate = otherPublicCertificate;
	}


	/**
	 * checks the ID of the Session
	 *
	 * @param  ID The ID of the session
	 * @return    true if the ID is correct, false otherwise
	 * @since
	 */
	public boolean isID(long ID) {
		if (ID == this.id) {
			return true;
		}

		return false;
	}
 
	/**
	 * 
	 *
	 * @param  id The ID of the session 
	 * @return The PublicCertificate of the other communicating party, null is id is not correct    
	 * @since
	 */
	public PublicCertificate get_otherPublicCertificate(long id) {
		if (this.id == id) {
			return otherPublicCertificate;
		}

		return null;
	}
   
	/**
	 * 
	 *
	 * @param  id The ID of the session 
	 * @return  The sessionKey, null is  the id is not correct
	 * @since
	 */
	public Key get_sessionKey(long id) {
		if (this.id == id) {
			return sessionKey;
		}

		return null;
	}

	/**
	 * 
	 *
	 * @param  id The ID of the session
	 * @return The last random used in the authentication process, null is the ID is not correct.   
	 * @since
	 */
	public long get_lastrandom(long id) {
		if (this.id == id) {
			return lastrandom;
		}

		return 0;
	}
}

