/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.util.Map;

import javax.swing.JPanel;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;


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
@XmlRootElement
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
     * Get the value of the task. Throw the exception if the task generate
     *
     * @return the value of the task.
     * @throws Throwable If the value has generate an exception.
     */
    public Serializable value() throws Throwable;

    /**
     * Get the serialized value of the task.
     *
     * @return the value of the task, null if an exception occurred.
     */
    byte[] getSerializedValue();

    /**
     * If a FlowScript was executed on this task, its result
     * is stored it so that the action can be performed later when
     * processed by the core.
     * 
     * @return the Control Flow action embedded in this TaskResult
     */
    public FlowAction getAction();

    /**
     * Retrieve the exception threw by the task.
     * If the task didn't throw an exception, null is returned
     *
     * @return the exception threw by the task.
     */
    public Throwable getException();

    /**
     * Return the output of the execution, including stdout and stderr.
     *
     * @return the output of the execution, including stdout and stderr.
     */
    public TaskLogs getOutput();

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

    public Map<String, byte[]> getPropagatedVariables();
}
