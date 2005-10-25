package org.objectweb.proactive.core.exceptions.manager;

import java.io.Serializable;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


class NoOpNFEListener implements NFEListener {
    public boolean handleNFE(NonFunctionalException e) {

        /* do nothing */
    	return true;
    }
}


public interface NFEListener extends Serializable {
    public static final NoOpNFEListener NOOP_LISTENER = new NoOpNFEListener();

    public boolean handleNFE(NonFunctionalException e);
}
