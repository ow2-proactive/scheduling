package org.objectweb.proactive.p2p.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.nt.NTEventLogAppender;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class WinDaemon {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_DAEMON);

    /* Logging */
    private static final String LOG_DIR = ".." + File.separator + "logs" + File.separator;
    private static final String MAX_SIZE = "100KB";
    private static final String LOG_PATTERN = "%d %c %x %m\n\n";
    private static WriterAppender writerAppender;

    private static final String LOG_HEADER = "[Daemon] ";

    private static final int DAEMON_PORT = 12345;

    /* Commands */

    private static final String START_P2P_CMD = "P2P_START";
    private static final String STOP_P2P_CMD = "P2P_STOP";
    private static final String REGISTER_RM_CMD = "RM_REG";
    private static final String UNREGISTER_RM_CMD = "RM_UNREG";
    private static final String KILL_CMD = "KILL";

    private DaemonState state;
    private Runner runner = null;

    static {
        configureLogging();
    }

    public static void main(String[] args) {
        WinDaemon d = new WinDaemon();
    }

    public WinDaemon() {

        this.state = new DaemonState();
        state.setPriority(Priorities.LowestPriority);
        state.setServices(Services.NotRunning);

        try {
            Thread t = startCommandListener();
            t.join();
        } catch (Exception e) {
            flush("Cannot create command listener");
            System.exit(1);
        }
    }

    private Thread startCommandListener() {
        Thread listener = new Thread(new Runnable() {
            public void run() {
                ServerSocket server;

                try {
                    server = new ServerSocket(DAEMON_PORT);
                } catch (IOException ioe) {
                    log(ioe.getMessage(), true);
                    System.exit(1);

                    return; /* To avoid a javac error */
                }

                for (;;) {
                    try {
                        Socket client = server.accept();

                        /*
                         * TODO: When the experimenting phase is done we should
                         * allow connections only from localhost
                         */
                        InputStream stream = client.getInputStream();
                        InputStreamReader ireader = new InputStreamReader(stream);
                        BufferedReader reader = new BufferedReader(ireader);
                        String line = readCommand(reader);
                        handleCommand(client, line);

                        stream.close();
                        client.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        });
        listener.start();
        return listener;
    }

    void handleCommand(Socket client, String command) {
        Pattern pattern = Pattern.compile(";");

        String[] parts = pattern.split(command);
        if (parts.length < 2) {
            log("CorruptedMessage", true);
            return;
        }
        try {
            Priorities cmdPriority = Priorities.valueOf(parts[1]);

            if (command.startsWith(START_P2P_CMD)) {
                P2PInformation info = getP2PInformation(parts);
                tryChangeState(Services.P2PRunning, cmdPriority, info);
            } else if (command.startsWith(STOP_P2P_CMD)) {
                tryChangeState(Services.NotRunning, cmdPriority, null);
            } else if (command.startsWith(REGISTER_RM_CMD)) {
                RMInformation info = getRMInformation(parts);
                tryChangeState(Services.RMRunning, cmdPriority, info);
            } else if (command.startsWith(UNREGISTER_RM_CMD)) {
                tryChangeState(Services.NotRunning, cmdPriority, null);
            } else if (command.startsWith(KILL_CMD)) {
                tryChangeState(Services.KillAll, cmdPriority, null);
            } else
                return;

        } catch (IllegalArgumentException e) {
            log("CorruptedMessage", true);
            return;
        }

    }

    private RMInformation getRMInformation(String[] parts) {
        return new RMInformation();
    }

    private P2PInformation getP2PInformation(String[] parts) {
        P2PInformation info = new P2PInformation();
        List<String> peerList = new ArrayList<String>();
        for (int i = 2; i < parts.length; i++) {
            peerList.add(parts[i]);
        }
        info.setPeerList(peerList);
        return info;
    }

    private void tryChangeState(Services desiredState, Priorities priority, Object payLoad) {
        if (priority.getVal() <= state.getPriority().getVal() && (!desiredState.equals(state.getServices()))) {
            // we change state
            stopCurrentService();

            state.setPriority(priority);
            state.setServices(desiredState);

            dispatchNewState(desiredState, payLoad);
        }
    }

    private void dispatchNewState(Services newState, Object payLoad) {
        if (newState.equals(Services.P2PRunning)) {
            startP2P(payLoad);
        } else if (newState.equals(Services.RMRunning)) {
            startRM(payLoad);
        } else if (newState.equals(Services.NotRunning)) {
            state.setPriority(Priorities.LowestPriority);
            runner = null;
        } else if (newState.equals(Services.KillAll)) {
            System.exit(0);
        }
    }

    private void startP2P(Object payLoad) {
        P2PInformation p2pInfo = (P2PInformation) payLoad;
        this.runner = new P2PRunner(p2pInfo);
        runner.run();
    }

    private void startRM(Object payLoad) {
        RMInformation rmInfo = (RMInformation) payLoad;
        this.runner = new RMRunner(rmInfo);
        runner.run();
    }

    private void stopCurrentService() {
        if (runner != null)
            this.runner.stop();
    }

    static String readCommand(BufferedReader reader) {
        char[] tab = new char[256];
        int offset = 0;

        do {
            try {
                int nbRead = reader.read(tab, offset, tab.length - offset);

                if (nbRead <= 0) {
                    break;
                }

                offset += nbRead;
            } catch (IOException ioe) {
                break;
            }

            String line = new String(tab);
            String trimmed = line.trim();

            if (!trimmed.equals(line)) {
                return trimmed;
            }
        } while (offset < 256);

        return null;
    }

    static void log(String msg, boolean isError) {
        msg = LOG_HEADER + msg;

        try {
            if (isError) {
                logger.fatal(msg);
            } else {
                logger.fatal(msg);
            }
        } catch (Exception e) {
            /* Log the logging exception ;-) */
            System.out.println(e.getMessage() + " when logging : " + msg);
        }
    }

    private static void flush(String message) {
        if (writerAppender != null) {
            writerAppender.setImmediateFlush(true);
        }

        log(message, false);

        if (writerAppender != null) {
            writerAppender.setImmediateFlush(false);
        }
    }

    private static void configureLogging() {
        Appender appender;

        try {
            appender = new NTEventLogAppender("ProActive Daemon");
        } catch (java.lang.UnsatisfiedLinkError e) {

            String hostname = ProActiveInet.getInstance().getInetAddress().getCanonicalHostName()
                    .toLowerCase();

            Layout layout = new PatternLayout(LOG_PATTERN);
            String filename = LOG_DIR + hostname;
            RollingFileAppender rfa;

            try {
                new File(LOG_DIR).mkdir();
                rfa = new RollingFileAppender(layout, filename, true);
            } catch (IOException ioe) {
                ioe.printStackTrace();

                return;
            }

            rfa.setMaxBackupIndex(0);
            rfa.setMaxFileSize(MAX_SIZE);
            rfa.setImmediateFlush(true);
            writerAppender = rfa;
            appender = rfa;
        }

        Logger root = Logger.getRootLogger();
        root.addAppender(appender);

        /* First message :) */
        log("Starting ProActive Daemon", false);
    }

}
