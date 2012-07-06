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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.classloading;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.utils.FileToBytesConverter;


public class TaskClassUtils {

    public static final Logger logger_dev = ProActiveLogger.getLogger(TaskClassUtils.class);

    /**
    * Look for a classfile into a directory.
    * @param classname the looked up class.
    * @param directory the directory to look into.
    * @return the byte[] representation of the class if found, null otherwise.
    * @throws IOException if the jar file cannot be read.
    */
    public static byte[] lookIntoDirectory(String classname, File directory) throws IOException {
        String pathToClass = convertNameToPath(classname, true);
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
    public static byte[] lookIntoJarFile(String classname, JarFile file) throws IOException {
        byte result[] = null;
        String path = convertNameToPath(classname, false);
        ZipEntry entry = file.getEntry(path);
        if (entry != null) {
            final InputStream inStream = file.getInputStream(entry);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(); // ByteArrayOutputStream.close() is noop
            final byte[] data = new byte[1024];
            int count;
            while ((count = inStream.read(data, 0, 1024)) > -1) {
                bos.write(data, 0, count);
            }
            result = bos.toByteArray();
            inStream.close();
            return result;
        } else {
            logger_dev.debug("Entry " + path + " has not been found in jar " + file.getName());
            return null;
        }
    }

    /**
     * Return true if f is a jar file.
     */
    private static boolean isJarFile(File f) {
        return f.isFile() && f.getName().endsWith(".jar");
    }

    /**
     * Return true if f is a class file.
     */
    private static boolean isClassFile(File f) {
        return f.isFile() && f.getName().endsWith(".class");
    }

    /**
     * Convert classname parameter (qualified) into path to the class file
     * (with the .class suffix)
     */
    public static String convertNameToPath(String classname, boolean useSystemFileSeparator) {
        return classname.replace('.', useSystemFileSeparator ? File.separatorChar : '/') + ".class";
    }

    /**
     * Recursive delete for directories
     * @param path
     */
    public static void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
            path.delete();
        }
    }

}
