/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s):
*
* ################################################################
*/
package org.objectweb.proactive.core.exceptions;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.exceptions.communication.ProActiveCommunicationException;
import org.objectweb.proactive.core.exceptions.creation.ProActiveCreationException;
import org.objectweb.proactive.core.exceptions.group.ProActiveGroupException;
import org.objectweb.proactive.core.exceptions.handler.Handler;
import org.objectweb.proactive.core.exceptions.handler.HandlerCommunicationException;
import org.objectweb.proactive.core.exceptions.handler.HandlerCreationException;
import org.objectweb.proactive.core.exceptions.handler.HandlerGroupException;
import org.objectweb.proactive.core.exceptions.handler.HandlerMigrationException;
import org.objectweb.proactive.core.exceptions.handler.HandlerNonFunctionalException;
import org.objectweb.proactive.core.exceptions.handler.HandlerSecurityException;
import org.objectweb.proactive.core.exceptions.handler.HandlerServiceException;
import org.objectweb.proactive.core.exceptions.migration.ProActiveMigrationException;
import org.objectweb.proactive.core.exceptions.security.ProActiveSecurityException;
import org.objectweb.proactive.core.exceptions.service.ProActiveServiceException;

import java.util.HashMap;


/**
 * A manager for NFE.
 * @author  ProActive Team
 * @version 1.0,  2004/07/15
 * @since   ProActive 2.0
 *
 */
public class HandlerManager {
    // NFE logger
    static public Logger loggerNFE = Logger.getLogger("NFE");

    // Are handlers quiet ?
    static protected boolean isQuiet;

    // Do handlers have graphical representation ?
    static protected boolean isGraph;

    /**
     * Store automatic associations between proxy and handler
     */
    static protected HashMap handlerAssociatedToProxyObject;

    /**
     * Store automatic associations between proxy and handler
     */
    static protected HashMap handlerAssociatedToBodyObject;

    /**
     * Store automatic associations between proxy and handler
     */
    static protected HashMap handlerAssociatedToFutureObject;

    /**
     * @return Returns the isGraph.
     */
    static public boolean isGraph() {
        return isGraph;
    }

    /**
     * @param isGraph The isGraph to set.
     */
    static public void setGraph(boolean graph) {
        isGraph = graph;
    }

    /**
     * @return Returns the isQuiet.
     */
    static public boolean isQuiet() {
        return isQuiet;
    }

    /**
     * @param isQuiet The isQuiet to set.
     */
    static public void setQuiet(boolean quiet) {
        isQuiet = quiet;
        if (isQuiet) {
            loggerNFE.setLevel(Level.WARN);
        } else {
            loggerNFE.setLevel(Level.INFO);
        }
    }

    /**
     * Create a graphical windows to display handlers
     */
    static public void handleWindow(NonFunctionalException nfe, Handler h,
        Object info) {
        //System.out.println("*** CREATING HANDLER WINDOW !");
        HandlerWindow win = new HandlerWindow(nfe, h, info);
        win.setVisible(true);
    }

    /**
     * Default Constructor for HandlerManager.
     * A policy is set directly in this constructor.
     */
    static public void initialize() {
        // Creation of the handlerAssociatedToXXX map
        handlerAssociatedToBodyObject = new HashMap();
        handlerAssociatedToFutureObject = new HashMap();
        handlerAssociatedToProxyObject = new HashMap();

        // Clear handler policy
        clearHandlerPolicy();
        
        // Set default option
        setQuiet(false);
        setGraph(false);

        // Default handler are added to the default level
        if (loggerNFE.isDebugEnabled()) {
            loggerNFE.debug(
                "[NFE_INFO] Creation of the local manager for handler of NFE");
            loggerNFE.debug("[NFE_INIT] Initialization of default level");
        }
        ProActive.setExceptionHandler(HandlerNonFunctionalException.class,
            NonFunctionalException.class, Handler.ID_Default, null);
        ProActive.setExceptionHandler(HandlerCommunicationException.class,
            ProActiveCommunicationException.class, Handler.ID_Default, null);
        ProActive.setExceptionHandler(HandlerCreationException.class,
            ProActiveCreationException.class, Handler.ID_Default, null);
        ProActive.setExceptionHandler(HandlerGroupException.class,
            ProActiveGroupException.class, Handler.ID_Default, null);
        ProActive.setExceptionHandler(HandlerMigrationException.class,
            ProActiveMigrationException.class, Handler.ID_Default, null);
        ProActive.setExceptionHandler(HandlerSecurityException.class,
            ProActiveSecurityException.class, Handler.ID_Default, null);
        ProActive.setExceptionHandler(HandlerServiceException.class,
            ProActiveServiceException.class, Handler.ID_Default, null);
    }

    /**
     * Construct a new manager with the given handler policy
     * @param XMLdesc the file that contains the handler policy
     */
    static public void initialize(String XMLdesc) {
        // Initialize with default policy
        initialize();

        // Then load new handler policy
        loadHandlerPolicy(XMLdesc);
    }

    /**
     * Clear handler policy
     */
    static protected void clearHandlerPolicy() {
        // Clear global table
        if (ProActive.defaultLevel != null) {
            ProActive.defaultLevel.clear();
        }
        if (ProActive.VMLevel != null) {
            ProActive.VMLevel.clear();
        }
        if (ProActive.codeLevel != null) {
            ProActive.codeLevel.clear();
        }
    }

    /**
     * Load a handler policy from a file.
     * @param XMLdesc  the file that contains the handler policy
     */
    static public void loadHandlerPolicy(String XMLdesc) {
    }

    /**
     * Search and return the handler that must me automatically associated to the body of active object
     * @param obj is an active object
     */
    public static HashMap isHandlerAssociatedToBodyObject(Object obj) {
        return (HashMap) handlerAssociatedToBodyObject.get(obj);
    }

    /**
     * Search and return the handler that must me automatically associated to the future of active object
     * @param obj is an active object
     */
    public static HashMap isHandlerAssociatedToFutureObject(Object obj) {
        return (HashMap) handlerAssociatedToFutureObject.get(obj);
    }

    /**
     * Search and return the handler that must me automatically associated to the proxy of active object
     * @param obj is an active object
     */
    public static HashMap isHandlerAssociatedToProxyObject(Object obj) {
        return (HashMap) handlerAssociatedToProxyObject.get(obj);
    }
}
