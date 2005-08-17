package org.objectweb.proactive.core.exceptions.manager;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class NFEListenerList implements NFEProducer {

    /* The registered handlers */
    private Collection listeners;

    public NFEListenerList() {
        listeners = new LinkedList();
    }

    public void addNFEListener(NFEListener listener) {
        listeners.add(listener);
    }

    public void removeNFEListener(NFEListener listener) {
        listeners.remove(listener);
    }

    public int fireNFE(NonFunctionalException e) {
        int nbListeners = 0;

        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            NFEListener listener = (NFEListener) iter.next();
            listener.handleNFE(e);
            nbListeners++;
        }

        return nbListeners;
    }
}
