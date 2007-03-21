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
 * information about the task to be returned to the user
 * PS. notice that negative values are assigned to invalid longs and unknown is assigned to invalid strings
 * @author walzouab
 *
 */
public class Info implements java.io.Serializable{
	
	public Info(Status status, String taskID, String userName, String nodeURL, long timeCreated, long timeInsertedInQueue, long timeScheduled, long timeFinished,int failures) {
		this.status = status;
		this.taskID = taskID;
		this.userName = userName;
		this.nodeURL = nodeURL;
		this.timeCreated = timeCreated;
		this.timeInsertedInQueue = timeInsertedInQueue;
		this.timeScheduled = timeScheduled;
		this.timeFinished = timeFinished;
		this.failures=failures;
	}
	/**
	 * priactive no arg constuctor
	 *
	 */
	public Info(){}
private	Status status;

	private String taskID;
	private String userName; 

 private String nodeURL;

 

 private long timeCreated;
 private long timeInsertedInQueue;
 private long timeScheduled;
 private long timeFinished;
 private int failures;
public String getNodeURL() {
	return nodeURL;
}
public Status getStatus() {
	return status;
}
public String getTaskID() {
	return taskID;
}
public long getTimeCreated() {
	return timeCreated;
}
public long getTimeFinished() {
	return timeFinished;
}
public long getTimeInsertedInQueue() {
	return timeInsertedInQueue;
}
public long getTimeScheduled() {
	return timeScheduled;
}
public String getUserName() {
	return userName;
}
public int getFailures() {
	return failures;
}

}
