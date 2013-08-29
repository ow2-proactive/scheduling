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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.utils;

import java.util.HashMap;
import java.util.Random;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.ow2.proactive.jmx.provider.JMXProviderUtils;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


public class TestUtils {

    private static Random random = new Random();

    public static JMXConnector jmxConnect(String url, String login, String password) throws Exception {
        final HashMap<String, Object> env = new HashMap<String, Object>(1);
        env.put(JMXConnector.CREDENTIALS, new Object[] { login, password });
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, JMXProviderUtils.RO_PROVIDER_PKGS);
        return JMXConnectorFactory.connect(new JMXServiceURL(url), env);
    }

    public static String getRequiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null) {
            throw new IllegalArgumentException("Property '" + name + "' isn't set");
        }
        return value;
    }

    public static TopologyDescriptor getTopologyDescriptor(String topology) {
        if (topology == null || topology.isEmpty()) {
            return TopologyDescriptor.ARBITRARY;
        }

        if (topology.equals("ARBITRARY")) {
            return TopologyDescriptor.ARBITRARY;
        } else if (topology.equals("BEST_PROXIMITY")) {
            return TopologyDescriptor.BEST_PROXIMITY;
        } else if (topology.equals("DIFFERENT_HOSTS_EXCLUSIVE")) {
            return TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE;
        } else if (topology.equals("MULTIPLE_HOSTS_EXCLUSIVE")) {
            return TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE;
        } else if (topology.equals("SINGLE_HOST")) {
            return TopologyDescriptor.SINGLE_HOST;
        } else if (topology.equals("SINGLE_HOST_EXCLUSIVE")) {
            return TopologyDescriptor.SINGLE_HOST_EXCLUSIVE;
        } else {
            throw new IllegalArgumentException("Invalid topology name: " + topology);
        }
    }

    public static SelectionScript createSimpleSelectionScript(boolean returnValue, boolean dynamicContent,
            boolean dynamicScript) throws Exception {
        StringBuilder scriptText = new StringBuilder();
        if (dynamicContent) {
            scriptText.append(String.format("// dummy comment %d, %d\n", System.currentTimeMillis(), random
                    .nextLong()));
        }
        scriptText.append(String.format("selected = %s;", String.valueOf(returnValue)));
        return new SelectionScript(scriptText.toString(), "JavaScript", dynamicScript);
    }

}
