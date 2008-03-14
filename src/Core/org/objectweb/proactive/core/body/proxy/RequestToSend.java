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
package org.objectweb.proactive.core.body.proxy;

import java.io.IOException;

import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.HalfBody;
import org.objectweb.proactive.core.body.SendingThreadPool;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


public class RequestToSend implements Runnable {

    private MethodCall methodCall;
    private Future future;
    private AbstractBody sourceBody;
    private UniversalBodyProxy destBody;

    /**
     * Creates a {@link RequestToSend} instance which contains all the information to send the
     * original request
     */
    public RequestToSend(MethodCall mc, Future ft, AbstractBody sourceBody, UniversalBodyProxy destBody) {
        this.methodCall = mc;
        this.future = ft;
        this.sourceBody = sourceBody;
        this.destBody = destBody;
    }

    /**
     * Started when put in a ThreadPool by {@link SendingThreadPool}
     */
    public void run() {
        boolean isHalfBody = sourceBody instanceof HalfBody;
        try {
            if (!isHalfBody) {
                sourceBody.enterInThreadStore(); // Avoid not-Functional events
            }

            // --- sendRequest ---
            destBody.sendRequest(methodCall, future, sourceBody);
            sourceBody.getSendingQueue().getSendingQueueProxyFor(destBody).sendingACK(); // Success
            // -------------------

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RenegotiateSessionException e) {
            e.printStackTrace();
        } catch (CommunicationForbiddenException e) {
            e.printStackTrace();
        } finally {
            if (!isHalfBody) {
                sourceBody.exitFromThreadStore();
            }
        }
    }

    public String toString() {
        return methodCall.getName();
    }
}
