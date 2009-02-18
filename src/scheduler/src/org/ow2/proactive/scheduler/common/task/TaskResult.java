/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.swing.JPanel;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Interface representing the task result.<br>
 * A task result can be an exception or a serializable object that you have to cast into your own type.<br>
 * Before getting the object it is recommended to call the {@link #hadException()} method.<br>
 * It will tell you if an exception occurred in the task that generate this result.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public interface TaskResult extends Serializable {

    /**
     * To know if an exception has occurred on this task.
     *
     * @return true if an exception occurred, false if not.
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
     * @throws Throwable If the value has generate an exception.
     */
    public Serializable value() throws Throwable;

    /**
     * To get the exception of the task.
     *
     * @return the exception of the task.
     */
    public Throwable getException();

    /**
     * Return the output of the execution, including stdout and stderr.
     *
     * @return the output of the execution, including stdout and stderr.
     */
    public TaskLogs getOutput();

    /**
     * Set the class that is able to describe this result. See ResultPreview.
     *
     * @param descClass the name of the class that is able to describe this result.
     */
    public void setPreviewerClassName(String descClass);

    /**
     * Set the classpath of the job that contained the corresponding task.
     *
     * @param jcp the classpath of the job
     */
    public void setJobClasspath(String[] jcp);

    /**
     * Return a swing panel describing this result.
     *
     * @return a swing panel describing this result.
     */
    public JPanel getGraphicalDescription();

    /**
     * Return a string describing this result.
     *
     * @return a string describing this result.
     */
    public String getTextualDescription();
}
