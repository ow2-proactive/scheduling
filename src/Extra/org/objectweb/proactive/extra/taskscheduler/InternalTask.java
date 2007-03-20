/*
 * ################################################################
 *
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
package org.objectweb.proactive.extra.taskscheduler;


/**
 * Internal task object managed by the scheduler. holds times required plus the result and also the executer object and node in addition to
 * @author walzouab
 *
 */
public class InternalTask implements java.io.Serializable {
    Status status;
    private ProActiveTask userTask;
    private String taskID;
    private String userName;
    public NodeNExecuter nodeNExecuter;
    InternalResult result;

    /*
     * a negative value indicates invalidity
     */
    long timeCreated;
    long timeInsertedInQueue;
    long timeScheduled;
    long timeFinished;

    /**
     * The parameters passed are private with only getters and no setters available , so once they are created they cannot be changed.
     * @param userTask
     * @param taskID
     * @param userName
     */
    public InternalTask(ProActiveTask userTask, String taskID, String userName) {
        this.status = Status.NEW;
        this.userTask = userTask;
        this.taskID = taskID;
        this.userName = userName;
        this.timeCreated = System.currentTimeMillis();

        timeInsertedInQueue = -1;
        timeScheduled = -1;
        timeFinished = -1;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getUserName() {
        return userName;
    }

    public ProActiveTask getUserTask() {
        return userTask;
    }

    public Info getTaskINFO() {
        String nodeURL;
        if (nodeNExecuter != null) {
            nodeURL = nodeNExecuter.node.getNodeInformation().getURL();
        } else {
            nodeURL = "unknown";
        }

        return new Info(status, taskID, userName, nodeURL, timeCreated,
            timeInsertedInQueue, timeScheduled, timeFinished);
    }
}
