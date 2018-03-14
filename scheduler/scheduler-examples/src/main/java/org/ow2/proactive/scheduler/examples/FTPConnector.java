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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
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

    //Default value is getLocalSpace() because it will always be writable and moreover can be used to transfer files to another data space (global, user)
    private String ftpLocalFilePath = getLocalSpace();

    private String ftpRemoteFilePath = null;

    private String ftpMode = null;

    private String ftpUsername = null;

    private String ftpPassword = null;

    private static final String FTP_LOCAL_FILE_PATH = "ftpLocalFilePath";

    /**
     * @see JavaExecutable#init(Map)
     */
    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        if (args.containsKey("ftpHostname")) {
            ftpHostname = args.get("ftpHostname").toString();
        }
        if (args.containsKey("ftpPort")) {
            ftpPort = Integer.parseInt(args.get("ftpPort").toString());
        }
        if (args.containsKey("ftpExtractArchive")) {
            ftpExtractArchive = Boolean.parseBoolean(args.get("ftpExtractArchive").toString());
        }
        if (args.containsKey(FTP_LOCAL_FILE_PATH) && args.get(FTP_LOCAL_FILE_PATH).toString() != null) {
            ftpLocalFilePath = args.get(FTP_LOCAL_FILE_PATH).toString();
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
            throw new IllegalArgumentException("You first need to add your ftp username and password (FTP_USERNAME, FTP_PASSWORD) to the third party credentials");
        }
    }

    /**
     * @see JavaExecutable#execute(TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws IOException {
        List<String> filesRelativePathName;
        FTPClient ftpClient = new FTPClient();
        // connect and login to the server
        ftpClient.connect(ftpHostname, ftpPort);
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }
        ftpClient.login(ftpUsername, ftpPassword);

        // use local passive mode to pass firewall
        ftpClient.enterLocalPassiveMode();

        getOut().println("Connected");

        switch (ftpMode) {
            //FTP mode is GET
            case "GET":
                filesRelativePathName = FtpGet(ftpClient);
                break;

            //FTP mode is PUT
            case "PUT":
                filesRelativePathName = FtpPut(ftpClient);
                break;

            default:
                throw new RuntimeException("wrong value");
        }
        // log out and disconnect from the server
        ftpClient.logout();
        ftpClient.disconnect();

        getOut().println("Disconnected");
        return (Serializable) filesRelativePathName;
    }

    private List<String> FtpGet(FTPClient ftpClient) throws IOException {
        List<String> filesRelativePathName;
        getOut().println("Importing file(s) from " + ftpRemoteFilePath + " to " + ftpLocalFilePath);
        FTPFile[] ftpFile = ftpClient.listFiles(ftpRemoteFilePath);
        if (ftpFile.length == 0) {
            throw new IllegalArgumentException(ftpRemoteFilePath + " not found. Please, enter a valid path.");
        }
        filesRelativePathName = new ArrayList<>();
        // If it is a single file:
        if (ftpFile.length == 1 && ftpRemoteFilePath.contains(ftpFile[0].getName())) {
            String saveFilePath = Paths.get(ftpLocalFilePath, ftpFile[0].getName()).toString();
            if (downloadSingleFile(ftpClient, ftpRemoteFilePath, saveFilePath)) {
                getOut().println("Succeed to download the file " + ftpRemoteFilePath + " to " + ftpLocalFilePath);
                filesRelativePathName.add(saveFilePath);
            } else {
                throw new IOException("Error: COULD NOT download the file: " + ftpRemoteFilePath);
            }

            // If the file is a zip, and ftpExtractArchive is set to true
            if (ftpExtractArchive && ftpRemoteFilePath.endsWith(".zip")) {
                extractArchive(saveFilePath);
            }
        } // If it is a folder, download all its contents recursively
        else {
            ftpClient.changeWorkingDirectory(ftpRemoteFilePath);
            filesRelativePathName.addAll(new HashSet(downloadDirectory(ftpClient, "", "", ftpLocalFilePath)));
        }
        getOut().println("END Import file(s) from FTP.");
        return filesRelativePathName;
    }

    private void extractArchive(String saveFilePath) throws IOException {
        try (ZipFile zipFile = new ZipFile(saveFilePath)) {
            // Create an enumeration of the entries in the zip file
            Enumeration zipFileEntries = zipFile.entries();
            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                Path path = Paths.get(ftpLocalFilePath, entry.getName());
                getOut().println("path: " + path.toString());
                if (entry.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    Path parentDir = path.getParent();
                    getOut().println("parentDir of path: " + parentDir.toString());
                    if (!Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    if (Files.exists(path)) {
                        new File(path.toString()).delete();
                    }

                    Files.copy(zipFile.getInputStream(entry), path);
                }
            }
        }
    }

    private List<String> FtpPut(FTPClient ftpClient) throws IOException {
        List<String> filesRelativePathName;
        if (ftpLocalFilePath.isEmpty()) {
            throw new IllegalArgumentException("The ftp local file path variable is empty. Please, enter a value.");
        }
        getOut().println("Exporting file(s) from " + ftpLocalFilePath + " to " + ftpRemoteFilePath);
        filesRelativePathName = new ArrayList<>();
        File localFile = new File(ftpLocalFilePath);
        // if the ftp remote file path does not exist then it is created.
        if (!checkDirectoryExists(ftpClient, ftpRemoteFilePath)) {
            ftpClient.makeDirectory(ftpRemoteFilePath);
            getOut().println(ftpRemoteFilePath + " does not exist. Therefore, it is created.");
        } else {
            getOut().println(ftpRemoteFilePath + " exists.");
        }
        // If it is a single file:
        if (localFile.isFile()) {
            getOut().println("A single FILE to upload");
            ftpClient.changeWorkingDirectory(ftpRemoteFilePath);
            if (uploadSingleFile(ftpClient, ftpLocalFilePath, localFile.getName())) {
                getOut().println("UPLOADED a file to: " + Paths.get(ftpRemoteFilePath, localFile.getName()));
                filesRelativePathName.add(localFile.getName());
            } else {
                throw new IOException("Error: COULD NOT upload the file: " + ftpLocalFilePath);
            }
        }
        // If it is a folder, upload all its contents recursively
        else if (localFile.isDirectory()) {
            getOut().println("A DIRECTORY to upload");
            ftpClient.changeWorkingDirectory(ftpRemoteFilePath);
            filesRelativePathName.addAll(new HashSet(uploadDirectory(ftpClient, "", ftpLocalFilePath, "")));
        } else {
            throw new IllegalArgumentException(localFile + " not found. Please, enter a valid path.");
        }
        getOut().println("END Export file(s) to FTP.");
        return filesRelativePathName;
    }

    private List<String> uploadDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir,
            String remoteParentDir) throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();
        getOut().println("LISTING directory: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null) {
            for (File item : subFiles) {
                filesRelativePathName.addAll(uploadSubFilesDirectory(ftpClient,
                                                                     remoteDirPath,
                                                                     localParentDir,
                                                                     remoteParentDir,
                                                                     item));
            }
        }
        return filesRelativePathName;
    }

    private List<String> uploadSubFilesDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir,
            String remoteParentDir, File item) throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();
        String remoteFilePath = Paths.get(remoteDirPath, remoteParentDir, item.getName()).toString();
        if (remoteParentDir.isEmpty()) {
            remoteFilePath = Paths.get(remoteDirPath, item.getName()).toString();
        }

        if (item.isFile()) {
            // upload the file
            String localFilePath = item.getAbsolutePath();
            getOut().println("About to upload the file: " + localFilePath);
            boolean uploaded = uploadSingleFile(ftpClient, localFilePath, remoteFilePath);
            if (uploaded) {
                getOut().println("UPLOADED a file to: " + remoteFilePath);
                filesRelativePathName.add(remoteFilePath);
            } else {
                throw new IOException("Error: COULD NOT upload the file: " + localFilePath);
            }
        } else {
            // create directory on the server
            boolean created = ftpClient.makeDirectory(remoteFilePath);
            if (created) {
                getOut().println("CREATED the directory: " + remoteFilePath);
            } else {
                throw new IOException("Error: COULD NOT create the directory: " + remoteFilePath);
            }

            // upload the sub directory
            String parent = Paths.get(remoteParentDir, item.getName()).toString();
            if (remoteParentDir.equals("")) {
                parent = item.getName();
            }

            localParentDir = item.getAbsolutePath();
            filesRelativePathName.addAll(uploadDirectory(ftpClient, remoteDirPath, localParentDir, parent));
        }
        return filesRelativePathName;
    }

    private boolean uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath)
            throws IOException {
        File localFile = new File(localFilePath);

        try (InputStream inputStream = new FileInputStream(localFile)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.storeFile(remoteFilePath, inputStream);
        }
    }

    private boolean checkDirectoryExists(FTPClient ftpClient, String dirPath) throws IOException {
        ftpClient.changeWorkingDirectory(dirPath);
        int returnCode = ftpClient.getReplyCode();
        return (returnCode != 550);
    }

    private List<String> downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir)
            throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();
        String dirToList = parentDir;
        if (!currentDir.isEmpty()) {
            dirToList = Paths.get(dirToList, currentDir).toString();
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = Paths.get(parentDir, currentDir, currentFileName).toString();
                if (currentDir.isEmpty()) {
                    filePath = Paths.get(parentDir, currentFileName).toString();
                }
                String newDirPath = Paths.get(saveDir, parentDir, currentDir, currentFileName).toString();
                if (currentDir.isEmpty()) {
                    newDirPath = Paths.get(saveDir, parentDir, currentFileName).toString();
                }

                filesRelativePathName.addAll(downloadSubFilesDirectory(ftpClient,
                                                                       newDirPath,
                                                                       filePath,
                                                                       saveDir,
                                                                       dirToList,
                                                                       currentFileName,
                                                                       aFile));

            }
        }
        return filesRelativePathName;
    }

    private List<String> downloadSubFilesDirectory(FTPClient ftpClient, String newDirPath, String filePath,
            String saveDir, String dirToList, String currentFileName, FTPFile aFile) throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();
        if (aFile.isDirectory()) {
            // create the directory in saveDir
            File newDir = new File(newDirPath);
            boolean created = newDir.mkdirs();
            if (created) {
                getOut().println("CREATED the directory: " + newDirPath);
            } else {
                throw new IOException("Error: COULD NOT create the directory: " + newDirPath);
            }

            // download the sub directory
            filesRelativePathName.addAll(downloadDirectory(ftpClient, dirToList, currentFileName, saveDir));
        } else {
            // download the file
            boolean success = downloadSingleFile(ftpClient, filePath, newDirPath);
            if (success) {
                getOut().println("DOWNLOADED the file: " + filePath);
                filesRelativePathName.add(filePath);
            } else {
                throw new IOException("Error: COULD NOT download the file: " + filePath);
            }
        }
        return filesRelativePathName;
    }

    private boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException {
        getOut().println("About to download the file: " + remoteFilePath);
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        }
    }

}
