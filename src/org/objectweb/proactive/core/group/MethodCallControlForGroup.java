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

import java.io.IOException;
import java.lang.reflect.Method;

import org.objectweb.proactive.core.mop.MethodCall;

/**
 * @author Laurent Baduel
 */
public abstract class MethodCallControlForGroup extends MethodCall {

	public MethodCallControlForGroup () { }

	public Method getReifiedMethod () {
		return null;
	}

	/**
	 * ControlCall for group never are asynchronous
	 * @return <code>false</code>  
	 * @see org.objectweb.proactive.core.mop.MethodCall#isAsynchronousWayCall()
	 */
	public boolean isAsynchronousWayCall() {
		return false;
	}

	/**
	 * ControlCall for group always are oneway
	 * @return <code>true</code>  
	 * @see org.objectweb.proactive.core.mop.MethodCall#isOneWayCall()
	 */
	public boolean isOneWayCall() {
		return true;
	}

	/**
	 * Returns the number of parmeters
	 * @return 0 
	 * @see org.objectweb.proactive.core.mop.MethodCall#getNumberOfParameter()
	 */
	public abstract int getNumberOfParameter();

	//
	// --- PRIVATE METHODS FOR SERIALIZATION --------------------------------------------------------------
	//

	private void writeObject(java.io.ObjectOutputStream out)
		throws java.io.IOException {
			this.writeTheObject(out);
	}

	protected void writeTheObject(java.io.ObjectOutputStream out)
		throws java.io.IOException {
		out.defaultWriteObject();
		//System.out.println("L'appel de control est serialise");
	}



	private void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException {
			this.readTheObject(in);
	}

	protected void readTheObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		//System.out.println("L'appel de control est de-serialise");
	}

	// Overloaded to avoid this MethodCallControlForGroup object
	// go inside the recycling pool of MethodCall.
	protected void finalize () { }

}
