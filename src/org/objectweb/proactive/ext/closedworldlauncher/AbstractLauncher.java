package org.objectweb.proactive.ext.closedworldlauncher;

public abstract class AbstractLauncher implements Runnable {
    protected WorldInfo wi;

    public AbstractLauncher() {
        wi = new WorldInfo();
        wi.init();
    }

    /**
     * Should be implemented to start the needed classes
     * for the current node number
     */
    public abstract void run(int i);

    public void run() {
        this.run(wi.getCurrenHostNumber());
    }
}
