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
 * This interface presents the group abilities extending java.util.Collection.
 *
 * @see java.util.Collection
 *
 * @author Laurent Baduel - INRIA
 *
 */
public interface Group extends java.util.Collection { 


	/**
	 * Returns the (upper) class of member.
	 */
	public Class getType() throws java.lang.ClassNotFoundException;

	/**
	 * Returns the name of the (upper) class of member.
	 */    
	public String getTypeName();

	/**
	 * Returns an object representing the group, and assignable from the (upper) class of member.
	 */
	public Object getGroupByType();


    
    /**
     * Merge a group into the group.
     */
    public void addMerge(Object ogroup);

    /**
     * Remove the object at the specified index.
     */
    public void remove(int index);

    /**
     * Returns the index in the group of the first occurence of the specified element, -1 if the list does not contain this element.
     */
    public int indexOf(Object obj);
    

}
