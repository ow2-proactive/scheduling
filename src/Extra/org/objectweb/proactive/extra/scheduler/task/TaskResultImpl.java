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
package org.objectweb.proactive.extra.scheduler.task;

import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;


/**
 * Class representing the task result.
 * A task result can be an exception or an object that you have to cast into your own type.
 * Before getting the object it is recommended that you call the hadException() method.
 * It will tell you if an exception occured in the task that generate this result.
 *
 * @author ProActive Team
 * @version 1.0, Aug 3, 2007
 * @since ProActive 3.2
 */
public class TaskResultImpl implements TaskResult {

    /** Serial Version UID */
    private static final long serialVersionUID = 2976276079143998102L;

    /** The task identification of the result */
    private TaskId id = null;

    /** The value of the result if no exception occured */
    private Object value = null;

    /** The exception throwed by the task */
    private Throwable exception = null;

    /** ProActive empty constructor. */
    public TaskResultImpl() {
    }

    /**
     * Return a new instance of task result represented by a task id and its result.
     *
     * @param id the identification of the task that send this result.
     * @param value the result of the task.
     */
    public TaskResultImpl(TaskId id, Object value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Return a new instance of task result represented by a task id and its exception.
     *
     * @param id the identification of the task that send this result.
     * @param exception the exception that occured in the task.
     */
    public TaskResultImpl(TaskId id, Throwable exception) {
        this.id = id;
        this.exception = exception;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.task.TaskResult#hadException()
     */
    public boolean hadException() {
        return exception != null;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.task.TaskResult#getTaskId()
     */
    public TaskId getTaskId() {
        return id;
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.task.TaskResult#value()
     */
    public Object value() throws Throwable {
        if (this.exception != null) {
            throw new RuntimeException(this.exception);
        } else {
            return value;
        }
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.common.task.TaskResult#getException()
     */
    public Throwable getException() {
        return exception;
    }
}
