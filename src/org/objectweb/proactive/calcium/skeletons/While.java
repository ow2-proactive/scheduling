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
import org.objectweb.proactive.calcium.muscle.Condition;
import org.objectweb.proactive.calcium.statistics.Timer;

/**
 * The while skeleton represents conditioned iteration.
 * The child skeleton will be executed while the Condition
 * holds true.
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <P>
 */
public class While<P> implements Instruction<P,P>, Skeleton<P,P> {

	Condition<P> cond;
	Skeleton<P,P> child;
	
	public While(Condition<P> cond, Skeleton<P,P> child){
		this.cond=cond;
		this.child=child;
	}
	
	public Vector<Instruction<?,?>> getInstructionStack() {
		Vector<Instruction<?,?>> v = new Vector<Instruction<?,?>>();
		v.add(this);
		return v;
	}
	
	public Task<P> compute(Task<P> task) throws Exception{
		Timer timer = new Timer();
		boolean evalCondition=cond.evalCondition(task.getObject());
		timer.stop();
		task.getStats().getWorkout().track(cond, timer);
		
		if(evalCondition){
			//Get Child stack
			Vector<Instruction<?,?>> childStack=child.getInstructionStack();
			
			//Add me to evaluate while condition after executing child
			childStack.add(0,this); 
			
			//Add new elements to the task's stack
			Vector<Instruction<?,?>> taskStack=task.getStack();
			taskStack.addAll(childStack);
			task.setStack(taskStack);
		}

		return task;
	}

	public Task<?> computeUnknown(Task<?> t) throws Exception {
		return compute((Task<P>) t);
	}
}