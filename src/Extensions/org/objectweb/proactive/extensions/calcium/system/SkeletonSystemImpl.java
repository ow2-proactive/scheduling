/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is not serializable on purpose. It is very dependant on the
 * environment where it lives.
 *
 * @author The ProActive Team (mleyton)
 */
public class SkeletonSystemImpl implements SkeletonSystem, java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    static String DEFAULT_ROOTDIR = System.getProperty("java.io.tmpdir");
    WSpaceImpl wspace; // workspace

    public SkeletonSystemImpl() throws IOException {
        this(new File(DEFAULT_ROOTDIR, "calcium"));
    }

    public SkeletonSystemImpl(File rootDir) throws IOException {
        File wspacedir = null;

        while ((wspacedir == null) || wspacedir.exists()) {
            wspacedir = new File(rootDir, ProActiveRandom.nextPosInt() + "");
        }

        wspace = new WSpaceImpl(wspacedir);
    }

    /**
     * This methods creates or returns an already created working space for the
     * calling thread.
     *
     * @see org.objectweb.proactive.extensions.calcium.system.SkeletonSystem#getWorkingSpace()
     */
    public synchronized WSpaceImpl getWorkingSpace() {
        return wspace;
    }

    /**
     * @see org.objectweb.proactive.extensions.calcium.system.SkeletonSystem#execCommand(File, String)
     */
    public int execCommand(File command, String arguments)
        throws IOException, InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Command: " + command.toString() + " " +
                arguments);
        }

        //TODO change this with JAVA 1.6 chmod features
        Process process = execCommandInternal(wspace, new File("/bin/chmod"),
                ("+x " + command.getPath()).split(" "), "");
        if (process.waitFor() != 0) {
            process.exitValue();
            String msg = "Command did not finish successfully: " + command +
                " " + arguments;
            logger.error(msg);
            return process.exitValue();
        }

        process = execCommandInternal(wspace, command, arguments.split(" "), "");
        if (process.waitFor() != 0) {
            BufferedReader err = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
            String msg = "Command did not finish successfully: " + command +
                " " + arguments + System.getProperty("line.separator");
            logger.error(msg);

            String line = err.readLine();
            while (line != null) {
                logger.error(line);
                line = err.readLine();
            }
        }

        return process.exitValue();
    }

    @Override
    public synchronized void finalize() {
        //wspace.delete();
    }

    // ************* UTILITY METHODS ******************
    public static boolean copyFile(File src, File dst)
        throws IOException {
        if (src.equals(dst)) {
            return false; //TODO maybe return IOException instead?
        }

        boolean retval = dst.createNewFile();

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();

        return retval;
    }

    static public void download(URL src, File dst) throws IOException {
        InputStream in = src.openStream();
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    static public boolean deleteDirectory(File path) {
        if (path == null) {
            return false;
        }

        boolean retval = true;

        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return false;
            }

            for (File f : files) {
                if (f.isDirectory()) {
                    retval = deleteDirectory(f) && retval;
                } else {
                    retval = f.delete() && retval;
                }
            }
        }

        retval = path.delete() && retval;
        return retval;
    }

    static private Process execCommandInternal(WSpaceImpl wspace, File program,
        String[] args, String add2path) throws IOException {
        if ((program == null) || (program.getPath().length() <= 0)) {
            throw new IllegalArgumentException("Program path is not specified");
        }

        List<String> command = new ArrayList<String>();
        command.add(program.getPath());

        for (String s : args) {
            command.add(s);
        }

        ProcessBuilder pb = new ProcessBuilder(command);

        pb.directory(wspace.wspace.getAbsoluteFile());

        if ((add2path != null) && (add2path.length() > 0)) {
            Map<String, String> env = pb.environment();
            env.put("PATH",
                env.get("PATH") + System.getProperty("path.separator") +
                add2path);
        }

        Process p = pb.start();

        return p;
    }

    static public File newRandomNamedDirIn(File rootDir) {
        File root = null;

        while ((root == null) || root.exists()) {
            root = new File(rootDir, ProActiveRandom.nextPosInt() + "");
        }

        return root;
    }

    static public boolean checkWritableDirectory(File rootDir) {
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new IllegalArgumentException("Can't creat directory: " +
                rootDir);
        }

        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + rootDir);
        }

        if (!rootDir.canWrite()) {
            throw new IllegalArgumentException("Can not write to: " + rootDir);
        }

        return true;
    }

    static public File newDirInTmp(String dirname) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tmpRoot = new File(tmpdir, dirname);

        return tmpRoot;
    }
}
