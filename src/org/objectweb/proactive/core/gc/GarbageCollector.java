package org.objectweb.proactive.core.gc;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * This class will become some day a garbage collector
 */
public class GarbageCollector {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.GC);

    /**
     * List of (weak references to) proxies
     */
    private final List<Proxy> proxies;

    /**
     * Build a GarbageCollector instance for each active object
     */
    public GarbageCollector(AbstractBody body) {
        this.proxies = new LinkedList<Proxy>();
    }

    /**
     * Turn all proxies into strong references, in order to remove dead
     * proxies, and work on alive ones
     */
    private void strongReferenceProxies() {
        int nrRemoved = 0;

        for (ListIterator<Proxy> iter = this.proxies.listIterator();
                iter.hasNext();) {
            Proxy p = iter.next();

            if (!p.setStrong()) {
                iter.remove();
                nrRemoved++;
            }
        }

        if (nrRemoved != 0) {
            logger.debug("Removed " + nrRemoved + " proxies");
        }
    }

    /**
     * After working with strong references, reset the proxies to weak
     * references
     */
    private void weakReferenceProxies() {
        for (Proxy p : this.proxies) {
            p.setWeak();
        }
    }

    public synchronized void addProxy(UniversalBodyProxy proxy) {
        proxies.add(new Proxy(proxy));
        logger.debug("New proxy");
    }

    public static String getDgcState(UniqueID bodyID) {
        UniversalBody body = LocalBodyStore.getInstance().getLocalBody(bodyID);

        if (body == null) {
            logger.error("Body " + bodyID + " not found");

            return "Body not found";
        }

        return "DGC";
    }

    public Collection<UniversalBodyProxy> getReferences() {
        Collection<UniversalBodyProxy> refs = new LinkedList<UniversalBodyProxy>();
        strongReferenceProxies();

        for (Proxy p : this.proxies) {
            refs.add(p.getStrong());
        }

        weakReferenceProxies();

        return refs;
    }

    public static boolean isBuildingTopology() {
        return "true".equals(System.getProperty("proactive.build_topology"));
    }
}
