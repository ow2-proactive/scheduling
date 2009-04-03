/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * Object which performs the Resource Manager (RM)creation,
 * and provides RM's front-end active objects. :<BR>
 * -{@link RMAdmin}.<BR>
 * -{@link RMMonitoring}.<BR>
 * -{@Link RMUSer}.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
/**
 * @author gsigety
 *
 */
@PublicAPI
public class RMFactory {

    /** Logger of the RMFactory */
    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.RMFACTORY);

    /** RMCore interface of the created Resource manager */
    private static RMCoreInterface rmcore = null;

    /** ProActive node that will contains Resource manager active objects */
    private static String RM_NODE_NAME = PAResourceManagerProperties.RM_NODE_NAME.getValueAsString();

    /**
     * Creates Resource manager on local host.
     * @throws NodeException If the RM's node can't be created
     * @throws ActiveObjectCreationException If RMCore cannot be created
     * @throws AlreadyBoundException if a node with the same RMNode's name is already exist. 
     * @throws IOException If node and RMCore fails.
     */
    public static void startLocal() throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {

        String RMCoreName = RMConstants.NAME_ACTIVE_OBJECT_RMCORE;
        if (rmcore == null) {
            Node nodeRM = NodeFactory.createLocalNode(RM_NODE_NAME, false, null, null, null);
            rmcore = (RMCoreInterface) PAActiveObject.newActive(RMCore.class.getName(), // the class to deploy
                    new Object[] { RMCoreName, nodeRM }, nodeRM);

            if (logger.isInfoEnabled()) {
                logger.info("New RM core localy started");
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("RM Core already localy running");
            }
        }
    }

    /**
     * Creates Resource manager on local host with the further deployment of infrastructure from given deployment descriptor.
     * 
     * @param localGCMDeploymentDescriptors list of local gcm deployment descriptors which will be processed during core startup
     * 
     * @throws NodeException If the RM's node can't be created
     * @throws ActiveObjectCreationException If RMCore cannot be created
     * @throws AlreadyBoundException if a node with the same RMNode's name is already exist. 
     * @throws IOException If node and RMCore fails.
     */
    public static void startLocal(Collection<String> localGCMDeploymentDescriptors) throws NodeException,
            ActiveObjectCreationException, AlreadyBoundException, IOException {
        String RMCoreName = RMConstants.NAME_ACTIVE_OBJECT_RMCORE;
        if (rmcore == null) {
            Node nodeRM = NodeFactory.createLocalNode(RM_NODE_NAME, false, null, null, null);
            rmcore = (RMCoreInterface) PAActiveObject.newActive(RMCore.class.getName(), // the class to deploy
                    new Object[] { RMCoreName, nodeRM, localGCMDeploymentDescriptors }, nodeRM);

            if (logger.isInfoEnabled()) {
                logger.info("New RM core localy started");
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("RM Core already localy running");
            }
        }
    }

    /**
     * Main function, create the resource Manager.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            startLocal();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set Java Property "os" used by default GCM deployment file.
     */
    public static void setOsJavaProperty() {
        //set appropriate os java property used in default GCM deployment descriptor.
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.setProperty("os", "windows");
        } else {
            System.setProperty("os", "unix");
        }
    }
}
