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
package org.ow2.proactive.scheduler.ext.filessplitmerge.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.ow2.proactive.scheduler.ext.filessplitmerge.logging.LoggerManager;


public class FilesTools {

    /**
     * We assume that, in a xxx.conf file, attributes are written on a single
     * line: attrName = value OR attrName value
     * 
     * This method will read the xxx.conf file line by line and look for the
     * attributeName at the beginning of a line in order to get the value.
     * 
     * @param configFile
     *            the gold configuration file
     * @param attributeName
     * @param stopToFirstBlank
     *            If true, the value of the attribute is between "=" and the
     *            first blank. Otherwise, the value is between "=" and the end
     *            of the line.
     * @return the value of attributeName, or null if the attribute is not
     *         defined in the configuration file
     * @throws IOException
     */
    public synchronized String getValueForAttribute(File configFile, String attributeName,
            boolean stopToFirstBlank) throws IOException {
        FileReader fr = new FileReader(configFile);
        BufferedReader gcReader = new BufferedReader(fr);
        String line;
        while ((line = gcReader.readLine()) != null) {
            // look for the attributeName at the begining at the line
            line = line.trim();

            if (line.startsWith(attributeName))
            // we found the atttribute
            {
                line = line.substring(attributeName.length());
                line = line.trim();
                // the line contains now eather the value eather '= '+value
                if (line.startsWith("="))
                    line = line.substring(1);
                line = line.trim();

                if (stopToFirstBlank) {
                    if (line.indexOf(" ") != -1) {
                        line = line.substring(0, line.indexOf(" "));
                        line.trim();
                    }
                }// stop to first blank

                // we have our value in the line variable.
                gcReader.close();
                return line;
            }

        }
        return null;
    }

    public synchronized void saveValueForAttribute(File configFile, String attributeName,
            String atttributeValue) throws IOException {
        boolean attributeFound = false;
        String attributeLine = attributeName + " = " + atttributeValue;

        String fileContentUpdated = "";
        FileReader fr = new FileReader(configFile);
        BufferedReader gcReader = new BufferedReader(fr);

        String line;
        while ((line = gcReader.readLine()) != null) {
            // look for the attributeName at the begining at the line
            line = line.trim();

            if (line.startsWith(attributeName))
            // we found the atttribute
            {
                line = attributeLine;
                attributeFound = true;
            }
            fileContentUpdated = fileContentUpdated + line + "\n";
        }

        if (!attributeFound) {
            fileContentUpdated = fileContentUpdated + attributeLine + "\n";
        }

        gcReader.close();
        configFile.delete();
        configFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(configFile));
        writer.write(fileContentUpdated);
        writer.close();
    }

    /**
     * remove the line with the assignment of the value of an attribute this
     * removes
     * 
     * @param attributesFile
     * @param attributeName
     * @throws IOException
     */
    public synchronized void removeAttribute(File attributesFile, String attributeName) throws IOException {
        String fileContentUpdated = "";
        FileReader fr = new FileReader(attributesFile);
        BufferedReader gcReader = new BufferedReader(fr);

        String line;
        while ((line = gcReader.readLine()) != null) {
            // look for the attributeName at the begining at the line
            line = line.trim();

            if (!line.startsWith(attributeName))
            // we write all the lines that do not start with the attr name
            {
                fileContentUpdated = fileContentUpdated + line + "\n";
            }
        }

        gcReader.close();
        attributesFile.delete();
        attributesFile.createNewFile();
        BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(attributesFile));
        writer.write(fileContentUpdated);
        writer.close();
    }

    public boolean copyfile(String srFile, String dtFile) {
        try {
            File f1 = new File(srFile);
            File f2 = new File(dtFile);
            InputStream in = new FileInputStream(f1);

            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            return false;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static File createDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();

            // Cannot set read write attributes, need java5 compliance
            //dir.setWritable(true, false);
            //dir.setReadable(true, false);
            // System.out.println("Directory "+path+" hhave been created." );
            return dir;
        }

        if (dir.isDirectory()) {
            LoggerManager.getInstane().warning(
                    "Directory " + path + " has not been created as it already existed.");
            return dir;
        }

        LoggerManager
                .getInstane()
                .error(
                        "Could not use " + path +
                            " as a directory as it is a file. Please delete this file or choose a different directory.");
        return null;

    }

    public void deletefilesByExtension(String extension, String folderPath) throws Exception {
        File folder = new File(folderPath);
        if ((!folder.exists()) || (!folder.isDirectory())) {
            throw new FileNotFoundException("Could not delete files in " + folderPath +
                ". the path does not designate a folder. ");
        }

        File[] files = folder.listFiles();

        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.getName().endsWith("." + extension))
                f.delete();
        }
    }

    /**
     * Uses the system-dependent rename function in order to move the file to
     * the destination folder
     * 
     * @param f
     * @param destDirPath
     * @return true if the file has been moved, false otherwise
     */
    public boolean moveFile(File f, String destDirPath) {
        String destFilePath = destDirPath + File.separator + f.getName();
        File dest = new File(destFilePath);
        if (f.renameTo(dest)) {
            return true;
        } else {
            LoggerManager.getLogger().warn(
                    "Could not move file " + f.getAbsolutePath() + " to folder " + destDirPath);
            return true;
        }

    }

    public void moveFiles(String srcFolder, String destFolder, List<String> extentionsToExclude,
            List<String> filesToExclude, List<String> fileNamesToInclude, boolean deleteSource)
            throws IOException {
        File sFolder = new File(srcFolder);
        File dFolder = new File(destFolder);
        if ((!sFolder.exists() || (!sFolder.isDirectory())))

        {
            throw new FileNotFoundException("Source folder " + srcFolder + " not found or is not a directory");
        }

        if (dFolder.isFile()) {
            throw new IOException("Destination path " + destFolder +
                " is not a directory, but a file. could not create directory at this path. ");
        }

        if (!dFolder.exists()) {
            if (!dFolder.mkdir()) {
                throw new IOException("Could not create directory at location " + destFolder);
            }
        }

        FilesFilter gff = new FilesFilter(extentionsToExclude, filesToExclude, fileNamesToInclude);

        File[] files = sFolder.listFiles(gff);

        if (files == null) {
            System.out.println("No gold result files found in folder " + srcFolder);
            return;
        }

        // move files from scrFolder To destFolder
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (deleteSource)
                moveFile(f, destFolder);
            else {
                String destFilePath = destFolder + File.separator + f.getName();
                copyfile(f.getAbsolutePath(), destFilePath);
            }

        }
    }

}
