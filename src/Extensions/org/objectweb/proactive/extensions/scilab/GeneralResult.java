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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.scilab;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This is a container for Scilab or Matlab results
 * @author The ProActive Team
 *
 */
@PublicAPI
public interface GeneralResult extends Serializable {
    public static final int SUCCESS = 0;
    public static final int ABORT = 1;

    /**
     * Retrieve the final state of the task SUCCESS or ABORT
     * @return state
     */
    public abstract int getState();

    /**
     * Set the final state of the task SUCCESS or ABORT
     * @param state new state
     */
    public abstract void setState(int state);

    /**
     * Retrieve the total execution time of the task
     * @return execution time
     */
    public abstract long getTimeExecution();

    /**
     * Set the total execution time of the task
     * @param timeExecution
     */
    public abstract void setTimeExecution(long timeExecution);

    /**
     * Retrieve the id of the task associated to this result
     * @return id
     */
    public abstract String getId();

    /**
     * Add a result data to this container
     * @param data scilab or matlab data
     */
    public void add(AbstractData data);

    /**
     * Get the list of all data in this container
     * @return list of data
     */
    public List<AbstractData> getList();

    /**
     * Retrieve a data of the given name
     * @param name name of the data (Matlab or Scilab)
     * @return the data of the given name
     */
    public AbstractData get(String name);

    /**
     * Set the message associated with this task
     * @param message
     */
    public void setMessage(String message);

    /**
     * Does the result have a message associated with it
     * @return answer
     */
    public boolean hasMessage();

    /**
     * Returns the message associated with this result
     * @return message
     */
    public String getMessage();

    /**
     * Did the task launch an exception?
     * @return answer
     */
    public boolean isException();

    /**
     * Return the exception thrown by the task
     * @return exception thrown
     */
    public Exception getException();

    /**
     * Set the exception thrown by the task
     * @param exp
     */
    public void setException(Exception exp);
}
