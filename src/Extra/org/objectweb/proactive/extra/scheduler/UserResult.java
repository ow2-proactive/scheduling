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

/**
 *
 *
 * @author walzouab
 *
 */
package org.objectweb.proactive.extra.scheduler;

import org.objectweb.proactive.extra.scheduler.exception.UserException;


public class UserResult implements java.io.Serializable {
    private String taskID;
    private UserScheduler userScheduler;
    private boolean resultAvailable;
    private InternalResult result;
    private String userName;

    public UserResult(String ID, String user, UserScheduler API) {
        resultAvailable = false;
        taskID = ID;
        userName = user;
        userScheduler = API;
    }

    //warning this function is blocking
    public Object getResult() throws UserException, Exception {
        //if the result isnt already available fetch result
        if (!resultAvailable) {
            try {
                result = userScheduler.getResult(taskID, userName);
            } catch (Exception e) {
                throw new UserException("Scheduler has been shut down");
            }

            resultAvailable = true;
        }

        if (!result.getErrorMessage().equals("")) {
            throw new UserException(result.getErrorMessage());
        }

        //checks for exceptions if occured throws it, otherwise returns the result		
        if (result.getExceptionOccured().booleanValue()) {
            throw result.getProActiveTaskException().getObject();
        } else {
            return result.getProActiveTaskExecutionResult().getObject();
        }
    }

    //a non blocking function that returns if the task has finished execution
    public boolean isFinished() throws UserException {
        if (resultAvailable) {
            return true;
        } else {
            try {
                return userScheduler.isFinished(taskID).booleanValue();
            } catch (Exception e) {
                throw new UserException("Scheduler has been shut down");
            }
        }
    }

    /**
     * Returns execution time. It will return zero if get result hasnt bee called yet
     * @return execution time on the node
     */
    public long getExecutionTime() throws UserException {
        if (!this.isFinished()) {
            return 0;
        }

        if (!result.getErrorMessage().equals("")) {
            throw new UserException(result.getErrorMessage());
        }

        return result.getExecutionTime().longValue();
    }

	public String getTaskID() {
		return taskID;
	}
}
