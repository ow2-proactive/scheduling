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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.calcium.Skernel;
import org.objectweb.proactive.calcium.exceptions.PanicException;

/**
 * This class provides a Facade access to the kernel.
 * Since the kernel handles tasks for multiple streams at the same
 * time, this class is in charge of redirecting:
 *  -Tasks from streams into the kernel
 *  -Tasks comming from the kernel into their respective streams.
 *  
 * @author The ProActive Team (mleyton)
 */
public class Facade {

	private Skernel skernel;
	private TaskStreamQueue results;
	
	public Facade(Skernel skernel){
		this.skernel=skernel;
		this.results = new TaskStreamQueue();
	}
	
	public synchronized void putTask(Task<?> task){	
		skernel.putTask(task);
	}
	
	public void boot(Skernel skernel){
		this.skernel=skernel;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> Task<T> getResult(int streamId) throws PanicException{
			
		//Get new tasks from the skernel
		try {
			while(results.isEmpty(streamId)){
				wait(1000);
				loadResultsFromSkernel();
			}
		} catch (InterruptedException e) {		 
			e.printStackTrace();
			return null;
		}
		
		return (Task<T>)results.get(streamId);
	}
	
	private synchronized void loadResultsFromSkernel() throws PanicException{
		while(skernel.hasResults()){
			Task<?> taskResult =  (Task<?>) skernel.getResult();

			//TODO Temporary ProActive generics bug workaround 
			//This is the supelec trick
			taskResult=(Task<?>)ProActive.getFutureValue(taskResult);
			results.put(taskResult);
		}
	}

	/**
	 * This class stores tasks indexed by a the stream identifier.
	 * It is used for storing the solved tasks and delivering them
	 * to the user once they are updated.
	 * 
	 * @author mleyton
	 */
	class TaskStreamQueue implements Serializable{
		
		Hashtable<Integer, Vector<Task<?>>> results;
		int size;
		
		public TaskStreamQueue(){
			results = new Hashtable<Integer, Vector<Task<?>>>();
			size=0;
		}
		
		/**
		 * Stores a task in the queue.
		 * The stream id must be stored inside the task before storing it.
		 * @param task The task to store.
		 */
		public synchronized void put(Task<?> task){
			int streamId=task.getStreamId();

			if(!results.containsKey(streamId)){
				results.put(streamId, new Vector<Task<?>>());
			}
			
			Vector<Task<?>> vector=results.get(streamId);
			
			vector.add(task);
			size++;
		}
		
		/**
		 * Retrieves a task stored in this queue. The
		 * task retrieved correspons to the oldest one for
		 * the specified stream id.
		 * @param streamId The stream id that will be retrieved.
		 * @return A task or null if no tasks are available for this stream.
		 */
		public synchronized Task<?> get(int streamId){
			if(isEmpty(streamId)) return null;
			 
			Vector<Task<?>> vector=results.get(streamId);
			if(vector.size()==0) return null;
			if(vector.size()==1) results.remove(streamId);
			size--;
			return vector.remove(0);
		}
		
		/**
		 * This method can be used to determine if tasks are available
		 * for this stream.
		 * @param streamId The stream id we wish to evaluate.
		 * @return True if there is a task available, false otherwise.
		 */
		public synchronized boolean isEmpty(int streamId){
			return !results.containsKey(streamId);
		}
		
		/**
		 * @return The number of total elements on this queue.
		 */
		public synchronized int size(){
			return size;
		}
		
		/**
		 * 
		 * @param streamId The stream we whant to get.
		 * @return The number of elements in this queue for the given stream.
		 */
		public synchronized int size(int streamId){
			if(isEmpty(streamId)) return 0;
			Vector<Task<?>> vector=results.get(streamId);
			return vector.size();
		}
	}
}
