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
package org.objectweb.proactive.ext.scilab.test;

import java.util.ArrayList;

import javasci.SciData;

import org.objectweb.proactive.ext.scilab.SciResult;
import org.objectweb.proactive.ext.scilab.SciTask;
import org.objectweb.proactive.ext.scilab.monitor.SciEvent;
import org.objectweb.proactive.ext.scilab.monitor.SciEventListener;
import org.objectweb.proactive.ext.scilab.monitor.SciTaskInfo;
import org.objectweb.proactive.ext.scilab.monitor.ScilabService;

public class SciTest2 {


	private ScilabService scilab;
	
	public void  displayResult(SciTaskInfo scitaskInfo){
		SciResult sciResult = scitaskInfo.getSciResult();
		ArrayList listResult;
		listResult = sciResult.getList();

		for (int i = 0; i < listResult.size(); i++) {
			SciData result = (SciData) listResult.get(i);
			System.out.println(result.toString());
		}
		scilab.exit();
		System.exit(0);
	}
	
	
	public SciTest2(String nameVN, String pathVN) throws Exception{
		scilab = new ScilabService();
		scilab.deployEngine(nameVN, pathVN, new String[]{"Scilab"});
		
		scilab.addEventListenerTask( new SciEventListener(){
			public void actionPerformed(SciEvent evt){
				SciTaskInfo sciTaskInfo = (SciTaskInfo) evt.getSource();
				
				if(sciTaskInfo.getState() == SciTaskInfo.SUCCEEDED){
					 displayResult(sciTaskInfo);
					return;
				}
			}
		});
		
		SciTask task = new SciTask("id");
		task.setJobInit("n = 10;");
		task.addDataOut(new SciData("n"));
		task.setJob("n = n+1;");
		System.out.println("Job : " + task.getJob());
		scilab.sendTask(task);
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length != 2){
			System.out.println("Invalid number of parameter : " + args.length);
			return;
		}
		
		new SciTest2(args[0], args[1]);
	}

}
