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
package org.ow2.proactive.scheduler.signal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobVariable;


public class Signal implements Serializable {
    private String name;

    private boolean received = false;

    private List<JobVariable> inputVariables;

    private Map<String, String> outputValues = null;

    public Signal(String name, List<JobVariable> variables) {
        this.name = name;
        this.inputVariables = variables;
    }

    public String getName() {
        return name;
    }

    public boolean isReceived() {
        return received;
    }

    public List<JobVariable> getInputVariables() {
        return inputVariables;
    }

    public Map<String, String> getOutputValues() {
        return outputValues;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public void setInputVariables(List<JobVariable> inputVariables) {
        this.inputVariables = inputVariables;
    }

    public void setOutputValues(Map<String, String> outputValues) {
        this.outputValues = outputValues;
    }
}
