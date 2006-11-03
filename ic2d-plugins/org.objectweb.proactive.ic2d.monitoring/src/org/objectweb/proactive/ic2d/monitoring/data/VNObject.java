/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ic2d.monitoring.data;


public class VNObject extends AbstractDataObject {
	
	/** The virtual node name */
	private String name;
	/** The virtual node job ID */
	private String jobID;
	
	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	protected VNObject(String name, String jobID, WorldObject world) {
		super(world);
		this.name = name;
		this.jobID = jobID;
		world.putVNChild(this);
		
		this.allMonitoredObjects.put(getKey(), this);
	}

	
	//
	// -- PUBLIC METHOD -----------------------------------------------
	//
	
	

	
	@Override
	public void explore() {/* Do nothing */}

	@Override
	public String getFullName() {
		return name;
	}

	@Override
	public String getKey() {
		return name;
	}

	@Override
	public String getType() {
		return "vn";
	}
	
	public String getJobID() {
		return jobID;
	}
	
	//
	// -- PROTECTED METHOD -----------------------------------------------
	//
	
	@Override
	protected void alreadyMonitored() { /* Do nothing*/ }
	
	@Override
	protected void foundForTheFirstTime() { /* Do nothing*/ }

}
