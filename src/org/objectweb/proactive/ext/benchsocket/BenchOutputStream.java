package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.OutputStream;


public class BenchOutputStream extends OutputStream implements BenchStream {
    private OutputStream realOutputStream;
    private int total;
   private int number; 

    public BenchOutputStream(OutputStream o, int number) {
        this.realOutputStream = o;
        this.number = number;
        //we register a hook to be run
        //when the JVM is killed
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
    }

    public void write(int b) throws IOException {
    	if (BenchSocketFactory.measure){
    		total ++;
    	}
        this.realOutputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
    	if (BenchSocketFactory.measure){
    		total += len;
    	}
       
        this.realOutputStream.write(b, off, len);
    }

    public void write(byte[] b) throws IOException {
    	if (BenchSocketFactory.measure){
    		total += b.length;
    	}
        this.realOutputStream.write(b);
    }

    public void displayTotal() {
        System.out.println("=== Total Output for socket " + number + " = " + total);
    }
}
