/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
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
