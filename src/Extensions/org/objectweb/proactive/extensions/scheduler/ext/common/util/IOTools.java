package org.objectweb.proactive.extensions.scheduler.ext.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * Utility class which performs some IO work 
 * @author The ProActive Team
 *
 */
public class IOTools {

    /**
     * Return the content read through the given text input stream as a list of file
     * @param is input stream to read
     * @return content as list of strings
     */
    public static ArrayList<String> getContentAsList(InputStream is) {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader d = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

        String line = null;

        try {
            line = d.readLine();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        while (line != null) {
            lines.add(line);

            try {
                line = d.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                line = null;
            }
        }

        try {
            d.close();
        } catch (IOException e) {
        }

        return lines;
    }

    /**
     * An utility class (Thread) which collects the output from a process and prints it on the JVM's standard output
     * @author The ProActive Team
     *
     */
    public static class LoggingThread implements Runnable, Serializable {
        private String appendMessage;
        private Boolean goon = true;
        private InputStream streamToLog;

        public ArrayList<String> output = new ArrayList<String>();

        public LoggingThread() {

        }

        public LoggingThread(InputStream is, String appendMessage) {
            this.streamToLog = is;
            this.appendMessage = appendMessage;
        }

        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(streamToLog));
            String line = null;
            ;
            try {
                while (!br.ready()) {
                    Thread.yield();
                }

                line = br.readLine();
            } catch (IOException e) {
                goon = false;
            }

            while ((line != null) && goon) {
                //output.add(line);
                System.out.println(appendMessage + line);
                System.out.flush();

                try {
                    while (!br.ready()) {
                        Thread.yield();
                    }

                    line = br.readLine();
                } catch (IOException e) {
                    goon = false;
                }
            }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
