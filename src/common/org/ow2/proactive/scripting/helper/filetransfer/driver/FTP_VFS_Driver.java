/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.ow2.proactive.scripting.helper.filetransfer.FileTransfertUtils;
import org.ow2.proactive.scripting.helper.filetransfer.exceptions.AuthentificationFailedException;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerFTP;


/**
 * FTP_VFS_Driver...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class FTP_VFS_Driver implements FileTransfertDriver {
    private FTPClient ftpClient = null;

    //--- Information connection ---
    private String host = "";
    private String user = "";
    private String pass = "";
    private boolean useBinaryMode;
    private boolean usePassiveMode;
    private int port = 21;

    /** Retrieve connection parameters contained into the Initializer
     * @param myInit the Initializer used to set connection informations
     */
    public void init(FileTransfertInitializer myInit) {
        //System.out.println("init vfs ftp");
        FileTransfertInitializerFTP connexionParamaters = (FileTransfertInitializerFTP) myInit;
        host = connexionParamaters.getHost();
        user = connexionParamaters.getUser();
        pass = connexionParamaters.getPassword();
        port = connexionParamaters.getPort();
        useBinaryMode = connexionParamaters.isBinaryType();
        usePassiveMode = connexionParamaters.isPassiveMode();
    }

    public void getFile(String remoteFilePath, String localFolder) throws Exception {
        this.getFile(remoteFilePath, localFolder, true);
    }

    /**
     * Get a file from the remote location to the local folder
     *
     * @param remoteFilePath
     * @param localFolder
     * @param manageConnection - if set to true, it will initiate a connection by calling the connect() method, otherwise it considers that the caller has already initiated a connection
     * @throws Exception
     */
    public void getFile(String remoteFilePath, String localFolder, boolean manageConnection) throws Exception {
        debug("Get " + remoteFilePath + " in local folder " + localFolder);

        //--Setup the FTP connection
        if (manageConnection)
            connect();
        else {
            //connection should already be opened at this time
            if ((ftpClient == null) || (!ftpClient.isConnected()))
                throw new Exception("there is no connection to the ftp server");
        }

        File localFolderFile = new File(localFolder);
        if (!localFolderFile.exists()) {
            localFolderFile.mkdirs();
            debug("Created local folder " + localFolderFile);
        }

        //--Define fileName destination
        String fileName = FileTransfertUtils.getFilenameFromPathfile(remoteFilePath);

        //--Local destination file
        File file = new File(localFolder + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);

        if (!ftpClient.retrieveFile(remoteFilePath, fos)) {
            throw new FileNotFoundException("Could not find file " + remoteFilePath + " on ftp server at " +
                this.host + ". Make sure the path is relative to the specified user's ftp home folder. ");
        }
        fos.flush();
        fos.close();

        if (manageConnection)
            disconnect();

        //--Logout and disconnect
    }

    /**
     *
     * @param localFilePath - absolute path, on the local machine, of the file to be copied
     * @param remoteFolder - the path for the remote folder, relative to the ftp user's root folder
     * @param manageConnection - if set to true, it will initiate a connection by calling the connect() method, otherwise it considers that the caller has already initiated a connection
     * @throws Exception
     */
    private void putFile(String localFilePath, String remoteFolder, boolean manageConnection)
            throws Exception {
        if (remoteFolder == "")
            remoteFolder = ".";

        debug("put " + localFilePath + " to " + remoteFolder);

        //--Setup the FTP connection
        if (manageConnection) {
            connect();

            if (!ftpClient.changeWorkingDirectory(remoteFolder)) {
                if (!ftpClient.makeDirectory(remoteFolder)) {
                    throw new IOException("FTP: Could not create directory: " + remoteFolder);
                }
                if (!ftpClient.changeWorkingDirectory(remoteFolder)) {
                    throw new IOException("FTP: Could not change to directory: " + remoteFolder);
                }
            }
            debug("Changed to working directory " + remoteFolder);
        } else {
            //connection should already be opened at this time
            if ((ftpClient == null) || (!ftpClient.isConnected()))
                throw new Exception("there is no connection to the ftp server");
        }

        //--Define filename destination
        String fileName = FileTransfertUtils.getFilenameFromPathfile(localFilePath);

        //--Local destination file
        File file = new File(localFilePath);

        FileInputStream fis = new FileInputStream(file);

        if (!ftpClient.storeFile(fileName, fis)) {
            throw new Exception("Could not store File on ftp server. ");
        }
        //--Logout and disconnect
        if (manageConnection)
            disconnect();
    }

    public void putFile(String localPathFile, String remoteFolder) throws Exception {

        this.putFile(localPathFile, remoteFolder, true);

    }

    /**
     *
     * @param remoteFolder
     * @param manageConnection
     * @return
     * @throws Exception
     */
    public ArrayList<String> list(String remoteFolder, boolean manageConnection) throws Exception {

        //--Setup the FTP connection
        if (manageConnection)
            connect();
        else {
            //connection should already be opened at this time
            if ((ftpClient == null) || (!ftpClient.isConnected()))
                throw new Exception("there is no connection to the ftp server");
        }

        //--Change the remote folder
        if (remoteFolder == "") {
            remoteFolder = ".";
        }
        System.out.println("Reading of = " + remoteFolder);

        FTPFile[] ftpFiles = ftpClient.listFiles(remoteFolder);
        ArrayList<String> files = new ArrayList<String>();

        for (int i = 0; i < ftpFiles.length; i++) {
            files.add(ftpFiles[i].getName());
            //System.out.println("=>" + ftpFiles[i].getName());
        }

        if (manageConnection)
            disconnect();

        return files;

    }

    /** List of files & folders name which a directory contains (dont change the working directory)
     * @param remoteFolder folder which must be read
     * @return List of files & folders name which a directory contains
     */
    public ArrayList<String> list(String remoteFolder) throws Exception {
        return this.list(remoteFolder, true);
    }

    /**
     * Recursively copies all files in the remoteFodlerPath to the localFodlerPath
     * @param remoteFolderPath
     * @param localFolderPath
     */
    public void getFolder(String remoteFolderPath, String localFolderPath) throws Exception {
        this.connect();
        ftpClient.changeWorkingDirectory(remoteFolderPath);
        this.getCurrentFolder(localFolderPath);
        this.disconnect();
    }

    private void getCurrentFolder(String localFolderPath) throws Exception {
        ArrayList<String> files = this.list(".", false);
        Iterator<String> it = files.iterator();
        while (it.hasNext()) {
            String file = it.next();
            if (ftpClient.changeWorkingDirectory(file)) {
                getCurrentFolder(localFolderPath + File.separator + file);
                ftpClient.changeToParentDirectory();
            } else
                this.getFile(file, localFolderPath, false);
        }
    }

    public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception {
        connect();
        if (!ftpClient.changeWorkingDirectory(remoteFolderPath)) {
            ftpClient.makeDirectory(remoteFolderPath);
            ftpClient.changeWorkingDirectory(remoteFolderPath);
        }
        this.putFolder(localFolderPath);
        disconnect();
    }

    /**
     * Recursively copies all files and folders in the localFolderPath to the current ftp folder
     * @param localFolderPath
     * @throws Exception
     */
    private void putFolder(String localFolderPath) throws Exception {
        File folder = new File(localFolderPath);
        String[] files = folder.list();
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            File f = new File(localFolderPath + File.separator + file);
            if (f.isDirectory()) {
                ftpClient.makeDirectory(f.getName());
                ftpClient.changeWorkingDirectory(f.getName());
                putFolder(f.getAbsolutePath());
                ftpClient.changeToParentDirectory();
            } else {
                this.putFile(f.getAbsolutePath(), ".", false);
            }
        }
    }

    /************************************************************************
     * Connect and disconnect methods
     ************************************************************************/
    private void connect() throws Exception {
        //--Connection
        ftpClient = new FTPClient();
        ftpClient.connect(host, port);

        //--Get reply code to check if FTP connection is setup
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            ftpClient.disconnect();
            throw new Exception("FTP server refused connection.");
        }

        debug("Connected to remote host " + host);

        //--Login using user and password
        if (!ftpClient.login(user, pass)) {
            throw new AuthentificationFailedException(
                "Username and password do not match. Could not login to ftp server at " + host);
        }

        if (useBinaryMode) {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            debug("Filetype set to binary type");
        } else {
            ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
            debug("Filetype set to ascii type");
        }

        if (usePassiveMode) {

            ftpClient.enterLocalPassiveMode();
            debug("Enetring passive mode");
        }
    }

    public void disconnect() {

        if (!ftpClient.isConnected()) {
            System.out.println("ftp client is not connected");
            return;
        }

        try {
            if (!ftpClient.logout()) {
                System.out.println("Could not logout. ");
            }
            ftpClient.disconnect();
        } catch (IOException e) {
            System.out.println("disconnect from ftp server failed. ");
            e.printStackTrace();
        }

        debug("Disconnected from host " + host);
    }

    public void getFiles(List<String> files, String localFolder) throws Exception {
        this.connect();
        Iterator<String> it = files.iterator();
        while (it.hasNext()) {
            String file = it.next();
            this.getFile(file, localFolder, false);
        }

        this.disconnect();
    }

    public void putFiles(List<String> localFilePaths, String remoteFolder) throws Exception {
        this.connect();
        if (!ftpClient.changeWorkingDirectory(remoteFolder)) {
            if (!ftpClient.makeDirectory(remoteFolder)) {
                throw new IOException("FTP: Could not create directory: " + remoteFolder);
            }
            if (!ftpClient.changeWorkingDirectory(remoteFolder)) {
                throw new IOException("FTP: Could not change to directory: " + remoteFolder);
            }
        }
        debug("Changed to directory: " + remoteFolder);

        Iterator<String> it = localFilePaths.iterator();
        while (it.hasNext()) {
            String file = it.next();
            this.putFile(file, remoteFolder, false);
        }
        this.disconnect();
    }

    private void debug(String msg) {
        System.out.println(this.getClass().getName() + ": " + msg);
    }

}
