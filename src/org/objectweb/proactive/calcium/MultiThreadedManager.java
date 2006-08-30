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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Example of a manager that uses threads to
 * execute the skeleton program.
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <T>
 */
public class MultiThreadedManager extends ResourceManager{
	
	ExecutorService threadPool;  //Thread pool
	int numThreads; 			 //Maximum number of threads to be used
	
	public MultiThreadedManager(int numThreads){
		
		this.threadPool=Executors.newFixedThreadPool(numThreads);
		this.numThreads=numThreads;
	}
	
	@Override
	public <T> Skernel<T> start(Skernel<T> skernel) {
		
		for(int i=0;i<numThreads;i++){
			threadPool.submit(new CallableInterpreter<T>(skernel));
		}
		return skernel;
	}
	
	@Override
	public void finish(){
		threadPool.shutdownNow();
	}
	
	/**
	 * Callable class for invoking the interpret method in a new thread (processor).
	 * 
	 */
	protected class CallableInterpreter<T> extends Interpreter implements Callable<Task<T>>{
		
		Skernel<T> skernel;
		
		public CallableInterpreter(Skernel<T> skernel){
			this.skernel=skernel;
		}

		public Task<T> call() throws Exception {
			
			Task<T> task = skernel.getReadyTask();
			
			while(task!=null){
				task = super.interpret(task);
				skernel.putTask(task);
				task = skernel.getReadyTask();
			}
			return task;
		}
	}
}
