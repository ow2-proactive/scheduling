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

import java.net.URI;
import java.util.Map;

import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestStaxJobFactory {

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

}
