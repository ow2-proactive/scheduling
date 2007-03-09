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

import java.util.Stack;
import java.util.Vector;

import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.calcium.muscle.Conquer;
import org.objectweb.proactive.calcium.muscle.Divide;

/**
 * Map is only a special case of Divide and Conquer, and therfore
 * represents data parallelism.
 * 
 * A taks is Divided once into subtaks (without evaluating a condition),
 * the subtasks are then executed using the child skeleton, and then the 
 * results are conquered using the Conquer object.
 * 
 * @author The ProActive Team (mleyton)
 *
 */
public class Map<P,R> implements Skeleton<P,R>, Instruction<P,P> {
	
	Divide<P,?> div;
	Skeleton child;
	Conquer<?,R> conq;
	ConquerInst<?,R> conquerInst;
	DivideSIMD<?,R> divideInst;
	
	@SuppressWarnings("unchecked")
	public <X,Y> Map(Divide<P,X> div, Skeleton<X,Y> child, Conquer<Y,R> conq){
		
		this.div=div;
		this.child=child;
		this.conq = conq;

		conquerInst = new ConquerInst<Y,R>(conq);
		divideInst  = new DivideSIMD(div, child.getInstructionStack());
	}
	
	public Stack<Instruction> getInstructionStack() {

		Stack<Instruction> v= new Stack<Instruction>();
		v.add(this);
		
		return v;
	}
	
	public Task<P> compute(Task<P> t) throws EnvironmentException {
		
		t.pushInstruction(conquerInst);
		t.pushInstruction(divideInst);
		return t;
	}

	public String toString(){
		return "Map";
	}
}
