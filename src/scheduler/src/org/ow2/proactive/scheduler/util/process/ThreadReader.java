package org.ow2.proactive.scheduler.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.ow2.proactive.scheduler.common.task.executable.Executable;


/** Pipe between two streams */
public class ThreadReader implements Runnable {
    private BufferedReader in;
    private PrintStream out;
    private Executable executable;

    /**
     * Create a new instance of ThreadReader.
     *
     * @param in input stream.
     * @param out output stream
     * @param executable Executable that is concerned by the read.
     */
    public ThreadReader(BufferedReader in, PrintStream out, Executable executable) {
        this.in = in;
        this.out = out;
        this.executable = executable;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        String str = null;

        try {
            while ((str = in.readLine()) != null && !executable.isKilled()) {
                out.println(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}