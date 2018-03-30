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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.zeroturnaround.zip.ZipUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


/**
 * Import and export file(s) using SFTPConnector server.
 *
 * @author The ProActive Team
 */
public class SFTPConnector extends JavaExecutable {

    private static final String SFTP_LOCAL_RELATIVE_PATH_ARG = "sftpLocalRelativePath";

    private static final String DEFAULT_SFTP_HOSTNAME = "localhost";

    private static final int DEFAULT_SFTP_PORT = 22;

    /**
     * The SFTP_URL_KEY = "sftp://<username>@<host>" is used as a key to store sftp passwords in 3rd party credentials
     */
    private static final String SFTP_URL_KEY = "sftp://<username>@<host>";

    private static final String GET = "GET";

    private static final String PUT = "PUT";

    private static final String STRICT_HOST_KEY_CHEKING = "StrictHostKeyChecking";

    private static final String CURRENT_FOLDER = ".";

    private static final String PARENT_FOLDER = "..";

    private String sftpHostname = DEFAULT_SFTP_HOSTNAME;

    private int sftpPort = DEFAULT_SFTP_PORT;

    private boolean sftpExtractArchive;

    private String sftpLocalAbsolutePath;

    private String sftpRemoteRelativePath;

    private String sftpMode;

    private String sftpUsername;

    private String sftpUrlKey;

    private String sftpPassword;

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        if (args.containsKey("sftpHostname")) {
            sftpHostname = args.get("sftpHostname").toString();
        }
        if (args.containsKey("sftpPort")) {
            try {
                sftpPort = Integer.parseInt(args.get("sftpPort").toString());
            } catch (NumberFormatException e) {
                getOut().println("Invalid port number. Using default sftp port=" + DEFAULT_SFTP_PORT);
            }
        }
        if (args.containsKey("sftpMode")) {
            sftpMode = args.get("sftpMode").toString();
        }
        if (args.containsKey("sftpExtractArchive")) {
            sftpExtractArchive = Boolean.parseBoolean(args.get("sftpExtractArchive").toString());
        }
        if (args.containsKey(SFTP_LOCAL_RELATIVE_PATH_ARG) &&
            !args.get(SFTP_LOCAL_RELATIVE_PATH_ARG).toString().isEmpty()) {
            sftpLocalAbsolutePath = Paths.get(getLocalSpace(), args.get(SFTP_LOCAL_RELATIVE_PATH_ARG).toString())
                                         .toString();
        } else {
            //we throw an exception tp prevent transferring all the contents of the global space.
            if (sftpMode.equals(PUT)) {
                throw new IllegalArgumentException("Please specify a local relative path. Empty value is not allowed.");
            }
            //default value is getlocalspace() because it will always be writable and moreover can be used to transfer files to another data space(global, user)
            sftpLocalAbsolutePath = getLocalSpace();
        }
        if (args.containsKey("sftpRemoteRelativePath")) {
            sftpRemoteRelativePath = args.get("sftpRemoteRelativePath").toString();
        }

        if (args.containsKey("sftpUsername")) {
            sftpUsername = args.get("sftpUsername").toString();
        }

        if (sftpUsername == null) {
            throw new IllegalArgumentException("Username is required to access an SFTP server");
        }

        // This key is used for logs and for getting the password from 3rd party credentials.
        sftpUrlKey = "sftp://" + sftpUsername + "@" + sftpHostname;

        sftpPassword = getThirdPartyCredential(sftpUrlKey);

        if (sftpPassword == null) {
            throw new IllegalArgumentException("Please add your sftp password to 3rd-party credentials under the key :\"" +
                                               SFTP_URL_KEY + "\"");
        }
    }

    @Override
    public Serializable execute(TaskResult... results) throws IOException, JSchException, SftpException {

        List<String> filesRelativePathName = null;

        ChannelSftp channelsftp = null;
        Session session = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHostname, sftpPort);
            session.setPassword(sftpPassword);
            Properties config = new Properties();
            config.put(STRICT_HOST_KEY_CHEKING, "no");
            session.setConfig(config);
            // create sftp Session
            getOut().println("Connecting to " + sftpUrlKey);
            session.connect();
            // open sftp Channel
            channel = session.openChannel("sftp");
            channel.connect();
            channelsftp = (ChannelSftp) channel;

            switch (sftpMode) {
                //ftp mode is get
                case GET:
                    getOut().println("BEGIN DOWNLOAD FROM SFTP SERVER: " + sftpUrlKey);
                    // recursive folder content download from sftp server
                    filesRelativePathName = recursiveFolderDownload(channelsftp,
                                                                    sftpRemoteRelativePath,
                                                                    sftpLocalAbsolutePath);
                    getOut().println("END DOWNLOAD FROM SFTP");
                    break;

                //ftp mode is put
                case PUT:
                    getOut().println("BEGIN UPLOAD TO SFTP SERVER: " + sftpUrlKey);
                    // Create remote directory(ies) if it(they) does(do) not exist
                    if (!remoteDirExists(channelsftp, sftpRemoteRelativePath)) {
                        makeRemoteDirectories(channelsftp, Paths.get(sftpRemoteRelativePath));
                    }
                    // recursive folder content upload to sftp server
                    filesRelativePathName = recursiveFolderUpload(channelsftp,
                                                                  sftpLocalAbsolutePath,
                                                                  sftpRemoteRelativePath);
                    getOut().println("END UPLOAD TO SFTP");
                    break;

                default:
                    throw new IllegalArgumentException("SFTP mode can only be " + PUT + " or " + GET);
            }
        } catch (JSchException e) {
            throw new JSchException("An error occured while trying to connect to SFTP server: ", e.getCause());
        } catch (SftpException e) {
            throw new SftpException(e.id,
                                    "File not found, permission denied or operation not supported by SFTP server.",
                                    e);
        } finally {
            if (channelsftp != null) {
                channelsftp.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
            getOut().println("Disconnected from SFTP server");
        }

        return (Serializable) filesRelativePathName;
    }

    /**
     * This method recursively uploads a local file/folder to an SFTP server
     *
     * @param sourcePath
     * @param destinationPath
     * @throws SftpException
     * @throws FileNotFoundException
     */
    private List<String> recursiveFolderUpload(ChannelSftp channelSftp, String sourcePath, String destinationPath)
            throws SftpException, FileNotFoundException {

        List<String> filesRelativePathName = new ArrayList<>();
        File sourceFile = new File(sourcePath);
        String remoteFilePath = Paths.get(destinationPath, sourceFile.getName()).toString();

        if (sourceFile.isFile()) {
            // copy if it is a file
            getOut().println("Uploading " + sourcePath);
            channelSftp.put(new FileInputStream(sourceFile), remoteFilePath, ChannelSftp.OVERWRITE);
            getOut().println("File uploaded successfully to " + remoteFilePath);
            filesRelativePathName.add(remoteFilePath);

        } else {

            File[] files = sourceFile.listFiles();

            if (!sourceFile.isHidden()) {

                // create a directory if it does not exist
                if (!remoteDirExists(channelSftp, remoteFilePath)) {
                    channelSftp.mkdir(remoteFilePath);
                }

                for (File f : files) {
                    filesRelativePathName.addAll(recursiveFolderUpload(channelSftp,
                                                                       f.getAbsolutePath(),
                                                                       remoteFilePath));
                }
            }
        }
        return filesRelativePathName;
    }

    /**
     * This method downloads a single file over SFTP.<br>
     * By default, this method overwrites existing files.
     *
     * @param channelSftp
     * @param remoteFilePath
     * @param localFilePath
     * @return
     * @throws SftpException
     */
    private String downloadSingleFile(ChannelSftp channelSftp, String remoteFilePath, String localFilePath)
            throws SftpException {

        File localFile = new File(localFilePath);

        // Create parent folders if they don't exist.
        File parentDir = localFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Download file from remoteFilePath -> localFilePath).
        try {
            getOut().println("Downloading " + remoteFilePath);
            channelSftp.get(remoteFilePath, localFilePath);
            getOut().println("File downloaded successfully to " + localFilePath);
            return localFilePath;
        } catch (SftpException e) {
            throw new SftpException(e.id, "ERROR: could not download the file: " + remoteFilePath, e);
        }
    }

    /**
     * This file downloads recursively a folder and all its contents over SFTP. <br>
     * By default, this method <b>OVERWRITES</b> existing files and <b>APPENDS</b> existing folders.
     *
     * @param channelSftp
     * @param sftpRemoteRelativePath
     * @param sftpLocalRelativePath
     * @return
     * @throws SftpException
     * @throws IOException
     */
    private List<String> recursiveFolderDownload(ChannelSftp channelSftp, String sftpRemoteRelativePath,
            String sftpLocalRelativePath) throws SftpException, IOException {

        List<String> filesRelativePathName = new ArrayList<>();

        // Do an ls on the remote path (sftpRemoteRelativePath)
        List<ChannelSftp.LsEntry> fileAndFolderList = null;
        try {
            fileAndFolderList = channelSftp.ls(sftpRemoteRelativePath);
        } catch (SftpException e) {
            throw new SftpException(e.id,
                                    "File not found or permission denied at " + sftpRemoteRelativePath,
                                    e.getCause());
        }

        // Check if the LS results point to a file not a directory
        if (isRemoteFile(fileAndFolderList)) {

            String localFilePath = Paths.get(sftpLocalRelativePath, fileAndFolderList.get(0).getFilename()).toString();

            // Download single file
            filesRelativePathName.add(downloadSingleFile(channelSftp, sftpRemoteRelativePath, localFilePath));

            // if the file is a zip and sftpExtractArchive == true then extract the archive
            if (sftpExtractArchive && sftpRemoteRelativePath.endsWith(".zip")) {
                extractArchive(localFilePath, sftpLocalRelativePath);
            }

            return filesRelativePathName;
        }

        // Iterate through list of folder content
        for (ChannelSftp.LsEntry item : fileAndFolderList) {

            String remoteFilePath = Paths.get(sftpRemoteRelativePath, item.getFilename()).toString();
            String localFilePath = Paths.get(sftpLocalRelativePath, item.getFilename()).toString();
            File localFile = new File(localFilePath);

            // Check if it is a file (not a directory).
            if (!item.getAttrs().isDir()) {
                filesRelativePathName.add(downloadSingleFile(channelSftp, remoteFilePath, localFilePath));

            } else // skip '.' and '..'
            if (!(CURRENT_FOLDER.equals(item.getFilename()) || PARENT_FOLDER.equals(item.getFilename()))) {
                // Copy empty folder.
                localFile.mkdirs();
                // Enter found folder on server to read its contents and create locally.
                filesRelativePathName.addAll(recursiveFolderDownload(channelSftp, remoteFilePath, localFilePath));
            }
        }
        return filesRelativePathName;
    }

    /**
     * This method checks if a directory exists in a remote SFTP server
     *
     * @param channelSftp
     * @param remoteFilePath
     * @return true if the remote directory exists, false otherwise
     */
    private boolean remoteDirExists(ChannelSftp channelSftp, String remoteFilePath) {
        try {
            return channelSftp.stat(remoteFilePath).isDir();
        } catch (SftpException e) {
            // An exception is raised if file/directory is not found.
            return false;
        }
    }

    /**
     * This method checks if the given LS results correspond to a file or not.
     *
     * @param filesList
     * @return
     */
    private boolean isRemoteFile(List<ChannelSftp.LsEntry> filesList) {
        return (filesList.size() == 1 && !filesList.get(0).getAttrs().isDir());
    }

    /**
     * This method recursively creates nonexisting nested folders
     * Example /folder/sub1/sub2/sub3/sub...
     *
     * @param channelSftp
     * @param path
     * @throws SftpException if the remote folder(s) cannot be created
     */
    private void makeRemoteDirectories(ChannelSftp channelSftp, Path path) throws SftpException {
        if (!remoteDirExists(channelSftp, path.toString())) {
            if (path.getParent() == null) {
                channelSftp.mkdir(path.toString());
            } else {
                makeRemoteDirectories(channelSftp, path.getParent());
                channelSftp.mkdir(path.toString());
            }
        }
    }

    /**
     * This method extracts a zip file located at localFilePath and saves the unpacked files to sftpLocalRelativePath.
     * @param localFilePath
     * @param sftpLocalRelativePath
     */
    private void extractArchive(String localFilePath, String sftpLocalRelativePath) {
        getOut().println("Decompressing archive: " + localFilePath);
        ZipUtil.unpack(new File(localFilePath), new File(sftpLocalRelativePath));
        getOut().println("Archive decompressed successfully at: " + sftpLocalRelativePath);
    }
}
