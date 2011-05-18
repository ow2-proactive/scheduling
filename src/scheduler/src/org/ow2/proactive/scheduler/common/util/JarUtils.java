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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Utils for creating and reading jar files.
 * @author The ProActive Team
 */
@PublicAPI
public class JarUtils extends ZipUtils {

    /**
     * Create a jar file that contains all the directory listed in directories parameter.
     * @param directoriesAndFiles the list of directories and files to be jarred.
     * @param manifestVerion the version of the jar manifest (can be null).
     * @param mainClass the main class of the jar (can be null).
     * @param jarInternalClasspath the class-path of the jar (can be null).
     * @throws IOException if the jar file cannot be created.
     * @return the jar file as a byte[].
     */
    public static byte[] jarDirectoriesAndFiles(String[] directoriesAndFiles, String manifestVerion,
            String mainClass, String jarInternalClasspath) throws IOException {

        // Fill in a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create jar stream
        JarOutputStream jos = new JarOutputStream(baos, JarUtils.createManifest(manifestVerion, mainClass,
                jarInternalClasspath));
        int iBaseFolderLength = 0;
        jos.setLevel(COMP_LEVEL);

        // Jar file is ready
        zipIt(jos, directoriesAndFiles, iBaseFolderLength);

        // Close the file output streams
        jos.flush();
        jos.close();
        baos.flush();
        baos.close();
        return baos.toByteArray();

    }

    private static Manifest createManifest(String manifestVerion, String mainClass,
            String jarInternalClasspath) {
        // Create manifest
        Manifest manifest = new Manifest();
        Attributes manifestAttr = manifest.getMainAttributes();
        //note:Must set Manifest-Version,or the manifest file will be empty!
        if (manifestVerion != null) {
            manifestAttr.putValue("Manifest-Version", manifestVerion);
            if (mainClass != null) {
                manifestAttr.putValue("Main-Class", mainClass);
            }
            if (jarInternalClasspath != null) {
                manifestAttr.putValue("Class-Path", jarInternalClasspath);
            }
        }
        return manifest;
    }

    /**
     * Create a jar file that contains all the directory listed in directories parameter and
     * store the create content in a file denoted by the path in the given dest argument
     * 
     * @param directoriesAndFiles the directories and files to jar (recursively)
     * @param dest the jar destination file that will contains the jarred version of the first argument.
     * @param manifestVerion the version of the jar manifest (can be null).
     * @param mainClass the main class of the jar (can be null).
     * @param jarInternalClasspath the class-path of the jar (can be null).
     * @throws IOException if the zip file cannot be created.
     */
    public static void jar(String[] directoriesAndFiles, File dest, String manifestVerion, String mainClass,
            String jarInternalClasspath) throws IOException {
        byte[] jarred = jarDirectoriesAndFiles(directoriesAndFiles, manifestVerion, mainClass,
                jarInternalClasspath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));
        bos.write(jarred);
        bos.close();
    }

    /**
     * Unjar a jar file into a directory.
     * 
     * @param jarFile The jar file to be unjared.
     * @param dest the destination directory.
     * @throws IOException if the destination does not exist or is not a directory, or if the jar file cannot be extracted.
     */
    public static void unjar(JarFile jarFile, File dest) throws IOException {
        unzip(jarFile, dest);
    }

}
