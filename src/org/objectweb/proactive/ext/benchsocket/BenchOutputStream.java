package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.OutputStream;


public class BenchOutputStream extends OutputStream {
    private OutputStream realOutputStream;
    private int total;

    public BenchOutputStream(OutputStream o) {
        this.realOutputStream = o;
        //we register a hook to be run
        //whant the JVM is killed
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
    }

    public void write(int b) throws IOException {
        //System.out.println("int");
        total++;
        //System.out.println(total);
        this.realOutputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        //	System.out.println("write [], int");
        total += len;
        //System.out.println(total);
        this.realOutputStream.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
        //System.out.println("write []");
        total += b.length;
        //System.out.println(total);
        this.realOutputStream.write(b);
    }

    public void displayTotal() {
        System.out.println("=== Total = " + total);
    }
}
