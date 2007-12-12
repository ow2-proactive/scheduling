/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;
import org.objectweb.proactive.extra.gcmdeployment.process.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.BridgeDummy;
import org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder.CommandBuilderDummy;
import org.objectweb.proactive.extra.gcmdeployment.process.group.GroupDummy;
import org.objectweb.proactive.extra.gcmdeployment.process.hostinfo.HostInfoImpl;
import org.xml.sax.SAXException;


public class GCMDeploymentDescriptorImpl implements GCMDeploymentDescriptor {
    private GCMDeploymentParser parser;
    private VariableContract environment;
    private GCMDeploymentResources resources;

    public GCMDeploymentDescriptorImpl(File descriptor,
        Set<FileTransferBlock> ftBlocks)
        throws SAXException, IOException, XPathExpressionException,
            TransformerException, ParserConfigurationException {
        parser = new GCMDeploymentParserImpl(descriptor);
        environment = parser.getEnvironment();
        resources = parser.getResources();
    }

    /**
     * This constructor is only for unit Testing. <strong>Do not use it</strong>
     */
    private GCMDeploymentDescriptorImpl() {
    }

    public void start(CommandBuilder commandBuilder,
        GCMApplicationDescriptor gcma) {
        // Start Local JVMs
        startLocal(commandBuilder, gcma);

        startGroups(commandBuilder, gcma);
        startBridges(commandBuilder, gcma);
    }

    private void startLocal(CommandBuilder commandBuilder,
        GCMApplicationDescriptor gcma) {
        HostInfo hostInfo = resources.getHostInfo();
        if (hostInfo != null) {
            // Something needs to be started on this host
            String command = commandBuilder.buildCommand(hostInfo, gcma);

            GCMD_LOGGER.info("Starting a process on localhost");
            GCMD_LOGGER.debug("command= " + command);
            Executor.getExecutor().submit(command);
        }
    }

    private void startGroups(CommandBuilder commandBuilder,
        GCMApplicationDescriptor gcma) {
        List<Group> groups = resources.getGroups();
        for (Group group : groups) {
            List<String> commands = group.buildCommands(commandBuilder, gcma);
            GCMD_LOGGER.info("Starting group id=" + group.getId() +
                " #commands=" + commands.size());

            for (String command : commands) {
                GCMD_LOGGER.debug("group id=" + group.getId() + " command= " +
                    command);
                Executor.getExecutor().submit(command);
            }
        }
    }

    private void startBridges(CommandBuilder commandBuilder,
        GCMApplicationDescriptor gcma) {
        List<Bridge> bridges = resources.getBridges();
        for (Bridge bridge : bridges) {
            List<String> commands = bridge.buildCommands(commandBuilder, gcma);

            GCMD_LOGGER.info("Starting bridge id=" + bridge.getId() +
                " #commands=" + commands.size());

            for (String command : commands) {
                GCMD_LOGGER.debug("bridge id=" + bridge.getId() + " command= " +
                    command);
                Executor.getExecutor().submit(command);
            }
        }
    }

    public long getMaxCapacity() {
        // TODO Auto-generated method stub
        return 0;
    }

    public VariableContract getEnvironment() {
        return environment;
    }

    public GCMDeploymentResources getResources() {
        return resources;
    }

    public GCMDeploymentParser getParser() {
        return parser;
    }

    @SuppressWarnings("unused")
    static public class UnitTestGCMDeploymentDescriptorImpl {
        @Test
        public void test() {
            CommandBuilder commandBuilder = new CommandBuilderDummy("sleep 60");

            HostInfo hostInfo = new HostInfoImpl();

            List<String> groupCommands = new ArrayList<String>();
            groupCommands.add("ssh duff");
            groupCommands.add("ssh naruto");
            GroupDummy group = new GroupDummy(groupCommands);
            group.setHostInfo(hostInfo);

            BridgeDummy bridge = new BridgeDummy("ssh cheypa");
            bridge.setId("dummyBridge");
            bridge.addGroup(group);

            GCMDeploymentResources resources = new GCMDeploymentResources();
            // resources.addBridge(bridge);
            resources.addGroup(group);

            GCMDeploymentDescriptorImpl gcmd = new GCMDeploymentDescriptorImpl();
            gcmd.resources = resources;

            GCMD_LOGGER.warn("Starting...");
            gcmd.start(commandBuilder, null);
        }

        public static void main(String[] args) {
            UnitTestGCMDeploymentDescriptorImpl test = new UnitTestGCMDeploymentDescriptorImpl();
            test.test();
        }
    }

    public String getDescriptorFilePath() {
        return parser.getDescriptorFilePath();
    }
}
