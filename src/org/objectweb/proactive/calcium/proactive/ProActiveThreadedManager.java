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
package org.objectweb.proactive.calcium.proactive;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.calcium.Interpreter;
import org.objectweb.proactive.calcium.Skernel;
import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

public class ProActiveThreadedManager extends AbstractProActiveManager {

	ExecutorService threadPool;

	public ProActiveThreadedManager(Node nodes[]){		
		super(nodes);
	}
	
	public ProActiveThreadedManager(VirtualNode vn){
		super(vn);
	}
	
	public ProActiveThreadedManager(String descriptorPath, String virtualNodeName){
		super(descriptorPath,virtualNodeName);
	}

	@Override
	public <T> Skernel<T> start(Skernel<T> skernel) {
		logger.info("ProActive skeleton manager is using "+nodes.length+" nodes");
		threadPool=Executors.newCachedThreadPool();
		try {
			for(int i=0;i<nodes.length;i++){
				Interpreter interp=(Interpreter)ProActive.newActive(Interpreter.class.getName(),null,nodes[i]);
				threadPool.submit(new ProActiveCallableInterpreter<T>(skernel,interp));
			}
		} catch (Exception e) {
			logger.error("Error, unable to create interpreter active objects");
			e.printStackTrace();
		}
		return skernel;
	}
	
	@Override
	public void finish() {
		super.finish();
		if(threadPool != null){
			threadPool.shutdownNow();
		}
	}
	
	protected class ProActiveCallableInterpreter<T> implements Callable<Task<T>>{
			
		Interpreter interp;
		Skernel<T> skernel;
		
		public ProActiveCallableInterpreter(Skernel<T> skernel, Interpreter interp){
			this.skernel=skernel;
			this.interp=interp;
		}

		public Task<T> call() throws Exception {
			
			Task<T> task = skernel.getReadyTask();
			
			while(task!=null){
				task = interp.interpret(task);
				
				ProActive.waitFor(task); //Wait for the future

				skernel.putTask(task);
				task = skernel.getReadyTask();
			}
			return task;
		}
	}
}