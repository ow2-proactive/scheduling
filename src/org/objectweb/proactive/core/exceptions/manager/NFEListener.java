package org.objectweb.proactive.core.exceptions.manager;

import java.io.Serializable;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


class NoOpNFEListener implements NFEListener {
    public boolean canHandleNFE(NonFunctionalException e) {
        return true;
    }

    public void handleNFE(NonFunctionalException e) {

        /* do nothing */
    }
}


public interface NFEListener extends Serializable {
    public static final NoOpNFEListener NOOP_LISTENER = new NoOpNFEListener();

    public boolean canHandleNFE(NonFunctionalException e);

    public void handleNFE(NonFunctionalException e);
}
