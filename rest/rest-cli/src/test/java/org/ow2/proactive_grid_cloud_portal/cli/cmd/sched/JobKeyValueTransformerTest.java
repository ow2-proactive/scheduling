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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;

import com.google.common.collect.Maps;


public class JobKeyValueTransformerTest {

    @Test
    public void transformVariablesToMapTest() {
        Map<String, String> expected = Maps.newHashMap();
        expected.put("name", "devTest");
        expected.put("age", "36");
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap("{\"name\":\"devTest\",\"age\":\"36\"}"), is(expected));
    }

    @Test(expected = CLIException.class)
    public void transformVariablesToMapWithWrongSeparatorTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"name\":\"devTest\";\"age\":\"36\"}");
    }

    @Test
    public void transformVariablesWithoutValueToMapTest() {
        Map<String, String> expected = Maps.newHashMap();
        expected.put("name", "");
        expected.put("age", "");
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap("{\"name\":\"\",\"age\":\"\"}"), is(expected));

    }

    @Test(expected = CLIException.class)
    public void transformVariablesWithoutkeyToMapTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{\"\":\"devTest\",\"\":\"36\"}");
    }

    @Test(expected = CLIException.class)
    public void emptyJsonVariablesToMapTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap("{}");
    }

    @Test(expected = CLIException.class)
    public void emptyVariablesToMapTest() {
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        jkvTransformer.transformVariablesToMap(" ");
    }

    @Test
    public void noVariablesToMapTest() {
        Map<String, String> expected = Maps.newHashMap();
        JobKeyValueTransformer jkvTransformer = new JobKeyValueTransformer();
        assertThat(jkvTransformer.transformVariablesToMap(null), is(expected));
    }

}
