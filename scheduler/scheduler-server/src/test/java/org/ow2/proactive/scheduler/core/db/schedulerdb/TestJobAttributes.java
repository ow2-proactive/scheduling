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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.job.InternalJob;


public class TestJobAttributes extends BaseSchedulerDBTest {

    @Test
    public void testJobIdGeneration() throws Exception {
        // test job ids are sequential 
        for (int i = 0; i < 5; i++) {
            TaskFlowJob jobDef = new TaskFlowJob();
            for (int j = 0; j < 10; j++) {
                jobDef.addTask(createDefaultTask("task" + j));
            }
            InternalJob job = defaultSubmitJob(jobDef);
            Assert.assertEquals(String.valueOf(i + 1), job.getId().value());
        }
    }

    @Test
    public void testLargeStringValues() throws Exception {
        String description = createString(500);

        TaskFlowJob jobDef = new TaskFlowJob();
        jobDef.setDescription(description);

        InternalJob job = defaultSubmitJobAndLoadInternal(false, jobDef);
        Assert.assertEquals(description, job.getDescription());
    }

    @Test
    public void testJobAttribute() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setMaxNumberOfExecution(10);
        job.setOnTaskError(OnTaskError.CANCEL_JOB);
        job.setDescription("desc");
        job.setProjectName("project");
        job.setName("name");
        job.setInputSpace("input");
        job.setOutputSpace("output");
        job.setGenericInformation(null);
        job.setPriority(JobPriority.HIGHEST);

        InternalJob jobData = defaultSubmitJob(job, DEFAULT_USER_NAME);
        Assert.assertNotNull(jobData.getId());
        Assert.assertEquals("name", jobData.getId().getReadableName());

        jobData = loadInternalJob(true, jobData.getId());

        Assert.assertNotNull(jobData.getId());
        Assert.assertEquals("name", jobData.getId().getReadableName());
        Assert.assertEquals(10, jobData.getMaxNumberOfExecution());
        Assert.assertEquals(OnTaskError.CANCEL_JOB, jobData.getOnTaskErrorProperty().getValue());
        Assert.assertEquals("name", jobData.getName());
        Assert.assertEquals("desc", jobData.getDescription());
        Assert.assertEquals("project", jobData.getProjectName());
        Assert.assertEquals("input", jobData.getInputSpace());
        Assert.assertEquals("output", jobData.getOutputSpace());
        Assert.assertEquals(JobPriority.HIGHEST, jobData.getPriority());
        Assert.assertNotNull(jobData.getGenericInformation());
        Assert.assertTrue(jobData.getGenericInformation().isEmpty());
        Map<String, JobVariable> jobDataVariables = jobData.getVariables();
        Assert.assertNotNull(jobDataVariables);
        Assert.assertTrue(jobDataVariables.isEmpty());
    }

    @Test
    public void testGenericInformation() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        Map<String, String> genericInfo;
        InternalJob jobData;

        genericInfo = new HashMap<>();
        job.setGenericInformation(genericInfo);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertNotNull(jobData.getGenericInformation());
        Assert.assertTrue(jobData.getGenericInformation().isEmpty());

        genericInfo = new HashMap<>();
        genericInfo.put("p1", "v1");
        genericInfo.put("p2", "v2");
        job.setGenericInformation(genericInfo);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertEquals(2, jobData.getGenericInformation().size());
        Assert.assertEquals("v1", jobData.getGenericInformation().get("p1"));
        Assert.assertEquals("v2", jobData.getGenericInformation().get("p2"));

        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longString.append("0123456789abcdefghijklmnopqrstuvwxyz");
        }
        genericInfo = new HashMap<>();
        genericInfo.put("longProperty", longString.toString());
        job.setGenericInformation(genericInfo);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertEquals(1, jobData.getGenericInformation().size());
        Assert.assertEquals(longString.toString(), jobData.getGenericInformation().get("longProperty"));
    }

    @Test
    public void testJobVariables() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        InternalJob jobData;

        HashMap<String, JobVariable> jobVariables = new HashMap<>();
        job.setVariables(jobVariables);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertNotNull(jobData.getVariables());
        Assert.assertTrue(jobData.getVariables().isEmpty());

        jobVariables.put("var1", new JobVariable("var1", "value1", null));
        jobVariables.put("var2", new JobVariable("var2", "value2", null));
        job.setVariables(jobVariables);

        jobVariables = new HashMap<>();
        jobVariables.put("var1", new JobVariable("var1", "value1", null));
        jobVariables.put("var2", new JobVariable("var2", "value2", null));
        job.setVariables(jobVariables);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertEquals(2, jobData.getVariables().size());
        Assert.assertEquals("value1", jobData.getVariables().get("var1").getValue());
        Assert.assertEquals("value2", jobData.getVariables().get("var2").getValue());

        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longString.append("0123456789abcdefghijklmnopqrstuvwxyz");
        }
        jobVariables = new HashMap<>();
        jobVariables.put("longProperty", new JobVariable("longProperty", longString.toString()));
        job.setVariables(jobVariables);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertEquals(1, jobData.getVariables().size());
        Assert.assertEquals(longString.toString(), jobData.getVariables().get("longProperty").getValue());
    }

}
