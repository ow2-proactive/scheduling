package org.objectweb.proactive.core.exceptions.manager;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.HalfBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;
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

    public static int fireNFE(NonFunctionalException e) {
        return fireNFE(e, ProActive.getBodyOnThis());
    }

    public static int fireNFE(NonFunctionalException e, UniversalBody body) {
        return fireNFE(e, body, null);
    }

    public static int fireAndThrowNFE(NonFunctionalException nfe, Exception e,
        AbstractProxy proxy) throws Exception {
        fireNFE(nfe, proxy);
        throw e;
    }

    public static int fireNFE(NonFunctionalException e, AbstractProxy proxy) {
        return fireNFE(e, null, proxy);
    }

    public static int fireNFE(NonFunctionalException e, UniversalBody body,
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

        /* Hack to avoid killing an active object */
        if (ProActive.getBodyOnThis() instanceof HalfBody) {
            logger.warn("NFE in a HalfBody", nfe);
            //throw nfe;
        } else {
            logger.warn("NFE in a Body", nfe);
        }
    }
}
