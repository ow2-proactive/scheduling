/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.*;


/*
 * The DataModelTraversal is the order in which children are explored.
 * As we can visit the keys in any order, we let the user choose.
 * So we propose 3 views, and a custom one.
 */
public class DataModelTraversal implements JobMonitorConstants {
    private Branch[] branches;

    public DataModelTraversal(int[] keys) {
        branches = new Branch[keys.length];
        for (int i = 0; i < keys.length; i++)
            branches[i] = new Branch(keys[i]);
    }

    public int getNbKey() {
        return branches.length;
    }

    public Branch getBranch(int index) {
        return branches[index];
    }

    public int indexOf(int key) {
        for (int i = 0; i < branches.length; i++)
            if (branches[i].getKey() == key) {
                return i;
            }

        return -1;
    }

    public int getFollowingKey(int key) {
        int newIndex;
        int newKey;
        if (key == NO_KEY) {
            // root
            newIndex = 0;
        } else {
            int index = indexOf(key);
            newIndex = index + 1;
        }

        do {
            if (newIndex == branches.length) {
                return NO_KEY;
            }
            newKey = branches[newIndex].getKey();
            newIndex++;
        } while (isHidden(newKey));

        return newKey;
    }

    public void setHidden(int key, boolean hide) {
        if (key != NO_KEY) {
            branches[indexOf(key)].setHidden(hide);
        }
    }

    public boolean isHidden(int key) {
        if (key != NO_KEY) {
            return branches[indexOf(key)].isHidden();
        }

        return false;
    }

    public void setHighlighted(int key, boolean highlight) {
        if (key != NO_KEY) {
            branches[indexOf(key)].setHighlighted(highlight);
        }
    }

    public boolean isHighlighted(int key) {
        if (key != NO_KEY) {
            return branches[indexOf(key)].isHighlighted();
        }

        return false;
    }

    public void exchange(int fromKey, int toKey) {
        int fromIndex = indexOf(fromKey);
        int toIndex = indexOf(toKey);

        Branch tmp = branches[fromIndex];
        branches[fromIndex] = branches[toIndex];
        branches[toIndex] = tmp;
    }
}
