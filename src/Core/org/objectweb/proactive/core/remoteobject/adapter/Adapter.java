package org.objectweb.proactive.core.remoteobject.adapter;

import java.io.Serializable;

import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


public abstract class Adapter<T> implements Serializable, StubObject {
    protected T target;

    public Adapter() {
    }

    public Adapter(T target) {
        this.target = target;
        construct();
    }

    public void setAdapter(T t) {
        this.target = t;
        construct();
    }

    public T getAdapter() {
        return target;
    }

    protected abstract void construct();

    /**
    * set the proxy to the active object
    */
    public void setProxy(Proxy p) {
        ((StubObject) target).setProxy(p);
    }

    /**
     * return the proxy to the active object
     */
    public Proxy getProxy() {
        return ((StubObject) target).getProxy();
    }
}
