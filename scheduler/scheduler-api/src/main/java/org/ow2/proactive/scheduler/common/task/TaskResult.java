/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;
import java.util.Map;

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
    boolean hadException();

    /**
     * To get the id of the task.
     *
     * @return the id of the task.
     */
    TaskId getTaskId();

    /**
     * Get the value of the task. Throw the exception if the task generate
     *
     * @return the value of the task.
     * @throws Throwable If the value has generate an exception.
     */
    Serializable value() throws Throwable;

    /**
     * Get the serialized value of the task.
     *
     * @return the value of the task, null if an exception occurred.
     */
    byte[] getSerializedValue();

    /**
     * Get metadata associated with this result.
     *
     * Metadata let the task add additional information bound to the result.
     * It is the responsibility of the business-code to define the metadata semantics.
     *
     * For example, if the result contains binary data, the metadata map could contain the binary content type,
     * or the name of the file if the result needs to be written on disk.
     *
     * @return a map of metadata
     */
    Map<String, String> getMetadata();

    /**
     * If a FlowScript was executed on this task, its result
     * is stored it so that the action can be performed later when
     * processed by the core.
     * 
     * @return the Control Flow action embedded in this TaskResult
     */
    FlowAction getAction();

    /**
     * Retrieve the exception threw by the task.
     * If the task didn't throw an exception, null is returned
     *
     * @return the exception threw by the task.
     */
    Throwable getException();

    /**
     * Return the output of the execution, including stdout and stderr.
     *
     * @return the output of the execution, including stdout and stderr.
     */
    TaskLogs getOutput();

    Map<String, byte[]> getPropagatedVariables();

}
