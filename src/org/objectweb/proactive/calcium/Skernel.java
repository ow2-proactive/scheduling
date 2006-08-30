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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.statistics.StatsGlobalImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents the skeleton kernel.
 * It handles the tasks that must be solved, which can be in
 * one of the following states: ready, waiting, processing, results(finished).
 * 
 * Data parallelism is represented as child tasks of a parent task. This class
 * handles the proper enqueing of tasks (child or parent).
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <T>
 */
public class Skernel<T> implements Serializable{
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_KERNEL);
	
	//State Queues
	private PriorityQueue<Task<T>> ready; //Tasks ready for execution
	private Hashtable<Task<T>,Task<T>> waiting; //Tasks waiting for subtasks completition
	private Vector<Task<T>> results; //Finished root-tasks
	private Hashtable<Task<T>,Task<T>> processing; //Tasks being processed at this moment
	
	private PanicException panicException;
	
	//Statistics

	private StatsGlobalImpl stats;
	
	public Skernel(){
		this.ready= new PriorityQueue<Task<T>>();
		this.waiting=new Hashtable<Task<T>,Task<T>>();
		this.results=new Vector<Task<T>>();
		this.processing=new Hashtable<Task<T>,Task<T>>();
		this.stats=new StatsGlobalImpl();
		this.panicException=null;
	}
	
	public synchronized Task<T> getResult() throws PanicException{
		while(results.size()<=0 && !isPaniqued()){
			try {
				if(logger.isDebugEnabled()){
					logger.debug("Thread waiting for results:"+Thread.currentThread().getId());
				}
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		if(isPaniqued()) {
			notifyAll();
			throw panicException;
		}
			
		Task<T> resultTask=results.remove(0);
		resultTask.getStats().exitResultsState();
		//keep stats for future use
		
		if(resultTask.hasException()){
			//Only runtime exception can be found here
			throw (RuntimeException)resultTask.getException(); 
		}
		
		return resultTask;
	}

	/**
	 * This method is used to get task ready for execution.
	 * If there are no ready tasks, then this method can do two things:
	 * 1. Block until a ready task is available (only if there are taks being processed).
	 * 2. Return null if no tasks are being processed.
	 * @return A task ready for execution or null.
	 */
	public synchronized Task<T> getReadyTask(){
		
		while(ready.isEmpty()){
			
			if(isFinished()) {
				notifyAll();
				return null;
			}
			
			try {
				if(logger.isDebugEnabled()){
					logger.debug("Waiting for ready task:"+Thread.currentThread().getId());
				}
				wait();
			} catch (InterruptedException e) {
				logger.error("Error while waiting for ready task");
				e.printStackTrace();
				return null;
			}
		}
		
		if(ready.isEmpty()) return null;
		
		Task<T> task = ready.poll(); //get the highest priority task
		task.getStats().exitReadyState();

		processing.put(task,task);
		if(logger.isDebugEnabled()){
			logger.debug("Serving taskId="+task);
		}

		return task;
	}
	
	public synchronized void putTask(Task<T> task){
		
		stats.markStart();
		//TODO think about throwing an exception here!
		//if(isPaniqued()) throw panicException;
		
		Task<T> processingTask=processing.remove(task);
		if(processingTask==null && logger.isDebugEnabled()){
			logger.debug("Enqueing new taskId="+task);
		} else if( logger.isDebugEnabled()){
			logger.debug("Updating taskId="+task);
		}
		
		//The task can be tainted by exceptions on other family members
		if(processingTask!=null && processingTask.isTainted()){
			if(logger.isDebugEnabled()){
				logger.debug("Dropping tainted taskId="+task);
			}
			return;
		}
		
		task.getStats().exitProcessingState();
		
		if(task.hasException()){
			updateExceptionedTask(task);
		}
		else if(task.isFinished()){
			updateFinishedTask(task);
		}
		else{
			updateTask(task);
		}

		if(!ready.isEmpty()){
			notifyAll();
		}
	}
	
	public synchronized boolean isPaniqued(){
		
		return this.panicException != null;
	}
	
	private void updateExceptionedTask(Task<T> task){
		
		if(logger.isDebugEnabled()){
			logger.debug("Updating Exceptioned Task taskId="+task);
		}
		
		Exception e=task.getException();
		
		if( e instanceof PanicException) {
			kernelPanic((PanicException)e);		//Panic exception
			return;
		}
				
		if( e instanceof RuntimeException){ 	//Fatal Exception
			deleteTaskFamilyFromQueues(task);
			return;
		}

		//TODO handle Scheduling Exceptions
		
		//Else: handle regular exceptions
		if(task.isFinished()){
			String msg="Panic Error. Task with exceptions cannot be a finished task!";
			logger.error(msg);
			kernelPanic(new PanicException(msg));
		}
		
		if(task.isRootTask()){	//if its a root task then thats all folks
			deleteTaskFamilyFromQueues(task);
			return;
		}
		
		//if its a child task, we update it as a finished task
		updateFinishedTask(task); 
	}
	
	private void updateFinishedTask(Task<T> task){
		
		if(!task.isFinished()){
			String msg="Error, updating unfinished task as finished!";
			logger.debug(msg);
			kernelPanic(new PanicException(msg));
			return;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("Updating Finished Task taskId="+task);
		}
		
		task.markFinishTime();
		stats.increaseSolvedTasks(task);
		
		if(task.isRootTask()){ //Task finished
			if(logger.isDebugEnabled()){
				logger.debug("Adding to results task="+task);
			}
			results.add(task);
			if(isFinished()) {
				stats.markFinish(); //only mark if not already marked
			}
		}
		else{ //task is a subtask
			int parentId=task.getParentId();
			if(!this.waiting.containsKey(parentId)){
				logger.error("Error. Parent task id="+parentId+" is not waiting for child tasks");
				logger.error("Dropping task id="+task);
				return;
			}
			
			Task<T> parent=waiting.get(parentId);
			if(!parent.setFinishedChild(task)){
				logger.error("Parent did not recognize child task. Dropping task id="+task);
				return;
			}
			
			//If this was the last subtask, then the parent is ready for execution
			if(parent.isReady()){
				if(logger.isDebugEnabled()){
					logger.debug("Parent taskId="+parent.getId() +" is ready");
				}
				if(waiting.remove(parent)==null){
					logger.error("Error, parent not waiting when it should have been.");
				}
				parent.getStats().exitWaitingState();
				ready.add(parent);
			}
		}
	}//if its a child task, we update it as a finished task
	
	private void updateTask(Task<T> task){
		
		//logger.debug("Unfinished taskId="+task);
		if(task.hasReadyChildTask()){
			while(task.hasReadyChildTask()){
				Task<T> child=task.getReadyChild();
				if(logger.isDebugEnabled()){
					logger.debug("Child taskId="+child.getId() +" is ready");
				}
				ready.add(child); //child will have more priority than uncles
			}
			if(logger.isDebugEnabled()){
				logger.debug("Parent Task taskId="+task.getId() +" is waiting");
			}				
			waiting.put(task,task); //the parent task will wait for it's subtasks
			return;
		}
		else{
			if(logger.isDebugEnabled()){
				logger.debug("Task taskId="+task.getId() +" is ready");
			}
			ready.add(task);
		}
	}//method
	
	public synchronized boolean isFinished(){

		return ready.isEmpty() && processing.isEmpty();
	}

	public synchronized boolean hasResults(){
		return !results.isEmpty();
	}

	public synchronized int getReadyQueueLength() {
		return ready.size();
	}
	
	public synchronized StatsGlobalImpl getStatsGlobal() {
		stats.setQueueLengths(ready.size(), processing.size(), 
								waiting.size(), results.size());
		return stats;
	}
	
	private void deleteTaskFamilyFromQueues(Task<T> blackSheepTask){
		
		//1. Put the root tasks in the results queue
		Task<T> root;
		try {
			root = getRootTask(blackSheepTask);
			root.setException(blackSheepTask.getException());
			results.add(root);
		} catch (PanicException e) {
			kernelPanic(e); //panic if can not get root task
			return;
		}
		
		//2. Delete ready family tasks
		for(Task<T> task:ready){
			if(task.getFamilyId()==blackSheepTask.getFamilyId()){
				ready.remove(task);
				task.getStats().exitReadyState();
			}
		}
		
		//3. Delete waiting family tasks
		Enumeration<Task<T>> enumeration = waiting.elements();
		while(enumeration.hasMoreElements()){
			Task<T> task = enumeration.nextElement();
			if(task.getFamilyId()==blackSheepTask.getFamilyId()){
				waiting.remove(task);
				task.getStats().exitWaitingState();
			}
		}
		
		//4. Mark family tasks in the processing queue as tainted.
		enumeration = processing.elements();
		while(enumeration.hasMoreElements()){
			Task<T> task = enumeration.nextElement();
			if(task.getFamilyId()==blackSheepTask.getFamilyId()){
				task.setTainted(true);
			}
		}
	}
	
	/**
	 * Looks for the root task of this task in the different
	 *  internal queues. If the root tasks is found, then the
	 *  the task is deleted from the queue and returned. 
	 * 
	 *  1. It's self. (The parameter might be it's own root task)
	 *  2. The waiting queue (Most likely place to find the root task)
	 *  3. The processing queue (It is an error to find it here)
	 *  4. The ready queue (It is an even bigger error to find it here)
	 *  5. The results queue (It is a very big error to find it here).
	 * 
	 * @param task the root tasks associated with this task
	 * @return The root task (if found), or null if it was not found.
	 * @throws PanicException If the root task is found where it shouldn't be.
	 * 
	 */
	private Task<T> getRootTask(Task<T> task) throws PanicException{
		if(task.isRootTask()) return task;
		
		if(this.waiting.contains(task.getFamilyId())){
			Task<T> root = waiting.remove(task.getFamilyId());
			root.getStats().exitWaitingState();
			return root;
		}
		
		if(this.processing.contains(task.getFamilyId())){
			throw new PanicException("Error, root taskId="
					+task.getFamilyId()+" found in processing queue");
		}
		
		for(Task<T> r:ready){
			if(r.getId()==task.getFamilyId()){
				throw new PanicException("Error, root taskId="
						+task.getFamilyId()+" found in ready queue");
			}
		}
		
		for(Task<T> r:results){
			if(r.getId()==task.getFamilyId()){
				throw new PanicException("Error, root taskId="
						+task.getFamilyId()+" found in results queue");
			}	
		}
		
		return null;
	}
	
	private void kernelPanic(PanicException e){
		logger.error("Kernel Panic:"+e.getCause());
		this.panicException=e;
		
		notifyAll();
	}
}//class