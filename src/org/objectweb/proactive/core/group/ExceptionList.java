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

import java.util.Iterator;
import java.util.Vector;


/**
 * A list of the ExceptionInGroup occured in a group.
 * 
 * @author Laurent Baduel
 */
public class ExceptionList extends RuntimeException {

	/** A vector implements the list */
	private Vector list;
	

	/**
	 * Builds a new empty list of exception
	 */ 
	public ExceptionList () {
		this.list = new Vector();
	}
	

	/**
	 * Adds an exception into this list 
	 * @param exception - the exception to add
	 */
	public void add (ExceptionInGroup exception) {
		this.list.add(exception);
	}
	
	/**
	 * Removes all of the exceptions from this list.
	 */
	public void clear () {
		this.list.clear();
	}
	
	/**
	 * Returns an iterator over the exceptions in this list in proper sequence.
	 * @return an iterator over the exceptions in this list in proper sequence.
	 */
	public Iterator iterator () {
		return this.list.iterator();
	}

	/**
	 * Returns the number of exceptions in this list.
	 * @return the number of exceptions in this list.
	 */
	public int size () {
		return this.list.size();
	}
	
	/**
	 * Tests if this ExceptionList has no ExceptionInGroup.
	 * @return <code>true</code> if and only if this list has no components, that is, its size is zero; <code>false otherwise.
	 */
	public boolean isEmpty() {
		return this.list.isEmpty();
	}
}
