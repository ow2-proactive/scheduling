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
 * This class gives to it implementors the ability to contact the other members of the group containting the object.
 *  
 * @author Laurent Baduel
 */
public abstract class GroupMember implements java.io.Serializable {

	/** the group of the object */
	protected Object myGroup = null;
	/** index of the object into the Group */
	protected int myRank;

	
	/**
	 * Returns the group of the object.
	 * @return the group of the object
	 * @throws Exception
	 */
	public Object getMyGroup() throws UnreachableGroupException {
		if (this.myGroup == null)
			throw new UnreachableGroupException();
		return this.myGroup;
	}
	
	/**
	 * Specifies the group of the object.
	 * @param o - a typed group 
	 */
	public void setMyGroup(Object o) {
		this.myGroup = o;
	}

	/**
	 * Returns the rank (position) of the object in the Group
	 * @return the index of the object
	 */
	public int getMyRank() {
		return myRank;
	}

	/**
	 * Specifies the rank (position) of the object in the Group
	 * @param index - the index of the object
	 */
	public void setMyRank(int index) {
		this.myRank = index;
	}

}
