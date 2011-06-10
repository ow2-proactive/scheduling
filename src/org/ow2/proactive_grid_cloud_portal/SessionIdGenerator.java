package org.ow2.proactive_grid_cloud_portal;

import org.objectweb.proactive.core.UniqueID;

public class SessionIdGenerator {

    public static String newSessionId() {
        return new UniqueID().toString().replaceAll("-","");

    }

}
