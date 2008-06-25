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
package org.ow2.proactive.scheduler.util.classloading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * This class defines a classserver based on ProActive remote objects. 
 * @author The ProActive team 
 * TODO cdelbe : add classpath as a directory to allows jar in jar.
 */
public class TaskClassServer {

    // cache for byte[] classes 
    public static final boolean useCache = PASchedulerProperties.SCHEDULER_CLASSSERVER_USECACHE
            .getValueAsBoolean();
    private Hashtable<String, byte[]> cachedClasses;

    // root classpath directory
    // unjared jar job classpath
    private File classpath;

    /**
     * Empty constructor for remote object creation.
     */
    public TaskClassServer() {
    }

    /**
     * Create a new class server. 
     * @param pathToJarClasspath the path to the jar file containing the classes that 
     * should be served.
     * @throws IOException if the class server cannot be created.
     */
    public TaskClassServer(String pathToJarClasspath) throws IOException {
        this.classpath = new File(pathToJarClasspath);
        if (!this.classpath.exists()) { // || !this.classpath.isDirectory()) {
            throw new IOException("Classpath " + pathToJarClasspath + " does not exist");// or is not a directory");
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
                cb = this.lookIntoJarFile(classname, new JarFile(classpath));
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

    // TODO    
    //    private byte[] lookIntoDirectory(String classname, File directory){
    //		String pathToClass = convertNameToPath(classname);
    //		if (directory.exists() && directory.isDirectory()){
    //			File[] files = directory.listFiles();
    //			for (int i=0;i<files.length;i++){
    //				if (files[i].isDirectory()){
    //					return lookIntoDirectory(classname, files[i]);
    //				} else if (isClassFile(files[i]) && files[i].getAbsolutePath().endsWith(pathToClass)) { // IS IT SUFFICIENT ??
    //					files[i].
    //						
    //				}
    //			}
    //		}
    //	}

    /**
     * Look for a class definition into a jar file.
     * @param classname the looked up class.
     * @param file the jar file.
     * @return the byte[] representation of the class if found, null otherwise.
     * @throws IOException if the jar file cannot be read.
     */
    private byte[] lookIntoJarFile(String classname, JarFile file) throws IOException {
        System.out.println("TaskClassServer.getClassBytes() : looking for " + convertNameToPath(classname) +
            " in " + file.getName());
        byte result[] = null;
        ZipEntry entry = file.getEntry(convertNameToPath(classname));
        if (entry != null) {
            System.out.println("Found entry " + entry.getName() + " in " + file.getName());
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
