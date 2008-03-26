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
package org.objectweb.proactive.core.body.ft.message;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;


/**
 * This class is used for logging a request.
 * It contains its original destination.
 * @author The ProActive Team
 * @since ProActive 2.2
 */
public class RequestLog implements MessageLog {

    /**
     *
     */

    // Logged message and its destination
    private UniversalBody destination;
    private Request request;

    /**
     * Create a request log.
     * @param r The request to log
     * @param d The destination body
     */
    public RequestLog(Request r, UniversalBody d) {
        this.destination = d;
        this.request = r;
    }

    /**
     * Return the logged request
     * @return the logged request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Return the destination of this request
     * @return the destination of this request
     */
    public UniversalBody getDestination() {
        return destination;
    }
}
