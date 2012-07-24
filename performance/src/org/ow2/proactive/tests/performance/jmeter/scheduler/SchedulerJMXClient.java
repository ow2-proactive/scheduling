package org.ow2.proactive.tests.performance.jmeter.scheduler;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.scheduler.core.jmx.SchedulerJMXHelper;
import org.ow2.proactive.scheduler.core.jmx.mbean.AllAccountsMBean;
import org.ow2.proactive.scheduler.core.jmx.mbean.ManagementMBean;
import org.ow2.proactive.scheduler.core.jmx.mbean.MyAccountMBean;
import org.ow2.proactive.scheduler.core.jmx.mbean.RuntimeDataMBean;
import org.ow2.proactive.tests.performance.utils.TestUtils;


/**
 * Test scenario 'Use Scheduler JMX'.
 * <p/>
 * Scenario connects to the JMX interface provided by the SchedulerJMXClient 
 * and invokes various operations provided by the SchedulerJMXClient MBeans. 
 * It measures time required to call getters on the MyAccountMBean and RuntimeDataMBean.
 * 
 * @author ProActive team
 *
 */
public class SchedulerJMXClient extends BaseJMeterSchedulerClient {

    protected String jmxROUrl;

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        jmxROUrl = getScheduler().getJmxROUrl();
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        logInfo("Connecting to the Scheduler JMX using URL " + jmxROUrl);

        SampleResult result = new SampleResult();
        result.setSuccessful(true);

        JMXConnector jmxConnector;

        SchedulerConnectionParameters connectionParameters = new SchedulerConnectionParameters(context);
        jmxConnector = TestUtils.jmxConnect(jmxROUrl, connectionParameters.getSchedulerLogin(),
                connectionParameters.getSchedulerPassword());
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            result.sampleStart();
            checkMyAccountsMBean(connection, result);
            checkRuntimeDataMBean(connection, result);
            result.sampleEnd();
        } finally {
            jmxConnector.close();
        }

        // connect as admin to get access to the AllAccountsMBean and ManagementMBean
        jmxConnector = TestUtils.jmxConnect(jmxROUrl, DEFAULT_ADMIN_LOGIN, DEFAULT_ADMIN_PASSWORD);
        try {
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            checkAllAccountsMBean(connection, connectionParameters.getSchedulerLogin(), result);
            checkManagementMBean(connection, result);
        } finally {
            jmxConnector.close();
        }

        return result;
    }

    private void checkRuntimeDataMBean(MBeanServerConnection connection, SampleResult result)
            throws Exception {
        final ObjectName beanName = new ObjectName(SchedulerJMXHelper.RUNTIMEDATA_MBEAN_NAME);
        RuntimeDataMBean bean = JMX.newMXBeanProxy(connection, beanName, RuntimeDataMBean.class);

        assertTrue(bean.getPendingJobsCount() >= 0, "Unexpected PendingJobsCount", result);
        assertTrue(bean.getRunningJobsCount() >= 0, "Unexpected RunningJobsCount", result);
        assertTrue(bean.getFinishedJobsCount() >= 0, "Unexpected FinishedJobsCount", result);
        assertTrue(bean.getJobSubmittingPeriod() >= 0, "Unexpected JobSubmittingPeriod", result);
        assertTrue(bean.getMeanJobExecutionTime() >= 0, "Unexpected MeanJobExecutionTime", result);
        assertTrue(bean.getMeanJobPendingTime() >= 0, "Unexpected MeanJobPendingTime", result);
        assertTrue(!bean.getStatus().isEmpty(), "Unexpected status", result);
        assertTrue(bean.getTotalJobsCount() >= 0, "Unexpected TotalJobsCount", result);
        assertTrue(bean.getTotalTasksCount() >= 0, "Unexpected TotalTasksCount", result);

        // uncomment when SCHEDULING-1676, SCHEDULING-1677 are fixed 
        /*
        assertTrue(bean.getConnectedUsersCount() >= 1, "Unexpected ConnectedUsersCount:" +
                bean.getConnectedUsersCount(), result);
        assertTrue(bean.getPendingTasksCount() >= 0, "Unexpected PendingTasksCount", result);
        assertTrue(bean.getFinishedTasksCount() >= 0, "Unexpected FinishedTasksCount", result);
        assertTrue(bean.getRunningTasksCount() >= 0, "Unexpected RunningTasksCount", result);
         */
    }

    private void checkAllAccountsMBean(MBeanServerConnection connection, String username, SampleResult result)
            throws Exception {
        final ObjectName beanName = new ObjectName(SchedulerJMXHelper.ALLACCOUNTS_MBEAN_NAME);
        AllAccountsMBean bean = JMX.newMXBeanProxy(connection, beanName, AllAccountsMBean.class);
        bean.setUsername(username);
        assertAccountBean(bean, result);
    }

    private void checkMyAccountsMBean(MBeanServerConnection connection, SampleResult result) throws Exception {
        final ObjectName beanName = new ObjectName(SchedulerJMXHelper.MYACCOUNT_MBEAN_NAME);
        MyAccountMBean bean = JMX.newMXBeanProxy(connection, beanName, MyAccountMBean.class);
        assertAccountBean(bean, result);
    }

    private void assertAccountBean(MyAccountMBean bean, SampleResult result) {
        assertTrue(bean.getTotalJobCount() >= 0, "Unexpected TotalJobCount", result);
        assertTrue(bean.getTotalJobDuration() >= 0, "Unexpected TotalJobDuration", result);
        assertTrue(bean.getTotalTaskCount() >= 0, "Unexpected TotalTaskCount", result);
        assertTrue(bean.getTotalTaskDuration() >= 0, "Unexpected TotalTaskDuration", result);
    }

    private void checkManagementMBean(MBeanServerConnection connection, SampleResult result) throws Exception {
        final ObjectName beanName = new ObjectName(SchedulerJMXHelper.MANAGEMENT_MBEAN_NAME);
        ManagementMBean bean = JMX.newMXBeanProxy(connection, beanName, ManagementMBean.class);
        bean.getAccountingCacheValidityTimeInSeconds();
        bean.getLastRefreshDurationInMilliseconds();
    }

}
