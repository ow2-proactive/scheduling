/**
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 *
 */
package org.objectweb.proactive.core.rmi;

import org.objectweb.proactive.core.component.asmgen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.asmgen.RepresentativeInterfaceClassGenerator;


/**
 * The ClassFileServer implements a ClassServer that
 * reads class files from the file system. See the
 * doc for the "Main" method for how to run this
 * server.
 */
public class ClassFileServer extends ClassServer {
    private java.io.File[] codebases;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Constructs a ClassFileServer.
     */
    public ClassFileServer() throws java.io.IOException {
        this(0, null);
    }

    /**
     * Constructs a ClassFileServer.
     * @param port the port to bound the server to
     */
    public ClassFileServer(int port) throws java.io.IOException {
        this(port, null);
    }

    /**
     * Constructs a ClassFileServer.
     * @param paths the classpath where the server locates classes
     */
    public ClassFileServer(String paths) throws java.io.IOException {
        this(0, paths);
    }

    /**
     * Constructs a ClassFileServer.
     * @param port the port to bound the server to
     * @param paths the classpath where the server locates classes
     */
    public ClassFileServer(int port, String paths) throws java.io.IOException {
        super(port);
        if (paths != null) {
            codebases = findClasspathRoots(paths);
        }
        printMessage();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public static boolean isPortAlreadyBound(int port) {
        java.net.Socket socket = null;
        try {
            socket = new java.net.Socket(java.net.InetAddress.getLocalHost(),
                    port);
            // if we can connect to the port it means the server already exists
            return true;
        } catch (java.io.IOException e) {
            return false;
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (java.io.IOException e) {
            }
        }
    }

    /**
     * Main method to create the class server that reads
     * class files. This takes two optional command line arguments, the
     * port on which the server accepts requests and the
     * root of the classpath. To start up the server: <br><br>
     *
     * <code>   java ClassFileServer [&lt;classpath>] [&lt;port>]
     * </code><br><br>
     *
     * The codebase of an RMI server using this webserver would
     * simply contain a URL with the host and port of the web
     * server (if the webserver's classpath is the same as
     * the RMI server's classpath): <br><br>
     *
     * <code>   java -Djava.rmi.server.codebase=http://zaphod:2001/ RMIServer
     * </code> <br><br>
     *
     * You can create your own class server inside your RMI server
     * application instead of running one separately. In your server
     * main simply create a ClassFileServer: <br><br>
     *
     * <code>   new ClassFileServer(port, classpath);
     * </code>
     */
    public static void main(String[] args) {
        int port = 0;
        String classpath = null;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            classpath = args[1];
        }
        try {
            new ClassFileServer(port, classpath);
        } catch (java.io.IOException e) {
            logger.fatal("Unable to start ClassServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

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
    protected byte[] getBytes(String path)
        throws java.io.IOException, ClassNotFoundException {
        //	System.out.println("ClassFileServer: looking for class " + path);
        byte[] b = null;
        if (codebases == null) {
            // reading from resources in the classpath
            b = getBytesFromResource(path);
        } else {
            for (int i = 0; i < codebases.length; i++) {
                try {
                    if (codebases[i].isDirectory()) {
                        b = getBytesFromDirectory(path, codebases[i]);
                    } else {
                        b = getBytesFromArchive(path, codebases[i]);
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
        b = org.objectweb.proactive.core.mop.MOPClassLoader.getMOPClassLoader()
                                                           .getClassData(path);
        if (b != null) {
            return b;
        }

        // COMPONENTS
        // try to get the class as a generated component interface reference
        b = RepresentativeInterfaceClassGenerator.getClassData(path);

        if (b != null) {
            return b;
        }

        // COMPONENTS
        // try to get the class as a generated component interface reference
        b = MetaObjectInterfaceClassGenerator.getClassData(path);
        if (b != null) {
            return b;
        }

        throw new ClassNotFoundException("Cannot find class " + path);
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
    private byte[] getBytesFromResource(String path) throws java.io.IOException {
        //	System.out.println("ClassFileServer: looking for class " + path);
        String filename = path.replace('.', '/') + ".class";
        java.io.InputStream in = this.getClass().getClassLoader()
                                     .getResourceAsStream(filename);
        if (in == null) {
            return null;
        }
        int length = in.available();

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + filename+"  length="+length+" from classpath");
        //}
        if (length == -1) {
            throw new java.io.IOException("File length is unknown: " +
                filename);
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
    private byte[] getBytesFromArchive(String path, java.io.File archive)
        throws java.io.IOException {
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
            throw new java.io.IOException("File length is unknown: " +
                filename);
        } else {
            return getBytesFromInputStream(jarFile.getInputStream(zipEntry),
                length);
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
    private byte[] getBytesFromDirectory(String path, java.io.File directory)
        throws java.io.IOException {
        java.io.File f = new java.io.File(directory,
                path.replace('.', java.io.File.separatorChar) + ".class");
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
            return getBytesFromInputStream(new java.io.FileInputStream(f),
                length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private byte[] getBytesFromInputStream(java.io.InputStream in, int length)
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

    private void printMessage() {
        if (logger.isDebugEnabled()) {
            logger.debug(
                "To use this ClassFileServer set the property java.rmi.server.codebase to http://" +
                hostname + ":" + port + "/");
        }
        if (codebases == null) {
            logger.info(
                " --> This ClassFileServer is reading resources from classpath");
        } else {
            logger.info(
                " --> This ClassFileServer is reading resources from the following paths");
            for (int i = 0; i < codebases.length; i++) {
                logger.info("     (" + i + ") : " +
                    codebases[i].getAbsolutePath());
            }
        }
    }

    private java.io.File[] findClasspathRoots(String classpath) {
        String pathSeparator = System.getProperty("path.separator");
        java.util.StringTokenizer st = new java.util.StringTokenizer(classpath,
                pathSeparator);
        int n = st.countTokens();
        java.io.File[] roots = new java.io.File[n];
        for (int i = 0; i < n; i++) {
            roots[i] = new java.io.File(st.nextToken());
        }
        return roots;
    }
}
