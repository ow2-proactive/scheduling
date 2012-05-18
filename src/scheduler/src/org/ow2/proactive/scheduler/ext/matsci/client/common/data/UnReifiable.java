package org.ow2.proactive.scheduler.ext.matsci.client.common.data;

import java.io.Serializable;


/**
 * UnReifiable
 *
 * @author The ProActive Team
 */
public final class UnReifiable<T> implements Serializable {

    private static final long serialVersionUID = 32L;
    T t;

    public UnReifiable() {

    }

    public UnReifiable(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public void set(T t) {
        this.t = t;
    }
}
