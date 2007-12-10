package org.objectweb.proactive.api;

import org.objectweb.proactive.Main;
import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
public class PAVersion {

    /**
     * Returns the version number
     */
    public static String getProActiveVersion() {
        return Main.getProActiveVersion();
    }
}
