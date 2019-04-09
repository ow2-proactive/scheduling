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
package org.ow2.proactive.scheduler.common.job.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobVariable;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;


public class GetJobContentGeneratorTest {

    GetJobContentGenerator generator = new GetJobContentGenerator();

    @Test
    public void testRemoveVarsAndInfo() throws IOException {

        URL url = Resources.getResource("org/ow2/proactive/scheduler/common/job/factories/job_with_vars_and_info.xml");
        String jobContent = Resources.toString(url, Charsets.UTF_8);

        final String newJobContent = generator.replaceVarsAndGenericInfo(jobContent,
                                                                         Collections.emptyMap(),
                                                                         Collections.emptyMap());

        assertFalse(newJobContent.contains("<variables>"));
        assertFalse(newJobContent.contains("<genericInformation>"));
        assertNotEquals(jobContent, newJobContent);

    }

    @Test
    public void testReplaceVars() throws IOException {

        URL url = Resources.getResource("org/ow2/proactive/scheduler/common/job/factories/job_with_vars_and_info.xml");
        String jobContent = Resources.toString(url, Charsets.UTF_8);

        Map<String, JobVariable> vars = new HashMap<>();
        vars.put("var", new JobVariable("var", "myvalue"));
        final String newJobContent = generator.replaceVarsAndGenericInfo(jobContent, vars, Collections.emptyMap());

        assertTrue(newJobContent.contains("<variables>"));
        assertTrue(newJobContent.contains("myvalue"));
        assertFalse(newJobContent.contains("<genericInformation>"));
        assertNotEquals(jobContent, newJobContent);
    }

    @Test
    public void testReplaceInfo() throws IOException {

        URL url = Resources.getResource("org/ow2/proactive/scheduler/common/job/factories/job_with_vars_and_info.xml");
        String jobContent = Resources.toString(url, Charsets.UTF_8);

        Map<String, String> info = new HashMap<>();
        info.put("var", "myvalue");
        final String newJobContent = generator.replaceVarsAndGenericInfo(jobContent, Collections.emptyMap(), info);

        assertTrue(newJobContent.contains("<genericInformation>"));
        assertTrue(newJobContent.contains("myvalue"));
        assertFalse(newJobContent.contains("<variables>"));
        assertNotEquals(jobContent, newJobContent);

    }

}
