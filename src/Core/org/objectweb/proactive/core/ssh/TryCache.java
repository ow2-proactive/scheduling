/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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
    private java.util.Hashtable<String, Boolean> _hash;

    public TryCache() {
        _hash = new java.util.Hashtable<String, Boolean>();
    }

    private String getKey(String host, int port) {
        return host;
    }

    public boolean everTried(String host, int port) {
        String key = getKey(host, port);
        Boolean bool = _hash.get(key);
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
        Boolean bool = _hash.get(key);
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
