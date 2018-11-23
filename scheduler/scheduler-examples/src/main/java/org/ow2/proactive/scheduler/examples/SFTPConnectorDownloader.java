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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
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
 * @author ActiveEon Team
 * @since 07/11/2018
 */
public class SFTPConnectorDownloader extends JavaExecutable {

    private static final String OUTPUT_PATH = "outputPath";

    private static final String DEFAULT_SFTP_HOST = "localhost";

    private static final int DEFAULT_SFTP_PORT = 22;

    /**
     * The SFTP_URL_KEY = "sftp://<username>@<host>" is used as a key to store sftp passwords in 3rd party credentials
     */
    private static final String SFTP_URL_KEY = "sftp://<username>@<host>";

    private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private static final String CURRENT_FOLDER = ".";

    private static final String PARENT_FOLDER = "..";

    private String host = DEFAULT_SFTP_HOST;

    private int port = DEFAULT_SFTP_PORT;

    private boolean extractArchive;

    private String absoluteOutputPath;

    private String outputPath;

    private String inputPath;

    private String username;

    private String sftpUrlKey;

    private String password;

    private String wildcardSearchMode;

    private static final String WILDCARD_STRING_PATTERN = ".*[\\*\\?]+.*";

    private static final String ZIP_EXTENSION = ".zip";

    private enum InputPathType {
        FILE,
        FOLDER,
        WILDCARD
    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        if (args.containsKey("host")) {
            host = args.get("host").toString();
        }
        if (args.containsKey("port")) {
            try {
                port = Integer.parseInt(args.get("port").toString());
            } catch (NumberFormatException e) {
                getOut().println("Invalid port number. Using default sftp port=" + DEFAULT_SFTP_PORT);
            }
        }
        if (args.containsKey("extractArchive")) {
            extractArchive = Boolean.parseBoolean(args.get("extractArchive").toString());
        }
        if (args.containsKey(OUTPUT_PATH) && !args.get(OUTPUT_PATH).toString().isEmpty()) {
            outputPath = args.get(OUTPUT_PATH).toString();
            absoluteOutputPath = Paths.get(getLocalSpace(), args.get(OUTPUT_PATH).toString()).toString();
        } else {
            //default value is getlocalspace() because it will always be writable and moreover can be used to transfer files to another data space(global, user)
            absoluteOutputPath = getLocalSpace();
        }
        if (args.containsKey("inputPath")) {
            inputPath = args.get("inputPath").toString();
        }

        if (inputPath == null) {
            throw new IllegalArgumentException("Please specify a local relative path in the SFTP server. Empty value is not allowed.");
        }

        if (args.containsKey("username")) {
            username = args.get("username").toString();
        }

        if (args.containsKey("wildcardSearchMode")) {
            wildcardSearchMode = args.get("wildcardSearchMode").toString();
        }

        if (username == null) {
            throw new IllegalArgumentException("Username is required to access an SFTP server");
        }

        // This key is used for logs and for getting the password from 3rd party credentials.
        sftpUrlKey = "sftp://" + username + "@" + host;

        password = getThirdPartyCredential(sftpUrlKey);

        if (password == null) {
            throw new IllegalArgumentException("Please add your sftp password to 3rd-party credentials under the key :\"" +
                                               SFTP_URL_KEY + "\"");
        }
    }

    @Override
    public Serializable execute(TaskResult... results) throws IOException, JSchException, SftpException {

        List<String> filesRelativePathName = null;

        ChannelSftp channelSftp = null;
        Session session = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            Properties config = new Properties();
            config.put(STRICT_HOST_KEY_CHECKING, "no");
            session.setConfig(config);
            // create sftp Session
            getOut().println("Connecting to " + sftpUrlKey);
            session.connect();
            // open sftp Channel
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            createFileTreeIfNotExist(inputPath, channelSftp);
            switch (checkInputPathType(inputPath, channelSftp)) {
                case FILE:
                    String destinationPath;
                    if (outputPath == null || outputPath.isEmpty()) {
                        destinationPath = Paths.get(inputPath).getFileName().toString();
                    } else {
                        destinationPath = Paths.get(absoluteOutputPath, Paths.get(inputPath).getFileName().toString())
                                               .toString();
                    }
                    //import the input file from the sftp server
                    filesRelativePathName = downloadSingleFile(channelSftp, inputPath, destinationPath);
                    break;
                case WILDCARD:
                    if (outputPath == null) {
                        outputPath = "";
                    }
                    filesRelativePathName = wildcardFilesDownloader(channelSftp,
                                                                    inputPath,
                                                                    absoluteOutputPath,
                                                                    wildcardSearchMode);
                    break;
                case FOLDER:
                    if (outputPath == null) {
                        outputPath = "";
                    }
                    // recursive folder content import from sftp server
                    filesRelativePathName = recursiveFolderDownload(channelSftp, inputPath, absoluteOutputPath);
                    break;
            }
            getOut().println("END IMPORTING FROM SFTP SERVER");

        } catch (JSchException e) {
            throw new JSchException("An error occurred while trying to connect to SFTP server: ", e.getCause());
        } catch (SftpException e) {
            throw new SftpException(e.id,
                                    "FILE not found, permission denied or operation not supported by SFTP server.",
                                    e);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
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
     * This method imports a single file over SFTP.<br>
     * By default, this method overwrites existing files.
     *
     * @param channelSftp
     * @param sftpInputPath
     * @param outputPath
     * @return a list containing the path of the imported file
     * @throws SftpException
     */
    private List<String> downloadSingleFile(ChannelSftp channelSftp, String sftpInputPath, String outputPath)
            throws SftpException {
        List<String> filesRelativePathName = new ArrayList<>();
        // Download file from sftpInputPath -> inputPath).
        try {
            getOut().println("Importing " + sftpInputPath);
            channelSftp.get(sftpInputPath, outputPath);
            filesRelativePathName.add(outputPath);
            getOut().println("FILE imported successfully to " + outputPath);
        } catch (SftpException e) {
            throw new SftpException(e.id, "ERROR: could not download the file: " + sftpInputPath, e);
        }

        // if the file is a zip and extractArchive == true then extract the archive
        if (extractArchive && sftpInputPath.endsWith(ZIP_EXTENSION)) {
            File zipDir = new File(FilenameUtils.removeExtension(outputPath));
            if (!zipDir.isDirectory()) {
                zipDir.mkdirs();
            }
            extractArchive(outputPath, FilenameUtils.removeExtension(outputPath));
        }
        return filesRelativePathName;
    }

    private List<ChannelSftp.LsEntry> listFilesAndFolders(ChannelSftp channelSftp, String sftpInputPath)
            throws SftpException {
        // Do an ls on the input path
        List<ChannelSftp.LsEntry> fileAndFolderList = null;
        try {
            fileAndFolderList = channelSftp.ls(sftpInputPath);
        } catch (SftpException e) {
            throw new SftpException(e.id, "FILE not found or permission denied at " + sftpInputPath, e.getCause());
        }
        return fileAndFolderList;
    }

    /**
     * This file downloads recursively a folder and all its contents over SFTP. <br>
     * By default, this method <b>OVERWRITES</b> existing files and <b>APPENDS</b> existing folders.
     *
     * @param channelSftp
     * @param sftpInputPath
     * @param outputPath
     * @return
     * @throws SftpException
     * @throws IOException
     */
    private List<String> recursiveFolderDownload(ChannelSftp channelSftp, String sftpInputPath, String outputPath)
            throws SftpException, IOException {

        List<String> filesRelativePathName = new ArrayList<>();

        List<ChannelSftp.LsEntry> fileAndFolderList = listFilesAndFolders(channelSftp, sftpInputPath);

        // Iterate through list of folder content
        for (ChannelSftp.LsEntry item : fileAndFolderList) {

            String inputFilePath = Paths.get(sftpInputPath, item.getFilename()).toString();
            String outputFilePath = Paths.get(outputPath, item.getFilename()).toString();
            File localFile = new File(outputFilePath);

            // Check if it is a file (not a directory).
            if (!item.getAttrs().isDir()) {
                filesRelativePathName.addAll(downloadSingleFile(channelSftp, inputFilePath, outputFilePath));

            } else // skip '.' and '..'
            if (!(CURRENT_FOLDER.equals(item.getFilename()) || PARENT_FOLDER.equals(item.getFilename()))) {
                // Copy empty folder.
                localFile.mkdirs();
                // Enter found folder on server to read its contents and create locally.
                filesRelativePathName.addAll(recursiveFolderDownload(channelSftp, inputFilePath, outputFilePath));
            }
        }
        return filesRelativePathName;
    }

    /**
     * This method checks if the given LS results correspond to a file.
     *
     * @param filesList
     * @return
     */
    private static boolean isFile(List<ChannelSftp.LsEntry> filesList) {
        return (filesList.size() == 1 && !filesList.get(0).getAttrs().isDir());
    }

    /**
     * This method extracts a zip file located at localFilePath and saves the unpacked files to sftpLocalRelativePath.
     *
     * @param localFilePath
     * @param sftpLocalRelativePath
     */
    private void extractArchive(String localFilePath, String sftpLocalRelativePath) {
        getOut().println("Decompressing archive: " + localFilePath);
        ZipUtil.unpack(new File(localFilePath), new File(sftpLocalRelativePath));
        getOut().println("Archive decompressed successfully at: " + sftpLocalRelativePath);
    }

    /**
     * This method imports file(s) matching a wildcard string inputPath from an SFTP server
     *
     * @param channelSftp
     * @param inputPath
     * @param outputPath
     * @return a list containing the path of the imported file(s)
     * @throws SftpException
     * @throws IOException
     */
    private List<String> wildcardFilesDownloader(ChannelSftp channelSftp, String inputPath, String outputPath,
            String wildcardMode) throws IOException, SftpException {
        List<String> filesRelativePathName = new ArrayList<>();
        List<ChannelSftp.LsEntry> filteredFiles = null;
        switch (wildcardMode) {
            case "INCLUDE_DIRECTORIES":
                filteredFiles = listFilesAndFolders(channelSftp, inputPath);
                for (ChannelSftp.LsEntry item : filteredFiles) {

                    String inputFilePath = Paths.get(Paths.get(inputPath).getParent() == null ? ""
                                                                                              : Paths.get(inputPath)
                                                                                                     .getParent()
                                                                                                     .toString(),
                                                     item.getFilename())
                                                .toString();
                    String outputFilePath = Paths.get(outputPath, item.getFilename()).toString();
                    File localFile = new File(outputFilePath);

                    // Check if it is a file (not a directory).
                    if (!item.getAttrs().isDir()) {
                        filesRelativePathName.addAll(downloadSingleFile(channelSftp, inputFilePath, outputFilePath));

                    } else // skip '.' and '..'
                    if (!(CURRENT_FOLDER.equals(item.getFilename()) || PARENT_FOLDER.equals(item.getFilename()))) {
                        // Copy empty folder.
                        localFile.mkdirs();
                        // Enter found folder on server to read its contents and create locally.
                        filesRelativePathName.addAll(recursiveFolderDownload(channelSftp,
                                                                             inputFilePath,
                                                                             outputFilePath));
                    }
                }
                break;
            case "FILES_ONLY":
            case "NONE":
                filteredFiles = listFilesAndFolders(channelSftp, inputPath);
                for (ChannelSftp.LsEntry item : filteredFiles) {
                    String inputFilePath = Paths.get(Paths.get(inputPath).getParent() == null ? ""
                                                                                              : Paths.get(inputPath)
                                                                                                     .getParent()
                                                                                                     .toString(),
                                                     item.getFilename())
                                                .toString();
                    String outputFilePath = Paths.get(outputPath, item.getFilename()).toString();

                    // Check if it is a file (not a directory).
                    if (!item.getAttrs().isDir()) {
                        filesRelativePathName.addAll(downloadSingleFile(channelSftp, inputFilePath, outputFilePath));

                    }
                }
                break;
            default:
                throw new IllegalArgumentException("The provided wildcard search mode is unknown. Please enter a valid value");

        }
        return filesRelativePathName;
    }

    /**
     * This method checks if the input path is a file, folder or a wildcard string
     *
     * @param sftpInputPath
     * @param channelSftp
     * @return the type of the sftp input path which can be a file, folder or a wildcard string
     * @throws SftpException            if the input path is not found or permission denied
     * @throws IllegalArgumentException if the input path is not a valid Path i.e parent folders contain wildcards
     */
    private InputPathType checkInputPathType(String sftpInputPath, ChannelSftp channelSftp) throws SftpException {

        if (Paths.get(inputPath).getParent() != null &&
            Paths.get(sftpInputPath).getParent().toString().matches(WILDCARD_STRING_PATTERN)) {
            throw new IllegalArgumentException(sftpInputPath +
                                               " is not a valid path. Parent folders cannot contain wildcards.");
        }
        if (Paths.get(sftpInputPath).getFileName().toString().matches(WILDCARD_STRING_PATTERN)) {
            return InputPathType.WILDCARD;
        }
        // Do an ls on the sftp input path
        List<ChannelSftp.LsEntry> fileAndFolderList = null;
        try {
            fileAndFolderList = channelSftp.ls(sftpInputPath);
            if (isFile(fileAndFolderList)) {
                return InputPathType.FILE;
            } else {
                return InputPathType.FOLDER;
            }

        } catch (SftpException e) {
            throw new SftpException(e.id,
                                    sftpInputPath +
                                          " not found or permission denied. Please, enter a valid input path.",
                                    e.getCause());
        }
    }

    /**
     * This method creates in the data space the file tree of the input path if it not exist
     *
     * @param sftpInputPath
     * @param channelSftp
     */
    private void createFileTreeIfNotExist(String sftpInputPath, ChannelSftp channelSftp) throws SftpException {
        String fileType = "";
        switch (checkInputPathType(sftpInputPath, channelSftp)) {
            case FILE:
                fileType = "A FILE";
                break;
            case WILDCARD:
                fileType = "FILE(S) MATCHING THE WILDCARD PATTERN " + Paths.get(sftpInputPath).getFileName();
                break;
            case FOLDER:
                fileType = "A FOLDER";

                break;
        }
        if (outputPath != null && !outputPath.isEmpty() && !remoteDirExists(absoluteOutputPath)) {
            makeRemoteDirectories(Paths.get(absoluteOutputPath));
        }
        getOut().println("BEGIN IMPORTING " + fileType + " FROM THE SFTP SERVER: " + sftpUrlKey);
    }

    /**
     * This method checks if a directory exists in the data space
     *
     * @param remoteFilePath
     * @return true if the remote directory exists, false otherwise
     */
    private boolean remoteDirExists(String remoteFilePath) {
        return new File(remoteFilePath).isDirectory();
    }

    /**
     * This method recursively creates nonexisting nested folders
     * Example /folder/sub1/sub2/sub3/sub...
     *
     * @param path
     */
    private void makeRemoteDirectories(Path path) {
        new File(path.toString()).mkdirs();
    }

}
