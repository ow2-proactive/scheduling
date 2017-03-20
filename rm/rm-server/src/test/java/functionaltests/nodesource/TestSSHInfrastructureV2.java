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
package functionaltests.nodesource;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPassword;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.SSHInfrastructureV2;
import org.ow2.proactive.resourcemanager.nodesource.policy.AccessType;
import org.ow2.proactive.resourcemanager.nodesource.policy.RestartDownNodesPolicy;
import org.ow2.proactive.resourcemanager.nodesource.policy.StaticPolicy;
import org.ow2.proactive.utils.Criteria;
import org.ow2.proactive.utils.NodeSet;

import functionaltests.monitor.RMMonitorsHandler;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


public class TestSSHInfrastructureV2 extends RMFunctionalTest {

    private static int port;

    private static SshServer sshd;

    private static String javaExePath;

    private static Object[] infraParams;

    private static Object[] policyParameters;

    private static String nsname = "testSSHInfra";

    private static int NB_NODES = 3;

    private ResourceManager resourceManager;

    @Test
    public void testSSHInfrastructureV2() throws Exception {

        nsname = "testSSHInfra";

        resourceManager = this.rmHelper.getResourceManager();

        RMTHelper.log("Test - Create SSH infrastructure on ssh://localhost on port " + this.port);

        resourceManager.createNodeSource(nsname,
                                         SSHInfrastructureV2.class.getName(),
                                         infraParams,
                                         StaticPolicy.class.getName(),
                                         policyParameters);
        this.rmHelper.waitForNodeSourceCreation(nsname, NB_NODES, this.rmHelper.getMonitorsHandler());

        RMTHelper.log("Checking scheduler state after node source creation");
        RMState s = resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());
    }

    @Test
    public void testSSHInfrastructureV2WithRestartDownNodes() throws Exception {

        nsname = "testSSHInfraRestart";

        resourceManager = this.rmHelper.getResourceManager();

        RMTHelper.log("Test - Create SSH infrastructure with RestartDownNodes policy on ssh://localhost on port " +
                      this.port);

        resourceManager.createNodeSource(nsname,
                                         SSHInfrastructureV2.class.getName(),
                                         infraParams,
                                         RestartDownNodesPolicy.class.getName(),
                                         policyParameters);
        RMMonitorsHandler monitorsHandler = this.rmHelper.getMonitorsHandler();

        this.rmHelper.waitForNodeSourceCreation(nsname, NB_NODES, monitorsHandler);

        RMState s = resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());

        NodeSet ns = resourceManager.getNodes(new Criteria(NB_NODES));

        if (ns.size() != NB_NODES) {
            RMTHelper.log("Illegal state : the infrastructure could not deploy nodes or they died immediately. Ending test");
            return;
        }

        for (Node n : ns) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                      n.getNodeInformation().getURL(),
                                      60000,
                                      monitorsHandler);
        }

        String nodeUrl = ns.get(0).getNodeInformation().getURL();
        RMTHelper.log("Killing nodes");
        // Nodes will be redeployed only if we kill the whole runtime
        rmHelper.killRuntime(nodeUrl);

        RMTHelper.log("Wait for down nodes detection by the rm");
        for (Node n : ns) {
            RMNodeEvent ev = rmHelper.waitForNodeEvent(RMEventType.NODE_STATE_CHANGED,
                                                       n.getNodeInformation().getURL(),
                                                       120000,
                                                       monitorsHandler);
            assertEquals(NodeState.DOWN, ev.getNodeState());
        }

        for (Node n : ns) {
            rmHelper.waitForNodeEvent(RMEventType.NODE_REMOVED,
                                      n.getNodeInformation().getURL(),
                                      120000,
                                      monitorsHandler);
        }

        RMTHelper.log("Wait for nodes restart by the policy");
        rmHelper.waitForAnyMultipleNodeEvent(RMEventType.NODE_ADDED, NB_NODES);
        for (int i = 0; i < NB_NODES; i++) {
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            rmHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        }

        RMTHelper.log("Final checks on the scheduler state");
        s = resourceManager.getState();
        assertEquals(NB_NODES, s.getTotalNodesNumber());
        assertEquals(NB_NODES, s.getFreeNodesNumber());
    }

    @After
    public void removeNS() throws Exception {
        RMTHelper.log("Removing node source");
        try {
            resourceManager.removeNodeSource(nsname, true);
        } catch (Exception ignored) {

        }
    }

    @BeforeClass
    public static void startSSHServer() throws Exception {
        // Disable bouncy castle to avoid versions conflict
        System.setProperty("org.apache.sshd.registerBouncyCastle", "false");

        sshd = SshServer.setUpDefaultServer();

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        if (OsUtils.isUNIX()) {
            sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" },
                                                         EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr)));
        } else {
            sshd.setShellFactory(new ProcessShellFactory(new String[] { "cmd.exe " },
                                                         EnumSet.of(ProcessShellFactory.TtyOptions.Echo,
                                                                    ProcessShellFactory.TtyOptions.ICrNl,
                                                                    ProcessShellFactory.TtyOptions.ONlCr)));
        }

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<>(1);
        userAuthFactories.add(new UserAuthPassword.Factory());
        sshd.setUserAuthFactories(userAuthFactories);

        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return username != null && username.equals(password);
            }
        });

        sshd.setCommandFactory(new ScpCommandFactory(new CommandFactory() {
            @Override
            public Command createCommand(String command) {
                EnumSet<ProcessShellFactory.TtyOptions> ttyOptions;
                if (OsUtils.isUNIX()) {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr);
                } else {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.Echo,
                                            ProcessShellFactory.TtyOptions.ICrNl,
                                            ProcessShellFactory.TtyOptions.ONlCr);
                }
                return new ProcessShellFactory(splitCommand(command), ttyOptions).create();
            }
        }));

        sshd.start();

        port = sshd.getPort();

        javaExePath = System.getProperty("java.home") + File.separator + "bin" + File.separator +
                      (OsUtils.isWin32() ? "java.exe" : "java");
        javaExePath = "\"" + javaExePath + "\"";

        infraParams = new Object[] { ("localhost " + NB_NODES + "\n").getBytes(), //hosts
                                     60000, //timeout
                                     0, //attempts
                                     10, //wait between failures
                                     port, //ssh server port
                                     "toto", //ssh username
                                     "toto", //ssh password
                                     new byte[0], // optional ssh private key
                                     new byte[0], // optional ssh options file
                                     javaExePath, //java path on the remote machines
                                     PAResourceManagerProperties.RM_HOME.getValueAsString(), //Scheduling path on remote machines
                                     OperatingSystem.getOperatingSystem(), "" }; // extra java options

        policyParameters = new Object[] { AccessType.ALL.toString(), AccessType.ALL.toString(), "20000" };

    }

    @AfterClass
    public static void stopSSHServer() throws Exception {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

    private static String[] splitCommand(String command) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\\\\\"]\\S*|\\\\\".+?\\\\\")\\s*").matcher(command);
        while (m.find()) {
            list.add(m.group(1).replaceAll("\\\\\"", ""));
        }
        return list.toArray(new String[list.size()]);
    }
}
