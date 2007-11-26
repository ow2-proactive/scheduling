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
import java.net.URI;
import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCoreInterface;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;


public class IMFactory implements IMConstants {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_FACTORY);
    private static IMCoreInterface imcore = null;

    /**
     * Start the infrastructure manager in the local host
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
            imcore = (IMCoreInterface) ProActiveObject.newActive(IMCore.class.getName(), // the class to deploy
                    new Object[] { "IMCORE", nodeIM }, nodeIM);
            ProActiveObject.register(imcore,
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

    //----------------------------------------------------------------------//
    // GET IMCORE

    /**
     * Get the actif object IMCORE in the local host
     * @return IMCore
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    private static IMCoreInterface getIMCore()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            return imcore;
        } else {
            return getIMCore("//localhost/" + NAME_ACTIVE_OBJECT_IMCORE);
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE (For example : "//localhost/IMCORE")
     * @see the name of the IM node in the classe IMConstantes (NAME_ACTIVE_OBJECT_IMCORE)
     * @return IMCore
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    private static IMCoreInterface getIMCore(String urlIM)
        throws ActiveObjectCreationException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("lookup of IMCore at the IM url node : " + urlIM);
        }

        IMCoreInterface imcoreLookUp;

        //TODO GERMS lookup sur interface IMNodeManager ou sur IMNodeManagerImpl ?  
        imcoreLookUp = (IMCoreInterface) ProActiveObject.lookupActive(IMCoreInterface.class.getName(),
                urlIM);

        return imcoreLookUp;
    }

    /**
     * Call the method getIMCore(String);<BR/>
     * URL -> String
     * @param urlIM
     * @return IMCore
     * @throws ActiveObjectCreationException
     * @throws IOException
     */
    private static IMCoreInterface getIMCore(URI urlIM)
        throws ActiveObjectCreationException, IOException {
        String locationIM = urlIM.toString();

        if (!(locationIM.charAt(locationIM.length() - 1) == '/')) {
            locationIM += "/";
        }

        locationIM += IMConstants.NAME_ACTIVE_OBJECT_IMCORE;

        return getIMCore(locationIM);
    }

    /**
     * @return IMAdmin
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static IMAdmin getAdmin()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            if (logger.isInfoEnabled()) {
                logger.info("We have started the imcore");
            }

            return imcore.getAdmin();
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("We try to look at localhost for IM");
            }

            return getIMCore().getAdmin();
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE
     *                 (For example : "rmi://localhost:1099/")
     * @return IMAdmin
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    public static IMAdmin getAdmin(URI uriIM)
        throws ActiveObjectCreationException, IOException {
        IMCoreInterface imcoreLookUp = getIMCore(uriIM);

        return imcoreLookUp.getAdmin();
    }

    //----------------------------------------------------------------------//
    // GET MONITORING

    /**
     * @return IMMonitoring
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static IMMonitoring getMonitoring()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            return imcore.getMonitoring();
        } else {
            return getIMCore().getMonitoring();
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE
     *                 (For example : "rmi://localhost:1099/")
     * @return IMMonitoring
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    public static IMMonitoring getMonitoring(URI uriIM)
        throws ActiveObjectCreationException, IOException {
        IMCoreInterface imcoreLookUp = getIMCore(uriIM);

        return imcoreLookUp.getMonitoring();
    }

    //----------------------------------------------------------------------//
    // GET USER

    /**
     * @return IMUser
     * @throws IOException
     * @throws AlreadyBoundException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static IMUser getUser()
        throws NodeException, ActiveObjectCreationException,
            AlreadyBoundException, IOException {
        if (imcore != null) {
            return imcore.getUser();
        } else {
            return getIMCore().getUser();
        }
    }

    /**
     * @param urlIM : the location of the actif object IMCORE
     *                 (For example : "rmi://localhost:1099/")
     * @return IMUser
     * @throws IOException
     * @throws ActiveObjectCreationException
     */
    public static IMUser getUser(URI uriIM)
        throws ActiveObjectCreationException, IOException {
        IMCoreInterface imcoreLookUp = getIMCore(uriIM);

        return imcoreLookUp.getUser();
    }

    //----------------------------------------------------------------------//
    // MAIN : start the IM 
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
