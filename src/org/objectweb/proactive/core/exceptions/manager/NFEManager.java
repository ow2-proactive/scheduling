package org.objectweb.proactive.core.exceptions.manager;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
import org.objectweb.proactive.core.exceptions.body.BodyNonFunctionalException;
import org.objectweb.proactive.core.exceptions.proxy.ProxyNonFunctionalException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class NFEManager {
    // NFEProducer implementation
    private static NFEListenerList nfeListeners = null;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.NFE);

    public static void addNFEListener(NFEListener listener) {
        if (nfeListeners == null) {
            nfeListeners = new NFEListenerList();
        }
        nfeListeners.addNFEListener(listener);
    }

    public static void removeNFEListener(NFEListener listener) {
        if (nfeListeners != null) {
            nfeListeners.removeNFEListener(listener);
        }
    }

    public static int fireNFE(BodyNonFunctionalException e, UniversalBody body) {
        return fireNFE(e, body, null);
    }

    public static int fireNFE(ProxyNonFunctionalException e, AbstractProxy proxy) {
        return fireNFE(e, null, proxy);
    }

    private static int fireNFE(NonFunctionalException e, UniversalBody body,
        AbstractProxy proxy) {
        int nbListeners = 0;

        if (body != null) {
            nbListeners += body.fireNFE(e);
        }

        if (proxy != null) {
            nbListeners += proxy.fireNFE(e);
        }

        if (nfeListeners != null) {
            nbListeners += nfeListeners.fireNFE(e);
        }

        if (nbListeners == 0) {
            defaultNFEHandler(e);
        }

        return nbListeners;
    }

    public static void defaultNFEHandler(NonFunctionalException nfe) {
        try {
            throw nfe;
        } catch (BodyNonFunctionalException bnfe) {

            /* Avoid killing an AO */
            logger.warn("NFE in a Body", nfe);

            /* But the exception will be thrown if it's on the proxy */
        }
    }
}
