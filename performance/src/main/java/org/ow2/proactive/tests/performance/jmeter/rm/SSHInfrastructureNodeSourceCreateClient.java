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
package org.ow2.proactive.tests.performance.jmeter.rm;

import java.io.File;
import java.util.Set;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;


/**
 * Test scenario 'Create Node Source with SSHInfrastructure'.
 * <p/>
 * Scenario creates new node source using call 'ResourceManager.createNodeSource'
 * (SSHInfrastructure is used), waits when deployment of all nodes in the created 
 * node source completes and then removes node source using 'ResourceManager.removeNodeSource'. 
 * Scenario measures total time required to create node source and deploy all nodes.
 *
 * @author ProActive team
 *
 */
public class SSHInfrastructureNodeSourceCreateClient extends BaseNodeSourceCreateClient {

    @Override
    protected boolean createNodeSource(String nodeSourceName, Set<String> hostNames, int nodesNumber,
            String javaPath, String schedulingPath, JavaSamplerContext context) throws Exception {
        String rmUrl = context.getParameter(RMConnectionParameters.PARAM_RM_URL);

        StringBuilder hostsListBuilder = new StringBuilder();
        for (String hostName : hostNames) {
            hostsListBuilder.append(String.format("%s %d\n", hostName, nodesNumber));
        }

        String hostsList = hostsListBuilder.toString();
        int timeout = NODE_DEPLOY_TIMEOUT;
        int attempt = 1;
        String sshOptions = "";
        String targetOs = "UNIX";
        String javaOptions = getNodeJavaOptions(context) + " -Dproactive.home=" + schedulingPath +
            " -Dpa.rm.home=" + schedulingPath;

        byte[] creds = FileToBytesConverter.convertFileToByteArray(new File(schedulingPath +
            "/config/authentication/rm.cred"));

        ResourceManager rm = getResourceManager();

        byte[] sshKey = FileToBytesConverter.convertFileToByteArray(new File("/home/" +
            System.getProperty("user.name") + "/.ssh/id_rsa"));
        Object[] infrastructureParameters = new Object[] { hostsList.getBytes(), timeout, attempt, 22,
                System.getProperty("user.name"), "", sshKey, "".getBytes(), javaPath, schedulingPath,
                targetOs, javaOptions };

        Object[] policyParameters = new Object[] { "users=dummyUser", AccessType.ME.toString() };

        String message = String.format(
                "Creating node source, SSH infrastructure, hostsList=%s javaOpts=%s (%s)", hostsList,
                javaOptions, Thread.currentThread().toString());
        logInfo(message);

        BooleanWrapper result = rm.createNodeSource(nodeSourceName, SSHInfrastructureV2.class.getName(),
                infrastructureParameters, StaticPolicy.class.getName(), policyParameters);
        return result.getBooleanValue();
    }

}
