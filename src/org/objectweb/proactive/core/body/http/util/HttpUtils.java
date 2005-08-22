/*
 * Created on Apr 8, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.body.http.util;

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
