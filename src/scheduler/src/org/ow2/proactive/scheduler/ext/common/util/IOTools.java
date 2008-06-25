package org.ow2.proactive.scheduler.ext.common.util;

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
        public Boolean goon = true;
        private boolean err;
        private InputStream streamToLog;

        public ArrayList<String> output = new ArrayList<String>();

        public LoggingThread() {

        }

        public LoggingThread(InputStream is, String appendMessage, boolean err) {
            this.streamToLog = is;
            this.appendMessage = appendMessage;
            this.err = err;
        }

        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(streamToLog));
            String line = null;
            ;
            try {
                boolean first_line = true;
                while ((line = br.readLine()) != null && goon) {
                    if (err) {
                        if (first_line && line.trim().length() > 0) {
                            first_line = false;
                            System.err.println(appendMessage + line);
                            System.err.flush();
                        } else if (!first_line) {
                            System.err.println(appendMessage + line);
                            System.err.flush();
                        }
                    } else {
                        if (first_line && line.trim().length() > 0) {
                            System.out.println(appendMessage + line);
                            System.out.flush();
                        } else if (!first_line) {
                            System.err.println(appendMessage + line);
                            System.err.flush();
                        }
                    }
                }

                //line = br.readLine();
            } catch (IOException e) {
                goon = false;
            }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
