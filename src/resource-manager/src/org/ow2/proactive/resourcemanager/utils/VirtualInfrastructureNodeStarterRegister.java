/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.security.KeyException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.VirtualInfrastructure;


/**
 * This class is in charge of starting RMNodes and registering them
 * to the Resource Manager identified by the third argument on the
 * command line.
 *
 * @author ProActive team
 * @deprecated use {@link VIRMNodeStarter} instead
 */
@Deprecated
public final class VirtualInfrastructureNodeStarterRegister {

    /**
     * The starter will try to connect to the Resource Manager
     * before killing itself
     * that means that it will try to connect during
     * RM_WAIT_ON_JOIN_TIMEOUT_IN_MS milliseconds */
    private static final int RM_WAIT_ON_JOIN_TIMEOUT_IN_MS = 60000;

    /**	PALogger */
    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.RMNODE);

    /**
     * Creates a new instance of this class and calls registersInRm method. The
     * arguments must be as follows: arg[0] = credentials
     * arg[1] = rmUrl, arg[2] = nodeSource, arg[3] = vmname, arg[4] = processID
     *
     * @param args
     *            The arguments needed to join the Resource Manager
     */
    public static void main(final String args[]) {
        Credentials credentials = null;
        try {
            credentials = Credentials.getCredentialsBase64(args[0].getBytes());
        } catch (KeyException e) {
            logger.fatal("Invalid credentials.", e);
        }
        final String rmUrl = args[1];
        final String nodeSource = args[2];
        final String vmname = args[3];
        final int processID = Integer.parseInt(args[4]);
        int currentPort = CentralPAPropertyRepository.PA_RMI_PORT.getValue();
        CentralPAPropertyRepository.PA_RMI_PORT.setValue(currentPort + processID - 1);//processID starts from 1...
        // Use given args
        final VirtualInfrastructureNodeStarterRegister starter = new VirtualInfrastructureNodeStarterRegister();

        if (starter.registerInRM(credentials, rmUrl, nodeSource, vmname, processID)) {
            logger.info("Connected to the Resource Manager with url " + rmUrl);
        } else {
            logger.error("The Resource Manager at " + rmUrl + " is unreachable ! The application will exit.");
            System.exit(1);
        }
    }

    /**
     * Registers a in ResourceManager at given URL in parameter and handles all
     * errors/exceptions. Tries to joins the Resource Manager with a specified
     * timeout then logs as admin with the provided username and password and
     * adds the created node to the Resource Manager. The created Node handles a
     * PAProperty, the holding virtual machine's name, to be able, once registered,
     * to identify the virtual machine in which this node is running.
     */
    private boolean registerInRM(final Credentials creds, final String rmUrl, final String nodeSource,
            final String vmName, final int processID) {
        Node node = null;
        try {
            node = NodeFactory.createLocalNode(vmName + "_node_" + processID, false, null, null);
            if (node == null) {
                throw new RuntimeException("The node returned by the NodeFactory is null");
            }
            node.setProperty(VirtualInfrastructure.Prop.HOLDING_VIRTUAL_MACHINE.getValue(), vmName);
        } catch (Throwable t) {
            logger.error("Could not create local node.", t);
            return false;
        }
        // Create the full url to contact the Resource Manager
        final String fullUrl = rmUrl.endsWith("/") ? rmUrl + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION
                : rmUrl + "/" + RMConstants.NAME_ACTIVE_OBJECT_RMAUTHENTICATION;
        // 2 - Try to join the Resource Manager with a specified timeout
        RMAuthentication auth = null;
        try {
            auth = RMConnection.waitAndJoin(fullUrl, RM_WAIT_ON_JOIN_TIMEOUT_IN_MS);
            if (auth == null) {
                throw new RuntimeException("The RMAuthentication instance is null");
            }
        } catch (Throwable t) {
            logger.error("Could not join the Resource Manager at " + rmUrl, t);
            return false;
        }
        // 3 - Log as admin with the provided username and password
        ResourceManager resourceManager = null;
        try {
            resourceManager = auth.login(creds);
            if (resourceManager == null) {
                throw new RuntimeException("The RMAdmin instance is null");
            }
        } catch (Throwable t) {
            logger.error("Could not log as admin into the Resource Manager at " + rmUrl, t);
            return false;
        }
        // 4 - Add the created node to the Resource Manager
        try {
            boolean added = false;
            int cb = 0, cbTreshHold = 10;
            while (!added && cb < cbTreshHold) {
                BooleanWrapper res = resourceManager.addNode(node.getNodeInformation().getURL(), nodeSource);
                added = res.getBooleanValue();
                cb++;
            }
        } catch (Throwable t) {
            logger.error("Could not add the local node the Resource Manager at " + rmUrl, t);
            return false;
        }
        return true;
    }
}
