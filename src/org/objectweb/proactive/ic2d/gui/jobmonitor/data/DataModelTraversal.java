package org.objectweb.proactive.ic2d.gui.jobmonitor.data;

import org.objectweb.proactive.ic2d.gui.jobmonitor.*;


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
