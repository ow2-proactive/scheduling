package org.objectweb.proactive.core.exceptions.manager;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public interface NFEListener {
    public boolean canHandleNFE(NonFunctionalException e);

    public void handleNFE(NonFunctionalException e);
}
