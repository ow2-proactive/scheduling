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
package org.objectweb.proactive.core.body;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;


/**
 * Define an execution context for a thread. A context is associated to a thread.
 * A context contains the body associated to the thread, and the currently served request (or null if any).
 * @see org.objectweb.proactive.core.body.LocalBodyStore
 * @author The ProActive Team
 * @since 3.2.1
 */
@PublicAPI
public class Context implements Serializable {

    /** Body associated to this context */
    private final Body body;

    /** The currently served request */
    private final Request currentRequest;

    /**
     * Create a new context.
     * @param owner the body associated to this context.
     * @param currentRequest the currently served request, null if any.
     */
    public Context(Body owner, Request currentRequest) {
        this.body = owner;
        this.currentRequest = currentRequest;
    }

    /**
     * @return the body associated to this context.
     */
    public Body getBody() {
        return body;
    }

    /**
     * @return the currently served request, null if any.
     */
    public Request getCurrentRequest() {
        return currentRequest;
    }

    /**
     * Returns a stub on the active object that sent the currently served request.
     * @return a stub on the active object that sent the currently served request.
     */
    public Object getStubOnCaller() {
        if (this.currentRequest != null) {
            try {
                UniversalBody caller = currentRequest.getSender();
                return MOP.createStubObject(caller.getReifiedClassName(), caller);
            } catch (MOPException e) {
                throw new ProActiveRuntimeException("Cannot create stub on caller : " + e);
            }
        } else {
            throw new ProActiveRuntimeException("No request is currently served by " + this.body);
        }
    }

    /**
     * Pretty printing.
     */
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer("Execution context for body " + this.body.getID() + " : ");
        if (this.currentRequest == null) {
            res.append("no current service.");
        } else {
            res.append("service of " + this.currentRequest.getMethodName() + " from " +
                this.currentRequest.getSourceBodyID());
        }
        return res.toString();
    }
}
