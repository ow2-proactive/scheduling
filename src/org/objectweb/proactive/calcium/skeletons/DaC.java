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
package org.objectweb.proactive.calcium.skeletons;

import java.util.Vector;

import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.calcium.muscle.Condition;
import org.objectweb.proactive.calcium.muscle.Conquer;
import org.objectweb.proactive.calcium.muscle.Divide;
import org.objectweb.proactive.calcium.statistics.Timer;

/**
 * This skeleton represents Divide and Conquer parallelism (data parallelism).
 * To function, a Divide, Condition, and Conquer objects must
 * be passed as parameter.
 * 
 * If the Condition is met, a Task will be divided using the Divide object. 
 * If the Condition is not met, the child skeleton will be executed.
 * If the task has subchilds, then the Conquer object will be used to conquer
 * the child tasks into the parent task.
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <P>
 */
public class DaC<P,R> implements Skeleton<P,R>, Instruction<P,P> {

	Divide<P,P> div;
	Conquer<R,R> conq;
	Condition<P> cond;
	Skeleton<P,R> child;
	
	/**
	 * Creates a Divide and Conquer skeleton structure
	 * @param div Divides a task into subtasks
	 * @param cond True if divide should be applied to the task. False if it should be solved. 
	 * @param child The skeleton that should be applied to the subtasks.
	 * @param conq Conqueres the computed subtasks into a single task.
	 */
	public DaC(Divide<P, P> div, Condition<P> cond, Skeleton<P,R> child, Conquer<R, R> conq){
	
		this.div=div;
		this.cond=cond;
		this.child=child;
		this.conq=conq;
	}
	
	public Vector<Instruction<?,?>> getInstructionStack() {

		Vector<Instruction<?,?>> v= new Vector<Instruction<?,?>>();
		v.add(this);
		
		return v;
	}

	public Task<P> compute(Task<P> t) throws EnvironmentException{

		Timer timer = new Timer();
		boolean evalCondition=cond.evalCondition(t.getObject());
		timer.stop();
		t.getStats().getWorkout().track(cond, timer);
		
		if(evalCondition){ //Split the task if required
			t.pushInstruction(new ConquerInst(conq));
			t.pushInstruction(new DivideInst(div, this));
			return t;
		}
		else{ //else execute the child skeleton
			//Append the child skeleton code to the stack
			Vector<Instruction<?,?>> currentStack = t.getStack();
			currentStack.addAll(child.getInstructionStack());
			t.setStack(currentStack);
		}
		return t;
	}
	
	public String toString(){
		return "DaC";
	}
	
	public Task<?> computeUnknown(Task<?> t) throws Exception {
		return compute((Task<P>) t);
	}
	
	static class ConquerInst<R> implements Instruction<R,R> {

		private Conquer<R,R> conq;

		public ConquerInst(Conquer<R,R> conq) {
			this.conq = conq;
		}
		
		public Task<R> compute(Task<R> parent) throws Exception {
			/**
			 * We get the result objects from the child
			 * and then we execute the conquer.
			 * Finally, we create a rebirth task
			 * of the parent with the result of the conquer.
			 */
			Vector<R> childResults = new Vector<R>();
			
			while(parent.hasFinishedChild()){
				
				Task<R> child = parent.getFinishedChild();
				childResults.add(child.getObject());
			}

			if(childResults.size() <=0 ){
				String msg="Can't conquer less than one child parameter!";
				logger.error(msg);
				throw new MuscleException(msg);
			}
			
			Timer timer = new Timer();
	 		R resultObject=conq.conquer(childResults);
	 		timer.stop();
	 		Task<R> resultTask=parent.reBirth(resultObject);
	 		
	 		resultTask.getStats().getWorkout().track(conq, timer);
			return resultTask;
		}

		public Task<?> computeUnknown(Task<?> t) throws Exception {
			return compute((Task<R>) t);
		}	
	}
	
	/**
	 * This class is an instruction that will perform a divition of one task,
	 * and return the task with its correponding subtasks.
	 * 
	 * @author The ProActive Team (fviale)
	 *
	 * @param <T> The type of the parameter held by the tasks.
	 */
	static class DivideInst<T> implements Instruction<T,T> {
		
		private Divide<T,T> div;
		private Skeleton<T,?> skel;

		public DivideInst(Divide<T,T> div, Skeleton<T, ?> skel) {
			this.div = div;
			this.skel=skel;
		}

		public Task<T> compute(Task<T> parent) throws Exception {
			/*
			 * We pass the t.object to the div. Each result
			 * of divide will be then encapsulated by a Task, and 
			 * the Tasks will be held in an array  
			 */	
			Timer timer = new Timer();
			Vector<T> childObjects=div.divide(parent.getObject());
			timer.stop();
			
			if(childObjects.size()<=0){
				String msg="Parameter was divided into less than 1 part.";
				logger.error(msg);
				throw new MuscleException(msg);
			}
			
			for(T o:childObjects){
				Task<T> child = new Task<T>(o);
				child.setStack(skel.getInstructionStack()); //To do divide or execute in the child
				parent.addReadyChild(child); //parent holds it's children
			}

			if(logger.isDebugEnabled()){
				logger.debug("Task "+parent+" divided into "+childObjects.size()+" parts");
			}
			
			parent.getStats().getWorkout().track(div, timer);
			return parent;
		}

		public Task<?> computeUnknown(Task<?> t) throws Exception {
			return compute((Task<T>) t);
		}
	}
}
