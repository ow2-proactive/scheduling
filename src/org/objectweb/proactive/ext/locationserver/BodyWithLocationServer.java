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
package org.objectweb.proactive.ext.locationserver;

import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;

import java.lang.reflect.InvocationTargetException;

public class BodyWithLocationServer extends BodyImpl {

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    public BodyWithLocationServer(ConstructorCall c, String nodeURL) throws
            InvocationTargetException,
            org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException {
        super(c, nodeURL);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    protected MigrationManager createMigrationManager() {
        System.out.println("BodyWithLocationServer.createMigrationManager");
        return new MigrationManagerWithLocationServer(LocationServerFactory.getLocationServer());
    }


    /**
     * Creates the factory in charge of constructing the requests.
     * @return the factory in charge of constructing the requests.
     */
    protected RequestFactory createRequestFactory() {
        return new RequestWithLocationServerFactory();
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //

    protected class RequestWithLocationServerFactory implements RequestFactory,
            java.io.Serializable {

        private LocationServer server = LocationServerFactory.getLocationServer();

        /**
         * Creates a request object based on the given parameter
         * @return a Request object.
         */
        public Request createRequest(MethodCall methodCall, UniversalBody sourceBody,
                                     boolean isOneWay, long sequenceID) {
            return new RequestWithLocationServer(methodCall, sourceBody, isOneWay, sequenceID, server);
        }

    } // end inner class RequestWithLocationServerFactory

}
