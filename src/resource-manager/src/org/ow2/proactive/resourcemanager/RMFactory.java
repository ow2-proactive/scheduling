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
package org.ow2.proactive.resourcemanager;

import java.io.IOException;
import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.frontend.RMUser;


/**
 * Object which performs the Resource Manager (RM)creation,
 * and provides RM's front-end active objects. :<BR>
 * -{@link RMAdmin}.<BR>
 * -{@link RMMonitoring}.<BR>
 * -{@Link RMUSer}.<BR>
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 *
 */
@PublicAPI
public class RMFactory implements RMConstants {

    /** Logger of the RMFactory */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.RM_FACTORY);

    /** RMCore interface of the created Resource manager */
    private static RMCoreInterface rmcore = null;

    /* Proactive node that will contains Resource manager active objects */
    private static String RM_NODE_NAME = PAResourceManagerProperties.RM_NODE_NAME.getValueAsString();

    /**
     * Creates Resource manager on local host.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     * @throws AlreadyBoundException
     * @throws IOException
     */
    public static void startLocal() throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {

        String RMCoreName = RMConstants.NAME_ACTIVE_OBJECT_RMCORE;
        if (rmcore == null) {
            Node nodeRM = NodeFactory.createNode(RM_NODE_NAME);
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
     * Gives the active object IMADMIN on local host.
     * @return IMAdmin active object.
     * @throws RMException if the resource Manager hasn't been created before.
     */
    public static RMAdmin getAdmin() throws RMException {
        if (rmcore != null) {
            if (logger.isInfoEnabled()) {
                logger.info("We have started the rmcore");
            }
            return rmcore.getAdmin();
        } else {
            throw new RMException("Resource Manager has not been created before");
        }
    }

    /**
     * @return IMMonitoring active object on local host.
     * @throws RMException if the resource Manager hasn't been created before
     */
    public static RMMonitoring getMonitoring() throws RMException {
        if (rmcore != null) {
            return rmcore.getMonitoring();
        } else {
            throw new RMException("resource Manager has not been created before");
        }
    }

    /**
     * @return IMUser active object on local host.
     * @throws RMException if the resource Manager hasn't been created before
     */
    public static RMUser getUser() throws RMException {
        if (rmcore != null) {
            return rmcore.getUser();
        } else {
            throw new RMException("resource Manager has not been created before");
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
}
