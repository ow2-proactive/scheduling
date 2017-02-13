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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.TaskVariable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class TestStaxJobFactory {

    private static final ImmutableMap<String, String> EXPECTED_KEY_VALUE_ENTRIES = ImmutableMap.of("name1",
                                                                                                   "value1",
                                                                                                   "name2",
                                                                                                   "value2");

    private static final String JOB_FACTORY_IMPL = StaxJobFactory.class.getName();

    private static URI jobDescriptorUri;

    private static URI jobDescriptorSysPropsUri;

    private StaxJobFactory factory;

    @BeforeClass
    public static void setJobDescriptorcUri() throws Exception {
        jobDescriptorUri = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_update_variables.xml")
                                                   .toURI();
        jobDescriptorSysPropsUri = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_update_variables_using_system_properties.xml")
                                                           .toURI();
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

    @Before
    public void setJobFactory() {
        factory = (StaxJobFactory) JobFactory.getFactory(JOB_FACTORY_IMPL);
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
    public void testCreateJobShouldUseJobVariablesToReplaceTaskGenericInfoVariables() throws Exception {
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri);

        assertEquals("updated_task_generic_info_value",
                     testJob.getTask("task1").getGenericInformation().get("task_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapToReplaceTaskGenericInfoVariables() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        variablesMap.put("task_generic_info", "updated_task_generic_info_value2");

        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap);

        assertEquals("updated_task_generic_info_value2",
                     testJob.getTask("task1").getGenericInformation().get("task_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapParameterToReplaceVariableValue() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        variablesMap.put("from_create_job_parameter", "from_create_job_parameter_value");

        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap);

        assertEquals("from_create_job_parameter_value",
                     testJob.getVariables().get("from_create_job_parameter").getValue());
    }

    @Test
    public void testCreateJobShouldUseSyspropsToReplaceVariables() throws Exception {
        System.setProperty("system_property", "system_property_value");

        Job testJob = factory.createJob(jobDescriptorSysPropsUri);

        assertEquals("system_property_value", testJob.getVariables().get("system_property").getValue());
    }

    /**
     * The next 3 tests are there to check that parsing a workflow XML description involving XML elements
     * with more than 1 attribute (defined in any order) returns an object description with expected values
     * in corresponding data structures.
     */

    @Test
    public void testJobCreationAttributeOrderDefinitionGenericInformationXmlElement()
            throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(getResource("job_attr_def_generic_information_xml_element.xml"));
        Map<String, String> genericInformation = job.getTask("task").getGenericInformation();
        assertExpectedKeyValueEntriesMatch(genericInformation);
    }

    @Test
    public void testJobCreationAttributeOrderDefinitionParameterXmlElement()
            throws URISyntaxException, JobCreationException, IOException, ClassNotFoundException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(getResource("job_attr_def_parameter_xml_element.xml"));
        Map<String, Serializable> arguments = ((JavaTask) job.getTask("task")).getArguments();
        assertExpectedKeyValueEntriesMatch(arguments);
    }

    @Test
    public void testJobCreationAttributeOrderDefinitionVariableXmlElement()
            throws URISyntaxException, JobCreationException {
        Job job = factory.createJob(getResource("job_attr_def_variable_xml_element.xml"));
        Map<String, JobVariable> jobVariables = job.getVariables();
        assertEquals(2, jobVariables.size());
        JobVariable jobVariable = jobVariables.get("name1");
        assertNotNull(jobVariable);
        assertEquals("name1", jobVariable.getName());
        assertEquals("value1", jobVariable.getValue());
        assertEquals("model1", jobVariable.getModel());
        jobVariable = jobVariables.get("name2");
        assertNotNull(jobVariable);
        assertEquals("name2", jobVariable.getName());
        assertEquals("value2", jobVariable.getValue());
        assertEquals("model2", jobVariable.getModel());
    }

    @Test
    public void testHandleVariablesReplacements() throws JobCreationException {
        // null replacement
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1", new JobVariable("a1", "v1", "m1")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1",
                                                                                                                            "m1")),
                                                                      null));

        // replace existing variable, leave the model intact
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1", new JobVariable("a1", "v2", "m1")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1",
                                                                                                                            "m1")),
                                                                      ImmutableMap.<String, String> of("a1", "v2")));

        // add new variable
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1",
                                                                  new JobVariable("a1", "v1", "m1"),
                                                                  "a2",
                                                                  new JobVariable("a2", "v2")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1",
                                                                                                                            "m1")),
                                                                      ImmutableMap.<String, String> of("a2", "v2")));

        // reuse replacement variable in pattern and add new variable
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1",
                                                                  new JobVariable("a1", "v1v2", "m1v2"),
                                                                  "a2",
                                                                  new JobVariable("a2", "v2")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1${a2}",
                                                                                                                            "m1${a2}")),
                                                                      ImmutableMap.<String, String> of("a2", "v2")));

        // existing variable uses another existing variable in a pattern, null replacements
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1",
                                                                  new JobVariable("a1", "v1v2", "m1v2"),
                                                                  "a2",
                                                                  new JobVariable("a2", "v2")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1${a2}",
                                                                                                                            "m1${a2}"),
                                                                                                            "a2",
                                                                                                            new JobVariable("a2",
                                                                                                                            "v2")),
                                                                      null));

        // existing variable uses another existing variable in a pattern, this other existing variable uses itself a replacement variable
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1",
                                                                  new JobVariable("a1", "v1v3", "m1v3"),
                                                                  "a2",
                                                                  new JobVariable("a2", "v3", "m2v3"),
                                                                  "a3",
                                                                  new JobVariable("a3", "v3")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1${a2}",
                                                                                                                            "m1${a2}"),
                                                                                                            "a2",
                                                                                                            new JobVariable("a2",
                                                                                                                            "${a3}",
                                                                                                                            "m2${a3}")),
                                                                      ImmutableMap.<String, String> of("a3", "v3")));

        // existing variable uses a replacement variable in a pattern, this replacement variable uses itself a job variable from the workflow
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1",
                                                                  new JobVariable("a1", "v1v3", "m1v3"),
                                                                  "a2",
                                                                  new JobVariable("a2", "v3"),
                                                                  "a3",
                                                                  new JobVariable("a3", "v3", "m3v3")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1${a2}",
                                                                                                                            "m1${a2}"),
                                                                                                            "a3",
                                                                                                            new JobVariable("a3",
                                                                                                                            "v3",
                                                                                                                            "m3${a2}")),
                                                                      ImmutableMap.<String, String> of("a2", "${a3}")));

        // existing variable uses a replacement variable in a pattern, but is overwritten by another replacement variable
        Assert.assertEquals(ImmutableMap.<String, JobVariable> of("a1",
                                                                  new JobVariable("a1", "v1", "m1v2"),
                                                                  "a2",
                                                                  new JobVariable("a2", "v2")),
                            factory.replaceVariablesInJobVariablesMap(ImmutableMap.<String, JobVariable> of("a1",
                                                                                                            new JobVariable("a1",
                                                                                                                            "v1${a2}",
                                                                                                                            "m1${a2}")),
                                                                      ImmutableMap.<String, String> of("a2",
                                                                                                       "v2",
                                                                                                       "a1",
                                                                                                       "v1")));

    }

    @Test
    public void testTaskVariables() throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(getResource("task_variables.xml"));
        Map<String, TaskVariable> taskVariables = job.getTask("task").getVariables();
        assertEquals(2, taskVariables.size());
        TaskVariable taskVariable = taskVariables.get("name1");
        assertNotNull(taskVariable);
        assertEquals("name1", taskVariable.getName());
        assertEquals("value1", taskVariable.getValue());
        assertEquals("model1", taskVariable.getModel());
        assertFalse(taskVariable.isJobInherited());
        taskVariable = taskVariables.get("name2");
        assertNotNull(taskVariable);
        assertEquals("name2", taskVariable.getName());
        assertEquals("value2", taskVariable.getValue());
        assertEquals("model2", taskVariable.getModel());
        assertTrue(taskVariable.isJobInherited());
    }

    private static <K, V> void assertExpectedKeyValueEntriesMatch(Map<K, V> map) {
        // map variable is assumed to contain attributes name/value parsed from XML

        // expected attribute names and parsed ones should be the same, so the symmetric
        // difference between both sets should be empty
        Assert.assertTrue(Sets.symmetricDifference(EXPECTED_KEY_VALUE_ENTRIES.keySet(), map.keySet()).isEmpty());

        // expected attribute values and parsed ones should be the same, so the symmetric
        // difference between both sets should be empty
        Assert.assertTrue(CollectionUtils.disjunction(EXPECTED_KEY_VALUE_ENTRIES.values(), map.values()).isEmpty());
    }

    private URI getResource(String filename) throws URISyntaxException {
        return TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/" + filename)
                                       .toURI();
    }

}
