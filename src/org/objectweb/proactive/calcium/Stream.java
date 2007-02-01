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

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.futures.Future;
import org.objectweb.proactive.calcium.futures.FutureImpl;
import org.objectweb.proactive.calcium.interfaces.Instruction;
import org.objectweb.proactive.calcium.interfaces.Skeleton;

import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Stream<T>{
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_KERNEL);
	
	private int streamId;
	private Facade facade;
	private Skeleton<T> skeleton;
	private int lastPriority;
	
	protected Stream(Facade facade,  Skeleton<T> skeleton){
		
		this.streamId=(int)(Math.random()*Integer.MAX_VALUE);
		this.skeleton=skeleton;
		this.facade=facade;
		this.lastPriority=0;
	}

	/**
	 * Inputs a new T to be computed.
	 * @param param The T to be computed.
	 * @throws PanicException 
	 * @throws InterruptedException 
	 */
	public Future<T> input(T param) throws InterruptedException, PanicException{
		
		//Put the parameters in a Task container
		Task<T> task = new Task<T>(param);

		Vector<Instruction<T>> instructionStack = (Vector<Instruction<T>>) skeleton.getInstructionStack();
		task.setStack(instructionStack);
		task.setStreamId(streamId);
		task.setPriority(lastPriority--);
		
		FutureImpl<T> future = new FutureImpl<T>(task.getId());
		facade.putTask(task, future);

		return (Future<T>)future;
	}
	
	/**
	 * Inputs a vector of T to be computed.
	 * @param paramV A vector containing the T.
	 * @throws PanicException 
	 * @throws InterruptedException 
	 */
	public Vector<Future<T>> input(Vector<T> paramV) throws InterruptedException, PanicException{
		
		Vector<Future<T>> vector= new Vector<Future<T>>(paramV.size());
		for(T param:paramV)
			vector.add(input(param));
		
		return vector;
	}
}