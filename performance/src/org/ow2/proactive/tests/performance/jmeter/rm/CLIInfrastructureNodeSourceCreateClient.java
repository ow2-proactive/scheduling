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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.CLIInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;
import org.ow2.proactive.utils.FileToBytesConverter;


public class CLIInfrastructureNodeSourceCreateClient extends BaseNodeSourceCreateClient {

    public static final String PARAM_CLI_DEPLOYMENT_SCRIPT = "cliNodeDeploymentScript";

    public static final String PARAM_CLI_REMOVAL_SCRIPT = "cliNodeRemovalScript";

    public static final String PARAM_CLI_USE_DEFAULT_SCRIPTS = "cliUseDefaultScripts";

    public static final String DEFAULT_INTERPRETER = "bash";

    private boolean useDefaultScripts;

    private byte[] defaultDeploymentScript;

    private byte[] defaultRemovalScript;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = super.getDefaultParameters();
        args.addArgument(PARAM_CLI_DEPLOYMENT_SCRIPT, "${cliNodeDeploymentScript}");
        args.addArgument(PARAM_CLI_REMOVAL_SCRIPT, "${cliNodeRemovalScript}");
        args.addArgument(PARAM_CLI_USE_DEFAULT_SCRIPTS, "${cliUseDefaultScripts}");
        return args;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);

        useDefaultScripts = getBooleanParameter(context, PARAM_CLI_USE_DEFAULT_SCRIPTS);
        if (useDefaultScripts) {
            String javaPath = getRequiredParameter(context, PARAM_JAVA_PATH);
            String schedulingPath = getRequiredParameter(context, PARAM_SCHEDULING_PATH);
            String nodeJavaOptions = getNodeJavaOptions(context);
            defaultDeploymentScript = createDefaultDeploymentScript(javaPath, nodeJavaOptions, schedulingPath);
            defaultRemovalScript = "#!/bin/bash\n".getBytes();
        }
    }

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
        String interpreter = DEFAULT_INTERPRETER;

        byte[] deploymentScript;
        byte[] removalScript;

        if (useDefaultScripts) {
            deploymentScript = defaultDeploymentScript;
            removalScript = defaultRemovalScript;
        } else {
            String deploymentScriptPath = getRequiredParameter(context, PARAM_CLI_DEPLOYMENT_SCRIPT);
            deploymentScript = FileToBytesConverter.convertFileToByteArray(new File(deploymentScriptPath));

            String removalScriptPath = getRequiredParameter(context, PARAM_CLI_REMOVAL_SCRIPT);
            removalScript = FileToBytesConverter.convertFileToByteArray(new File(removalScriptPath));
        }

        ResourceManager rm = getResourceManager();

        Object[] infrastructureParameters = new Object[] { rmUrl, hostsList.getBytes(), timeout, attempt,
                interpreter, deploymentScript, removalScript };

        Object[] policyParameters = new Object[] { "users=dummyUser", AccessType.ME.toString() };

        String nodeJavaOptions = getNodeJavaOptions(context);

        String message = String.format(
                "Creating node source, CLI infrastructure, hostsList=%s javaOpts=%s (%s)", hostsList,
                nodeJavaOptions, Thread.currentThread().toString());
        System.out.println(message);

        BooleanWrapper result = rm.createNodeSource(nodeSourceName, CLIInfrastructure.class.getName(),
                infrastructureParameters, StaticPolicy.class.getName(), policyParameters);

        return result.getBooleanValue();
    }

    private byte[] createDefaultDeploymentScript(String javaPath, String nodeJavaOptions,
            String schedulingPath) throws Exception {
        InputStream in = getClass().getResourceAsStream(
                "/org/ow2/proactive/tests/performance/jmeter/rm/defaultSSHDeployment");
        if (in == null) {
            throw new RuntimeException("Default deployment script not found");
        }

        String script = TestFileUtils.readStreamToString(in);
        String credentials = TestFileUtils.readStreamToString(new FileInputStream(schedulingPath +
            "/config/authentication/rm.cred"));

        script = script.replace("@RM_HOME_NODE", schedulingPath).replace("@JAVA_PATH_NODE", javaPath)
                .replace("@CREDENTIALS", credentials).replace("@NODE_JAVA_OPTS", nodeJavaOptions);

        return script.getBytes();
    }

}
