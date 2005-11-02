/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.body.http.util.messages;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.http.util.HttpMessage;
import org.objectweb.proactive.core.body.http.util.HttpUtils;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.ext.security.exceptions.RenegotiateSessionException;


public class HttpRequest extends HttpMessage implements Serializable {
    private Request request;
    private UniqueID IdBody;

    public HttpRequest(Request request, UniqueID idBody, String url) {
        super(url);
        this.request = request;
        this.IdBody = idBody;
    }

    public int getReturnedObject() {
        if (this.returnedObject != null) {
            return ((Integer) this.returnedObject).intValue();
        }

        return 0; // or throws an exception ...
    }

    /**
     *
     */
    public Object processMessage() {
        if (this.request != null) {
            try {
                Body body = HttpUtils.getBody(IdBody);

                //TODO 
                if (body == null) {
                    try {
                        Thread.sleep(1000);
                        body = HttpUtils.getBody(IdBody);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                return new Integer(body.receiveRequest(this.request));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
