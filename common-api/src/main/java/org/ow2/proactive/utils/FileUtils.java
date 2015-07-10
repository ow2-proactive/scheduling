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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Utils for file handling files.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
@PublicAPI
public class FileUtils {

    /**
     * Remove directories and files recursively
     * 
     * @param dir the root dir to be removed, (will be also removed)
     */
    public static void removeDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                removeDir(f);
            }
        }
        try {
            dir.delete();
        } catch (Exception e) {
        }
    }

    /**
     * Deletes a file or directory and all contents recursively.
     *
     * @param path the file to delete.
     */
    public static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a new temp directory in the specified directory, using the given prefix and suffix strings to generate its name.<br/>
     * If this method returns successfully then it is guaranteed that the directory is created.<br/>
     * <br/>
     * The prefix argument must be at least three characters long. <br/>
     * It is recommended that the prefix be a short, meaningful string such as "hjb" or "mail". <br/>
     * The suffix argument may be null, in which case the suffix ".tmpdir" will be used. <br/>
     * <br/>
     * @param prefix The prefix string to be used in generating the directory's name; must be at least three characters long
     * @param suffix The suffix string to be used in generating the directory's name; may be null, in which case the suffix ".tmpdir" will be used
     * @param directory The directory in which the temp directory is to be created, or null if the default temporary-file directory is to be used
     * @throw IOException if the directory cannot be created
     * @return the created temp directory
     */
    public static File createTempDirectory(String prefix, String suffix, File directory) throws IOException {
        if (suffix == null) {
            suffix = ".tmpdir";
        }
        int nbTries = 0;
        do {
            //create new generated temp file name
            File newdir = File.createTempFile(prefix, suffix, directory);
            //remove it
            if (newdir.delete()) {
                //if success, create a dir with the same name
                newdir.mkdir();
                return newdir;
            } else {
                //if problem to delete the file, try to delete it when VM exit
                newdir.deleteOnExit();
            }
            nbTries++;
        } while (nbTries < 5);//5 is arbitrary
        throw new IOException("Cannot create temp directory after 5 tries.");
    }

    public static String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

}
