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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public class SciEngineWorker implements Serializable  {
	
	private JVMProcessImpl process;
	private SciEngineTask sciEngineTask;
	private String idEngine; 
	private String idTask; 
	
	private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_WORKER);
	/**
	 * default constructor
	 *
	 */
	public SciEngineWorker() {
	
	}
	
	/**
	 * 
	 * @param idEngine
	 */
	public SciEngineWorker(String idEngine) {
		this.idEngine = idEngine;
	}
	
	
	public void exit(){
		logger.debug("->SciEngineWorker In:exit");
		this.killEngineTask();
	}
	
	public SciResult execute(SciTask sciTask){
		logger.debug("->SciEngineWorker In:execute:" + sciTask.getId());
		this.idTask = sciTask.getId();
		return this.sciEngineTask.execute(sciTask);
	}
	
	public BooleanWrapper activate(){
		logger.debug("->SciEngineWorker In:activate");
		String url = "//localhost/" + idEngine + (long)(Math.random() * 1000000) + (new Date()).getTime(); 
		try{
			process = new JVMProcessImpl();
			process.setClassname("org.objectweb.proactive.core.node.StartNode");
			process.setParameters(url);
			process.startProcess();
		}catch(IOException e){
			return new BooleanWrapper(false);
		}
		
		for(int i=0; i<60; i++){
			try{	
				Thread.sleep(3000);
				sciEngineTask  =  (SciEngineTask) ProActive.newActive(SciEngineTask.class.getName(), null, url);	
				return new BooleanWrapper(true);
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(ProActiveException e){
			}
		}  
		return new BooleanWrapper(false);
	}
	
	public void killEngineTask(){
		logger.debug("->SciEngineWorker In:killEngineTask" + idTask);
		process.stopProcess();
		this.sciEngineTask = null;
	}
	
}
