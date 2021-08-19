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
package org.ow2.proactive.scheduler.common.job.factories.globalvariables;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.JobVariable;


/**
 * @author ActiveEon Team
 * @since 15/07/2021
 */
public class GlobalVariablesData {

    Map<String, JobVariable> variables = new LinkedHashMap<>();

    Map<String, String> genericInformation = new LinkedHashMap<>();

    public GlobalVariablesData() {

    }

    public GlobalVariablesData(Map<String, JobVariable> variables, Map<String, String> genericInformation) {
        this.variables = variables;
        this.genericInformation = genericInformation;
    }

    public Map<String, JobVariable> getVariables() {
        return new LinkedHashMap<>(variables);
    }

    public Map<String, String> getGenericInformation() {
        return new LinkedHashMap<>(genericInformation);
    }

    public void setVariables(Map<String, JobVariable> variables) {
        this.variables = variables;
    }

    public void setGenericInformation(Map<String, String> genericInformation) {
        this.genericInformation = genericInformation;
    }

    @Override
    public String toString() {
        return "GlobalVariablesData{" + "variables=" + variables + ", genericInformation=" + genericInformation + '}';
    }
}
