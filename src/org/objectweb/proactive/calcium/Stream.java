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
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.interfaces.Instruction;
import org.objectweb.proactive.calcium.interfaces.Skeleton;
import org.objectweb.proactive.calcium.statistics.Stats;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Stream<T>{
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_KERNEL);
	
	private int streamId;
	private Facade facade;
	private Skeleton<T> skeleton;
	private int pendingTasks;
	
	private Hashtable<T, Stats> taskStats;
	protected Stream(Facade facade,  Skeleton<T> skeleton){
		
		this.streamId=(int)(Math.random()*Integer.MAX_VALUE);
		this.skeleton=skeleton;
		this.facade=facade;
		this.taskStats = new Hashtable<T, Stats>();
		this.pendingTasks=0;
	}

	/**
	 * Inputs a new T to be computed.
	 * @param param The T to be computed.
	 */
	public void input(T param){
		Vector<T> paramV= new Vector<T>(1);
		paramV.add(param);
		input(paramV);
	}
	
	/**
	 * Inputs a vector of T to be computed.
	 * @param paramV A vector containing the T.
	 */
	public void input(Vector<T> paramV){
		
		Vector<Instruction<T>> v = (Vector<Instruction<T>>) skeleton.getInstructionStack();

		//Put the parameters in a Task container
		for(T param:paramV){
			Task<T> task = new Task<T>(param);
			task.setStack(v);
			task.setStreamId(streamId);
			facade.putTask(task); //add them to the ready queue in the kernel
			pendingTasks++;
		}
	}
	
	/**
	 * This method returns the result of the computation for
	 * every inputed parameter. If no parameter is yet available
	 * this method will block. 
	 * 
	 * @return The result of the computation on a parameter, or null if there are no more
	 * parameters being computed.
	 * @throws PanicException Is thrown if a unrecoverable error takes place inside the framework.
	 * @throws MuscleException Is thrown if a functional exception happens during the execution
	 * of the skeleton's muscle.
	 */
	@SuppressWarnings("unchecked")
	public T getResult() throws PanicException, MuscleException{
		if(pendingTasks==0) return null; //waiting for no results
	
		Task<T> task =  (Task<T>) facade.getResult(streamId);
		T res= task.getObject();
		
		taskStats.put(res,task.getStats());
		pendingTasks--;
		
		return res;
	}

	/**
	 * After a T has been computed and obtained through getResult(),
	 * this method retrieves the statistics for this T.
	 * @param res The T obtained through getResult().
	 * @return The statistics of this T, or null if the parameter is unknown.
	 */
	public Stats getStats(T res) {
		return this.taskStats.get(res);
	}
}