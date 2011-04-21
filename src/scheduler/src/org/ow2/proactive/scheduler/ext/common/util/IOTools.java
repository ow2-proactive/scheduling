/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.ext.common.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 * Utility class which performs some IO work
 *
 * @author The ProActive Team
 */
public class IOTools {

    private static String nl = System.getProperty("line.separator");

    public static ProcessResult blockingGetProcessResult(Process process) {

        final InputStream is = process.getInputStream();
        final InputStream es = process.getErrorStream();
        final ArrayList<String> out_lines = new ArrayList<String>();
        final ArrayList<String> err_lines = new ArrayList<String>();
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                ArrayList<String> linesTemp = getContentAsList(is);
                out_lines.addAll(linesTemp);
            }
        });
        Thread t2 = new Thread(new Runnable() {
            public void run() {
                ArrayList<String> linesTemp = getContentAsList(es);
                err_lines.addAll(linesTemp);
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();

        } catch (InterruptedException e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }

        int retValue = 0;
        try {
            retValue = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
        return new ProcessResult(retValue, out_lines.toArray(new String[0]), err_lines.toArray(new String[0]));
    }

    public static String generateHash(String pathname) throws NoSuchAlgorithmException,
            FileNotFoundException, IOException {
        File file = new File(pathname);
        if (!file.exists() || !file.canRead()) {
            throw new IOException("File doesn't exist : " + file);
        }
        MessageDigest md = MessageDigest.getInstance("SHA"); // SHA or MD5
        String hash = "";

        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();

        md.update(data); // Reads it all at one go. Might be better to chunk it.

        byte[] digest = md.digest();

        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(digest[i]);
            if (hex.length() == 1)
                hex = "0" + hex;
            hex = hex.substring(hex.length() - 2);
            hash += hex;
        }

        return hash;
    }

    /**
     * Return the content read through the given text input stream as a list of file
     *
     * @param is input stream to read
     * @return content as list of strings
     */
    public static ArrayList<String> getContentAsList(final InputStream is) {
        final ArrayList<String> lines = new ArrayList<String>();
        final BufferedReader d = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

        String line = null;

        try {
            line = d.readLine();
        } catch (IOException e1) {
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
            is.close();
        } catch (IOException e) {
        }

        return lines;
    }

    public static class RedirectionThread implements Runnable, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 30L;
        private InputStream is;
        private OutputStream os;

        private PrintStream out;
        private BufferedReader br;

        public RedirectionThread(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            this.out = new PrintStream(new BufferedOutputStream(os));
            this.br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
        }

        public void run() {

            String s;
            try {
                while ((s = br.readLine()) != null) {
                    synchronized (out) {
                        out.println(s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setOutputStream(OutputStream os) {
            synchronized (out) {
                out = new PrintStream(new BufferedOutputStream(os));
            }

        }
    }

    /**
     * An utility class (Thread) which collects the output from a process and prints it on the JVM's standard output
     *
     * @author The ProActive Team
     */
    public static class LoggingThread implements Runnable, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 30L;
        private String appendMessage;
        /**  */
        public Boolean goon = true;
        private PrintStream out;
        private BufferedReader br;

        /**  */
        public ArrayList<String> output = new ArrayList<String>();
        private PrintStream debugStream;

        /**
         * Create a new instance of LoggingThread.
         */
        public LoggingThread() {

        }

        /**
         * Create a new instance of LoggingThread.
         *
         * @param is
         * @param appendMessage
         * @param err
         */
        public LoggingThread(InputStream is, String appendMessage, boolean err) {
            this.br = new BufferedReader(new InputStreamReader(is));
            this.appendMessage = appendMessage;
            if (err) {
                this.out = System.err;
            } else {
                this.out = System.out;
            }
        }

        /**
         * Create a new instance of LoggingThread.
         *
         * @param is
         * @param appendMessage
         * @param out
         */
        public LoggingThread(InputStream is, String appendMessage, PrintStream out) {
            this.br = new BufferedReader(new InputStreamReader(is));
            this.appendMessage = appendMessage;
            this.out = out;
        }

        /**
        * Create a new instance of LoggingThread.
        *
        * @param is
        * @param appendMessage
        * @param out
        */
        public LoggingThread(InputStream is, String appendMessage, PrintStream out, PrintStream ds) {
            this.br = new BufferedReader(new InputStreamReader(is));
            this.appendMessage = appendMessage;
            this.out = out;
            this.debugStream = ds;
        }

        public LoggingThread(InputStream is) {
            this.br = new BufferedReader(new InputStreamReader(is));
            this.out = null;
            this.appendMessage = null;
        }

        private String getLineOrDie() {
            String answer = null;
            try {

                while (!ready()) {
                    Thread.sleep(10);
                }
                answer = readLine();

            } catch (IOException e) {

            } catch (InterruptedException e) {

            }
            return answer;
        }

        private boolean ready() throws IOException {
            synchronized (br) {
                return br.ready();
            }
        }

        private String readLine() throws IOException {
            StringBuilder line = new StringBuilder(200);
            int chr;
            boolean r = false;

            while (true) {
                while (!ready()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }

                synchronized (br) {
                    chr = br.read();
                }
                if (chr < 0) {
                    return line.toString();
                } else if (chr == '\n') {
                    return line.toString();
                } else if (chr == '\r') {
                    if (r) {
                        br.reset();
                        return line.toString();
                    } else {
                        r = true;
                        br.mark(3);
                    }
                } else {
                    if (r) {
                        br.reset();
                        return line.toString();
                    } else {
                        line.append((char) chr);
                    }
                }

            }

        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            String line = null;
            boolean first_line = true;
            if (out != null) {
                while ((line = getLineOrDie()) != null && goon) {
                    synchronized (out) {
                        if (first_line && line.trim().length() > 0) {
                            first_line = false;

                            out.println("[ " + new java.util.Date() + " ]" + appendMessage + line);
                            out.flush();

                            if (debugStream != null) {
                                debugStream
                                        .println("[ " + new java.util.Date() + " ]" + appendMessage + line);
                                debugStream.flush();
                            }
                        } else if (!first_line) {

                            out.println("[ " + new java.util.Date() + " ]" + appendMessage + line);
                            out.flush();

                            if (debugStream != null) {
                                debugStream
                                        .println("[ " + new java.util.Date() + " ]" + appendMessage + line);
                                debugStream.flush();
                            }
                        }
                    }

                }

            } else
                while ((line = getLineOrDie()) != null && goon) {
                }

            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeStream() {
            synchronized (out) {
                try {
                    out.close();
                } catch (Exception e) {

                }
            }
        }

        public void setStream(PrintStream st, PrintStream ds) {
            synchronized (out) {
                try {
                    out.close();
                } catch (Exception e) {

                }
                if (debugStream != null) {
                    try {
                        debugStream.close();
                    } catch (Exception e) {

                    }
                }
                out = st;
                debugStream = ds;
            }
        }

        public void setStream(PrintStream st) {
            setStream(st, null);
        }

        public void setInputStream(InputStream is) {
            synchronized (br) {
                br = new BufferedReader(new InputStreamReader(is));
            }
        }
    }
}
