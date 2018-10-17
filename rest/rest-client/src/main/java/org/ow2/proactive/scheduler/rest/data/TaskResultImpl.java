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
package org.ow2.proactive.scheduler.rest.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ow2.proactive.scheduler.common.task.SimpleTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.utils.ObjectByteConverter;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.TaskRestException;


public class TaskResultImpl implements TaskResult {
    private static final long serialVersionUID = 1L;

    private TaskId id;

    private byte[] serializedValue;

    private byte[] serializedException;

    private Serializable value;

    private TaskLogs taskLogs;

    private String exceptionMessage;

    private Map<String, byte[]> propagatedVariables;

    private Map<String, Serializable> jobMap = new ConcurrentHashMap<>();

    private Map<String, String> metadata = new HashMap<>();

    private boolean isRaw;

    public TaskResultImpl(TaskId id, TaskResultData d) {
        this.id = id;
        this.serializedValue = ObjectByteConverter.base64StringToByteArray(d.getSerializedValue());
        this.metadata = d.getMetadata();
        this.exceptionMessage = d.getExceptionMessage();
        this.serializedException = ObjectByteConverter.base64StringToByteArray(d.getSerializedException());
        this.propagatedVariables = ObjectByteConverter.mapOfBase64StringToByteArray(d.getSerializedPropagatedVariables());
        if (d.getOutput() != null) {
            this.taskLogs = new SimpleTaskLogs(d.getOutput().getStdoutLogs(), d.getOutput().getStderrLogs());
        }
        this.isRaw = d.isRaw();

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
            Throwable unserializedException = (Throwable) ObjectByteConverter.byteArrayToObject(serializedException);
            return unserializedException;
        } catch (ClassCastException | IOException | ClassNotFoundException e) {
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
    public Map<String, Serializable> getVariables() throws IOException, ClassNotFoundException {
        return ObjectByteConverter.mapOfByteArrayToSerializable(propagatedVariables);
    }

    @Override
    public boolean isRaw() {
        return isRaw;
    }

    @Override
    public Map<String, Serializable> getJobMap() {
        return jobMap;
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
        if (value == null) {
            if (serializedValue == null) {
                return null;
            } else if (isRaw) {
                return serializedValue;
            } else {
                return (Serializable) ObjectByteConverter.byteArrayToObject(serializedValue);
            }
        } else {
            return value;
        }
    }

    @Override
    public Serializable getValue() throws Throwable {
        return value();
    }

}
