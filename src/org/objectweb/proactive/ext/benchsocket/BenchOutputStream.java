package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.OutputStream;


public class BenchOutputStream extends OutputStream implements BenchStream {
    private OutputStream realOutputStream;
    private int total;
    private int number;
    private BenchClientSocket parent;
    private ShutdownThread shThread;

    public BenchOutputStream(OutputStream o, int number) {
        this.realOutputStream = o;
        this.number = number;

        //we register a hook to be run
        //when the JVM is killed
        try {
            shThread = new ShutdownThread(this);
            Runtime.getRuntime().addShutdownHook(shThread);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        //  ShutdownThread.addStream(this);
    }

    public BenchOutputStream(OutputStream o, int number,
        BenchClientSocket parent) {
        this(o, number);
        this.parent = parent;
    }

    public void write(int b) throws IOException {
        if (BenchSocketFactory.measure) {
            total++;
        }
        this.realOutputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (BenchSocketFactory.measure) {
            total += len;
        }

        this.realOutputStream.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        if (BenchSocketFactory.measure) {
            total += b.length;
        }
        this.realOutputStream.write(b);
    }

    public synchronized void displayTotal() {
        display("=== Total Output for socket ");
        total = 0;
    }

    public synchronized void dumpIntermediateResults() {
        display("---- Intermediate output for socket ");
    }

    protected void display(String s) {
        if (parent != null) {
            System.out.println(s + "" + number + " = " + total + " real " +
                parent);
        } else {
            System.out.println(s + "" + number + " = " + total);
        }
    }

    public void close() throws IOException {
        //	if (ShutdownThread.removeStream(this)){
        if (this.realOutputStream != null) {
            this.realOutputStream.close();
        }

        //System.out.println("BenchOutputStream.close() on " + this.number);
        this.displayTotal();

        //	}
        //no only we remove the thread, but we also fire it
        //because of java bug #4533
        try {
            Runtime.getRuntime().removeShutdownHook(shThread);
        } catch (Exception e) {
            //	e.printStackTrace();
        }
        if (shThread != null) {
            shThread.fakeRun();
        }
        shThread = null;
        this.parent = null;
    }
}
