/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ext.scilab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javasci.SciData;

/**
 * This class represents a Scilab result
 */
public class SciResult implements Serializable{
	public static final int SUCCESS = 0, ABORT = 1;
	private String id;
	private int state;
	private HashMap mapResult; //map of SciResult
	private long timeExecution;
	
	/**
	 * default constructor
	 *
	 */
	public SciResult(){
	}
	
	public SciResult(String id){
		this.id = id;
		mapResult = new HashMap();
	}
	
	/**
	 * add an Out data
	 * @param data
	 */
	public void add(SciData data){
		this.mapResult.put(data.getName(), data);
	}
	
	/**
	 * 
	 * @param name data id
	 * @return the data
	 */
	public SciData get(String name){
		return (SciData) this.mapResult.get(name);
	}
	
	/**
	 * 
	 * @return list of all out data 
	 */
	public ArrayList getList(){
		ArrayList listResult = new ArrayList();
		Iterator it = this.mapResult.values().iterator();
		
		while(it.hasNext()){
			listResult.add(it.next());
		}
		
		return listResult;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getTimeExecution() {
		return timeExecution;
	}

	public void setTimeExecution(long timeExecution) {
		this.timeExecution = timeExecution;
	}

	public String getId() {
		return id;
	}
}
