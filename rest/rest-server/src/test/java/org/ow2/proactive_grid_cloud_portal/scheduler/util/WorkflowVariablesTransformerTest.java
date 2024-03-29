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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class WorkflowVariablesTransformerTest {

    private WorkflowVariablesTransformer workflowVariablesTransformer;

    @Before
    public void init() {
        this.workflowVariablesTransformer = new WorkflowVariablesTransformer();
    }

    @Test
    public void testEmptyMap() {

        PathSegment pathSegment = mock(PathSegment.class);

        MultivaluedMap<String, String> multivalueMap = new MultivaluedHashMap();

        when(pathSegment.getMatrixParameters()).thenReturn(multivalueMap);

        Map<String, String> variables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

        assertThat(variables, is(nullValue()));
    }

    @Test
    public void testTwoVariablesMap() {

        Map<String, String> expectedVariables = Maps.newHashMap();

        expectedVariables.put("KEY1", "VALUE1");

        expectedVariables.put("KEY2", "VALUE2");

        PathSegment pathSegment = mock(PathSegment.class);

        MultivaluedMap<String, String> multivalueMap = new MultivaluedHashMap();

        multivalueMap.put("KEY1", Lists.newArrayList("VALUE1"));

        multivalueMap.put("KEY2", Lists.newArrayList("VALUE2"));

        when(pathSegment.getMatrixParameters()).thenReturn(multivalueMap);

        Map<String, String> variables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

        assertThat(variables, is(expectedVariables));
    }

    @Test
    public void testTwoVariablesEmptyMap() {

        Map<String, String> expectedVariables = Maps.newHashMap();

        expectedVariables.put("KEY1", "");

        expectedVariables.put("KEY2", "");

        PathSegment pathSegment = mock(PathSegment.class);

        MultivaluedMap<String, String> multivalueMap = new MultivaluedHashMap();

        multivalueMap.put("KEY1", Lists.newArrayList(""));

        multivalueMap.put("KEY2", Lists.newArrayList(""));

        when(pathSegment.getMatrixParameters()).thenReturn(multivalueMap);

        Map<String, String> variables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

        assertThat(variables, is(expectedVariables));
    }

    @Test
    public void testTwoVariablesNullMap() {

        Map<String, String> expectedVariables = Maps.newHashMap();

        expectedVariables.put("KEY1", "");

        expectedVariables.put("KEY2", "");

        PathSegment pathSegment = mock(PathSegment.class);

        MultivaluedMap<String, String> multivalueMap = new MultivaluedHashMap();

        multivalueMap.put("KEY1", null);

        multivalueMap.put("KEY2", null);

        when(pathSegment.getMatrixParameters()).thenReturn(multivalueMap);

        Map<String, String> variables = workflowVariablesTransformer.getWorkflowVariablesFromPathSegment(pathSegment);

        assertThat(variables, is(expectedVariables));
    }

}
