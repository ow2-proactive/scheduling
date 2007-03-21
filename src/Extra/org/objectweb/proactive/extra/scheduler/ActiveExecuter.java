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
package org.objectweb.proactive.extra.scheduler;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;


/**
 * A genereic executer used by the TaskScheduler  to create active objects on the remote node and execute the task.
 *
 * @author walzouab
 *
 */
public class ActiveExecuter implements java.io.Serializable {
    public ActiveExecuter() {
    }

    /**
     * Empty Method that uses the active object properties to make sure the object is alive
     *
     */
    public void ping() {
    }

    /**
     * Used in case the execution must be stopped.
     * <b>Warning, use with caution, it causes the node to be killed</b>
     *
     */
    public void kill() {
        System.exit(0);
    }

    /**
     * Runs the requried task, by executing it and catching any user exceptions that result.
     * @param the task to be exuetued
     * @return An internal Result object thatr wraps the result
     */
    public InternalResult start(ProActiveTask task) {
        //sets the time from when the function starts executing
        InternalResult result = new InternalResult();
        long executionBegins = System.currentTimeMillis();
        try {
            //execute the function
            result.setProActiveTaskExecutionResult(new GenericTypeWrapper<Object>(
                    task.run()));

            //set the execution time
            result.setExecutionTime(new LongWrapper(System.currentTimeMillis() -
                    executionBegins));

            //indicate that no user exception has occured
            result.setExceptionOccured(new BooleanWrapper(false));
        } catch (Exception e) {
            //sets the time the user code executed before an exception occured
            result.setExecutionTimeBeforeException(new LongWrapper(System.currentTimeMillis() -
                    executionBegins));

            //indicates that an exception has occured
            result.setExceptionOccured(new BooleanWrapper(true));

            //attaches the resultant exception in order to be sent back to the user
            result.setProActiveTaskException(new GenericTypeWrapper<Exception>(
                    e));
        }

        return result;
    }
}
