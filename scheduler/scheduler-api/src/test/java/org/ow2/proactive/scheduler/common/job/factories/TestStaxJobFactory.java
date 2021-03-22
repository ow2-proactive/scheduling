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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
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

    private static URI jobDescriptorNoVariablesUri;

    private static URI jobDescriptorWithMetadata;

    private static URI jobDescriptorWithEmptyMetadata;

    private static URI jobDescriptorWithUnresolvedGenericInfoAndVariables;

    private static URI jobDescriptorWithGlobalVariablesAndGenericInfo;

    private static URI jobDescriptorAttrDefGenericInformationXmlElement;

    private static URI jobDescriptorAttrDefParameterXmlElement;

    private static URI jobDescriptorAttrDefVariableXmlElement;

    private static URI jobWithParsingIssue;

    private static URI jobDescriptorTaskVariable;

    private static URI jobDescriptorVariableOrder;

    private StaxJobFactory factory;

    @BeforeClass
    public static void setJobDescriptorcUri() throws Exception {
        jobDescriptorUri = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_update_variables.xml")
                                                   .toURI();
        jobDescriptorNoVariablesUri = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_no_variables.xml")
                                                              .toURI();
        jobDescriptorSysPropsUri = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_update_variables_using_system_properties.xml")
                                                           .toURI();

        jobDescriptorWithMetadata = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_with_metadata.xml")
                                                            .toURI();

        jobDescriptorWithEmptyMetadata = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_with_empty_metadata.xml")
                                                                 .toURI();

        jobDescriptorWithUnresolvedGenericInfoAndVariables = TestStaxJobFactory.class.getResource("job_with_unresolved_generic_info_and_variables.xml")
                                                                                     .toURI();

        jobDescriptorWithGlobalVariablesAndGenericInfo = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/job_with_global_variables_and_gi.xml")
                                                                                 .toURI();

        jobDescriptorAttrDefGenericInformationXmlElement = TestStaxJobFactory.class.getResource("job_attr_def_generic_information_xml_element.xml")
                                                                                   .toURI();

        jobDescriptorAttrDefParameterXmlElement = TestStaxJobFactory.class.getResource("job_attr_def_parameter_xml_element.xml")
                                                                          .toURI();

        jobDescriptorAttrDefVariableXmlElement = TestStaxJobFactory.class.getResource("job_attr_def_variable_xml_element.xml")
                                                                         .toURI();

        jobDescriptorTaskVariable = TestStaxJobFactory.class.getResource("task_variables.xml").toURI();

        jobDescriptorVariableOrder = TestStaxJobFactory.class.getResource("job_variables_order.xml").toURI();

        jobWithParsingIssue = TestStaxJobFactory.class.getResource("/org/ow2/proactive/scheduler/common/job/factories/load_bmw_mini_datafeeds.ksh $WV_ARGS $BATCH_NAME.xml")
                                                      .toURI();

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
    }

    @Before
    public void setJobFactory() {
        factory = (StaxJobFactory) JobFactory.getFactory(JOB_FACTORY_IMPL);
        factory.globalVariables = new LinkedHashMap<>();
        factory.globalGenericInformation = new LinkedHashMap<>();
    }

    @After
    public void cleanGlobals() {
        factory.globalVariables = new LinkedHashMap<>();
        factory.globalGenericInformation = new LinkedHashMap<>();
    }

    @Test
    public void testCreateJobShouldUseJobVariablesToReplaceJobNameVariable() throws Exception {
        Job testScriptJob = factory.createJob(jobDescriptorUri);
        assertEquals("updated_job_name", testScriptJob.getName());
    }

    @Test
    public void testJobWithParsingIssue() throws Exception {
        Job job = factory.createJob(jobWithParsingIssue);
        assertEquals("load_bmw_mini_datafeeds.ksh  -f load_mini_datafeeds.cfg  -t Service ",
                     job.getVariables().get("WV_COMMAND").getValue());
        assertEquals(" -f load_mini_datafeeds.cfg  -t Service ", job.getVariables().get("WV_ARGS").getValue());
        assertEquals("process_DP,process_DLR_NGR", job.getGenericInformation().get("REQUIRED_LICENSES"));
        assertEquals("60d", job.getGenericInformation().get("REMOVE_DELAY"));
        assertEquals("load_bmw_mini_datafeeds.ksh  -f load_mini_datafeeds.cfg  -t Service   -- BMWLOAD", job.getName());
    }

    @Test
    public void testCreateJobShouldNotFailWhenParsingMetadata() throws Exception {
        factory.createJob(jobDescriptorWithMetadata);
    }

    @Test
    public void testCreateJobShouldNotFailWhenParsingEmptyMetadata() throws Exception {
        factory.createJob(jobDescriptorWithEmptyMetadata);
    }

    @Test
    public void testCreateJobShouldPreserveVariablesOrder() throws Exception {
        Job job = factory.createJob(jobDescriptorVariableOrder);
        int index = 1;
        for (String variable : job.getVariables().keySet()) {
            assertEquals("var_" + index, variable);
            index++;
        }
    }

    @Test
    public void testCreateJobWithNoVariablesShouldReferenceGlobalVariablesAndGenericInfo() throws Exception {
        factory.globalVariables.put("globalVar", new JobVariable("globalVar", "globalValue"));
        factory.globalGenericInformation.put("globalGI", "globalGIValue");
        Job testScriptJob = factory.createJob(jobDescriptorNoVariablesUri);
        assertNotNull(testScriptJob.getVariables().get("globalVar"));
        assertEquals("globalValue", testScriptJob.getVariables().get("globalVar").getValue());
        assertEquals("globalGIValue", testScriptJob.getGenericInformation().get("globalGI"));
    }

    @Test
    public void testCreateJobWithVariablesAndGenericInfoShouldReferenceGlobalVariables() throws Exception {
        factory.globalVariables.put("referenced_global_var",
                                    new JobVariable("referenced_global_var", "global_var_value"));
        Job testScriptJob = factory.createJob(jobDescriptorWithGlobalVariablesAndGenericInfo);
        assertNotNull(testScriptJob.getVariables().get("job_var_referencing_global_var"));
        assertEquals("global_var_value", testScriptJob.getVariables().get("job_var_referencing_global_var").getValue());
        assertEquals("global_var_value", testScriptJob.getGenericInformation().get("gen_info_referencing_global_var"));
        // other vars and gi in the workflow should be present
        assertNotNull(testScriptJob.getVariables().get("job_var"));
        assertNotNull(testScriptJob.getGenericInformation().get("gen_info"));
    }

    @Test
    public void testCreateJobWithVariablesAndGenericInfoShouldOverrideGlobalVariablesAndGenericInfo() throws Exception {
        factory.globalVariables.put("global_var", new JobVariable("global_var", "global_var_value"));
        factory.globalGenericInformation.put("global_gi", "global_gi_value");
        Job testScriptJob = factory.createJob(jobDescriptorWithGlobalVariablesAndGenericInfo);
        assertNotNull(testScriptJob.getVariables().get("global_var"));
        assertEquals("global_var_overridden_by_xml", testScriptJob.getVariables().get("global_var").getValue());
        assertEquals("global_gi_overridden_by_xml", testScriptJob.getGenericInformation().get("global_gi"));
        // other vars and gi in the workflow should be present
        assertNotNull(testScriptJob.getVariables().get("job_var"));
        assertNotNull(testScriptJob.getGenericInformation().get("gen_info"));
    }

    @Test
    public void
            testCreateJobWithVariablesAndGenericInfoAndGlobalOnesShouldBeOverriddenBySubmittedVariablesAndGenericInfo()
                    throws Exception {
        factory.globalVariables.put("global_var", new JobVariable("global_var", "global_var_value"));
        factory.globalGenericInformation.put("global_gi", "global_gi_value");
        Job testScriptJob = factory.createJob(jobDescriptorWithGlobalVariablesAndGenericInfo,
                                              ImmutableMap.of("global_var", "submitted_var_value"),
                                              ImmutableMap.of("global_gi", "submitted_gi_value"));
        assertNotNull(testScriptJob.getVariables().get("global_var"));
        assertEquals("submitted_var_value", testScriptJob.getVariables().get("global_var").getValue());
        assertEquals("submitted_gi_value", testScriptJob.getGenericInformation().get("global_gi"));
        // other vars and gi in the workflow should be present
        assertNotNull(testScriptJob.getVariables().get("job_var"));
        assertNotNull(testScriptJob.getGenericInformation().get("gen_info"));
    }

    @Test
    public void testCreateJobWithSubmittedVariablesAndGenericInfoShouldReferenceGlobalVariables() throws Exception {
        factory.globalVariables.put("global_var", new JobVariable("global_var", "global_var_value"));
        factory.globalGenericInformation.put("global_gi", "global_gi_value");
        Job testScriptJob = factory.createJob(jobDescriptorNoVariablesUri,
                                              ImmutableMap.of("submitted_var", "${global_var}"),
                                              ImmutableMap.of("submitted_gi", "${global_var}"));
        assertNotNull(testScriptJob.getVariables().get("global_var"));
        assertEquals("global_var_value", testScriptJob.getVariables().get("global_var").getValue());
        assertNotNull(testScriptJob.getVariables().get("submitted_var"));
        assertEquals("global_var_value", testScriptJob.getVariables().get("submitted_var").getValue());
        assertEquals("global_gi_value", testScriptJob.getGenericInformation().get("global_gi"));
        assertEquals("global_var_value", testScriptJob.getGenericInformation().get("submitted_gi"));
    }

    @Test
    public void testCreateJobWithSubmittedVariablesAndGenericInfoShouldOverrideGlobalVariablesAndGenericInfo()
            throws Exception {
        factory.globalVariables.put("global_var", new JobVariable("global_var", "global_var_value"));
        factory.globalGenericInformation.put("global_gi", "global_gi_value");
        Job testScriptJob = factory.createJob(jobDescriptorNoVariablesUri,
                                              ImmutableMap.of("global_var", "submitted_var_value"),
                                              ImmutableMap.of("global_gi", "submitted_gi_value"));
        assertNotNull(testScriptJob.getVariables().get("global_var"));
        assertEquals("submitted_var_value", testScriptJob.getVariables().get("global_var").getValue());
        assertEquals("submitted_gi_value", testScriptJob.getGenericInformation().get("global_gi"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapToReplaceJobNameVariable() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        String updatedValue = "job_name_updated_using_variables_map";

        variablesMap.put("job_name", updatedValue);
        Job testScriptJob = factory.createJob(jobDescriptorUri, variablesMap, null);

        assertEquals(updatedValue, testScriptJob.getName());
    }

    @Test
    public void testCreateJobShouldUseGenericInfosMapToOverrideJobGenericInfos() throws Exception {
        Map<String, String> genericInfosMap = Maps.newHashMap();
        String updatedValue = "job_generic_info_updated_using_generic_info_map";

        genericInfosMap.put("job_generic_info", updatedValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, null, genericInfosMap);

        assertEquals(updatedValue, testJob.getGenericInformation().get("job_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseJobVariablesToReplaceInJobGenericInfo() throws Exception {
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri);
        assertEquals("updated_job_generic_info_value", testJob.getGenericInformation().get("job_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapToReplaceInJobGenericInfo() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        String updatedValue = "job_generic_info_updated_using_variables_map";

        variablesMap.put("job_generic_info", updatedValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap, null);

        assertEquals(updatedValue, testJob.getGenericInformation().get("job_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapAndGenericInfoMapToReplaceInJobGenericInfoInEmptyWorkflow()
            throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        String updatedValue = "job_generic_info_updated_using_variables_map";
        variablesMap.put("job_variable_key", updatedValue);

        Map<String, String> genericInfoMap = Maps.newHashMap();
        genericInfoMap.put("job_generic_info", "${job_variable_key}");
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorNoVariablesUri,
                                                              variablesMap,
                                                              genericInfoMap);

        assertEquals(updatedValue, testJob.getGenericInformation().get("job_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapAndGenericInfoMapToReplaceInJobGenericInfo() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        String updatedValue = "job_generic_info_updated_using_variables_map";
        variablesMap.put("job_variable_key", updatedValue);

        Map<String, String> genericInfoMap = Maps.newHashMap();
        genericInfoMap.put("job_generic_info", "${job_variable_key}");
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap, genericInfoMap);

        assertEquals(updatedValue, testJob.getGenericInformation().get("job_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseGenericInfoMapToReplaceInJobGenericInfo() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();

        Map<String, String> genericInfoMap = Maps.newHashMap();
        String updatedValue = "job_generic_info_novar_value";
        genericInfoMap.put("job_generic_info_novar", updatedValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap, genericInfoMap);

        assertEquals(updatedValue, testJob.getGenericInformation().get("job_generic_info_novar"));
    }

    @Test
    public void testCreateJobShouldUseGenericInfosMapToCreateJobGenericInfoVariables() throws Exception {
        Map<String, String> genericInfosMap = Maps.newHashMap();
        String updatedKey = "new_job_generic_info";
        String updatedValue = "new_job_generic_info_value";

        genericInfosMap.put(updatedKey, updatedValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, null, genericInfosMap);

        assertEquals(updatedValue, testJob.getGenericInformation().get(updatedKey));
    }

    @Test
    public void
            testCreateJobShouldUseGenericInfosMapToCreateJobGenericInfoOnWorkflowWithEmptyGenericInfoAndVariablesSection()
                    throws Exception {
        Map<String, String> genericInfosMap = Maps.newHashMap();
        String genericInfoKey = "generic_info_key";
        String genericInfoValue = "generic_info_value";

        genericInfosMap.put(genericInfoKey, genericInfoValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorNoVariablesUri, null, genericInfosMap);

        assertEquals(genericInfoValue, testJob.getGenericInformation().get(genericInfoKey));
        assertNull(testJob.getVariables().get(genericInfoKey));
    }

    @Test
    public void
            testCreateJobShouldUseVariablesMapToCreateJobVariablesOnWorkflowWithEmptyGenericInfoAndVariablesSection()
                    throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        String variableKey = "variable_key";
        String variableValue = "variable_value";

        variablesMap.put(variableKey, variableValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorNoVariablesUri, variablesMap, null);

        assertEquals(variableValue, testJob.getVariables().get(variableKey).getValue());
        assertNull(testJob.getGenericInformation().get(variableKey));
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
        String updatedValue = "task_generic_info_updated_using_variables_map";

        variablesMap.put("task_generic_info", updatedValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap, null);

        assertEquals(updatedValue, testJob.getTask("task1").getGenericInformation().get("task_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseGenericInfosMapToReplaceTaskGenericInfoVariables() throws Exception {
        Map<String, String> genericInfosMap = Maps.newHashMap();
        String updatedValue = "task_generic_info_updated_using_generic_infos_map";

        genericInfosMap.put("task_generic_info", updatedValue);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, null, genericInfosMap);

        assertEquals(updatedValue, testJob.getTask("task1").getGenericInformation().get("task_generic_info"));
    }

    @Test
    public void testCreateJobShouldUseVariableMapParameterToReplaceVariableValue() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        variablesMap.put("from_create_job_parameter_given", "from_create_job_parameter_value");

        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorUri, variablesMap, null);

        assertEquals("from_create_job_parameter_value",
                     testJob.getVariables().get("from_create_job_parameter").getValue());
    }

    @Test
    public void testCreateJobShouldUseSyspropsToReplaceVariables() throws Exception {
        System.setProperty("system_property", "system_property_value");

        Job testJob = factory.createJob(jobDescriptorSysPropsUri);

        assertEquals("system_property_value", testJob.getVariables().get("system_property").getValue());
    }

    @Test
    public void testCreateAndFillJobWithAJobVariableNotPresentInTheWorkflowXml() throws Exception {
        Map<String, String> variablesMap = Maps.newHashMap();
        String expectedPcaInstanceId = "42";
        String variablePcaInstanceId = "PCA_INSTANCE_ID";
        variablesMap.put(variablePcaInstanceId, expectedPcaInstanceId);
        TaskFlowJob testJob = (TaskFlowJob) factory.createJob(jobDescriptorNoVariablesUri, variablesMap, null);
        assertEquals(expectedPcaInstanceId, testJob.getVariables().get(variablePcaInstanceId).getValue());
    }

    /**
     * The next 3 tests are there to check that parsing a workflow XML description involving XML elements
     * with more than 1 attribute (defined in any order) returns an object description with expected values
     * in corresponding data structures.
     */

    @Test
    public void testJobCreationAttributeOrderDefinitionGenericInformationXmlElement()
            throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorAttrDefGenericInformationXmlElement);
        Map<String, String> genericInformation = job.getTask("task").getGenericInformation();
        assertExpectedKeyValueEntriesMatch(genericInformation);
    }

    @Test
    public void testJobCreationAttributeOrderDefinitionParameterXmlElement()
            throws URISyntaxException, JobCreationException, IOException, ClassNotFoundException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorAttrDefParameterXmlElement);
        Map<String, Serializable> arguments = ((JavaTask) job.getTask("task")).getArguments();
        assertExpectedKeyValueEntriesMatch(arguments);
    }

    @Test
    public void testJobCreationAttributeOrderDefinitionVariableXmlElement()
            throws URISyntaxException, JobCreationException {
        Job job = factory.createJob(jobDescriptorAttrDefVariableXmlElement);
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
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorTaskVariable);
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

    @Test
    public void testUnresolvedJobVariables() throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorWithUnresolvedGenericInfoAndVariables);
        Map<String, JobVariable> unresolvedVariables = job.getUnresolvedVariables();
        Map<String, JobVariable> variables = job.getVariables();
        assertEquals("value1", unresolvedVariables.get("variable1").getValue());
        assertEquals("value1", variables.get("variable1").getValue());
        assertEquals("${variable1}", unresolvedVariables.get("variable2").getValue());
        assertEquals("value1", variables.get("variable2").getValue());
    }

    @Test
    public void testUnresolvedGenericInformation() throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorWithUnresolvedGenericInfoAndVariables);
        Map<String, String> unresolvedGenericInformation = job.getUnresolvedGenericInformation();
        Map<String, String> genericInformation = job.getGenericInformation();
        assertEquals("${variable1}", unresolvedGenericInformation.get("info1"));
        assertEquals("value1", genericInformation.get("info1"));
    }

    @Test
    public void testUnresolvedTaskVariables() throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorWithUnresolvedGenericInfoAndVariables);
        Map<String, TaskVariable> unresolvedVariables = job.getTask("task").getUnresolvedVariables();
        Map<String, TaskVariable> variables = job.getTask("task").getVariables();
        // standard task variable definition, no referencing
        assertEquals("task_value1", unresolvedVariables.get("task_variable1").getValue());
        assertEquals("task_value1", variables.get("task_variable1").getValue());
        // task variable references another task variable
        assertEquals("${task_variable1}", unresolvedVariables.get("task_variable2").getValue());
        assertEquals("task_value1", variables.get("task_variable2").getValue());
        // task variable is inherited, has the same name as a job variable, and uses another task variable as default value
        assertEquals("${task_variable2}", unresolvedVariables.get("variable1").getValue());
        assertEquals("value1", variables.get("variable1").getValue());
        // task variable is not inherited, has the same name as a job variable, and uses another task variable as default value
        assertEquals("${task_variable2}", unresolvedVariables.get("variable2").getValue());
        assertEquals("task_value1", variables.get("variable2").getValue());
    }

    @Test
    public void testUnresolvedTaskGenericInformation() throws URISyntaxException, JobCreationException {
        TaskFlowJob job = (TaskFlowJob) factory.createJob(jobDescriptorWithUnresolvedGenericInfoAndVariables);
        Map<String, String> unresolvedGenericInformation = job.getTask("task").getUnresolvedGenericInformation();
        Map<String, String> genericInformation = job.getTask("task").getGenericInformation();
        // the gi references a task variable
        assertEquals("gi_${task_variable2}", unresolvedGenericInformation.get("task_generic_info1"));
        assertEquals("gi_task_value1", genericInformation.get("task_generic_info1"));
        // the gi references a task variable which inherits a job variable
        assertEquals("gi_${variable1}", unresolvedGenericInformation.get("task_generic_info2"));
        assertEquals("gi_value1", genericInformation.get("task_generic_info2"));
        // the gi references a task variable which overrides a job variable
        assertEquals("gi_${variable2}", unresolvedGenericInformation.get("task_generic_info3"));
        assertEquals("gi_task_value1", genericInformation.get("task_generic_info3"));
        // the gi overrides a job gi which references a non-inherited task variable (complicated case)
        assertEquals("gi_${variable2}", unresolvedGenericInformation.get("info1"));
        assertEquals("gi_task_value1", genericInformation.get("info1"));
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

}
