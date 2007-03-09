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

import java.util.Collections;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.muscle.Conquer;
import org.objectweb.proactive.calcium.statistics.Timer;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

class ConquerInst<Y, R> implements Instruction<Y,R> {
	static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
	
	private Conquer<Y, R> conq;

	protected ConquerInst(Conquer<Y, R> conq) {
		this.conq = conq;
	}
	
	public Task<R> compute(Task<Y> parent) throws Exception {
		
		/**
		 * We get the result objects from the child
		 * and then we execute the conquer.
		 * Finally, we create a rebirth task
		 * of the parent with the result of the conquer.
		 */
		//Vector<Y> childResults = new Vector<Y>();
		
		/*
		while(parent.hasFinishedChild()){
			Task<Y> child = parent.getFinishedChild();
			childResults.add(child.getObject());
		}
        */
		
		Vector<Task<Y>> childTasks = parent.getFinishedChildren();
		
		if(childTasks.size() <=0 ){
			String msg="Can't conquer less than one child parameter!";
			logger.error(msg);
			throw new MuscleException(msg);
		}

		Collections.sort(childTasks);
		Vector<Y> childResults = new Vector<Y>();
		for(Task<Y> t:childTasks){
			childResults.add(t.getObject());
		}
		
		Timer timer = new Timer();
 		R resultObject=conq.conquer(childResults);
 		timer.stop();
 		Task<R> resultTask=parent.reBirth(resultObject);
 		
 		resultTask.getStats().getWorkout().track(conq, timer);
		return resultTask;
	}
}
