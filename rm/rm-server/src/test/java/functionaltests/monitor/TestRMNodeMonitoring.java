/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionaltests.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import functionaltests.RMConsecutive;
import functionaltests.RMTHelper;
import org.junit.Assert;


/**
 * Test checks that the correct RMNode monitoring MBeans with certain attributes
 * are exposed.
 */
public class TestRMNodeMonitoring extends RMConsecutive {

    public static Map<String, String[]> mbeans = new HashMap<>();

    /* set of MBeans and its attributes to be checked. */
    static {
        mbeans.put("sigar:Type=Cpu", new String[] { "Mhz", "TotalCores" });
        mbeans.put("sigar:Type=Mem", new String[] { "Total", "Free", "ActualFree" });
        mbeans.put("sigar:Type=CpuUsage", new String[] { "Idle" });
        mbeans.put("sigar:Type=Processes", new String[] { "Processes" });
        mbeans.put("sigar:Type=FileSystem,Name=*", new String[] { "DirName", "Total", "Used" });
        mbeans.put("sigar:Type=NetInterface,Name=*", new String[] { "Name", "RxBytes", "TxBytes", "Speed",
                "Hwaddr" });
    }

    @org.junit.Test
    public void action() throws Exception {
        /*
         * prepare a Node Source with some nodes and get the JMX URL of any
         * RMNode in it
         */
        String jmxurl = prepareEnvAndGetRMNodeJmxUrl();

        /* get a jmx connector to retrieve monitoring info from the RMNode */
        JMXConnector jmxConnector = connectToRMNode(jmxurl);

        /* check the existence of each MBean and its attributes */
        for (String mbean : mbeans.keySet()) {
            String[] atts = mbeans.get(mbean);
            for (String att : atts) {
                Object ret = null;
                try {
                    RMTHelper.log("Checking MBean '" + mbean + "', attribute '" + att + "'...");
                    ret = getJMXSigarAttribute(jmxConnector, mbean, att);
                } catch (Exception e) {
                    // Ignore it.
                }
                Assert.assertTrue("MBean " + mbean + " att " + att + " not found.", ret != null);
            }
        }

        RMTHelper.log("End of test");

    }

    private String prepareEnvAndGetRMNodeJmxUrl() throws Exception {

        RMTHelper helper = RMTHelper.getDefaultInstance();

        helper.createNodeSource("TestRMNodeMonitoring");
        RMTHelper.log("Checking existence of Sigar MBeans...");

        String jmxurl = null;

        RMInitialState state = RMTHelper.getDefaultInstance().getResourceManager().getMonitoring().getState();
        for (RMNodeEvent ne : state.getNodesEvents()) {
            jmxurl = ne.getDefaultJMXUrl();
            break;
        }

        Assert.assertTrue("The JMX URL of a node could not be obtained.", jmxurl != null);

        RMTHelper.log("JMX URL obtained: " + jmxurl);

        return jmxurl;

    }

    private JMXConnector connectToRMNode(String jmxurl) throws Exception {
        Map<String, Object> env = new HashMap<>();
        String[] creds = { RM_USER_TEST, RM_PASS_TEST };
        env.put(JMXConnector.CREDENTIALS, creds);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxurl), env);

        return jmxConnector;
    }

    private Object getJMXSigarAttribute(JMXConnector connector, String objname, String attribute)
            throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException,
            MBeanException, ReflectionException, IOException {

        ObjectName name = null;

        RMTHelper.log("Looking for MBean '" + objname + "'...");
        Set<ObjectName> names = connector.getMBeanServerConnection()
                .queryNames(new ObjectName(objname), null);
        for (ObjectName o : names) {
            RMTHelper.log("  found: " + o.getCanonicalName());
            name = o;
        }

        if (name == null) {
            RMTHelper.log("Not found any '" + objname + "'.");
            return null;
        }

        return connector.getMBeanServerConnection().getAttribute(name, attribute);
    }
}
