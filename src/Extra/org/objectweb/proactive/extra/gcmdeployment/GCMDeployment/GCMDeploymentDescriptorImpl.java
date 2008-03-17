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

import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.acquisition.P2PEntry;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.hostinfo.HostInfo;
import org.xml.sax.SAXException;


public class GCMDeploymentDescriptorImpl implements GCMDeploymentDescriptor {
    private GCMDeploymentParser parser;
    private VariableContractImpl environment;
    private GCMDeploymentResources resources;
    private GCMDeploymentAcquisition acquisitions;

    public GCMDeploymentDescriptorImpl(File descriptor, VariableContractImpl vContract) throws SAXException,
            IOException, XPathExpressionException, TransformerException, ParserConfigurationException {
        parser = new GCMDeploymentParserImpl(descriptor, vContract);
        environment = parser.getEnvironment();
        resources = parser.getResources();
        acquisitions = parser.getAcquisitions();
    }

    /**
     * This constructor is only for unit Testing. <strong>Do not use it</strong>
     */
    private GCMDeploymentDescriptorImpl() {
    }

    public void start(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        // Start Local JVMs
        startLocal(commandBuilder, gcma);

        startGroups(commandBuilder, gcma);
        startBridges(commandBuilder, gcma);

        for (P2PEntry p2pAcq : acquisitions.getP2pEntries()) {
            startP2PAcquisition(p2pAcq);
        }
    }

    private void startLocal(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        HostInfo hostInfo = resources.getHostInfo();
        if (hostInfo != null) {
            // Something needs to be started on this host
            String command = commandBuilder.buildCommand(hostInfo, gcma);

            GCMD_LOGGER.info("Starting a process on localhost");
            GCMD_LOGGER.debug("command= " + command);
            Executor.getExecutor().submit(command);
        }
    }

    private void startGroups(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        List<Group> groups = resources.getGroups();
        for (Group group : groups) {
            List<String> commands = group.buildCommands(commandBuilder, gcma);
            GCMD_LOGGER.info("Starting group id=" + group.getId() + " #commands=" + commands.size());

            for (String command : commands) {
                GCMD_LOGGER.debug("group id=" + group.getId() + " command= " + command);
                Executor.getExecutor().submit(command);
            }
        }
    }

    private void startBridges(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        List<Bridge> bridges = resources.getBridges();
        for (Bridge bridge : bridges) {
            List<String> commands = bridge.buildCommands(commandBuilder, gcma);

            GCMD_LOGGER.info("Starting bridge id=" + bridge.getId() + " #commands=" + commands.size());

            for (String command : commands) {
                GCMD_LOGGER.debug("bridge id=" + bridge.getId() + " command= " + command);
                Executor.getExecutor().submit(command);
            }
        }
    }

    private void startP2PAcquisition(P2PEntry entry) {
        try {
            JVMProcessImpl process = new JVMProcessImpl(new StandardOutputMessageLogger());
            process.setClassname("org.objectweb.proactive.p2p.service.StartP2PService");

            process.setParameters("-port " + entry.getLocalClient().getPort() + " -acq " +
                entry.getLocalClient().getProtocol());

            process.startProcess();
            Thread.sleep(7000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public VariableContractImpl getEnvironment() {
        return environment;
    }

    public GCMDeploymentResources getResources() {
        return resources;
    }

    public GCMDeploymentParser getParser() {
        return parser;
    }

    public GCMDeploymentAcquisition getAcquisitions() {
        return acquisitions;
    }

    public String getDescriptorFilePath() {
        return parser.getDescriptorFilePath();
    }
}
