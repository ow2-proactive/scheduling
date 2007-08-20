package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;
public class Executor {
    final static private Executor singleton = new Executor();
    private List<Thread> threads;

    private Executor() {
        GCMD_LOGGER.trace("Executor started");
        threads = new ArrayList<Thread>();
    }

    static public synchronized Executor getExecutor() {
        return singleton;
    }

    public void submit(String command) {
        GCMD_LOGGER.trace("Command submited: " + command);
        try {
            System.out.println("executing command=" + command);

            Process p = Runtime.getRuntime()
                               .exec(new String[] { "sh", "-c", command });

            InputStreamMonitor stdoutM = new InputStreamMonitor(MonitorType.STDOUT,
                    p.getInputStream(), command);
            InputStreamMonitor stderrM = new InputStreamMonitor(MonitorType.STDERR,
                    p.getErrorStream(), command);
            stderrM.start();
            stdoutM.start();
            threads.add(stdoutM);
            threads.add(stderrM);
        } catch (IOException e) {
            GCMD_LOGGER.warn("Cannot execute: " + command, e);
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (Thread t : threads) {
            t.join();
        }
    }
    private enum MonitorType {STDOUT,
        STDERR;
    }
    private class InputStreamMonitor extends Thread {
        MonitorType type;
        InputStream stream;
        String cmd;

        public InputStreamMonitor(MonitorType type, InputStream stream,
            String cmd) {
            GCMD_LOGGER.trace("Monitor started: " + type.name() + " " + cmd);
            this.type = type;
            this.stream = stream;
            this.cmd = cmd;
        }

        public void run() {
            try {
                BufferedReader br;
                String line;

                br = new BufferedReader(new InputStreamReader(stream));
                while ((line = br.readLine()) != null) {
                    GCMD_LOGGER.info(line);
                }
                GCMD_LOGGER.trace("Monitor exited: " + type.name() + " " + cmd);
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }
}
