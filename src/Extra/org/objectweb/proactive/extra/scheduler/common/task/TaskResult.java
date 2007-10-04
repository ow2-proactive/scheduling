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
package org.objectweb.proactive.extra.scheduler.common.task;

import java.io.Serializable;


/**
 * Interface representing the task result.
 * A task result can be an exception or an object that you have to cast into your own type.
 * Before getting the object it is recommended that you call the hadException() method.
 * It will tell you if an exception occured in the task that generate this result.
 *
 * @author ProActive Team
 * @version 1.0, Aug 3, 2007
 * @since ProActive 3.2
 */
public interface TaskResult extends Serializable {

    /**
     * To know if an exception has occured on this task.
     *
     * @return true if an exception occured, false if not.
     */
    public boolean hadException();

    /**
     * To get the id of the task.
     *
     * @return the id of the task.
     */
    public TaskId getTaskId();

    /**
     * To get the value of the task.
     *
     * @return the value of the task.
     */
    public Object value();

    /**
     * To get the exception of the task.
     *
     * @return the exception of the task.
     */
    public Throwable getException();
}
