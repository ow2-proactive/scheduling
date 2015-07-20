package functionaltests.jmx;

import java.io.Serializable;
import java.util.HashMap;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBean;
import functionaltests.RMFunctionalTest;
import functionaltests.SchedulerTHelper;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test against SCHEDULING-1676 .
 * <p>
 * Test checks that RuntimeDataMBean reports correct tasks number in
 * case if task was restarted after failure.
 * 
 * @author ProActive team
 *
 */
public class SchedulerRuntimeDataMBeanTest extends RMFunctionalTest {

    public static class FailingTestJavaTask extends JavaExecutable {
        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            throw new Exception("Test exception");
        }
    }

    @Test
    public void test() throws Exception {
        testAsAdmin();
        testAsUser();
    }

    private void testAsAdmin() throws Exception {
        final SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { SchedulerTHelper.admin_username,
                SchedulerTHelper.admin_password });
        JMXConnector adminJmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RMI)), env);

        try {
            MBeanServerConnection connection = adminJmxConnector.getMBeanServerConnection();
            final ObjectName beanName = new ObjectName(SchedulerJMXHelper.RUNTIMEDATA_MBEAN_NAME);
            RuntimeDataMBean bean = JMX.newMXBeanProxy(connection, beanName, RuntimeDataMBean.class);
            checkDataConsistent(bean);

            JobId jobId;

            jobId = SchedulerTHelper.submitJob(createJobWithFailingTask());

            SchedulerTHelper.waitForEventTaskWaitingForRestart(jobId, "task1");
            checkDataConsistent(bean);

            SchedulerTHelper.waitForEventTaskWaitingForRestart(jobId, "task1");
            checkDataConsistent(bean);

            SchedulerTHelper.waitForEventJobFinished(jobId, 60000);
            checkDataConsistent(bean);

            checkTasksData(bean, 0, 0, 1, 1);
            checkJobData(bean, jobId);

            jobId = SchedulerTHelper.submitJob(createJobWithMultinodeTask(100));
            Thread.sleep(5000);
            checkDataConsistent(bean);
            checkTasksData(bean, 1, 0, 1, 2);

            SchedulerTHelper.getSchedulerInterface().killJob(jobId);
            checkDataConsistent(bean);
            checkTasksData(bean, 0, 0, 1, 2);
        } finally {
            adminJmxConnector.close();
        }
    }

    private void testAsUser() throws Exception {
        final SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { SchedulerTHelper.user_username,
                SchedulerTHelper.user_password });
        JMXConnector userJmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(auth
                .getJMXConnectorURL(JMXTransportProtocol.RMI)), env);

        try {
            MBeanServerConnection connection = userJmxConnector.getMBeanServerConnection();
            final ObjectName beanName = new ObjectName(SchedulerJMXHelper.RUNTIMEDATA_MBEAN_NAME);
            RuntimeDataMBean bean = JMX.newMXBeanProxy(connection, beanName, RuntimeDataMBean.class);
            checkDataConsistent(bean);
        } finally {
            userJmxConnector.close();
        }
    }

    private void checkJobData(RuntimeDataMBean bean, JobId jobId) throws Exception {
        JobState jobState = SchedulerTHelper.getSchedulerInterface().getJobState(jobId);
        long pendingTime = bean.getJobPendingTime(jobId.value());
        long runningTime = bean.getJobRunningTime(jobId.value());
        Assert.assertEquals("Unexpected pending time", jobState.getStartTime() - jobState.getSubmittedTime(),
                pendingTime);
        Assert.assertEquals("Unexpected running time", jobState.getFinishedTime() - jobState.getStartTime(),
                runningTime);
        Assert.assertEquals("Unexpected nodes number", 1, bean.getTotalNumberOfNodesUsed(jobId.value()));

        bean.getMeanTaskPendingTime(jobId.value());
        bean.getMeanTaskRunningTime(jobId.value());
    }

    private void checkTasksData(RuntimeDataMBean bean, int pendingTasks, int runningTasks, int finishedTasks,
            int totalTasks) throws Exception {
        int pending = bean.getPendingTasksCount();
        int running = bean.getRunningTasksCount();
        int finished = bean.getFinishedTasksCount();
        int total = bean.getTotalTasksCount();
        Assert.assertEquals("Invalid pending tasks", pendingTasks, pending);
        Assert.assertEquals("Invalid running tasks", runningTasks, running);
        Assert.assertEquals("Invalid finished tasks", finishedTasks, finished);
        Assert.assertEquals("Invalid total tasks", totalTasks, total);
    }

    private void checkDataConsistent(RuntimeDataMBean bean) throws Exception {
        Scheduler scheduler = SchedulerTHelper.getSchedulerInterface();

        int pendingJobs = bean.getPendingJobsCount();
        int runningJobs = bean.getRunningJobsCount();
        int finishedJobs = bean.getFinishedJobsCount();
        int totalJobs = bean.getTotalJobsCount();

        int pendingTasks = bean.getPendingTasksCount();
        int runningTasks = bean.getRunningTasksCount();
        int finishedTasks = bean.getFinishedTasksCount();
        int totalTasks = bean.getTotalTasksCount();

        System.out.println("Jobs: pending: " + pendingJobs + ", running: " + runningJobs + " " +
            ", finished " + finishedJobs + ", total: " + totalJobs);
        System.out.println("Tasks: pending: " + pendingTasks + ", running: " + runningTasks + ", finished " +
            finishedTasks + ", total " + totalTasks);

        SchedulerState state = scheduler.getState();

        Assert.assertEquals(state.getPendingJobs().size(), bean.getPendingJobsCount());
        Assert.assertEquals(state.getRunningJobs().size(), bean.getRunningJobsCount());
        Assert.assertEquals(state.getFinishedJobs().size(), bean.getFinishedJobsCount());
        Assert.assertEquals(bean.getPendingJobsCount() + bean.getRunningJobsCount() +
            bean.getFinishedJobsCount(), bean.getTotalJobsCount());

        Assert.assertTrue("Invalid pending tasks: " + pendingTasks, pendingTasks >= 0);
        Assert.assertTrue("Invalid running tasks: " + runningTasks, runningTasks >= 0);
        Assert.assertTrue("Invalid finished tasks: " + finishedTasks, finishedTasks >= 0);
    }

    private TaskFlowJob createJobWithFailingTask() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_FailingTask");
        job.setCancelJobOnError(false);
        JavaTask task = new JavaTask();
        task.setExecutableClassName(FailingTestJavaTask.class.getName());
        task.setName("task1");
        task.setMaxNumberOfExecution(3);
        task.setCancelJobOnError(false);
        job.addTask(task);
        return job;
    }

    private TaskFlowJob createJobWithMultinodeTask(int nodes) throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_MultinodeTask");
        JavaTask task = new JavaTask();
        task.setExecutableClassName(FailingTestJavaTask.class.getName());
        task.setName("task1");
        ParallelEnvironment env = new ParallelEnvironment(nodes);
        task.setParallelEnvironment(env);
        job.addTask(task);
        return job;
    }

}
