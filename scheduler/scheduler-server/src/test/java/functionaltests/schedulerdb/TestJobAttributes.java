package functionaltests.schedulerdb;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
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
        job.setCancelJobOnError(true);
        job.setDescription("desc");
        job.setProjectName("project");
        job.setName("name");
        job.setInputSpace("input");
        job.setOutputSpace("output");
        job.setGenericInformations(null);
        job.setPriority(JobPriority.HIGHEST);

        InternalJob jobData = defaultSubmitJob(job, DEFAULT_USER_NAME);
        Assert.assertNotNull(jobData.getId());
        Assert.assertEquals("name", jobData.getId().getReadableName());

        jobData = loadInternalJob(true, jobData.getId());

        Assert.assertNotNull(jobData.getId());
        Assert.assertEquals("name", jobData.getId().getReadableName());
        Assert.assertEquals(10, jobData.getMaxNumberOfExecution());
        Assert.assertEquals(true, jobData.isCancelJobOnError());
        Assert.assertEquals("name", jobData.getName());
        Assert.assertEquals("desc", jobData.getDescription());
        Assert.assertEquals("project", jobData.getProjectName());
        Assert.assertEquals("input", jobData.getInputSpace());
        Assert.assertEquals("output", jobData.getOutputSpace());
        Assert.assertEquals(JobPriority.HIGHEST, jobData.getPriority());
        Assert.assertNotNull(jobData.getGenericInformations());
        Assert.assertTrue(jobData.getGenericInformations().isEmpty());
    }

    @Test
    public void testGenericInformation() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        Map<String, String> genericInfo;
        InternalJob jobData;

        genericInfo = new HashMap<String, String>();
        job.setGenericInformations(genericInfo);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertNotNull(jobData.getGenericInformations());
        Assert.assertTrue(jobData.getGenericInformations().isEmpty());

        genericInfo = new HashMap<String, String>();
        genericInfo.put("p1", "v1");
        genericInfo.put("p2", "v2");
        job.setGenericInformations(genericInfo);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertEquals(2, jobData.getGenericInformations().size());
        Assert.assertEquals("v1", jobData.getGenericInformations().get("p1"));
        Assert.assertEquals("v2", jobData.getGenericInformations().get("p2"));

        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longString.append("0123456789abcdefghijklmnopqrstuvwxyz");
        }
        genericInfo = new HashMap<String, String>();
        genericInfo.put("longProperty", longString.toString());
        job.setGenericInformations(genericInfo);
        jobData = defaultSubmitJobAndLoadInternal(false, job);
        Assert.assertEquals(1, jobData.getGenericInformations().size());
        Assert.assertEquals(longString.toString(), jobData.getGenericInformations().get("longProperty"));
    }

}
