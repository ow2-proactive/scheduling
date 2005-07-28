/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.p2p.api.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * @author Alexandre di Costanzo
 *
 * Created on May 31, 2005
 */
public abstract class TaskGenerator implements Serializable{
	private List toBeAssignedTasks;
	private List pendingTasks;
	private List results;
	
	public void addResult(Result result) {
		this.pendingTasks.remove(result.getTaskIndex());
		this.results.add(result);
		this.generateTaskFromResult(result);
	}
	
	public void addResults(Collection results) {
		Iterator it = results.iterator();
		while (it.hasNext()) {
			Result current = (Result)it.next();
			this.addResult(current);
		}
	}
	
	public Task getTask() {
		if (this.toBeAssignedTasks.size() == 0) {
			// Re-assign a pending task
			if (this.pendingTasks.size() != 0) {
				// TODO
				return (Task) null;
			} else {
				// TODO Nor more task => the computation is over
			// TODO return new EmptyTask();
				return (Task) null;
			}
		} else {
			Task task = (Task) this.toBeAssignedTasks.remove(0);
			this.pendingTasks.add(task);
			int index = this.pendingTasks.indexOf(task);
			task.setTaskManagerIndex(index);
			return task;
		}
	}
	
	public Collection getTasks(int size) {
		// TODO
		return null;
	}
	
	public abstract void initTaskGenerator(Object [] args);
	
	public abstract void generateTaskFromResult(Result result);
	
	public abstract void endTaskGenerator();
	
}
