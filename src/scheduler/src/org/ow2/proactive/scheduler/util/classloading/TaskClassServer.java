/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.classloading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.ow2.proactive.resourcemanager.utils.FileToBytesConverter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * This class defines a classserver based on ProActive remote objects. 
 * @author The ProActive team 
 */
public class TaskClassServer {

    // cache for byte[] classes 
    public static final boolean useCache = PASchedulerProperties.SCHEDULER_CLASSSERVER_USECACHE
            .getValueAsBoolean();
    private Hashtable<String, byte[]> cachedClasses;

    // root classpath (directory *or* jar file)
    private File classpath;

    /**
     * Empty constructor for remote object creation.
     */
    public TaskClassServer() {
    }

    /**
     * Create a new class server. 
     * @param pathToClasspath the path to the jar file containing the classes that 
     * should be served.
     * @throws IOException if the class server cannot be created.
     */
    public TaskClassServer(String pathToClasspath) throws IOException {
        this.classpath = new File(pathToClasspath);
        if (!this.classpath.exists()) {
            throw new IOException("Classpath " + pathToClasspath + " does not exist");
        }
        this.cachedClasses = useCache ? new Hashtable<String, byte[]>() : null;
    }

    /**
     * Return the byte[] representation of the classfile for the class classname.
     * @param classname the name of the looked up class
     * @return the byte[] representation of the classfile for the class classname.
     * @throws ClassNotFoundException if the class classname cannot be found
     */
    public byte[] getClassBytes(String classname) throws ClassNotFoundException {
        byte[] cb = useCache ? this.cachedClasses.get(classname) : null;
        if (cb == null) {
            try {
                cb = this.classpath.isFile() ? this.lookIntoJarFile(classname, new JarFile(classpath)) : this
                        .lookIntoDirectory(classname, classpath);
                if (useCache) {
                    this.cachedClasses.put(classname, cb);
                }
            } catch (IOException e) {
                throw new ClassNotFoundException("Class " + classname + " has not be found in " +
                    classpath.getAbsolutePath() + ". Caused by " + e);
            }
        }
        return cb;
    }

    /**
     * Look for a classfile into a directory.
     * @param classname the looked up class.
     * @param directory the directory to look into.
     * @return the byte[] representation of the class if found, null otherwise.
     * @throws IOException if the jar file cannot be read.
     */
    private byte[] lookIntoDirectory(String classname, File directory) throws IOException {
        String pathToClass = convertNameToPath(classname);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    byte[] resInDir = lookIntoDirectory(classname, files[i]);
                    if (resInDir != null) {
                        return resInDir;
                    }
                } else if (isJarFile(files[i])) {
                    byte[] resInJar = lookIntoJarFile(classname, new JarFile(files[i]));
                    if (resInJar != null) {
                        return resInJar;
                    }
                } else if (isClassFile(files[i]) && files[i].getAbsolutePath().endsWith(pathToClass)) {
                    // TODO cdelbe : conlicts possible ? 
                    return FileToBytesConverter.convertFileToByteArray(files[i]);
                }
            }
            // not found
            return null;
        } else {
            throw new IOException("Directory " + directory.getAbsolutePath() + " does not exist");
        }
    }

    /**
     * Look for a class definition into a jar file.
     * @param classname the looked up class.
     * @param file the jar file.
     * @return the byte[] representation of the class if found, null otherwise.
     * @throws IOException if the jar file cannot be read.
     */
    private byte[] lookIntoJarFile(String classname, JarFile file) throws IOException {
        byte result[] = null;
        ZipEntry entry = file.getEntry(convertNameToPath(classname));
        if (entry != null) {
            InputStream inStream = file.getInputStream(entry);
            result = new byte[inStream.available()];
            inStream.read(result);
            inStream.close();
            return result;
        } else {
            return null;
        }
    }

    /**
     * Clear the cache for classfiles.
     */
    public void deleteCache() {
        if (this.cachedClasses != null) {
            this.cachedClasses.clear();
        }
    }

    /**
     * Return true if f is a jar file.
     */
    private boolean isJarFile(File f) {
        return f.isFile() && f.getName().endsWith(".jar");
    }

    /**
     * Return true if f is a class file.
     */
    private boolean isClassFile(File f) {
        return f.isFile() && f.getName().endsWith(".class");
    }

    /**
     * Convert classname parameter (qualified) into path to the class file 
     * (with the .class suffix)
     */
    private String convertNameToPath(String classname) {
        return classname.replace('.', '/') + ".class";
    }

    /**
     * Convert the path to a class into a qualified classname.
     */
    private String convertPathToName(String path) {
        return path.replace('/', '.').substring(0, path.length() - ".class".length());
    }

}
