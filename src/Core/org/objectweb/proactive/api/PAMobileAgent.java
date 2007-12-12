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
package org.objectweb.proactive.api;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.body.request.BodyRequest;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class provides methods to migrate active objects.
 *
 * @author The ProActive Team
 * @since ProActive 3.9 (November 2007)
 */
@PublicAPI
public class PAMobileAgent {
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);

    private static Node getNodeFromURL(String url) throws MigrationException {
        try {
            return NodeFactory.getNode(url);
        } catch (NodeException e) {
            throw new MigrationException("The node of given URL " + url +
                " cannot be localized", e);
        }
    }

    private static String getNodeURLFromActiveObject(Object o)
        throws MigrationException {
        //first we check if the parameter is an active object,
        if (!org.objectweb.proactive.core.mop.MOP.isReifiedObject(o)) {
            throw new MigrationException(
                "The parameter is not an active object");
        }

        //now we get a reference on the remoteBody of this guy
        BodyProxy destProxy = (BodyProxy) ((org.objectweb.proactive.core.mop.StubObject) o).getProxy();

        return destProxy.getBody().getNodeURL();
    }

    /**
     * Migrates the active object whose active thread is calling this method to the
     * given node.
     * This method must be called from an active object using the active thread as the
     * current thread will be used to find which active object is calling the method.
     * @param node an existing node where to migrate to.
     * @exception MigrationException if the migration fails
     * @see PAActiveObject#getBodyOnThis
     */
    public static void migrateTo(Node node) throws MigrationException {
        if (PAMobileAgent.logger.isDebugEnabled()) {
            PAMobileAgent.logger.debug("migrateTo " + node);
        }
        Body bodyToMigrate = PAActiveObject.getBodyOnThis();
        if (!(bodyToMigrate instanceof Migratable)) {
            throw new MigrationException(
                "This body cannot migrate. It doesn't implement Migratable interface");
        }

        ((Migratable) bodyToMigrate).migrateTo(node);
    }

    /**
     * Migrates the active object whose active thread is calling this method to the
     * same location as the active object given in parameter.
     * This method must be called from an active object using the active thread as the
     * current thread will be used to find which active object is calling the method.
     * The object given as destination must be an active object.
     * @param activeObject the active object indicating the destination of the migration.
     * @exception MigrationException if the migration fails
     * @see PAActiveObject#getBodyOnThis
     */
    public static void migrateTo(Object activeObject) throws MigrationException {
        migrateTo(PAMobileAgent.getNodeFromURL(
                PAMobileAgent.getNodeURLFromActiveObject(activeObject)));
    }

    /**
     * Migrates the active object whose active thread is calling this method to the
     * node caracterized by the given url.
     * This method must be called from an active object using the active thread as the
     * current thread will be used to find which active object is calling the method.
     * The url must be the url of an existing node.
     * @param nodeURL the url of an existing where to migrate to.
     * @exception MigrationException if the migration fails
     * @see PAActiveObject#getBodyOnThis
     */
    public static void migrateTo(String nodeURL) throws MigrationException {
        if (PAMobileAgent.logger.isDebugEnabled()) {
            PAMobileAgent.logger.debug("migrateTo " + nodeURL);
        }
        PAMobileAgent.migrateTo(PAMobileAgent.getNodeFromURL(nodeURL));
    }

    /**
     * Migrates the body <code>bodyToMigrate</code> to the given node.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param node an existing node where to migrate to.
     * @param isNFRequest a boolean indicating that the request is not functional i.e it does not modify the application's computation
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, Node node,
        boolean isNFRequest) throws MigrationException {
        //In the context of ProActive, migration of an active object is considered as a non functional request.
        //That's why "true" is set by default for the "isNFRequest" parameter.
        PAMobileAgent.migrateTo(bodyToMigrate, node, true,
            org.objectweb.proactive.core.body.request.Request.NFREQUEST_IMMEDIATE_PRIORITY);
    }

    /**
     * Migrates the body <code>bodyToMigrate</code> to the given node.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param node an existing node where to migrate to.
     * @param isNFRequest a boolean indicating that the request is not functional i.e it does not modify the application's computation
     * @param priority  the level of priority of the non functional request. Levels are defined in Request interface of ProActive.
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, Node node,
        boolean isNFRequest, int priority) throws MigrationException {
        if (!(bodyToMigrate instanceof Migratable)) {
            throw new MigrationException(
                "This body cannot migrate. It doesn't implement Migratable interface");
        }

        Object[] arguments = { node };

        try {
            BodyRequest request = new BodyRequest(bodyToMigrate, "migrateTo",
                    new Class[] { Node.class }, arguments, isNFRequest, priority);
            request.send(bodyToMigrate);
        } catch (NoSuchMethodException e) {
            throw new MigrationException("Cannot find method migrateTo this body. Non sense since the body is instance of Migratable",
                e);
        } catch (java.io.IOException e) {
            throw new MigrationException("Cannot send the request to migrate", e);
        }
    }

    /**
     * Migrates the given body to the same location as the active object given in parameter.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param activeObject the active object indicating the destination of the migration.
     * @param isNFRequest a boolean indicating that the request is not functional i.e it does not modify the application's computation
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, Object activeObject,
        boolean isNFRequest) throws MigrationException {
        PAMobileAgent.migrateTo(bodyToMigrate,
            PAMobileAgent.getNodeFromURL(
                PAMobileAgent.getNodeURLFromActiveObject(activeObject)),
            isNFRequest);
    }

    /**
     * Migrates the given body to the node caracterized by the given url.
     * This method can be called from any object and does not perform the migration.
     * Instead it generates a migration request that is sent to the targeted body.
     * The object given as destination must be an active object.
     * @param bodyToMigrate the body to migrate.
     * @param nodeURL the url of an existing where to migrate to.
     * @param isNFRequest a boolean indicating that the request is not functional i.e it does not modify the application's computation
     * @exception MigrationException if the migration fails
     */
    public static void migrateTo(Body bodyToMigrate, String nodeURL,
        boolean isNFRequest) throws MigrationException {
        PAMobileAgent.migrateTo(bodyToMigrate,
            PAMobileAgent.getNodeFromURL(nodeURL), isNFRequest);
    }
}
