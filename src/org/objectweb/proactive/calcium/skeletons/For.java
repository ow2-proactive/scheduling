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
package org.objectweb.proactive.calcium.skeletons;

import java.util.Vector;

import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.interfaces.Instruction;
import org.objectweb.proactive.calcium.interfaces.Skeleton;

public class For<T> implements Instruction<T>, Skeleton<T> {

	Skeleton<T> child;
	int times;
	
	public For(int times, Skeleton<T> child){
		this.child=child;
		this.times=times;
	}
	
	public Vector<Instruction<T>> getInstructionStack() {
		Vector<Instruction<T>> v = new Vector<Instruction<T>>();
		v.add(this);
		return v;
	}
	
	public Task<T> compute(Task<T> task) throws Exception {
		
		if(times > 0){
			//Get Child stack
			Vector<Instruction<T>> childStack=child.getInstructionStack();
			
			//Add the For with one less time to execute
			childStack.add(0,new For<T>(times-1,child)); 
			
			Vector<Instruction<T>> taskStack = task.getStack();
			taskStack.addAll(childStack);
			task.setStack(taskStack);
		}
		return task;
	}
}
