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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.zeroturnaround.zip.ZipUtil;


/**
 * Import/Export file(s) from/to an FTP server using an FTPConnector task.
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

    private String ftpLocalRelativePath = null;

    private String ftpRemoteRelativePath = null;

    private String ftpMode = null;

    private String ftpUsername = null;

    private String ftpPassword = null;

    private static final String FTP_LOCAL_RELATIVE_PATH = "ftpLocalRelativePath";

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
        if (args.containsKey("ftpMode")) {
            ftpMode = args.get("ftpMode").toString();
        }
        if (args.containsKey("ftpExtractArchive")) {
            ftpExtractArchive = Boolean.parseBoolean(args.get("ftpExtractArchive").toString());
        }
        if (args.containsKey(FTP_LOCAL_RELATIVE_PATH) && !args.get(FTP_LOCAL_RELATIVE_PATH).toString().isEmpty()) {
            ftpLocalRelativePath = args.get(FTP_LOCAL_RELATIVE_PATH).toString();
        } else {
            //We throw an exception tp prevent transferring all the contents of the global space.
            if (ftpMode.equals("PUT")) {
                throw new IllegalArgumentException("You have to specify the local relative path. Empty value is not allowed.");
            }
            //Default value is getLocalSpace() because it will always be writable and moreover can be used to transfer files to another data space (global, user)
            ftpLocalRelativePath = getLocalSpace();
        }
        if (args.containsKey("ftpRemoteRelativePath")) {
            ftpRemoteRelativePath = args.get("ftpRemoteRelativePath").toString();
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

        try {
            //connect to the server
            ftpClient.connect(ftpHostname, ftpPort);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new IOException("Exception in connecting to FTP Server");
            }
            //login to the server
            if (!ftpClient.login(ftpUsername, ftpPassword)) {
                throw new IOException("Logging refused.");
            }

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();

            getOut().println("Connected");

            switch (ftpMode) {
                //FTP mode is GET
                case "GET":
                    filesRelativePathName = ftpGet(ftpClient);
                    break;

                //FTP mode is PUT
                case "PUT":
                    filesRelativePathName = ftpPut(ftpClient);
                    break;

                default:
                    throw new IllegalArgumentException("FTP MODE can only be PUT or GET.");

            }
        } catch (IOException e) {
            throw new IOException("Connection issues. The hostname and/or ftpPort can be wrong.");
        } finally {
            // log out and disconnect from the server
            ftpClient.logout();
            ftpClient.disconnect();
            getOut().println("Disconnected");
        }
        return (Serializable) filesRelativePathName;
    }

    private List<String> ftpGet(FTPClient ftpClient) throws IOException {
        List<String> filesRelativePathName;
        getOut().println("Importing file(s) from " + ftpRemoteRelativePath + " to " + ftpLocalRelativePath);
        FTPFile[] ftpFile = ftpClient.listFiles(ftpRemoteRelativePath);
        if (ftpFile.length == 0) {
            throw new IllegalArgumentException(ftpRemoteRelativePath + " not found. Please, enter a valid path.");
        }
        filesRelativePathName = new ArrayList<>();

        // If it is a single file:
        if (ftpFile.length == 1 && ftpRemoteRelativePath.contains(ftpFile[0].getName())) {
            String saveFilePath = Paths.get(ftpLocalRelativePath, ftpFile[0].getName()).toString();
            filesRelativePathName.add(downloadSingleFile(ftpClient, ftpRemoteRelativePath, saveFilePath));

            // If the file is a zip, and ftpExtractArchive is set to true
            if (ftpExtractArchive && ftpRemoteRelativePath.endsWith(".zip")) {
                ZipUtil.unpack(new File(saveFilePath), new File(ftpLocalRelativePath));
            }
        }
        // If it is a folder, download all its contents recursively
        else {
            ftpClient.changeWorkingDirectory(ftpRemoteRelativePath);
            filesRelativePathName.addAll(new HashSet(downloadDirectory(ftpClient, "", "", ftpLocalRelativePath)));
        }
        getOut().println("END Import file(s) from FTP.");
        return filesRelativePathName;
    }

    private List<String> ftpPut(FTPClient ftpClient) throws IOException {
        List<String> filesRelativePathName = new ArrayList<>();
        getOut().println("Exporting file(s) from " + ftpLocalRelativePath + " to " + ftpRemoteRelativePath);
        File localFile = new File(ftpLocalRelativePath);

        // if the ftp remote file path does not exist then it is created.
        createRemoteDirectoryIfNotExists(ftpClient, ftpRemoteRelativePath);

        // If it is a single file:
        if (localFile.isFile()) {
            getOut().println("A single FILE to upload");
            ftpClient.changeWorkingDirectory(ftpRemoteRelativePath);
            filesRelativePathName.add(uploadSingleFile(ftpClient, ftpLocalRelativePath, localFile.getName()));

        }

        // If it is a folder, upload all its contents recursively
        else if (localFile.isDirectory()) {
            getOut().println("A DIRECTORY to upload");
            ftpClient.changeWorkingDirectory(ftpRemoteRelativePath);
            filesRelativePathName.addAll(new HashSet(uploadDirectory(ftpClient, "", ftpLocalRelativePath, "")));
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
                String remoteFilePath = Paths.get(remoteDirPath, remoteParentDir, item.getName()).toString();
                if (remoteParentDir.isEmpty()) {
                    remoteFilePath = Paths.get(remoteDirPath, item.getName()).toString();
                }

                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    getOut().println("About to upload the file: " + localFilePath);
                    filesRelativePathName.add(uploadSingleFile(ftpClient, localFilePath, remoteFilePath));

                } else {
                    // create directory on the server
                    createRemoteDirectory(ftpClient, remoteFilePath);

                    // upload the sub directory
                    String parent = Paths.get(remoteParentDir, item.getName()).toString();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    filesRelativePathName.addAll(uploadDirectory(ftpClient, remoteDirPath, localParentDir, parent));
                }
            }
        }
        return filesRelativePathName;
    }

    private String uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath)
            throws IOException {
        File localFile = new File(localFilePath);

        try (InputStream inputStream = new FileInputStream(localFile)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //upload a single file to the FTP server
            if (ftpClient.storeFile(remoteFilePath, inputStream)) {
                getOut().println("UPLOADED a file to: " + Paths.get(ftpRemoteRelativePath, localFile.getName()));
                return remoteFilePath;
            } else {
                throw new IOException("Error: COULD NOT upload the file: " + ftpLocalRelativePath);
            }
        }
    }

    private void createRemoteDirectoryIfNotExists(FTPClient ftpClient, String dirPath) throws IOException {
        ftpClient.changeWorkingDirectory(dirPath);
        int returnCode = ftpClient.getReplyCode();
        if (returnCode == 550) {
            createRemoteDirectory(ftpClient, ftpRemoteRelativePath);
        }
    }

    private void createRemoteDirectory(FTPClient ftpClient, String remoteDirPath) throws IOException {
        if (ftpClient.makeDirectory(remoteDirPath)) {
            getOut().println("CREATED the directory: " + remoteDirPath);
        } else {
            throw new IOException("Error: COULD NOT create the directory: " + remoteDirPath);
        }
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
                String remoteFilePath = Paths.get(parentDir, currentDir, currentFileName).toString();
                String savePath = Paths.get(saveDir, parentDir, currentDir, currentFileName).toString();
                if (aFile.isDirectory()) {

                    // create the directory savePath inside saveDir
                    makeDirectories(savePath);

                    // download the sub directory
                    filesRelativePathName.addAll(downloadDirectory(ftpClient, dirToList, currentFileName, saveDir));
                } else {
                    // download the file
                    filesRelativePathName.add(downloadSingleFile(ftpClient, remoteFilePath, savePath));
                }
            }
        }
        return filesRelativePathName;
    }

    private String downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException {
        getOut().println("About to download the file: " + remoteFilePath);
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {

            //retrieve a single file from the FTP server
            if (ftpClient.retrieveFile(remoteFilePath, outputStream)) {
                getOut().println("DOWNLOADED successfully the file " + ftpRemoteRelativePath + " to " +
                                 ftpLocalRelativePath);
                return savePath;
            } else {
                throw new IOException("Error: COULD NOT download the file: " + ftpRemoteRelativePath);
            }
        }
    }

    private void makeDirectories(String newDirPath) throws IOException {
        File newDir = new File(newDirPath);
        if (newDir.mkdirs()) {
            getOut().println("CREATED the directory: " + newDirPath);
        } else {
            throw new IOException("Error: COULD NOT create the directory: " + newDirPath);
        }
    }
}
