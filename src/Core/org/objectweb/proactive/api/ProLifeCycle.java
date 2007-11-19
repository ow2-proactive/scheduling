package org.objectweb.proactive.api;

import org.objectweb.proactive.core.gc.HalfBodies;


public class ProLifeCycle {

    /**
     * Inform the ProActive DGC that all non active threads will not use
     * anymore their references to active objects. This is needed when the
     * local GC does not reclaim stubs quickly enough.
     */
    public static void userThreadTerminated() {
        HalfBodies.end();
    }
}
