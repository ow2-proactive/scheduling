package org.objectweb.proactive.extra.p2p.daemon;

public enum Priorities {
    ServicePriority(1), ScreenSaverPriority(2), LowestPriority(10000);

    private int val;

    private Priorities(int num) {
        this.val = num;
    }

    public int getVal() {
        return val;
    }

}