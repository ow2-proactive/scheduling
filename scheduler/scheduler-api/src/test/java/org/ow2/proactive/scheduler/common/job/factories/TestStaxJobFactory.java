/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestStaxJobFactory {

    private static final ImmutableMap<String, String> EXPECTED_KEY_VALUE_ENTRIES =
            ImmutableMap.of("name1", "value1", "name2", "value2");

    private static final String JOB_FACTORY_IMPL = StaxJobFactory.class.getName();

    private static URI jobDescriptorUri;

    private static URI jobDescriptorSysPropsUri;

    private JobFactory factory;

    @BeforeClass
    public static void setJobDescriptorcUri() throws Exception {
        jobDescriptorUri = TestStaxJobFactory.class.getResource(
                "/org/ow2/proactive/scheduler/common/job/factories/job_update_variables.xml").toURI();
        jobDescriptorSysPropsUri = TestStaxJobFactory.class.getResource(
                "/org/ow2/proactive/scheduler/common/job/factories/job_update_variables_using_system_properties.xml")
                .toURI();
    }

    @Before
    public void setJobFactory() {
        factory = JobFactory.getFactory(JOB_FACTORY_IMPL);
    }

    @Test
    public void testCreateJobShouldUseJobVariablesToReplaceJobNameVariable() throws Exception {
        Job testScriptJob = factory.createJob(jobDescriptorUri);
        assertEquals("updated_job_name", testScriptJob.getName());
    }

    @Test
    public void testCreateJobShouldUseVariableMapToReplaceJobNameVariable() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        variablesMap.put("job_name", "updated_job_name2");
        Job testScriptJob = factory.createJob(jobDescriptorUri, variablesMap);
        assertEquals("updated_job_name2", testScriptJob.getName());
    }

    @Test
    public void testCreateJobShouldUseJobVariablesToReplaceTaskGenericInfoVariables()
            throws Exception {
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri);

        assertEquals("updated_task_generic_info_value", testJob.getTask("task1").getGenericInformation()
                .get("task_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapToReplaceTaskGenericInfoVariables()
            throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        variablesMap.put("task_generic_info", "updated_task_generic_info_value2");

        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap);

        assertEquals("updated_task_generic_info_value2", testJob.getTask("task1").getGenericInformation()
                .get("task_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapParameterToReplaceVariableValue()
      throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        variablesMap.put("from_create_job_parameter", "from_create_job_parameter_value");

        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap);

        assertEquals("from_create_job_parameter_value", testJob.getVariables().get(
                "from_create_job_parameter"));
    }

    @Test
    public void testCreateJobShouldUseSyspropsToReplaceVariables() throws Exception{
        System.setProperty("system_property", "system_property_value");

        Job testJob = factory.createJob(jobDescriptorSysPropsUri);

        assertEquals("system_property_value", testJob.getVariables().get("system_property"));
    }

    /**
     * The next 3 tests are there to check that parsing a workflow XML description involving XML elements
     * with more than 1 attribute (defined in any order) returns an object description with expected values
     * in corresponding data structures.
     */

    @Test
    public void testJobCreationAttributeOrderDefinitionGenericInformationXmlElement() throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(getResource("job_attr_def_generic_information_xml_element.xml"));
        Map<String, String> genericInformation = job.getTask("task").getGenericInformation();
        assertExpectedKeyValueEntriesMatch(genericInformation);
    }

    @Test
    public void testJobCreationAttributeOrderDefinitionParameterXmlElement() throws URISyntaxException, JobCreationException, IOException, ClassNotFoundException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(getResource("job_attr_def_parameter_xml_element.xml"));
        Map<String, Serializable> arguments = ((JavaTask) job.getTask("task")).getArguments();
        assertExpectedKeyValueEntriesMatch(arguments);
    }

    @Test
    public void testJobCreationAttributeOrderDefinitionVariableXmlElement() throws URISyntaxException, JobCreationException {
        Job job = factory.createJob(getResource("job_attr_def_variable_xml_element.xml"));
        Map<String, String> variables = job.getVariables();
        assertExpectedKeyValueEntriesMatch(variables);
    }

    private static <K,V> void assertExpectedKeyValueEntriesMatch(Map<K, V> map) {
        // map variable is assumed to contain attributes name/value parsed from XML

        // expected attribute names and parsed ones should be the same, so the symmetric
        // difference between both sets should be empty
        Assert.assertTrue(
                Sets.symmetricDifference(
                        EXPECTED_KEY_VALUE_ENTRIES.keySet(),
                        map.keySet()).isEmpty());

        // expected attribute values and parsed ones should be the same, so the symmetric
        // difference between both sets should be empty
        Assert.assertTrue(
                CollectionUtils.disjunction(
                        EXPECTED_KEY_VALUE_ENTRIES.values(), map.values()).isEmpty());
    }

    private URI getResource(String filename) throws URISyntaxException {
        return TestStaxJobFactory.class.getResource(
                "/org/ow2/proactive/scheduler/common/job/factories/" + filename).toURI();
    }

}
