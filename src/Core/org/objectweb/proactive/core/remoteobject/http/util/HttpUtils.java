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
package org.objectweb.proactive.core.remoteobject.http.util;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpUtils {
    public static final String SERVICE_REQUEST_CONTENT_TYPE = "application/java";
    public static final String SERVICE_REQUEST_URI = "/ProActiveHTTP";

    /**
     *  Search a Body matching with a given unique ID
     * @param id The unique id of the body we are searching for
     * @return The body associated with the ID
     */
    public static Body getBody(UniqueID id) {
        LocalBodyStore bodyStore = LocalBodyStore.getInstance();

        Body body = bodyStore.getLocalBody(id);

        if (body == null) {
            body = LocalBodyStore.getInstance().getLocalHalfBody(id);
        }

        if (body == null) {
            body = LocalBodyStore.getInstance().getForwarder(id);
        }

        return body;
    }
}
