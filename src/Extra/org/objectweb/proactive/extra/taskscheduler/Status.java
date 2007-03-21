/*
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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



package org.objectweb.proactive.taskscheduler;

/**
 * 
 * The status of each task submitted by the user
 * @author walzouab
 *
 */
public enum Status implements java.io.Serializable {
	/**
	 * 
	 * The task doesnt exist or The task was submitted by the user but hasnt been added to the queue yet
	 */
	NEW,
	/**
	 * The task is in the scheduler queue
	 */
	QUEUED,
	/**
	 * The task is executing
	 */
	RUNNNING,
	/**
	 * The task has finished execution
	 */
	FINISHED,
	/**
	 * 
	 * the task has failed during execution due to an iternal error and will be rescheduled again
	 */
	FAILED,
	/**
	 * 
	 * the task was killed
	 */
	KILLED,
	/**
	 * An Error has occured
	 */
	ERROR

}
