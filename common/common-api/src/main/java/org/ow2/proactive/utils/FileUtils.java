/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Utils for file handling files.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
@PublicAPI
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class);

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
     * Creates a new temp directory in the specified directory, using the given prefix and suffix strings to generate its name.
     * If this method returns successfully then it is guaranteed that the directory is created.
     * <p>
     * The prefix argument must be at least three characters long.
     * It is recommended that the prefix be a short, meaningful string such as "hjb" or "mail".
     * The suffix argument may be null, in which case the suffix ".tmpdir" will be used.
     * <p>
     * @param prefix The prefix string to be used in generating the directory's name; must be at least three characters long
     * @param suffix The suffix string to be used in generating the directory's name; may be null, in which case the suffix ".tmpdir" will be used
     * @param directory The directory in which the temp directory is to be created, or null if the default temporary-file directory is to be used
     * @exception  IOException if the directory cannot be created
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

        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart > 0) {
            extension = fileName.substring(extensionStart + 1);
        }
        return extension;
    }

    /**
     * Extract a file name with it's extension from the given URL.
     * This file name is computed based on the last occurence of the dot character inside the URL path
     * It follows the general rule:
     * protocol://host:port/leading_path/filename.extension/trailing_path?query
     *
     * If there is no dot character inside the URL path, the file name returned is based on the last occurrence of
     * the slash character:
     * protocol://host:port/leading_path/filename?query
     *
     * @param url input url
     * @return a filename with its extension.
     */
    public static String getFileNameWithExtension(URL url) {
        String pathSegment = url.getPath();
        String extension = "";
        String filename = "";

        int extensionStart = pathSegment.lastIndexOf('.');
        if (extensionStart > 0) {
            extension = pathSegment.substring(extensionStart + 1);
            int trailingPath = extension.indexOf("/");
            if (trailingPath >= 0) {
                extension = extension.substring(0, trailingPath);
            }
            pathSegment = pathSegment.substring(0, extensionStart);
        }

        int pathEnd = pathSegment.lastIndexOf('/');
        if (pathEnd >= 0) {
            filename = pathSegment.substring(pathEnd + 1);
        }
        if (extension.length() > 0) {
            return filename + "." + extension;
        }
        return filename;
    }

    public static Properties resolvePropertiesFile(String fileNamePath) {
        Properties properties = new Properties();
        logger.debug("Loading properties from file " + fileNamePath);
        try (FileInputStream stream = new FileInputStream(fileNamePath)) {
            properties.load(stream);
        } catch (Exception e) {
            logger.warn("Properties file not found : '" + fileNamePath + "'", e);
        }
        return properties;
    }

}
