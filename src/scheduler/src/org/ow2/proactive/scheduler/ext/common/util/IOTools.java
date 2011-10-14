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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
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

    public static String generateHash(File file) throws NoSuchAlgorithmException, FileNotFoundException,
            IOException {
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

    public static String generateHash(String bigString) throws NoSuchAlgorithmException,
            FileNotFoundException, IOException {

        MessageDigest md = MessageDigest.getInstance("SHA"); // SHA or MD5
        String hash = "";

        char[] data = new char[bigString.length()];
        StringReader fis = new StringReader(bigString);
        fis.read(data);
        fis.close();

        byte[] input = toByteArray(data);
        md.update(input); // Reads it all at one go. Might be better to chunk it.

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

    public static byte[] toByteArray(char[] array) {
        return toByteArray(array, Charset.defaultCharset());
    }

    public static byte[] toByteArray(char[] array, Charset charset) {
        CharBuffer cbuf = CharBuffer.wrap(array);
        ByteBuffer bbuf = charset.encode(cbuf);
        return bbuf.array();
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

        /**  */
        private static final long serialVersionUID = 31L;
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

        /**  */
        private static final long serialVersionUID = 31L;
        private String appendMessage;
        /**  */
        public Boolean goon = true;

        public Boolean patternFound = false;
        private PrintStream out;
        private PrintStream err;

        private Process p;

        private BufferedReader brout;
        private BufferedReader brerr;

        private boolean lastline_err = false;

        private String startpattern;
        private String stoppattern;
        private String patternToFind;

        private static String HOSTNAME;

        static {
            try {
                HOSTNAME = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
            }
        }

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
         * @param p
         * @param appendMessage
         * @param out
         */
        public LoggingThread(Process p, String appendMessage, PrintStream out, PrintStream err,
                String startpattern, String stoppattern, String patternToFind) {
            this(p, appendMessage, out, err, null, startpattern, stoppattern, patternToFind);
        }

        public LoggingThread(Process p, String appendMessage, PrintStream out, PrintStream err,
                PrintStream ds, String startpattern, String stoppattern, String patternToFind) {
            this.brout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            this.brerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            this.appendMessage = appendMessage;
            this.out = out;
            this.err = err;
            this.debugStream = ds;
            this.startpattern = startpattern;
            this.stoppattern = stoppattern;
            this.patternToFind = patternToFind;
            this.p = p;
        }

        /**
         * Create a new instance of LoggingThread.
         *
         * @param p
         * @param appendMessage
         * @param out
         */
        public LoggingThread(Process p, String appendMessage, PrintStream out, PrintStream err, PrintStream ds) {
            this(p, appendMessage, out, err, ds, null, null, null);
        }

        public LoggingThread(Process p) {
            this.brout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            this.brerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            this.out = null;
            this.err = null;
            this.appendMessage = null;
            this.p = p;
        }

        private String getLineOrDie() {
            String answer = null;
            try {

                while (goon) {
                    if (readyout()) {
                        answer = brout.readLine();
                        lastline_err = false;
                        return answer;
                    } else if (readyerr()) {
                        answer = brerr.readLine();
                        lastline_err = true;
                        return answer;
                    } else {
                        try {
                            p.exitValue();
                            return null;
                        } catch (IllegalThreadStateException ex) {
                            //Expected behaviour
                        }
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        goon = false;
                    }
                }
            } catch (IOException e) {
                return null;
            } finally {
                if (patternToFind != null) {
                    if (answer != null && answer.contains(patternToFind)) {
                        patternFound = true;
                    }
                }
            }
            return null;
        }

        private boolean readyout() throws IOException {
            synchronized (brout) {
                return brout.ready();
            }
        }

        private boolean readyerr() throws IOException {
            // We use only brout in locks
            synchronized (brout) {
                return brerr.ready();
            }
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            String line = null;
            boolean first_line = true;
            boolean last_line = false;
            if (out != null) {
                while ((line = getLineOrDie()) != null && goon) {
                    synchronized (out) {

                        if (first_line && line.trim().length() > 0) {

                            if ((startpattern != null) && (line.contains(startpattern))) {
                                // we eat everything until the startpattern, if provided
                                startpattern = null;
                                continue;
                            } else if (startpattern != null) {
                                continue;
                            }
                            first_line = false;
                            printLine(line);

                        } else if (!first_line) {
                            if ((stoppattern != null) && (line.contains(stoppattern))) {
                                // at the stoppattern, we exit
                                goon = false;
                            } else {
                                printLine(line);

                            }
                        }
                    }

                }

            } else
                while ((line = getLineOrDie()) != null && goon) {
                }

            try {
                brout.close();

            } catch (IOException e) {
                // SCHEDULING-1296 not necessary to print the Exception but we need two try catch blocks
            }
            try {
                brerr.close();
            } catch (IOException e) {
            }
        }

        private void printLine(String line) {
            if (debugStream == null) {
                if (lastline_err) {
                    err.println("[ " + HOSTNAME + " ] " + line);
                    err.flush();
                } else {
                    out.println("[ " + HOSTNAME + " ] " + line);
                    out.flush();
                }
            } else {
                if (lastline_err) {
                    err.println("[ " + HOSTNAME + " " + new java.util.Date() + " ]" + appendMessage + line);
                    err.flush();
                } else {
                    out.println("[ " + HOSTNAME + " " + new java.util.Date() + " ]" + appendMessage + line);
                    out.flush();
                }
                debugStream.println("[ " + new java.util.Date() + " ]" + appendMessage + line);
                debugStream.flush();
            }
        }

        public void closeStream() {
            synchronized (out) {
                try {
                    out.close();
                    err.close();
                } catch (Exception e) {

                }
            }
        }

        public void setOutStream(PrintStream out, PrintStream err, PrintStream ds) {
            synchronized (this.out) {
                try {
                    this.out.close();
                    this.err.close();
                } catch (Exception e) {

                }
                if (this.debugStream != null) {
                    try {
                        this.debugStream.close();
                    } catch (Exception e) {

                    }
                }
                this.out = out;
                this.err = err;
                this.debugStream = ds;
            }
        }

        public void setOutStream(PrintStream out, PrintStream err) {
            setOutStream(out, err, null);
        }

        public void setProcess(Process p) {
            synchronized (brout) {
                this.brout = new BufferedReader(new InputStreamReader(p.getInputStream()));
                this.brerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            }
        }
    }
}
