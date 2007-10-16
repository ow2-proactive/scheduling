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
package org.objectweb.proactive.ext.locationserver;

import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.rmi.ProActiveRmiMetaObjectFactory;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * <p>
 * This class overrides the default Factory to provide Request and MigrationManager
 * with location server.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public class LocationServerMetaObjectFactory
    extends ProActiveRmiMetaObjectFactory {
    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    private static MetaObjectFactory instance = null;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Constructor for LocationServerMetaObjectFactory.
     */
    protected LocationServerMetaObjectFactory() {
        super();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public static synchronized MetaObjectFactory newInstance() {
        if (instance == null) {
            instance = new LocationServerMetaObjectFactory();
        }
        return instance;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
    protected RequestFactory newRequestFactorySingleton() {
        return new RequestWithLocationServerFactory();
    }

    @Override
    protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
        return new MigrationManagerFactoryImpl();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class RequestWithLocationServerFactory implements RequestFactory,
        java.io.Serializable {
        transient private LocationServer server = LocationServerFactory.getLocationServer();

        public Request newRequest(MethodCall methodCall,
            UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
            return new RequestWithLocationServer(methodCall, sourceBody,
                isOneWay, sequenceID, server);
        }
    }

    protected static class MigrationManagerFactoryImpl
        implements MigrationManagerFactory, java.io.Serializable {
        public MigrationManager newMigrationManager() {
            return new MigrationManagerWithLocationServer(LocationServerFactory.getLocationServer());
        }
    }
}
