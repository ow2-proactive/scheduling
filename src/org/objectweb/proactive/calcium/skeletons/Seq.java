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
import org.objectweb.proactive.calcium.exceptions.SchedulingException;
import org.objectweb.proactive.calcium.interfaces.Execute;
import org.objectweb.proactive.calcium.interfaces.Instruction;
import org.objectweb.proactive.calcium.interfaces.Skeleton;


/**
 * The Seq skeleton is a wrapper for the user inputed
 * sequential code. This class allows the code to be nested
 * inside other skeletons.
 * 
 * @author The ProActive Team (mleyton)
 *
 * @param <T>
 */
public class Seq<T> implements Skeleton<T>, Instruction<T> {

	Execute<T> secCode;
	
	public Seq(Execute<T> secCode){
		this.secCode=secCode;
	}
	
	public Vector<Instruction<T>> getInstructionStack() {
		
		Vector<Instruction<T>> v=new Vector<Instruction<T>>();
		v.add(this);
		return v;
	}

	public Task<T> compute(Task<T> t) throws RuntimeException, SchedulingException {
		T resultObject=secCode.execute(t.getObject());
		Task<T> resultTask= t.reBirth(resultObject);
		resultTask.setStack(t.getStack());
		return resultTask; 
	}
	
	public String toString(){
		return "Seq("+this.secCode.getClass()+")";
	}
}
