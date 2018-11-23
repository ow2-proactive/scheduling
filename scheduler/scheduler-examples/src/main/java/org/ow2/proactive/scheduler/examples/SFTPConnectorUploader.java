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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

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
public class SFTPConnectorUploader extends JavaExecutable {

    private static final String INPUT_PATH = "inputPath";

    private static final String DEFAULT_SFTP_HOST = "localhost";

    private static final int DEFAULT_SFTP_PORT = 22;

    /**
     * The SFTP_URL_KEY = "sftp://<username>@<host>" is used as a key to store sftp passwords in 3rd party credentials
     */
    private static final String SFTP_URL_KEY = "sftp://<username>@<host>";

    private static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    private String host = DEFAULT_SFTP_HOST;

    private int port = DEFAULT_SFTP_PORT;

    private String absoluteInputPath;

    private String outputPath;

    private String username;

    private String sftpUrlKey;

    private String password;

    private String wildcardSearchMode;

    private static final String WILDCARD_STRING_PATTERN = ".*[\\*\\?]+.*";

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
        if (args.containsKey(INPUT_PATH) && !args.get(INPUT_PATH).toString().isEmpty()) {
            absoluteInputPath = Paths.get(getLocalSpace(), args.get(INPUT_PATH).toString()).toString();
        } else {
            throw new IllegalArgumentException("Please specify a local relative path. Empty value is not allowed.");
        }
        if (args.containsKey("outputPath")) {
            outputPath = args.get("outputPath").toString();
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

        ChannelSftp channelsftp = null;
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
            channelsftp = (ChannelSftp) channel;

            createFileTreeIfNotExist(absoluteInputPath, channelsftp);

            getOut().println("Exporting file(s) from " + absoluteInputPath +
                             " to the SFTP server under the destination path " + outputPath);
            switch (checkInputPathType(absoluteInputPath)) {
                case FILE:
                    String destinationPath;
                    if (outputPath == null || outputPath.isEmpty()) {
                        destinationPath = Paths.get(absoluteInputPath).getFileName().toString();
                    } else {
                        destinationPath = Paths.get(outputPath, Paths.get(absoluteInputPath).getFileName().toString())
                                               .toString();
                    }
                    //export the input file to sftp server
                    filesRelativePathName = fileUploader(channelsftp, absoluteInputPath, destinationPath);
                    break;
                case WILDCARD:
                    if (outputPath == null) {
                        outputPath = "";
                    }
                    filesRelativePathName = wildcardFilesUploader(channelsftp,
                                                                  absoluteInputPath,
                                                                  outputPath,
                                                                  wildcardSearchMode);
                    break;
                case FOLDER:
                    if (outputPath == null) {
                        outputPath = "";
                    }
                    // recursive folder content export to sftp server
                    filesRelativePathName = recursiveFolderUpload(channelsftp, absoluteInputPath, outputPath);
                    break;
            }
            getOut().println("END EXPORTING TO SFTP SERVER");

        } catch (JSchException e) {
            throw new JSchException("An error occurred while trying to connect to SFTP server: ", e.getCause());
        } catch (SftpException e) {
            throw new SftpException(e.id,
                                    "FILE not found, permission denied or operation not supported by SFTP server.",
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
     * @param channelSftp
     * @param sourcePath
     * @param destinationPath
     * @return a list containing the path of the exported files
     * @throws SftpException
     * @throws FileNotFoundException
     */
    private List<String> recursiveFolderUpload(ChannelSftp channelSftp, String sourcePath, String destinationPath)
            throws SftpException, FileNotFoundException {

        List<String> filesRelativePathName = new ArrayList<>();
        File sourceFile = new File(sourcePath);
        String outputFilePath = Paths.get(destinationPath, sourceFile.getName()).toString();
        if (sourceFile.isFile()) {
            // copy if it is a file
            getOut().println("Exporting " + sourcePath);
            channelSftp.put(new FileInputStream(sourceFile), outputFilePath, ChannelSftp.OVERWRITE);
            getOut().println("FILE exported successfully to " + outputFilePath);
            filesRelativePathName.add(destinationPath);

        } else {

            File[] files = sourceFile.listFiles();

            if (!sourceFile.isHidden()) {

                // create a directory if it does not exist
                if (!remoteDirExists(channelSftp, outputFilePath)) {
                    channelSftp.mkdir(outputFilePath);
                }

                for (File f : files) {
                    filesRelativePathName.addAll(recursiveFolderUpload(channelSftp,
                                                                       f.getAbsolutePath(),
                                                                       outputFilePath));
                }
            }
        }
        return filesRelativePathName;
    }

    /**
     * This method exports a local file to an SFTP server
     *
     * @param channelSftp
     * @param sourcePath
     * @param destinationPath
     * @return a list containing the path of the exported file
     * @throws SftpException
     * @throws FileNotFoundException
     */
    private List<String> fileUploader(ChannelSftp channelSftp, String sourcePath, String destinationPath)
            throws SftpException, FileNotFoundException {

        List<String> filesRelativePathName = new ArrayList<>();
        File sourceFile = new File(sourcePath);
        getOut().println("Exporting " + sourcePath);
        channelSftp.put(new FileInputStream(sourceFile), destinationPath, ChannelSftp.OVERWRITE);
        getOut().println("FILE exported successfully to " + destinationPath);
        filesRelativePathName.add(destinationPath);
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
     * This method exports file(s) matching a wildcard string sourcePath to an SFTP server
     *
     * @param channelSftp
     * @param sourcePath
     * @param destinationPath
     * @return a list containing the path of the exported file(s)
     */
    private List<String> wildcardFilesUploader(ChannelSftp channelSftp, String sourcePath, String destinationPath,
            String wildcardMode) {
        List<String> filesRelativePathName = new ArrayList<>();
        List<File> filteredFiles = null;
        switch (wildcardMode) {
            case "INCLUDE_DIRECTORIES":
                filteredFiles = listFilesAndDirs(sourcePath);
                break;
            case "FILES_ONLY":
            case "NONE":
                filteredFiles = (List<File>) FileUtils.listFiles(new File(Paths.get(sourcePath).getParent().toString()),
                                                                 new WildcardFileFilter(Paths.get(sourcePath)
                                                                                             .getFileName()
                                                                                             .toString()),
                                                                 null);
                break;
            default:
                throw new IllegalArgumentException("The provided wildcard search mode is unknown. Please enter a valid value");

        }
        filteredFiles.forEach(file -> {

            try {
                if (file.isDirectory()) {
                    filesRelativePathName.addAll(recursiveFolderUpload(channelSftp, file.getPath(), destinationPath));
                } else {
                    filesRelativePathName.addAll(fileUploader(channelSftp,
                                                              file.getPath(),
                                                              Paths.get(destinationPath, file.getName()).toString()));
                }
            } catch (SftpException | FileNotFoundException e) {
                throw new RuntimeException("FILE not found, permission denied or operation not supported by SFTP server.",
                                           e);
            }
        });
        return filesRelativePathName;
    }

    /**
     * This method returns a list containing file(s) and director(ies) matching a wildcard string pattern to an SFTP server
     *
     * @param pattern
     * @return a list containing the path of file(s) and directories matching the a wildcard string pattern
     */
    private List<File> listFilesAndDirs(String pattern) {
        return Arrays.asList(new File(Paths.get(pattern)
                                           .getParent()
                                           .toString()).listFiles(file -> FilenameUtils.wildcardMatch(file.getName(),
                                                                                                      Paths.get(pattern)
                                                                                                           .getFileName()
                                                                                                           .toString(),
                                                                                                      IOCase.SENSITIVE)));
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
     * This method checks if the input path is a file, folder or a wildcard string
     *
     * @param inputPath
     * @return the type of the input path which can be a file, folder or a wildcard string
     * @throws IllegalArgumentException if the input path does not exist or parent folders contain wildcards.
     */
    private InputPathType checkInputPathType(String inputPath) {
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            if (Paths.get(inputPath).getParent() != null &&
                Paths.get(inputPath).getParent().toString().matches(WILDCARD_STRING_PATTERN)) {
                throw new IllegalArgumentException(inputFile +
                                                   " is not a valid path. Parent folders cannot contain wildcards.");
            }
            if (Paths.get(inputPath).getFileName().toString().matches(WILDCARD_STRING_PATTERN)) {
                return InputPathType.WILDCARD;
            }
            throw new IllegalArgumentException(inputFile + " not found. Please, enter a valid input path.");
        } else {
            if (inputFile.isFile()) {
                return InputPathType.FILE;
            } else {
                return InputPathType.FOLDER;
            }
        }
    }

    /**
     * This method creates in the FTP server the file tree of the input path if it not exist
     *
     * @param absoluteInputPath
     * @param channelSftp
     */
    private void createFileTreeIfNotExist(String absoluteInputPath, ChannelSftp channelSftp) throws SftpException {
        String fileType = "";
        switch (checkInputPathType(absoluteInputPath)) {
            case FILE:
                fileType = "A FILE";
                break;
            case WILDCARD:
                fileType = "FILE(S) MATCHING THE WILDCARD PATTERN " + Paths.get(absoluteInputPath).getFileName();
                break;
            case FOLDER:
                fileType = "A FOLDER";

                break;
        }

        if (outputPath != null && !outputPath.isEmpty() && !remoteDirExists(channelSftp, outputPath)) {
            makeRemoteDirectories(channelSftp, Paths.get(outputPath));
        }

        getOut().println("BEGIN EXPORTING " + fileType + " TO SFTP SERVER: " + sftpUrlKey);
    }

}
