/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: Apr 18, 2002
 * Time: 2:39:30 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.objectweb.proactive.ext.mixedlocation;

import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;

import java.io.Serializable;

public class BodyWithMixedLocation extends BodyImpl implements Serializable {

    public BodyWithMixedLocation(ConstructorCall c, String nodeURL) throws
            java.lang.reflect.InvocationTargetException, ConstructorCallExecutionFailedException {
        super(c, nodeURL);
        //  startBody();
    }

    /**
     * Creates the factory in charge of constructing the requests.
     * @return the factory in charge of constructing the requests.
     */
    protected RequestFactory createRequestFactory() {
        return new RequestWithMixedLocationFactory();
    }

    protected MigrationManager createMigrationManager() {
        System.out.println("BodyWithMixedLocation.createMigrationManager");
        return new MigrationManagerWithMixedLocation(LocationServerFactory.getLocationServer());
    }

    protected class RequestWithMixedLocationFactory implements RequestFactory,
            java.io.Serializable {
        private LocationServer server = LocationServerFactory.getLocationServer();

        /**
         * Creates a request object based on the given parameter
         * @return a Request object.
         */
        public Request createRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
            return new RequestWithMixedLocation(methodCall, sourceBody, isOneWay, sequenceID, server);
        }
    }
}
