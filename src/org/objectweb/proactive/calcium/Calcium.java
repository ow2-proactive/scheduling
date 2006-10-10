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

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.calcium.exceptions.PanicException;
import org.objectweb.proactive.calcium.exceptions.ParameterException;
import org.objectweb.proactive.calcium.interfaces.Instruction;
import org.objectweb.proactive.calcium.interfaces.Skeleton;
import org.objectweb.proactive.calcium.statistics.Stats;
import org.objectweb.proactive.calcium.statistics.StatsGlobal;

/**
 * This class corresponds to the entry point of the skeleton framework.
 * 
 * In order to instantiate this class, a resource Manager must be provided.
 * This Manager must extend the AbstractManager class. As a result, the skeleton
 * kernel can be used with different Managers, for example: Monothreaded, Multihreaded
 * or Distributed (ProActive).
 * 
 * Also, a skeleton structure must be provided. This skeleton represents the structured
 * code that will be executed for each parameter.
 * 
 * @author The ProActive Teammleyton
 *
 * @param <T> The type of the parameter used.
 */
public class Calcium<T>{

	private ResourceManager manager;
	private Skernel<T> skernel;
	private Skeleton<T> skeleton;

	private Hashtable<T, Stats> taskStats;
	public Calcium(ResourceManager manager, Skeleton<T> skeleton){
		this.manager=manager;
		
		this.skeleton=skeleton;
		this.skernel=new Skernel<T>();
		this.taskStats = new Hashtable<T, Stats>();
	}

	public void inputParameter(T param){
		Vector<T> paramV= new Vector<T>(1);
		paramV.add(param);
		inputParameters(paramV);
	}
	
	public void inputParameters(Vector<T> paramV){
		
		Vector<Instruction<T>> v = (Vector<Instruction<T>>) skeleton.getInstructionStack();

		//Put the parameters in a Task container
		for(T param:paramV){
			Task<T> task = new Task<T>(param);
			task.setStack(v);
			skernel.putTask(task); //add them to the ready queue in the kernel
		}
	}
	
	public T getResult() throws PanicException, ParameterException{
		
		if(!skernel.hasResults() && skernel.isFinished()) {			
			return null;
		}
		
		Task<T> taskResult = skernel.getResult();
		
		//TODO Temporary ProActive generics bug workaround 
		//This is the supelec trick
		taskResult=(Task<T>)ProActive.getFutureValue(taskResult);

		if(skernel.isFinished()){
			manager.finish();
		}
		
		T res= taskResult.getObject();
		this.taskStats.put(res,taskResult.getStats());
		
		return res;
	}
	
	public void eval(){
		skernel=manager.start(skernel);
	}

	public StatsGlobal getStatsGlobal() {
		return skernel.getStatsGlobal();
	}

	public Stats getStats(T res) {
		return this.taskStats.get(res);
	}
}