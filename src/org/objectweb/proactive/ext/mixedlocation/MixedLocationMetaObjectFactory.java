/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ext.mixedlocation;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.RemoteBodyFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ibis.IbisBodyAdapter;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;


/**
 * <p>
 * This class overrides the default Factory to provide Request and MigrationManager
 * with a mixed location server.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public class MixedLocationMetaObjectFactory extends ProActiveMetaObjectFactory {
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
    protected MixedLocationMetaObjectFactory() {
        super();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public static synchronized MetaObjectFactory newInstance() {
        if (instance == null) {
            instance = new MixedLocationMetaObjectFactory();
        }
        return instance;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected RequestFactory newRequestFactorySingleton() {
        return new RequestWithMixedLocationFactory();
    }

    protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
        return new MigrationManagerFactoryImpl();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class RequestWithMixedLocationFactory implements RequestFactory,
        java.io.Serializable {
        transient private LocationServer server = LocationServerFactory.getLocationServer();

        public Request newRequest(MethodCall methodCall,
            UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
            return new RequestWithMixedLocation(methodCall, sourceBody,
                isOneWay, sequenceID, server);
        }
    }

    protected static class MigrationManagerFactoryImpl
        implements MigrationManagerFactory, java.io.Serializable {
        public MigrationManager newMigrationManager() {
            //System.out.println("BodyWithMixedLocation.createMigrationManager");
            return new MigrationManagerWithMixedLocation(LocationServerFactory.getLocationServer());
        }
    }

    protected RemoteBodyFactory newRemoteBodyFactorySingleton() {
        if ("ibis".equals(System.getProperty("proactive.communication.protocol"))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Factory is ibis");
            }
            return new RemoteIbisBodyFactoryImpl();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Factory is rmi");
            }
            return new RemoteRmiBodyFactoryImpl();
        }
    }

    protected static class RemoteIbisBodyFactoryImpl
        implements RemoteBodyFactory, java.io.Serializable {
        public UniversalBody newRemoteBody(UniversalBody body) {
            try {
                // 	System.out.println("Creating ibis remote body adapter");
                return new IbisBodyAdapter(body);
            } catch (ProActiveException e) {
                throw new ProActiveRuntimeException("Cannot create Ibis Remote body adapter ",
                    e);
            }
        }
    }

    // end
    protected static class RemoteRmiBodyFactoryImpl implements RemoteBodyFactory,
        java.io.Serializable {
        public UniversalBody newRemoteBody(UniversalBody body) {
            try {
                return new org.objectweb.proactive.core.body.rmi.RmiBodyAdapter(body);
            } catch (ProActiveException e) {
                throw new ProActiveRuntimeException("Cannot create Remote body adapter ",
                    e);
            }
        }
    }

    // end inner class RemoteBodyFactoryImpl
}
