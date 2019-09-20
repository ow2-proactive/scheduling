/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.common.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Set based on #getKey() that provides log(N) to add and remove, but keeps sorted version of items based on #getCounter().
 * @param <T>
 */
public class SortedUniqueSet<T extends SortedUniqueSet.Unique & Comparable<T> & Serializable> implements Serializable {

    interface Unique {

        /**
         * @return key which should be unique
         */
        String getKey();

        /**
         * @return counter which is used to keep sorted collection
         */
        long getCounter();
    }

    private TreeSet<T> sortedItems = new TreeSet<>();

    private HashMap<String, T> items = new HashMap<>();

    public void add(T toAdd) {
        remove(toAdd);

        items.put(toAdd.getKey(), toAdd);
        sortedItems.add(toAdd);
    }

    public Optional<T> get(String key) {
        final T value = items.get(key);
        if (value != null) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    public boolean remove(T toRemove) {
        if (items.containsKey(toRemove.getKey())) {
            final T oldItem = items.remove(toRemove.getKey());
            sortedItems.remove(oldItem);
            return true;
        } else {
            return false;
        }
    }

    public SortedSet<T> getSortedItems() {
        return sortedItems;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Iterator<T> iterator() {
        return sortedItems.iterator();
    }

    public boolean contains(String key) {
        return items.containsKey(key);
    }

    public void clear() {
        items.clear();
        sortedItems.clear();
    }

}
