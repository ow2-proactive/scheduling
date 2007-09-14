package org.objectweb.proactive.extra.p2pTest.utils;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;


public class ShutdownHook extends Thread {
    ProActiveDescriptor pad = null;

    public ShutdownHook(ProActiveDescriptor pad) {
        this.pad = pad;
    }

    public void run() {
        try {
            pad.killall(false);
        } catch (Exception e) {
        }
    }
}
