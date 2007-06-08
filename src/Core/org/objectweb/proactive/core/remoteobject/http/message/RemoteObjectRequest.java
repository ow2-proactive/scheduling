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
package org.objectweb.proactive.core.remoteobject.http.message;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectRequest extends HttpMessage implements Serializable {
    private Request request;

    public RemoteObjectRequest(Request request, String url) {
        super(url);
        this.request = request;
    }

    public Object getReturnedObject() { //throws Exception {
                                        //        if (this.returnedObject instanceof Exception) {
                                        //            throw (Exception) this.returnedObject;
                                        //        }
        return this.returnedObject;
    }

    @Override
    public boolean isOneWay() {
        return this.request.isOneWay();
    }

    /**
     *
     */
    @Override
    public Object processMessage() {
        if (this.request != null) {
            try {
                RemoteObject ro = HTTPRegistry.getInstance().lookup(url);

                if (ro == null) {
                    try {
                        Thread.sleep(1000);
                        ro = HTTPRegistry.getInstance().lookup(url);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                Object o = ro.receiveMessage(this.request);

                return o;
            } catch (Exception e) {
                return e;
            }
        }

        return null;
    }
}
