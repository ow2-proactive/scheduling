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
package org.objectweb.proactive.core.rmi;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.component.gen.Utils;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author vlegrand
 *
 */
public class FileProcess {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    private java.io.File[] codebases;
    protected RequestInfo info;

    public FileProcess(String paths, RequestInfo info) {
        if (paths != null) {
            codebases = findClasspathRoots(paths);
        }
        this.info = info;
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     *
     * @return the bytecodes for the class
     * @exception ClassNotFoundException if the class corresponding
     * to <b>path</b> could not be loaded.
     */
    public byte[] getBytes() throws ClassNotFoundException {
        byte[] b = null;
        if (codebases == null) {
            try {
                // reading from resources in the classpath
                b = getBytesFromResource(info.getClassFileName());
            } catch (IOException e) {
                throw new ClassNotFoundException("Cannot find class " + info.getClassFileName(), e);
            }
        } else {
            for (int i = 0; i < codebases.length; i++) {
                try {
                    if (codebases[i].isDirectory()) {
                        b = getBytesFromDirectory(info.getClassFileName(), codebases[i]);
                    } else {
                        b = getBytesFromArchive(info.getClassFileName(), codebases[i]);
                    }
                } catch (java.io.IOException e) {
                }
            }
        }
        if (b != null) {
            return b;
        }

        // try to get the class as a generated stub
        // generate it if necessary
        b = org.objectweb.proactive.core.mop.MOPClassLoader.getMOPClassLoader().getClassData(
                info.getClassFileName());
        if (b != null) {
            return b;
        }

        // COMPONENTS
        // try to get the class as a generated component interface reference
        b = Utils.getClassData(info.getClassFileName());
        if (b != null) {
            return b;
        }

        //if (info.path != null) {
        //    System.out.println("ClassServer sent class " + info.path +
        //        " successfully");
        //}
        throw new ClassNotFoundException("Cannot find class " + info.getClassFileName());
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     * @param path the fqn of the class
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    public static byte[] getBytesFromResource(String path) throws java.io.IOException {
        String filename = path.replace('.', '/') + ".class";
        java.io.InputStream in = FileProcess.class.getClassLoader().getResourceAsStream(filename);
        if (in == null) {
            return null;
        }
        int length = in.available();

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + filename+"  length="+length+" from classpath");
        //}
        if (length == -1) {
            throw new java.io.IOException("File length is unknown: " + filename);
        } else {
            return getBytesFromInputStream(in, length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     * @param path the fqn of the class
     * @param codeBase the File that must be a jar or zip archive that may contain the class
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private byte[] getBytesFromArchive(String path, java.io.File archive) throws java.io.IOException {
        String filename = path.replace('.', '/') + ".class";
        java.util.zip.ZipFile jarFile = new java.util.zip.ZipFile(archive);
        java.util.zip.ZipEntry zipEntry = jarFile.getEntry(filename);
        if (zipEntry == null) {
            return null;
        }
        int length = (int) (zipEntry.getSize());

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + filename+"  length="+length+" from jar/xip file "+archive.getAbsolutePath());
        //}
        if (length == -1) {
            throw new java.io.IOException("File length is unknown: " + filename);
        } else {
            return getBytesFromInputStream(jarFile.getInputStream(zipEntry), length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     * @param path the fqn of the class
     * @param codeBase the File that must be a directory that may contain the class
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private byte[] getBytesFromDirectory(String path, java.io.File directory) throws java.io.IOException {
        java.io.File f = new java.io.File(directory, path.replace('.', java.io.File.separatorChar) + ".class");
        if (!f.exists()) {
            return null;
        }
        int length = (int) (f.length());

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + f.getAbsolutePath()+"  length="+length);
        //}
        if (length == 0) {
            throw new java.io.IOException("File length is zero: " + path);
        } else {
            return getBytesFromInputStream(new java.io.FileInputStream(f), length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in, int length)
            throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[length];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }

    private java.io.File[] findClasspathRoots(String classpath) {
        String pathSeparator = File.pathSeparator;
        java.util.StringTokenizer st = new java.util.StringTokenizer(classpath, pathSeparator);
        int n = st.countTokens();
        java.io.File[] roots = new java.io.File[n];
        for (int i = 0; i < n; i++) {
            roots[i] = new java.io.File(st.nextToken());
        }
        return roots;
    }
}
