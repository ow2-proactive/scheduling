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
package org.objectweb.proactive.core.component.collectiveitfs;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.exceptions.ExceptionThrower;
import org.objectweb.proactive.core.node.Node;


/**
 * <p>Manages the distribution and update of futures when gathercast methods return a result.</p>
 *
 * <p>If the invoked method on a gathercast interface
 * returns a result, the method returns a future, although the invocation has
 * not been processed yet (an invocation on a gathercast interface will not proceed until
 * all client interfaces invoked the same method). We faced a complex problem: how to
 * return and update futures of client invocations on gathercast interfaces? We considered
 * two strategies. The fist one was to customize the request queue so that a local data
 * structure (similar to the one described in figure 5.9) would handle the incoming requests
 * for gathercast interfaces. A second option was to use a dedicated tier active object for
 * handling futures.</p>
 *
 * <p>As we did not want to intervene in the core of the ProActive library by modifying the
 * request queue, we selected and implemented the second option, which also provides an example
 * of the management of futures and automatic continuations with active objects.</p>
 *
 * <p>One futures handler active object is
 * created for each gathercast request to be processed. It has a special activity, which only
 * serves distribute requests once it has received the <code>setFutureResult</code> request.
 * When a request from a client is served by the gathercast interface, it is enqueued in
 * the queue data structure, and the result which is return is the result of the invocation
 * of the distribute method (with an index) on the futures handler object. This result is
 * therefore a future itself.</p>
 *
 * <p>When all clients have invoked the same method on the gathercast interface, a new
 * request is built and served, which leads to an invocation which is performed either on
 * the base object if the component is primitive, or on another connected interface if the
 * component is composite. The result of this invocation is sent to the futures handler
 * object, by invoking the <code>setFutureResult</code> method. The futures handler will then block
 * until the result value is available. Then the distribute methods are served and the
 * values of the futures received by the clients are updated.</p>
 *
 * <p>Although this mechanism fulfills its role using the standard mechanism of the li-
 * brary, we observed that it does not scale very well: one active object for managing fu-
 * tures is created for each gathercast request, and even though we implemented a pool
 * of active objects, there are too many active objects created when stressing the gather-
 * cast interface. Therefore, the first approach envisaged above should be preferred in the
 * future (this approach is not currently implemented; it is quite complex and
 * deals with sensitive parts of the library). </p>

 * @author The ProActive Team
 *
 */
public class GatherFuturesHandler implements RunActive, Serializable {
    List<ItfID> senders;
    List<?> resultOfGatheredInvocation;
    Throwable exceptionToRaise;
    String methodName = null;
    int step = 0;

    public GatherFuturesHandler() {
    }

    public void setFutureOfGatheredInvocation(MethodCallResult future) {
        if (future.getException() != null) {
            exceptionToRaise = future.getException();
        } else {
            // no cast for futures ==> need to get the result before casting
            resultOfGatheredInvocation = (List<?>) future.getResult();
            PAFuture.waitFor(resultOfGatheredInvocation);
        }
    }

    // returns
    public Object distribute(ItfID sender) {
        if (exceptionToRaise != null) {
            ExceptionThrower.throwException(exceptionToRaise); // guillaume's exception thrower
        }

        // redistribution policies for results can be applied here
        return resultOfGatheredInvocation.get(senders.indexOf(sender));
    }

    public void migrateTo(Node node) throws MigrationException {
        PAMobileAgent.migrateTo(node);
    }

    public void setConnectedClientItfs(List<ItfID> connectedClientItfs) {
        senders = connectedClientItfs;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void passivate() {
        senders = null;
        methodName = null;
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (PAActiveObject.getBodyOnThis().isActive()) {
            service.blockingServeOldest("setConnectedClientItfs");

            service.blockingServeOldest("setFutureOfGatheredInvocation");
            if (senders != null) {
                for (ItfID senderID : senders) {
                    service.blockingServeOldest("distribute");
                }
            }

            service.blockingServeOldest("passivate");
        }
    }
}
