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

import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.exceptions.SchedulingException;
import org.objectweb.proactive.calcium.interfaces.Conquer;
import org.objectweb.proactive.calcium.interfaces.Divide;
import org.objectweb.proactive.calcium.interfaces.Skeleton;

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
public class Map<T> extends DaC<T> {
	
	public Map(Divide<T> div, Skeleton<T> child, Conquer<T> conq){
		
		super(div, null,child,conq);
	}
	
	//Override parent method to avoid using the condition
	public Task<T> compute(Task<T> t) throws SchedulingException {
		
		//Split the task if not already splitted
		if(!t.hasFinishedChild()){
			return divide(t);
		}
		
		//else conquer the subtask
		return conquer(t);
	}

}
