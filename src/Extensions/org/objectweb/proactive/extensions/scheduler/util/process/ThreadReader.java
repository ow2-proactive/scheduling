package org.objectweb.proactive.extensions.scheduler.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;


/** Pipe between two streams */
public class ThreadReader implements Runnable {
    private BufferedReader in;
    private PrintStream out;
    private Executable executable;

    public ThreadReader(BufferedReader in, PrintStream out, Executable executable) {
        this.in = in;
        this.out = out;
        this.executable = executable;
    }

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