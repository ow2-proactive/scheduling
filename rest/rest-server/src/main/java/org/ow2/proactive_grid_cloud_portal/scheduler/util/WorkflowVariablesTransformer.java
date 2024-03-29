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
package org.ow2.proactive_grid_cloud_portal.scheduler.util;

import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import com.google.common.collect.Maps;


public class WorkflowVariablesTransformer {

    public Map<String, String> getWorkflowVariablesFromPathSegment(PathSegment pathSegment) {
        Map<String, String> variables = null;
        MultivaluedMap<String, String> matrixParams = pathSegment.getMatrixParameters();
        if (matrixParams != null && !matrixParams.isEmpty()) {
            // Remove any empty keys that might be mistakenly sent to the scheduler to prevent bad behaviour
            matrixParams.remove("");
            variables = Maps.newHashMap();
            for (String key : matrixParams.keySet()) {
                String value = matrixParams.getFirst(key) == null ? "" : matrixParams.getFirst(key);
                variables.put(key, value);
            }
        }
        return variables;
    }

    public Map<String, String> replaceNullValuesWithEmptyString(Map<String, String> map) {
        map = map.entrySet().stream().peek(entry -> {
            if (entry.getValue() == null)
                entry.setValue("");
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return map;
    }

}
