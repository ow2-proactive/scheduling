/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.p2p.core.service;

import java.io.Serializable;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * @author Alexandre di Costanzo
 *
 */
public class KnownTable implements Serializable {
    private Hashtable table = null;

    public KnownTable() {
        // the empty constructor
    }

    public KnownTableElement[] toArray() {
        if (this.table == null) {
            return new KnownTableElement[0];
        } else {
            return (KnownTableElement[]) this.table.values().toArray(new KnownTableElement[this.table.size()]);
        }
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        if (this.table == null) {
            return 0;
        } else {
            return this.table.size();
        }
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        if (this.table == null) {
            return true;
        } else {
            return this.table.isEmpty();
        }
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(String key) {
        if (this.table == null) {
            return false;
        } else {
            return this.table.containsKey(key);
        }
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(KnownTableElement value) {
        if (this.table == null) {
            return false;
        } else {
            return this.table.containsValue(value);
        }
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public KnownTableElement get(String key) {
        if (this.table == null) {
            return null;
        } else {
            return (KnownTableElement) this.table.get(key);
        }
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public KnownTableElement put(String key, KnownTableElement value) {
        if (this.table == null) {
            this.table = new Hashtable();
        }
        return (KnownTableElement) this.table.put(key, value);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public KnownTableElement remove(String key) {
        if (this.table == null) {
            return null;
        } else {
            return (KnownTableElement) this.table.remove(key);
        }
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        if (this.table == null) {
            this.table = new Hashtable();
        }
        this.table.putAll(t);
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        if (this.table != null) {
            this.table.clear();
        }
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        if (this.table == null) {
            return new TreeSet();
        } else {
            return this.table.keySet();
        }
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        if (this.table == null) {
            return new Vector();
        } else {
            return this.table.values();
        }
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        if (this.table == null) {
            return new TreeSet();
        } else {
            return this.table.entrySet();
        }
    }

    // -------------------------------------------------------------------------
    // Inners Classes
    // -------------------------------------------------------------------------
    protected class KnownTableElement implements Serializable {
        private P2PService service = null;
        private int load = 0;
        private long lastUpdate = 0;
        private String key = null;

        public KnownTableElement() {
            // empty
        }

        public KnownTableElement(String key, P2PService service, int load,
            long lastUpdate) {
            this.service = service;
            this.load = load;
            this.lastUpdate = lastUpdate;
            this.key = key;
        }

        /**
         * @return Returns the lastUpdate.
         */
        public long getLastUpdate() {
            return this.lastUpdate;
        }

        /**
         * @param lastUpdate The lastUpdate to set.
         */
        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        /**
         * @return Returns the load.
         */
        public int getLoad() {
            return this.load;
        }

        /**
         * @param load The load to set.
         */
        public void setLoad(int load) {
            this.load = load;
        }

        /**
         * @return Returns the runtime.
         */
        public P2PService getP2PService() {
            return this.service;
        }

        /**
         * @return Returns the key.
         */
        public String getKey() {
            return key;
        }
    }
}
