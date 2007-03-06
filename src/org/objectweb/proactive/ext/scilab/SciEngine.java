/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
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

/**
 * 
 * This class activates a new JVM which wraps the Scilab Engine and forwards the Scilab tasks
 *
 */
public class SciEngine implements Serializable  {
	private SciEngineWorker sciEngineWorker;
	private String idEngine; 
	private JVMProcessImpl process;
	private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_WORKER);
	
	/**
	 * default constructor
	 */
	public SciEngine() {
	}
	
	/**
	 * @param idEngine
	 */
	public SciEngine(String idEngine) {
		this.idEngine = idEngine; 
	}
	
	
	public void exit(){
		//logger.debug("->SciEngineWorker In:exit");
		this.killWorker();
		System.exit(0);
	}
	
	/**
	 * execute a task 
	 * @param sciTask Scilab task
	 * @return result of the computation 
	 */
	public SciResult execute(SciTask sciTask){
		logger.debug("->SciEngineWorker In:execute:" + sciTask.getId());
		SciResult sciResult = this.sciEngineWorker.execute(sciTask);
		return sciResult;
	}
	
	/**
	 * Activate a new JVM to wrap a worker
	 * @return a future representing the state of the activation
	 */
	public BooleanWrapper activate(){
		logger.debug("->SciEngineWorker In:activate");
		String url = "//localhost/" + idEngine + (new Date()).getTime();
		try{
			process = new JVMProcessImpl();
			process.setClassname("org.objectweb.proactive.core.node.StartNode");
			process.setParameters(url);
			process.startProcess();
		}catch(IOException e){
			return new BooleanWrapper(false);
		}
		
		for(int i=0; i<30; i++){
			try{
				try{		
					sciEngineWorker  =  (SciEngineWorker) ProActive.newActive(SciEngineWorker.class.getName(), null, url);
					return new BooleanWrapper(true);
				}catch(ProActiveException e){
					
				}
				
				Thread.sleep(1000);
			}catch(InterruptedException e){
				e.printStackTrace();
			} 
		}
		return new BooleanWrapper(false);
	}
	
	/**
	 * Kill the worker related the task
	 * It is an immediat services
	 */
	public synchronized void killWorker(){
		process.stopProcess();
	}
}
