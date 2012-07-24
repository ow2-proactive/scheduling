package org.ow2.proactive.tests.performance.jmeter.rm;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.AllAccountsMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.ManagementMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.MyAccountMBean;
import org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean;
import org.ow2.proactive.tests.performance.utils.TestUtils;


/**
 * Test scenario 'Use RM JMX'.
 * <p/>
 * Scenario connects to the JMX interface provided by the RM and invokes various 
 * operations provided by the RM MBeans. It measures time required to call 
 * getters on the MyAccountMBean and RuntimeDataMBean.
 * 
 * @author ProActive team
 *
 */
public class RMJMXClient extends BaseJMeterRMClient {

    protected String jmxROUrl;

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        jmxROUrl = getResourceManager().getJmxROUrl();
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        logInfo("Connecting to the RM JMX using URL " + jmxROUrl);

        SampleResult result = new SampleResult();
        result.setSuccessful(true);

        JMXConnector jmxConnector;

        RMConnectionParameters connectionParameters = new RMConnectionParameters(context);
        jmxConnector = TestUtils.jmxConnect(jmxROUrl, connectionParameters.getRmLogin(), connectionParameters
                .getRmPassword());
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
            checkAllAccountsMBean(connection, connectionParameters.getRmLogin(), result);
            checkManagementMBean(connection, result);
        } finally {
            jmxConnector.close();
        }

        return result;
    }

    private void checkManagementMBean(MBeanServerConnection connection, SampleResult result) throws Exception {
        final ObjectName beanName = new ObjectName(RMJMXHelper.MANAGEMENT_MBEAN_NAME);
        ManagementMBean bean = JMX.newMXBeanProxy(connection, beanName, ManagementMBean.class);
        bean.getAccountingCacheValidityTimeInSeconds();
        bean.getLastRefreshDurationInMilliseconds();
    }

    private void checkRuntimeDataMBean(MBeanServerConnection connection, SampleResult result)
            throws Exception {
        final ObjectName beanName = new ObjectName(RMJMXHelper.RUNTIMEDATA_MBEAN_NAME);
        RuntimeDataMBean bean = JMX.newMXBeanProxy(connection, beanName, RuntimeDataMBean.class);

        assertTrue(bean.getAvailableNodesCount() > 0, "Unexpected AvailableNodesCount: " +
            bean.getAvailableNodesCount(), result);
        assertTrue(bean.getAverageActivity() >= 0, "Unexpected AverageActivity", result);
        assertTrue(bean.getAverageInactivity() >= 0, "Unexpected AverageInactivity", result);
        assertTrue(bean.getBusyNodesCount() >= 0, "Unexpected BusyNodesCount", result);
        assertTrue(bean.getConfiguringNodesCount() >= 0, "Unexpected ConfiguringNodesCount", result);
        assertTrue(bean.getDeployingNodesCount() >= 0, "Unexpected DeployingNodesCount", result);
        assertTrue(bean.getDownNodesCount() >= 0, "Unexpected DownNodesCount", result);
        assertTrue(bean.getFreeNodesCount() >= 0, "Unexpected FreeNodesCount", result);
        assertTrue(bean.getLostNodesCount() >= 0, "Unexpected LostNodesCount", result);
        assertTrue(bean.getMaxBusyNodes() >= 0, "Unexpected MaxBusyNodes", result);
        assertTrue(bean.getMaxConfiguringNodes() >= 0, "Unexpected MaxConfiguringNodes", result);
        assertTrue(bean.getMaxDeployingNodes() >= 0, "Unexpected MaxDeployingNodes", result);
        assertTrue(bean.getMaxDownNodes() >= 0, "Unexpected MaxDownNodes", result);
        assertTrue(bean.getMaxFreeNodes() >= 0, "Unexpected MaxFreeNodes", result);
        assertTrue(bean.getMaxLostNodes() >= 0, "Unexpected MaxLostNodes", result);
    }

    private void checkAllAccountsMBean(MBeanServerConnection connection, String username, SampleResult result)
            throws Exception {
        final ObjectName beanName = new ObjectName(RMJMXHelper.ALLACCOUNTS_MBEAN_NAME);
        AllAccountsMBean bean = JMX.newMXBeanProxy(connection, beanName, AllAccountsMBean.class);
        bean.setUsername(username);
        assertAccountBean(bean, result);
    }

    private void checkMyAccountsMBean(MBeanServerConnection connection, SampleResult result) throws Exception {
        final ObjectName beanName = new ObjectName(RMJMXHelper.MYACCOUNT_MBEAN_NAME);
        MyAccountMBean bean = JMX.newMXBeanProxy(connection, beanName, MyAccountMBean.class);
        assertAccountBean(bean, result);
    }

    private void assertAccountBean(MyAccountMBean bean, SampleResult result) {
        assertTrue(bean.getProvidedNodesCount() >= 0, "Invalid nodes count: " + bean.getProvidedNodesCount(),
                result);
        assertTrue(bean.getUsedNodeTime() >= 0, "Invalid nodes count: " + bean.getProvidedNodesCount(),
                result);
        assertTrue(bean.getProvidedNodeTime() >= 0, "Invalid nodes count: " + bean.getProvidedNodesCount(),
                result);
    }

}
