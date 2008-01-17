package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

/**
 * @author FRADJ Johann
 */
public class TreeStatistic {

    private int freeNodes = 0;
    private int downNodes = 0;
    private int busyNodes = 0;

    /**
     * To get the freeNodes
     * 
     * @return the freeNodes
     */
    public int getFreeNodes() {
        return freeNodes;
    }

    /**
     * To get the downNodes
     * 
     * @return the downNodes
     */
    public int getDownNodes() {
        return downNodes;
    }

    /**
     * To get the busyNodes
     * 
     * @return the busyNodes
     */
    public int getBusyNodes() {
        return busyNodes;
    }

    public void increaseFreeNodes() {
        freeNodes++;
    }

    public void increaseBusyNodes() {
        busyNodes++;
    }

    public void increaseDownNodes() {
        downNodes++;
    }

    public void decreaseFreeNodes() {
        freeNodes--;
    }

    public void decreaseBusyNodes() {
        busyNodes--;
    }

    public void decreaseDownNodes() {
        downNodes--;
    }
}
