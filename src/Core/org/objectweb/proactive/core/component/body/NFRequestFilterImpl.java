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
package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.request.ComponentRequest;


/**
 * This class is a filter for non functional component requests : it can separate
 * component controller requests from component functional requests.
 *
 * @author The ProActive Team
 *
 */
public class NFRequestFilterImpl implements RequestFilter, java.io.Serializable {
    public NFRequestFilterImpl() {
    }

    // TODO_M requestPriority
    /**
     * This methods verifies whether a request is a component controller request.
     * @param request the request to filter
     * @return true if the request is a component controller request, false otherwise
     */
    public boolean acceptRequest(Request request) {
        if (request instanceof ComponentRequest) {
            return acceptRequest((ComponentRequest) request);
        } else {
            // standard requests cannot be component controller requests
            return false;
        }
    }

    /**
     * This methods verifies whether a component request is a component controller request.
     * @param request the component request to filter
     * @return true if the request is a component controller request, false otherwise
     */
    public boolean acceptRequest(ComponentRequest request) {
        return (request).isControllerRequest();
    }
}
