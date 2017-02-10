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
package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class TaskResultData {

    private TaskIdData id;

    private byte[] serializedValue;

    private byte[] serializedException;

    private String exceptionMessage;

    private Map<String, String> metadata;

    private Map<String, byte[]> propagatedVariables;

    private TaskLogsData output;

    public TaskIdData getId() {
        return id;
    }

    public void setId(TaskIdData id) {
        this.id = id;
    }

    public byte[] getSerializedValue() {
        return serializedValue;
    }

    public void setSerializedValue(byte[] serializedValue) {
        this.serializedValue = serializedValue;
    }

    public byte[] getSerializedException() {
        return serializedException;
    }

    public void setSerializedException(byte[] serializedException) {
        this.serializedException = serializedException;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public Map<String, byte[]> getPropagatedVariables() {
        return propagatedVariables;
    }

    public void setPropagatedVariables(Map<String, byte[]> propagatedVariables) {
        this.propagatedVariables = propagatedVariables;
    }

    public TaskLogsData getOutput() {
        return output;
    }

    public void setOutput(TaskLogsData output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "TaskResultData{" + "id=" + id + ", serializedValue=" + Arrays.toString(serializedValue) +
               ", serializedException=" + Arrays.toString(serializedException) + ", exception=" + exceptionMessage +
               ", metadata=" + metadata + ", output=" + output + '}';
    }
}
