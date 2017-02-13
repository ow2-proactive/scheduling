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
package functionaltests.monitor;

import static functionaltests.utils.RMTHelper.log;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;

import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.TestUsers;


/**
 * Test checks that the correct RMNode monitoring MBeans with certain attributes
 * are exposed.
 */
public class TestRMNodeMonitoring extends RMFunctionalTest {

    public static Map<String, String[]> mbeans = new HashMap<>();

    /* set of MBeans and its attributes to be checked. */
    static {
        mbeans.put("sigar:Type=Cpu", new String[] { "Mhz", "TotalCores" });
        mbeans.put("sigar:Type=Mem", new String[] { "Total", "Free", "ActualFree" });
        mbeans.put("sigar:Type=CpuUsage", new String[] { "Idle" });
        mbeans.put("sigar:Type=Processes", new String[] { "Processes" });
        mbeans.put("sigar:Type=FileSystem,Name=*", new String[] { "DirName", "Total", "Used" });
        mbeans.put("sigar:Type=NetInterface,Name=*", new String[] { "Name", "RxBytes", "TxBytes", "Speed", "Hwaddr" });
    }

    @Test
    public void action() throws Exception {
        /*
         * prepare a Node Source with some nodes and get the JMX URL of any RMNode in it
         */
        String jmxurl = prepareEnvAndGetRMNodeJmxUrl();

        /* get a jmx connector to retrieve monitoring info from the RMNode */
        JMXConnector jmxConnector = connectToRMNode(jmxurl);

        /* check the existence of each MBean and its attributes */
        for (String mbean : mbeans.keySet()) {
            String[] atts = mbeans.get(mbean);
            for (String att : atts) {
                log("Checking MBean '" + mbean + "', attribute '" + att + "'...");
                Object ret = getJMXSigarAttribute(jmxConnector, mbean, att);
                assertNotNull("MBean " + mbean + " att " + att + " not found.", ret);
            }
        }

        log("End of test");
    }

    private String prepareEnvAndGetRMNodeJmxUrl() throws Exception {
        rmHelper.createNodeSource("TestRMNodeMonitoring");
        log("Checking existence of Sigar MBeans...");

        String jmxurl = null;

        RMInitialState state = ((RMMonitorEventReceiver) rmHelper.getResourceManager()).getInitialState();
        jmxurl = state.getNodesEvents().get(0).getDefaultJMXUrl();

        assertNotNull("The JMX URL of a node could not be obtained.", jmxurl);

        log("JMX URL obtained: " + jmxurl);

        return jmxurl;

    }

    private JMXConnector connectToRMNode(String jmxurl) throws Exception {
        Map<String, Object> env = new HashMap<>();
        String[] creds = { TestUsers.ADMIN.username, TestUsers.ADMIN.password };
        env.put(JMXConnector.CREDENTIALS, creds);
        return JMXConnectorFactory.connect(new JMXServiceURL(jmxurl), env);
    }

    private Object getJMXSigarAttribute(JMXConnector connector, String objname, String attribute)
            throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {

        ObjectName name = null;

        log("Looking for MBean '" + objname + "'...");
        Set<ObjectName> names = connector.getMBeanServerConnection().queryNames(new ObjectName(objname), null);
        for (ObjectName o : names) {
            log("  found: " + o.getCanonicalName());
            name = o;
        }

        if (name == null) {
            log("Not found any '" + objname + "'.");
            return null;
        }

        return connector.getMBeanServerConnection().getAttribute(name, attribute);
    }
}
