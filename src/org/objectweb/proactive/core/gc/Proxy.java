package org.objectweb.proactive.core.gc;

import java.lang.ref.WeakReference;

import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;


public class Proxy {
    private final WeakReference<UniversalBodyProxy> weak;
    private UniversalBodyProxy strong;
    private int strongCount;

    public Proxy(UniversalBodyProxy proxy) {
        this.weak = new WeakReference<UniversalBodyProxy>(proxy);
        this.strong = null;
        this.strongCount = 0;
    }

    /**
     * Make the proxy a strong reference. If the call was successfull, it is
     * mandatory to call setWeak() when the reference is no more needed,
     * otherwise the proxy would never be garbage collected.
     */
    public boolean setStrong() {
        this.strong = this.weak.get();
        if (this.strong != null) {
            this.strongCount++;
        } else if (this.strongCount != 0) {
            throw new IllegalStateException("A strong proxy was GCed");
        }
        return this.strong != null;
    }

    /**
     * Drop a strong reference to the proxy.
     */
    public void setWeak() {
        if (this.strong == null) {
            throw new IllegalStateException("A strong proxy was GCed");
        }
        if (this.strongCount <= 0) {
            throw new IllegalStateException("Proxy reference was not strong");
        }
        if (--this.strongCount == 0) {
            this.strong = null;
        }
    }

    public UniversalBodyProxy getStrong() {
        return this.strong;
    }
}
