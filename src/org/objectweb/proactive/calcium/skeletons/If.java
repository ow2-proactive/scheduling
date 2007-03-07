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
 * This class provides If conditioning. 
 * Depending on the result of the Condition, either
 * one child skeletal structure will be executed, or the other.
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <P>
 */
public class If<P,R> implements Skeleton<P,R>, Instruction<P,P> {

	Condition<P> cond;
	Skeleton<P,?> ifChild, elseChild;
	public If(Condition<P> cond, Skeleton<P,R> ifChild, Skeleton<P,R> elseChild){
	
		this.cond=cond;
		this.ifChild=ifChild;
		this.elseChild=elseChild;
	}
	
	public Vector<Instruction<?,?>> getInstructionStack() {

		Vector<Instruction<?,?>> v = new Vector<Instruction<?,?>>();
		v.add(this);
		return v;
	}

	
	public Task<P> compute(Task<P> t) throws Exception{
		
		Vector<Instruction<?,?>> childStack;
		Timer timer = new Timer();
		boolean evalCondition= cond.evalCondition(t.getObject());
		timer.stop();
		
		if(evalCondition){
			childStack= ifChild.getInstructionStack();
		}
		else{
			childStack= elseChild.getInstructionStack();
		}
		
		Vector<Instruction<?,?>> taskStack = t.getStack();
		taskStack.addAll(childStack);
		t.setStack(taskStack);
		t.getStats().getWorkout().track(cond, timer);
		
		return t;
	}
	
	public Task<?> computeUnknown(Task<?> t) throws Exception {
		return compute((Task<P>) t);
	}
}
