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

import org.objectweb.proactive.core.group.MethodCallControlForGroup;


/**
 * This class represents a call of strong synchronization between the member of a SPMD Group.
 * @author Laurent Baduel
 */
public class MethodCallBarrier extends MethodCallControlForGroup {

	private String IDName;

	/**
	 * Constructor
	 * @param name - the id name of the barrier 
	 */
    public MethodCallBarrier(String idname) {
    	this.IDName = idname;
    }

    /**
     * Returns the name of the call
     * @return the String "MethodCallBarrier";
     */
    public String getName() {
        return "MethodCallBarrier";
    }
    
    /**
     * Returns the ID name of the barrier
     * @return the ID name of the barrier
     */
    public String getIDName() {
    	return this.IDName;
    }

    /**
     * Barrier call have no parameter.
     * @return 0
     * @see org.objectweb.proactive.core.mop.MethodCall#getNumberOfParameter()
     */
    public int getNumberOfParameter() {
        return 0;
    }
    
//	/**
//	 * BarrierControlCall for group never are oneway
//	 * @return <code>true</code>  
//	 * @see org.objectweb.proactive.core.mop.MethodCall#isOneWayCall()
//	 */
//	public boolean isOneWayCall() {
//		return false;
//	}
//
//	/**
//	 * BarrierControlCall for group never are asynchronous
//	 * @return <code>false</code>  
//	 * @see org.objectweb.proactive.core.mop.MethodCall#isAsynchronousWayCall()
//	 */
//	public boolean isAsynchronousWayCall() {
//		return false;
//	}

}
