package org.objectweb.proactive.ext.benchsocket;

import java.io.IOException;
import java.io.InputStream;


/**
 * @author fabrice
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BenchInputStream extends InputStream implements BenchStream {
    private InputStream realInputStream;
    private int number;
    private int total;

    public BenchInputStream(InputStream real, int number) {
        this.realInputStream = real;
        this.number = number;
        //we register a hook to be run
        //when the JVM is killed
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
    }

    public void displayTotal() {
        System.out.println("=== Total Input for socket " + number + " = " +
            total);
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        return this.realInputStream.available();
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        this.realInputStream.close();
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#mark(int)
     */
    public synchronized void mark(int readlimit) {
        this.realInputStream.mark(readlimit);
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return this.realInputStream.markSupported();
    }

    public int read() throws IOException {
        int tmp = this.realInputStream.read();
        if (BenchSocketFactory.measure) {
            total += tmp;
        }
        total += tmp;
        return tmp;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int tmp = this.realInputStream.read(b, off, len);
        if (BenchSocketFactory.measure) {
            total += tmp;
        }
        return tmp;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        int tmp = this.realInputStream.read(b);
        if (BenchSocketFactory.measure) {
            total += tmp;
        }
        return tmp;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#reset()
     */
    public synchronized void reset() throws IOException {
        this.realInputStream.reset();
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long n) throws IOException {
        return this.realInputStream.skip(n);
    }
}
