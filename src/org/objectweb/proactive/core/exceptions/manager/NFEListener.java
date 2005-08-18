package org.objectweb.proactive.core.exceptions.manager;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;


public interface NFEListener {
    public void handleNFE(NonFunctionalException e);
}
