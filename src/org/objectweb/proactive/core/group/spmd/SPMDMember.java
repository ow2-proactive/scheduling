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
package org.objectweb.proactive.core.group.spmd;

import org.objectweb.proactive.core.group.GroupMember;
import org.objectweb.proactive.core.group.ProActiveGroup;
//import org.objectweb.proactive.core.group.ProActiveGroup;

/**
 * This class gives to it implementors the ability to contact the other members of the group containting the object.
 *  
 * @author Laurent Baduel
 */
public class SPMDMember extends GroupMember {

	public SPMDMember() {}
	
	/**
	 * Returns the size of the group 
	 * @return the size of the group
	 */
	public int getMyGroupSize() {
		return ProActiveGroup.size(this.myGroup);
	}
	
	/**
	 * Returns the rank (position) of the object in the Group
	 * @return the index of the object
	 */
	public int getMyRank() {
		return myRank;
	}

	/**
	 * Requests for an object
	 * @param <code>o</code> - the object to send
	 * @return a copy of <code>o</code>
	 */
	public Object sendMe(Object o) {
		return o;
	}

	public SPMDMember barrier () throws Exception {
		return new SPMDMember();  // it doesn't return void due to the bug #487, it will be fix soon
	}

//	/**
//	 * A barrier of synchronisation. This method is blocking. The activity restart when all other members of the SPMD
//	 * activites reach this method. 
//	 */
//	public SPMDMember barrier () throws Exception {
////		boolean res = false;
////		if (this.getMyRank() != 0) {
////				res = ((SPMDMember) ProActiveGroup.getGroup(this.getMyGroup()).get(0)).synchroWithAll();
////		}
//		return new SPMDMember();
//	}
//	
//	/**
//	 * A synchrounous method that return true.
//	 * @return true
//	 */
//	public boolean synchroWithAll() {
//		return true;
//	}

}
