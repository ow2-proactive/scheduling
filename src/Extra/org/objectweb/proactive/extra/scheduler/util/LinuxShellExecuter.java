package org.objectweb.proactive.extra.scheduler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class LinuxShellExecuter {
    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);

    static public Process executeShellScript(File file, Shell shell)
        throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Read error on : " + file);
        }
        return executeShellScript(new FileInputStream(file), shell);
    }

    static public Process executeShellScript(InputStream input, Shell shell)
        throws IOException {
        InputStreamReader isr = new InputStreamReader(input);
        BufferedReader in = new BufferedReader(isr);
        Process pshell = Runtime.getRuntime().exec(shell.command());
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    pshell.getOutputStream()));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[SHELL SCRIPT] " + line);
                }
                out.println(line);
            }
            in.close();
        } catch (IOException ex) {
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[SHELL SCRIPT] exit");
        }
        out.println("exit");
        out.flush();
        out.close();
        return pshell;
    }
}
