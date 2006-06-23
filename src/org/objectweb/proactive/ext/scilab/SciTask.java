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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javasci.SciData;

public class SciTask implements Serializable {
	
	private String id;
	private ArrayList listDataIn;
	private ArrayList listDataOut;
	private String job;
	private String jobInit;

	public SciTask(String id) {
		this.id = id;
		this.listDataIn = new ArrayList();
		this.listDataOut = new ArrayList();
	}
	
	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}
	
	public void setJobInit(String jobInit){
		this.jobInit = jobInit;
	}
	
	public void setJob(File fileJob) throws FileNotFoundException, IOException{
		StringBuffer strBuffer = new StringBuffer();
	
		FileReader reader = new FileReader(fileJob);
		int c;
		
		while((c = reader.read()) != -1){
			strBuffer.append((char)c);
		}
		this.job = strBuffer.toString();
		
		reader.close();
	}

	public ArrayList getListDataIn() {
		return listDataIn;
	}

	public void setListDataIn(ArrayList listDataIn) {
		this.listDataIn = listDataIn;
	}

	public void addDataIn(SciData data) {
		this.listDataIn.add(data);
	}
	
	public ArrayList getListDataOut() {
		return listDataOut;
	}

	public void setListDataOut(ArrayList listDataOut) {
		this.listDataOut = listDataOut;
	}
	
	public void addDataOut(SciData data) {
		this.listDataOut.add(data);
	}

	public String getId() {
		return id;
	}

	public String getJobInit() {
		return jobInit;
	}
	
}
