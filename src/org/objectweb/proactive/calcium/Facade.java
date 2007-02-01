/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.calcium;


import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.calcium.Skernel;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.futures.FutureImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * This class provides a Facade access to the kernel.
 * Since the kernel handles tasks for multiple streams at the same
 * time, this class is in charge of redirecting:
 *  -Tasks from streams into the kernel
 *  -Tasks comming out from the kernel into their respective streams.
 *  
 * @author The ProActive Team (mleyton)
 */

public class Facade {
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_KERNEL);
	
	private Skernel skernel;
	private FutureUpdateThread results;
	private PanicException panic;
	
	public Facade(){
		this.skernel=null;
		this.results=null;
		this.panic=null;
	}
	
	public synchronized void putTask(Task<?> task, FutureImpl<?> future)  throws InterruptedException, PanicException{
		
		while(skernel==null){
			wait();
		}
		
		skernel.addReadyTask(task);
		results.put(future);
	}
	
	public synchronized void setSkernel(Skernel skernel){
		this.skernel=skernel;
		this.results = new FutureUpdateThread();
		results.start();
		notifyAll();
	}

	/**
	 * This class stores references to the futures, and updates
	 * the references once the results are available. 
	 * 
	 * @author mleyton
	 */
	class FutureUpdateThread extends Thread{
		
		Hashtable<Integer, FutureImpl<?>> pending;

		
		public FutureUpdateThread(){
			pending = new Hashtable<Integer, FutureImpl<?>>();

		}
		
		/**
		 * Stores a future in the strucutre.
		 * The stream id must be stored inside the task before storing it.
		 * @param task The task to store.
		 */
		public synchronized void put(FutureImpl<?> future){
			int taskId=future.getTaskId();

			if(pending.containsKey(taskId)){
				logger.error("Future already registered for task="+taskId);
				return;
			}
			
			pending.put(taskId, future);
		}
		
		public synchronized void updateFuture(Task<?> task){
						 
			if(!pending.containsKey(task.getId())){
				logger.error("No future is waiting for task:"+task.getId());
				return;
			}
			
			FutureImpl<?> future=pending.remove(task.getId());
			future.setFinishedTask(task);
		}
		
		/**
		 * This method can be used to determine if tasks are available
		 * for this stream.
		 * @param streamId The stream id we wish to evaluate.
		 * @return True if there is a task available, false otherwise.
		 */
		public synchronized boolean isEmpty(int streamId){
			return !pending.containsKey(streamId);
		}
		
		/**
		 * @return The number of total elements on this structure.
		 */
		public synchronized int size(){
			return pending.size();
		}
		
		public void run(){
			//TODO add terminatino condition
			while(panic==null){
				Task<?> taskResult=null;
				try{
					taskResult=  (Task<?>) skernel.getResult();
				}catch(PanicException ex){
					//logger.error("Facade has encounterd skernel panic! Stopping future update thread");
					ex.printStackTrace();
					panic=ex;
					//TODO update all the remaining future with the panic exception
					return;
				}
				
				
				//TODO Temporary ProActive generics bug workaround 
				//This is the supelec trick
				taskResult=(Task<?>)ProActive.getFutureValue(taskResult);
				results.updateFuture(taskResult);
			}
		}
	}
}
