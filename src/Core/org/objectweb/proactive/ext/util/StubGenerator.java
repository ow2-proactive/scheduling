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
package org.objectweb.proactive.ext.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javassist.ClassClassPath;
import javassist.ClassPool;

import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.MOPClassLoader;
import org.objectweb.proactive.core.mop.Utils;


public class StubGenerator {
    public static void main(String[] args) {
        StubGenerator sg = new StubGenerator(args);
        sg.run();
    }

    private File srcDir;
    private String pkg = "";
    private File destDir;
    private String cl;
    private boolean verbose = false;

    public StubGenerator(String[] args) {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(this.getClass()));

        int index = 0;
        while (index < args.length) {
            if (args[index].equals("-srcDir")) {
                srcDir = new File(args[index + 1]);
                index += 2;
            } else if (args[index].equals("-pkg")) {
                pkg = args[index + 1];
                index += 2;
            } else if (args[index].equals("-destDir")) {
                destDir = new File(args[index + 1]);
                index += 2;
            } else if (args[index].equals("-class")) {
                cl = args[index + 1];
                index += 2;
            } else if (args[index].equals("-verbose")) {
                verbose = true;
                index++;
            } else {
                usage();
                System.exit(1);
            }
        }
    }

    public void usage() {
        System.out.println("Usage:");
        System.out.println("\t-srcDir  directory where to find source classes");
        System.out.println("\t-destDir directory where to put generated stubs");
        System.out.println("\t-pkg     package name");
        System.out.println("\t-class   generate only a stub for this class");
        System.out.println("\t-verbose enable verbose mode");
        System.out.println("");
    }

    public void logAndExit(String str) {
        System.err.println(str);
        System.exit(2);
    }

    public void run() {
        if (srcDir == null) {
            logAndExit("srcDir attribute is not set");
        }
        if (!srcDir.exists()) {
            logAndExit("Invalid srcDir attribute: " + srcDir.toString() +
                " does not exist");
        }
        if (!srcDir.isDirectory()) {
            logAndExit("Invalid srcDir attribute: " + srcDir.toString() +
                " is not a directory");
        }

        if (pkg == null) {
            logAndExit("pkg attribute is not set");
        }
        File pkgDir = new File(srcDir.toString() + File.separator +
                pkg.replace('.', File.separatorChar));
        if (!pkgDir.exists()) {
            logAndExit("Invalid pkg attribute: " + pkgDir.toString() +
                " does not exist");
        }

        if (destDir == null) {
            destDir = srcDir;
        }
        if (!destDir.isDirectory()) {
            logAndExit("Invalid dest attribute: " + destDir.toString() +
                " is not a directory");
        }
        if (!destDir.isDirectory()) {
            logAndExit("Invalid src attribute: " + destDir.toString() +
                " is not a directory");
        }

        List<File> files = new ArrayList<File>();

        if (cl == null) {
            // Find all the classes in this package
            files.addAll(exploreDirectory(pkgDir));
        } else {
            File file = new File(pkgDir + File.separator + cl + ".class");
            if (!file.exists() || !file.isFile()) {
                logAndExit("Invalid pkg or class attribute: " +
                    file.toString() + " does not exist");
            }

            files.add(file);
        }

        PrintStream stderr = System.err;
        PrintStream mute = new PrintStream(new MuteOutputStream());

        if (!verbose) {
            System.setErr(mute);
        }

        //        ClassPool.releaseUnmodifiedClassFile = true;
        for (File file : files) {
            String str = file.toString().replaceFirst(srcDir.toString(), "");
            try {
                if (!verbose) {
                    System.setErr(mute);
                }

                StubGenerator.generateClass(str,
                    destDir.toString() + File.separator);
            } catch (Throwable e) {
                System.out.println("Stub generation failed: " + str);
            }
        }

        if (!verbose) {
            System.setErr(stderr);
        }
    }

    private List<File> exploreDirectory(File dir) {
        List<File> files = new ArrayList<File>();

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(exploreDirectory(file));
            }

            if (!file.toString().endsWith(".class")) {
                continue;
            }

            files.add(file);
        }

        return files;
    }

    /**
     * @param arg
     * @param directoryName
     */
    public static void generateClass(String arg, String directoryName) {
        String className = processClassName(arg);
        String fileName = null;

        String stubClassName;

        try {
            // Generates the bytecode for the class
            // ASM is now the default bytecode manipulator
            byte[] data;

            // if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("ASM")) {
            // ASMBytecodeStubBuilder bsb = new
            // ASMBytecodeStubBuilder(className);
            // data = bsb.create();
            // stubClassName = Utils.convertClassNameToStubClassName(className);
            // } else
            if (MOPClassLoader.BYTE_CODE_MANIPULATOR.equals("javassist")) {
                data = JavassistByteCodeStubBuilder.create(className, null);
                stubClassName = Utils.convertClassNameToStubClassName(className,
                        null);
            } else {
                // that shouldn't happen, unless someone manually sets the
                // BYTE_CODE_MANIPULATOR static variable
                System.err.println(
                    "byteCodeManipulator argument is optionnal. If specified, it can only be set to javassist (ASM is no longer supported).");
                System.err.println(
                    "Any other setting will result in the use of javassist, the default bytecode manipulator framework");
                return;
            }

            char sep = File.separatorChar;
            fileName = directoryName + stubClassName.replace('.', sep) +
                ".class";

            // And writes it to a file
            new File(fileName.substring(0, fileName.lastIndexOf(sep))).mkdirs();

            // String fileName = directoryName + System.getProperty
            // ("file.separator") +
            File f = new File(fileName);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            System.err.println("Cannot write file " + fileName);
            System.err.println("Reason is " + e);
        }
    }

    /**
     * Turn a file name into a class name if necessary. Remove the ending .class
     * and change all the '/' into '.'
     *
     * @param name
     */
    protected static String processClassName(String name) {
        int i = name.indexOf(".class");
        String tmp = name;
        if (i < 0) {
            return name;
        }
        tmp = name.substring(0, i);

        String tmp2 = tmp.replace(File.separatorChar, '.');

        if (tmp2.indexOf('.') == 0) {
            return tmp2.substring(1);
        }
        return tmp2;
    }

    public class MuteOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // Please shut up !
        }
    }
}
