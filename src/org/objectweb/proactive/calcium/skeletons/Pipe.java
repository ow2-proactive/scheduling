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

import java.util.Arrays;
import java.util.List;

import java.util.Vector;

import org.objectweb.proactive.calcium.interfaces.Instruction;
import org.objectweb.proactive.calcium.interfaces.Skeleton;

/**
 * The Pipe skeleton represents staged computation.
 * A Pipe will execute each skeleton in sequence of the next.
 * 
 * @author The ProActive Team (mleyton)
 */
public class Pipe<T,R> implements Skeleton<T,R> {
	
	Vector<Skeleton<?,?>> stages;
	
	public <X> Pipe(Skeleton<T,X> child1, Skeleton<X,R> child2){
		
		stages = new Vector<Skeleton<?,?>>();
		
		stages.add(child1);
		stages.add(child2);
	}
	
	public <X,Y,Z> Pipe(Skeleton<T,X> child1, Skeleton<X,Y> child2, Skeleton<Y,Z> child3, Skeleton<Z,R> child4){
		
		stages = new Vector<Skeleton<?,?>>();
		
		stages.add(child1);
		stages.add(child2);
		stages.add(child3);
		stages.add(child4);
	}
	

	/*public Pipe(Skeleton<T>... args){
		this(Arrays.asList(args));
	}
	
	public Pipe(List<Skeleton<T>> stages){
		if(stages.size() <=0 ) {
			throw new IllegalArgumentException("Pipe must have at least one stage");
		}
		this.stages = new Vector<Skeleton<T>>();
		this.stages.addAll(stages);
	}	*/
	
	public Vector<Instruction<?,?>> getInstructionStack() {

		Vector<Instruction<?,?>> instruction = new Vector<Instruction<?,?>>();
		
		//the last go into the stack first
		for(int i=stages.size()-1;i>=0;i--){
			instruction.addAll(stages.get(i).getInstructionStack());
		}	
		
		/*A 2 stage Pipe. Only here for hystorical/educational purposes.

		Vector<Instruction> v1= child1.getInstructionStack();
		Vector<Instruction> v2= child2.getInstructionStack();
		
		//execute first v1 and then v2
		v2.addAll(v1);
		
		return v2;
		*/
		return instruction;
	}
}
