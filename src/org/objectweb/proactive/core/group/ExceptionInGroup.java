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
package org.objectweb.proactive.core.group;

/**
 * This class represents an throwable occured in a group communication.
 * 
 * @author Laurent Baduel
 */
public class ExceptionInGroup extends RuntimeException {

	/** The Object who thrown the Throwable */
	private Object object;
	/** The Throwable thrown */
	private Throwable throwable;
	
	/**
	 * Built an ThrowableInGroup
	 * @param object - the object who thrown the throwable (use null if the object is unknown)
	 * @param throwable - the throwable thrown
	 */
	public ExceptionInGroup (Object object, Throwable throwable) {
		this.object = object;
		this.throwable = throwable; 
	}
	

	/**
	 * Returns the object who thrown the Throwable
	 * @return the object who thrown the Throwable
	 */
	public Object getObject() {
		return this.object;
	}

	/**
	 * Returns the Throwable thrown
	 * @return the Throwable thrown
	 */
	public Throwable getThrowable() {
		return this.throwable;
	}

}
