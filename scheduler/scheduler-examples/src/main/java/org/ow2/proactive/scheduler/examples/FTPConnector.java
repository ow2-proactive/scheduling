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
package org.ow2.proactive.scheduler.examples;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Import and export file(s) using FTPConnector server.
 * This task can be launched with parameters.
 *
 * @author The ProActive Team
 */
public class FTPConnector extends JavaExecutable {

    private static final String DEFAULT_FTP_HOSTNAME = "localhost";

    private static final int DEFAULT_FTP_PORT = 21;

    private String ftpHostname = DEFAULT_FTP_HOSTNAME;

    private int ftpPort = DEFAULT_FTP_PORT;

    private boolean ftpExtractArchive;

    private String ftpLocalFilePath = null;

    private String ftpRemoteFilePath = null;

    private String ftpMode = null;

    private String ftpUsername = null;

    private String ftpPassword = null;

    /**
     * @see JavaExecutable#init(Map)
     */
    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        if (args.containsKey("ftpHostname")) {
            ftpHostname = args.get("ftpHostname").toString();
        }
        if (args.containsKey("ftpPort")) {
            try {
                ftpPort = Integer.parseInt(args.get("ftpPort").toString());
            } catch (NumberFormatException e) {
            }
        }
        if (args.containsKey("ftpExtractArchive")) {
            ftpExtractArchive = Boolean.parseBoolean(args.get("ftpExtractArchive").toString());
        }
        if (args.containsKey("ftpLocalFilePath")) {
            ftpLocalFilePath = args.get("ftpLocalFilePath") == null ? "" : args.get("ftpLocalFilePath").toString();
        }
        if (args.containsKey("ftpRemoteFilePath")) {
            ftpRemoteFilePath = args.get("ftpRemoteFilePath").toString();
        }
        if (args.containsKey("ftpMode")) {
            ftpMode = args.get("ftpMode").toString();
        }
        ftpUsername = getThirdPartyCredential("FTP_USERNAME");
        ftpPassword = getThirdPartyCredential("FTP_PASSWORD");
        if (ftpUsername == null || ftpPassword == null) {
            throw new Exception("You first need to add your ftp username and password (FTP_USERNAME, FTP_PASSWORD) to the third party credentials");
        }
    }

    /**
     * @see JavaExecutable#execute(TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Exception {

        FTPClient ftpClient = new FTPClient();
        try {
            // connect and login to the server
            ftpClient.connect(ftpHostname, ftpPort);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new Exception("Exception in connecting to FTP Server");
            }
            ftpClient.login(ftpUsername, ftpPassword);

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();

            System.out.println("Connected");

            //FTP mode is GET
            if (ftpMode.equals("GET")) {
                System.out.println("Importing file(s) from " + ftpRemoteFilePath + " to the global space at: " +
                                   ftpLocalFilePath);
                FTPFile[] ftpFile = ftpClient.listFiles(ftpRemoteFilePath);
                if (ftpFile.length == 0)
                    throw new Exception(ftpRemoteFilePath + " not found. Please, enter a valid path.");

                // If it is a single file:
                if (ftpFile.length == 1 && ftpRemoteFilePath.contains(ftpFile[0].getName())) {
                    String saveFilePath = Paths.get(ftpLocalFilePath, ftpFile[0].getName()).toString();
                    boolean download = downloadSingleFile(ftpClient, ftpRemoteFilePath, saveFilePath);
                    if (download) {
                        System.out.println("Succeed to download the file " + ftpRemoteFilePath +
                                           " to the global space " + ftpLocalFilePath);
                    } else {
                        System.out.println("COULD NOT download the file: " + ftpRemoteFilePath);
                    }

                    // If the file is a zip, and ftpExtractArchive is set to true
                    if (ftpExtractArchive && ftpRemoteFilePath.endsWith(".zip")) {
                        ZipFile zipFile = null;
                        try {
                            zipFile = new ZipFile(saveFilePath);
                            // Create an enumeration of the entries in the zip file
                            Enumeration zipFileEntries = zipFile.entries();
                            // Process each entry
                            while (zipFileEntries.hasMoreElements()) {
                                // grab a zip file entry
                                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                                Path path = Paths.get(ftpLocalFilePath, entry.getName());
                                System.out.println("path: " + path.toString());
                                if (entry.isDirectory()) {
                                    Files.createDirectories(path);
                                } else {
                                    Path parentDir = path.getParent();
                                    System.out.println("parentDir of path: " + parentDir.toString());
                                    if (!Files.exists(parentDir)) {
                                        Files.createDirectories(parentDir);
                                    }
                                    if (Files.exists(path)) {
                                        new File(path.toString()).delete();
                                    }

                                    Files.copy(zipFile.getInputStream(entry), path);
                                }
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } finally {
                            zipFile.close();

                        }
                    }
                } // If it is a folder, download all its contents recursively
                else {
                    ftpClient.changeWorkingDirectory(ftpRemoteFilePath);
                    downloadDirectory(ftpClient, "", "", ftpLocalFilePath);
                }
                System.out.println("END Import file(s) from FTP.");
            }
            //FTP mode is PUT
            else if (ftpMode.equals("PUT")) {
                if (ftpLocalFilePath.isEmpty()) {
                    throw new Exception("The ftp local file path variable is empty. Please, enter a value.");
                }
                System.out.println("Exporting file(s) from global space at: " + ftpLocalFilePath + " to " +
                                   ftpRemoteFilePath);
                File localFile = new File(ftpLocalFilePath);
                // if the ftp remote file path does not exist then it is created.
                if (!checkDirectoryExists(ftpClient, ftpRemoteFilePath)) {
                    ftpClient.makeDirectory(ftpRemoteFilePath);
                    System.out.println(ftpRemoteFilePath + " does not exist. Therefore, it is created.");
                } else {
                    System.out.println(ftpRemoteFilePath + " exists.");
                }
                // If it is a single file:
                if (localFile.isFile()) {
                    System.out.println("A single FILE to upload");
                    ftpClient.changeWorkingDirectory(ftpRemoteFilePath);
                    boolean uploaded = uploadSingleFile(ftpClient, ftpLocalFilePath, localFile.getName());
                    if (uploaded) {
                        System.out.println("UPLOADED a file to: " + Paths.get(ftpRemoteFilePath, localFile.getName()));
                    } else {
                        System.out.println("COULD NOT upload the file: " + ftpLocalFilePath);
                    }
                }
                // If it is a folder, upload all its contents recursively
                else if (localFile.isDirectory()) {
                    System.out.println("A DIRECTORY to upload");
                    ftpClient.changeWorkingDirectory(ftpRemoteFilePath);
                    uploadDirectory(ftpClient, "", ftpLocalFilePath, "");
                } else {
                    throw new Exception(localFile + " not found. Please, enter a valid path.");
                }
                System.out.println("END Export file(s) to FTP.");
            }
            // log out and disconnect from the server
            ftpClient.logout();
            ftpClient.disconnect();

            System.out.println("Disconnected");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 1;
    }

    private void uploadDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir,
            String remoteParentDir) throws IOException {

        System.out.println("LISTING directory: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            for (File item : subFiles) {
                String remoteFilePath = Paths.get(remoteDirPath, remoteParentDir, item.getName()).toString();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = Paths.get(remoteDirPath, item.getName()).toString();
                }

                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("About to upload the file: " + localFilePath);
                    boolean uploaded = uploadSingleFile(ftpClient, localFilePath, remoteFilePath);
                    if (uploaded) {
                        System.out.println("UPLOADED a file to: " + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT upload the file: " + localFilePath);
                    }
                } else {
                    // create directory on the server
                    boolean created = ftpClient.makeDirectory(remoteFilePath);
                    if (created) {
                        System.out.println("CREATED the directory: " + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT create the directory: " + remoteFilePath);
                    }

                    // upload the sub directory
                    String parent = Paths.get(remoteParentDir, item.getName()).toString();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    uploadDirectory(ftpClient, remoteDirPath, localParentDir, parent);
                }
            }
        }
    }

    private static boolean uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath)
            throws IOException {
        File localFile = new File(localFilePath);

        InputStream inputStream = new FileInputStream(localFile);
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.storeFile(remoteFilePath, inputStream);
        } finally {
            inputStream.close();
        }
    }

    private boolean checkDirectoryExists(FTPClient ftpClient, String dirPath) throws IOException {
        ftpClient.changeWorkingDirectory(dirPath);
        int returnCode = ftpClient.getReplyCode();
        if (returnCode == 550) {
            return false;
        }
        return true;
    }

    private void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir)
            throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList = Paths.get(dirToList, currentDir).toString();
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = Paths.get(parentDir, currentDir, currentFileName).toString();
                if (currentDir.equals("")) {
                    filePath = Paths.get(parentDir, currentFileName).toString();
                }
                String newDirPath = Paths.get(saveDir, parentDir, currentDir, currentFileName).toString();
                if (currentDir.equals("")) {
                    newDirPath = Paths.get(saveDir, parentDir, currentFileName).toString();
                }

                if (aFile.isDirectory()) {
                    // create the directory in saveDir
                    File newDir = new File(newDirPath);
                    boolean created = newDir.mkdirs();
                    if (created) {
                        System.out.println("CREATED the directory: " + newDirPath);
                    } else {
                        System.out.println("COULD NOT create the directory: " + newDirPath);
                    }

                    // download the sub directory
                    downloadDirectory(ftpClient, dirToList, currentFileName, saveDir);
                } else {
                    // download the file
                    boolean success = downloadSingleFile(ftpClient, filePath, newDirPath);
                    if (success) {
                        System.out.println("DOWNLOADED the file: " + filePath);
                    } else {
                        System.out.println("COULD NOT download the file: " + filePath);
                    }
                }
            }
        }
    }

    private boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException {
        System.out.println("About to download the file: " + remoteFilePath);
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));

        try {
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

}
