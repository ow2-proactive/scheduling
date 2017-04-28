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
package functionaltests.jmx;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.jmx.naming.JMXTransportProtocol;
import org.ow2.proactive.scheduler.common.JobFilterCriteria;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ParallelEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBean;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import functionaltests.utils.TestUsers;


/**
 * Test against SCHEDULING-1676 .
 * <p>
 * Test checks that RuntimeDataMBean reports correct tasks number in
 * case if task was restarted after failure.
 * 
 * @author ProActive team
 *
 */
public class SchedulerRuntimeDataMBeanTest extends SchedulerFunctionalTestNoRestart {

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
        final SchedulerAuthenticationInterface auth = schedulerHelper.getSchedulerAuth();
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { TestUsers.DEMO.username, TestUsers.DEMO.password });
        JMXConnector adminJmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(auth.getJMXConnectorURL(JMXTransportProtocol.RMI)),
                                                                     env);

        List<JobInfo> existingFinishedJobs = schedulerHelper.getSchedulerInterface()
                                                            .getJobs(0,
                                                                     1000,
                                                                     new JobFilterCriteria(false,
                                                                                           false,
                                                                                           true,
                                                                                           true),
                                                                     null)
                                                            .getList();

        int nbFinishedTasks = 0;
        int nbTotalTasks = 0;
        for (JobInfo existingFinishedJob : existingFinishedJobs) {
            nbFinishedTasks += existingFinishedJob.getNumberOfFinishedTasks();
            nbTotalTasks += existingFinishedJob.getTotalNumberOfTasks();
        }

        try {
            MBeanServerConnection connection = adminJmxConnector.getMBeanServerConnection();
            final ObjectName beanName = new ObjectName(SchedulerJMXHelper.RUNTIMEDATA_MBEAN_NAME);
            RuntimeDataMBean bean = JMX.newMXBeanProxy(connection, beanName, RuntimeDataMBean.class);
            checkDataConsistent(bean);

            JobId jobId;

            jobId = schedulerHelper.submitJob(createJobWithFailingTask());

            schedulerHelper.waitForEventTaskWaitingForRestart(jobId, "task1");
            checkDataConsistent(bean);

            schedulerHelper.waitForEventTaskWaitingForRestart(jobId, "task1");
            checkDataConsistent(bean);

            schedulerHelper.waitForEventJobFinished(jobId, 60000);
            checkDataConsistent(bean);

            checkTasksData(bean, 0, 0, 1 + nbFinishedTasks, 1 + nbTotalTasks);
            checkJobData(bean, jobId);

            jobId = schedulerHelper.submitJob(createJobWithMultinodeTask(100));
            Thread.sleep(5000);
            checkDataConsistent(bean);
            checkTasksData(bean, 1, 0, 1 + nbFinishedTasks, 2 + nbTotalTasks);

            schedulerHelper.getSchedulerInterface().killJob(jobId);
            checkDataConsistent(bean);
            checkTasksData(bean, 0, 0, 1 + nbFinishedTasks, 2 + nbTotalTasks);
        } finally {
            adminJmxConnector.close();
        }
    }

    private void testAsUser() throws Exception {
        final SchedulerAuthenticationInterface auth = schedulerHelper.getSchedulerAuth();
        final HashMap<String, Object> env = new HashMap<>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { TestUsers.USER.username, TestUsers.USER.password });
        JMXConnector userJmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(auth.getJMXConnectorURL(JMXTransportProtocol.RMI)),
                                                                    env);

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
        JobState jobState = schedulerHelper.getSchedulerInterface().getJobState(jobId);
        long pendingTime = bean.getJobPendingTime(jobId.value());
        long runningTime = bean.getJobRunningTime(jobId.value());
        assertEquals("Unexpected pending time", jobState.getStartTime() - jobState.getSubmittedTime(), pendingTime);
        assertEquals("Unexpected running time", jobState.getFinishedTime() - jobState.getStartTime(), runningTime);
        assertEquals("Unexpected nodes number", 1, bean.getTotalNumberOfNodesUsed(jobId.value()));

        bean.getMeanTaskPendingTime(jobId.value());
        bean.getMeanTaskRunningTime(jobId.value());
    }

    private void checkTasksData(RuntimeDataMBean bean, int pendingTasks, int runningTasks, int finishedTasks,
            int totalTasks) throws Exception {
        int pending = bean.getPendingTasksCount();
        int running = bean.getRunningTasksCount();
        int finished = bean.getFinishedTasksCount();
        int total = bean.getTotalTasksCount();
        assertEquals("Invalid pending tasks", pendingTasks, pending);
        assertEquals("Invalid running tasks", runningTasks, running);
        assertEquals("Invalid finished tasks", finishedTasks, finished);
        assertEquals("Invalid total tasks", totalTasks, total);
    }

    private void checkDataConsistent(RuntimeDataMBean bean) throws Exception {
        Scheduler scheduler = schedulerHelper.getSchedulerInterface();

        int pendingJobs = bean.getPendingJobsCount();
        int runningJobs = bean.getRunningJobsCount();
        int finishedJobs = bean.getFinishedJobsCount();
        int totalJobs = bean.getTotalJobsCount();

        int pendingTasks = bean.getPendingTasksCount();
        int runningTasks = bean.getRunningTasksCount();
        int finishedTasks = bean.getFinishedTasksCount();
        int totalTasks = bean.getTotalTasksCount();

        System.out.println("Jobs: pending: " + pendingJobs + ", running: " + runningJobs + " " + ", finished " +
                           finishedJobs + ", total: " + totalJobs);
        System.out.println("Tasks: pending: " + pendingTasks + ", running: " + runningTasks + ", finished " +
                           finishedTasks + ", total " + totalTasks);

        SchedulerState state = scheduler.getState();

        assertEquals(state.getPendingJobs().size(), bean.getPendingJobsCount());
        assertEquals(state.getRunningJobs().size(), bean.getRunningJobsCount());
        assertEquals(state.getFinishedJobs().size(), bean.getFinishedJobsCount());
        assertEquals(bean.getPendingJobsCount() + bean.getRunningJobsCount() + bean.getFinishedJobsCount(),
                     bean.getTotalJobsCount());

        Assert.assertTrue("Invalid pending tasks: " + pendingTasks, pendingTasks >= 0);
        Assert.assertTrue("Invalid running tasks: " + runningTasks, runningTasks >= 0);
        Assert.assertTrue("Invalid finished tasks: " + finishedTasks, finishedTasks >= 0);
    }

    private TaskFlowJob createJobWithFailingTask() throws Exception {
        TaskFlowJob job = new TaskFlowJob();
        job.setName(this.getClass().getSimpleName() + "_FailingTask");
        job.setOnTaskError(OnTaskError.CONTINUE_JOB_EXECUTION);
        JavaTask task = new JavaTask();
        task.setExecutableClassName(FailingTestJavaTask.class.getName());
        task.setName("task1");
        task.setMaxNumberOfExecution(3);
        task.setOnTaskError(OnTaskError.NONE);
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
