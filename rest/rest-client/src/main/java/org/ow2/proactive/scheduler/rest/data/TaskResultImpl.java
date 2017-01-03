/*
 *  
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.rest.data;

import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.TaskRestException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.ow2.proactive_grid_cloud_portal.utils.ObjectUtility.object;


public class TaskResultImpl implements TaskResult {
    private static final long serialVersionUID = 1L;

    private TaskId id;
    private byte[] serializedValue;
    private byte[] serializedException;
    private Serializable value;
    private TaskLogs taskLogs;
    private String exceptionMessage;
    private Map<String, byte[]> propagatedVariables;

    private Map<String, String> metadata = new HashMap<>();

    public TaskResultImpl(TaskId id, TaskResultData d) {
        this.id = id;
        this.serializedValue = d.getSerializedValue();
        this.metadata = d.getMetadata();
        this.exceptionMessage = d.getExceptionMessage();
        this.serializedException = d.getSerializedException();
        this.propagatedVariables = d.getPropagatedVariables();
        if (d.getOutput() != null) {
            this.taskLogs = new SimpleTaskLogs(d.getOutput().getStdoutLogs(), d.getOutput().getStderrLogs());
        }

    }

    @Override
    public FlowAction getAction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Throwable getException() {
        if (serializedException == null || exceptionMessage == null) {
            return null;
        }
        try {
            Throwable unserializedException = (Throwable) object(serializedException);
            return unserializedException;
        } catch (ClassCastException e) {
            // If an error occurs during deserialization, a string is returned
            // in that case return the custom made server-side exception
            return new TaskRestException(exceptionMessage);
        }
    }

    public void setOutput(TaskLogs taskLogs) {
        this.taskLogs = taskLogs;
    }

    @Override
    public TaskLogs getOutput() {
        return taskLogs;
    }

    @Override
    public Map<String, byte[]> getPropagatedVariables() {
        return propagatedVariables;
    }

    @Override
    public byte[] getSerializedValue() {
        return serializedValue;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public TaskId getTaskId() {
        return id;
    }

    @Override
    public boolean hadException() {
        return serializedException != null;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    @Override
    public Serializable value() throws Throwable {
        return (null == value) ? ((null == serializedValue) ? null : (Serializable) object(serializedValue))
                : value;

    }

}
