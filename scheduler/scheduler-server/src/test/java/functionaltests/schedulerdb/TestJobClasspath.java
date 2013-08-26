package functionaltests.schedulerdb;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.core.db.JobClasspathContent;
import org.ow2.proactive.scheduler.job.InternalJob;


public class TestJobClasspath extends BaseSchedulerDBTest {

    @Test
    public void testEmptyClasspath() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        JobEnvironment env = new JobEnvironment();
        job.setEnvironment(env);

        InternalJob jobData;

        jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertNull(jobData.getEnvironment().getJobClasspath());
    }

    @Test
    public void testMultipleJars() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        final String[] classpath = { "lib/ProActive/ProActive.jar", "compile/lib/ant.jar" };

        JobEnvironment env = new JobEnvironment();
        env.setJobClasspath(classpath);
        job.setEnvironment(env);

        InternalJob jobData;

        jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(env.getJobClasspathCRC(), jobData.getEnvironment().getJobClasspathCRC());
        Assert.assertArrayEquals(classpath, jobData.getEnvironment().getJobClasspath());

        JobClasspathContent classpathContent = dbManager.loadJobClasspathContent(jobData.getEnvironment()
                .getJobClasspathCRC());
        Assert.assertTrue(classpathContent.isContainsJarFiles());
        Assert.assertTrue(classpathContent.getClasspathContent().length > 0);
    }

    @Test
    public void testClasspath() throws Exception {
        TaskFlowJob job = new TaskFlowJob();

        final String classpath1 = "lib/ProActive/ProActive.jar";

        JobEnvironment env = new JobEnvironment();
        env.setJobClasspath(new String[] { classpath1 });
        job.setEnvironment(env);

        InternalJob jobData;

        jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(1, jobData.getEnvironment().getJobClasspath().length);
        Assert.assertEquals(env.getJobClasspathCRC(), jobData.getEnvironment().getJobClasspathCRC());
        Assert.assertEquals(classpath1, jobData.getEnvironment().getJobClasspath()[0]);

        JobClasspathContent classpathContent = dbManager.loadJobClasspathContent(jobData.getEnvironment()
                .getJobClasspathCRC());
        Assert.assertTrue(classpathContent.isContainsJarFiles());
        Assert.assertTrue(classpathContent.getClasspathContent().length > 0);

        final String classpath2 = "lib/ProActive//ProActive.jar";

        job = new TaskFlowJob();
        env = new JobEnvironment();
        env.setJobClasspath(new String[] { classpath2 });
        job.setEnvironment(env);

        jobData = defaultSubmitJobAndLoadInternal(true, job);
        Assert.assertEquals(1, jobData.getEnvironment().getJobClasspath().length);
        Assert.assertEquals(classpath2, jobData.getEnvironment().getJobClasspath()[0]);

        Session session = dbManager.getSessionFactory().openSession();
        try {
            Assert.assertEquals(1, session.createCriteria(JobClasspathContent.class).list().size());
        } finally {
            session.close();
        }
    }

}
