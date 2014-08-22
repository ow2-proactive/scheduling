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
package org.ow2.proactive.perftests.rest.jmeter.rm;

import java.io.IOException;
import java.util.List;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.util.JMeterUtils;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive.perftests.rest.jmeter.BaseRESTfulClient;


/**
 * BaseRESTfulRMClient contains set of methods which are common to all
 * RESTfulRMClients. A child class can use these utility methods to retrieve the
 * list of known host or nodes in order to query some specific information from
 * each.
 */
public abstract class BaseRESTfulRMClient extends BaseRESTfulClient implements JavaSamplerClient {

    public static final String PROP_CLIENT_REFRESH_TIME = "clientRefreshTime";

    private RESTfulRMConnection conn;

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();
        RESTfulRMConnection.addDefaultParamters(defaultParameters);
        defaultParameters.addArgument(PROP_CLIENT_REFRESH_TIME, "${clientRefreshTime}");
        return defaultParameters;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);
        conn = new RESTfulRMConnection(context);
    }

    protected RESTfulRMConnection getConnection() {
        return conn;
    }

    protected List<Host> getMonitoringHostSet() {
        String serialized = JMeterUtils.getProperty(clientSpecificHostPropKey());
        if (serialized != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Host> hostSet = (List<Host>) getObjectMapper().readValue(serialized.getBytes(),
                        new TypeReference<List<Host>>() {
                        });
                return hostSet;
            } catch (IOException ioe) {
                logError("", ioe);
            }
        }
        return null;
    }

    protected List<Node> getMonitoringNodeSet() {
        String serialized = JMeterUtils.getProperty(clientSpecificNodePropKey());
        if (serialized != null) {
            try {
                @SuppressWarnings("unchecked")
                List<Node> nodeSet = (List<Node>) getObjectMapper().readValue(serialized.getBytes(),
                        new TypeReference<List<Node>>() {
                        });
                return nodeSet;
            } catch (IOException ioe) {
                logError("", ioe);
            }
        }
        return null;
    }

    protected String clientSpecificHostPropKey() {
        return getThreadNum() + "-hosts";
    }

    protected String clientSpecificNodePropKey() {
        return getThreadNum() + "-nodes";
    }

    static class Host {
        private String hostName;
        private String jmxUrl;

        public Host() {
        }

        public Host(String hostName, String jmxUrl) {
            this.hostName = hostName;
            this.jmxUrl = jmxUrl;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getJmxUrl() {
            return jmxUrl;
        }

        public void setJmxUrl(String jmxUrl) {
            this.jmxUrl = jmxUrl;
        }

    }

    static class Node {
        private String hostName;
        private String nodeUrl;
        private String jmxUrl;

        public Node() {
        }

        public Node(String hostName, String nodeUrl, String jmxUrl) {
            this.hostName = hostName;
            this.nodeUrl = nodeUrl;
            this.jmxUrl = jmxUrl;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getNodeUrl() {
            return nodeUrl;
        }

        public void setNodeUrl(String nodeUrl) {
            this.nodeUrl = nodeUrl;
        }

        public String getJmxUrl() {
            return jmxUrl;
        }

        public void setJmxUrl(String jmxUrl) {
            this.jmxUrl = jmxUrl;
        }
    }
}
