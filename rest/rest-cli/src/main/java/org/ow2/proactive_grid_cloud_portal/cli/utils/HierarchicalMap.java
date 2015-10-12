/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HierarchicalMap<K, V> implements Map<K, V> {

    private Map<K, V> child = new HashMap<>();
    private Map<K, V> parent = null;

    public HierarchicalMap(Map<K, V> parent) {
        this.parent = parent;
    }

    public HierarchicalMap() {
        parent = new HashMap<>();
    }

    @Override
    public int size() {
        return (child.size() + parent.size());
    }

    @Override
    public boolean isEmpty() {
        return child.isEmpty() && parent.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return child.containsKey(key) || parent.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return child.containsValue(value) || parent.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return (child.get(key) != null) ? child.get(key) : parent.get(key);
    }

    @Override
    public V put(K key, V value) {
        return child.put(key, value);
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        child.putAll(m);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        values.addAll(child.values());
        values.addAll(parent.values());
        return values;
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
