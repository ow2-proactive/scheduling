package org.objectweb.proactive.core.ssh;


/**
 *
 * @author mlacage
 *
 * This class maintains state which reflects whether a given host has already
 * been contacted through a tunnel or a direct connection or has
 * never been contacted before.
 */
public class TryCache {
    private java.util.Hashtable _hash;

    public TryCache() {
        _hash = new java.util.Hashtable();
    }

    private String getKey(String host, int port) {
        return host;
    }

    public boolean everTried(String host, int port) {
        String key = getKey(host, port);
        Boolean bool = (Boolean) _hash.get(key);
        if (bool != null) {
            // already tried this connection once.
            return true;
        } else {
            // never tried before.
            return false;
        }
    }

    /**
     * Return true only if we need to try this
     * host/port pair.
     */
    public boolean needToTry(String host, int port) {
        String key = getKey(host, port);
        Boolean bool = (Boolean) _hash.get(key);
        if (bool != null) {
            if (bool.booleanValue()) {
                // tried with success before.
                return true;
            } else {
                // previous try failed.
                return false;
            }
        } else {
            // never tried before.
            return true;
        }
    }

    public void recordTrySuccess(String host, int port) {
        String key = getKey(host, port);
        _hash.put(key, new Boolean(true));
    }

    public void recordTryFailure(String host, int port) {
        String key = getKey(host, port);
        _hash.put(key, new Boolean(false));
    }
}
