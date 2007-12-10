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
package org.objectweb.proactive.extra.infrastructuremanager;

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
import org.objectweb.proactive.extra.infrastructuremanager.common.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreInterface;
import org.objectweb.proactive.extra.infrastructuremanager.exception.IMException;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;


/**
 * Object which performs the Infrastructure Manager (IM)creation,
 * and provides IM's front-end active objects. :<BR>
 * -{@link IMAdmin}.<BR>
 * -{@link IMMonitoring}.<BR>
 * -{@Link IMUSer}.<BR>
 *
 * @author ProActive team
 *
 */
@PublicAPI
public class IMFactory implements IMConstants {

    /** Logger of the IMFactory */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_FACTORY);

    /** IMCore interface of the created Infrastructure manager */
    private static IMCoreInterface imcore = null;

    /**
     * Creates Infrastructure manager on local host.
     * @throws NodeException
     * @throws ActiveObjectCreationException
     * @throws AlreadyBoundException
     * @throws IOException
     */
    public static void startLocal()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore == null) {
            Node nodeIM = NodeFactory.createNode(NAME_NODE_IM);
            imcore = (IMCoreInterface) PAActiveObject.newActive(IMCore.class.getName(), // the class to deploy
                    new Object[] { "IMCORE", nodeIM }, nodeIM);
            PAActiveObject.register(imcore,
                "//localhost/" + NAME_ACTIVE_OBJECT_IMCORE);

            if (logger.isInfoEnabled()) {
                logger.info("New IM core localy started");
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("IM Core already localy running");
            }
        }
    }

    /**
     * Gives the active object IMADMIN on local host.
     * @return IMAdmin active object.
     * @throws IMException if the Infrastructure Manager hasn't been created before.
     */
    public static IMAdmin getAdmin() throws IMException {
        if (imcore != null) {
            if (logger.isInfoEnabled()) {
                logger.info("We have started the imcore");
            }
            return imcore.getAdmin();
        } else {
            throw new IMException(
                "Infrastructure Manager has not been created before");
        }
    }

    /**
     * @return IMMonitoring active object on local host.
     * @throws IMException if the Infrastructure Manager hasn't been created before
     */
    public static IMMonitoring getMonitoring() throws IMException {
        if (imcore != null) {
            return imcore.getMonitoring();
        } else {
            throw new IMException(
                "Infrastructure Manager has not been created before");
        }
    }

    /**
     * @return IMUser active object on local host.
     * @throws IMException if the Infrastructure Manager hasn't been created before
     */
    public static IMUser getUser() throws IMException {
        if (imcore != null) {
            return imcore.getUser();
        } else {
            throw new IMException(
                "Infrastructure Manager has not been created before");
        }
    }

    /**
     * Main function, create the Infrastructure Manager.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            startLocal();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
