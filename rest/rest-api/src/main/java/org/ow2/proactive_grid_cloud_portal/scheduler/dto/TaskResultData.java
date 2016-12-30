/*
 * ################################################################
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.scheduler.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Map;


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
        return "TaskResultData{" + "id=" + id + ", serializedValue=" + Arrays.toString(serializedValue) + ", serializedException=" + Arrays.toString(serializedException) + ", exception=" + exceptionMessage + ", metadata=" + metadata + ", output=" + output + '}';
    }
}
