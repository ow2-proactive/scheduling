package org.objectweb.proactive.ext.benchsocket;

public class ShutdownThread extends Thread {
    BenchStream bos;

    public ShutdownThread(BenchStream bos) {
        this.bos = bos;
    }

    public void run() {
        this.bos.displayTotal();
    }
}
