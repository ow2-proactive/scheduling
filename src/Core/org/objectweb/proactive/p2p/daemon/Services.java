package org.objectweb.proactive.p2p.daemon;

public enum Services {
    P2PRunning(1), RMRunning(2), NotRunning(3), KillAll(4);

    private int val;

    private Services(int num) {
        this.val = num;
    }

    public int getVal() {
        return val;
    }

}