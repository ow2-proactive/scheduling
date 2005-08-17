package org.objectweb.proactive.core.exceptions.manager;

import org.objectweb.proactive.core.exceptions.NonFunctionalException;

public interface NFEProducer {
    public void addNFEListener(NFEListener listener);

    public void removeNFEListener(NFEListener listener);

    public int fireNFE(NonFunctionalException e);
}

/*
 * To implement this interface, the following "mixin" can be copy/pasted:
 *
   // NFEProducer implementation
   private NFEListenerList nfeListeners = null;
   public void addNFEListener(NFEListener listener) {
       if (nfeListeners == null) {
           nfeListeners = new NFEListenerList();
       }
       nfeListeners.addNFEListener(listener);
   }
   public void removeNFEListener(NFEListener listener) {
       if (nfeListeners != null) {
           nfeListeners.removeNFEListener(listener);
       }
   }
   public int fireNFE(NonFunctionalException e) {
   	   if (nfeListeners != null) {
           return nfeListeners.fireNFE(e);
       }
       return 0;
   }
 */
