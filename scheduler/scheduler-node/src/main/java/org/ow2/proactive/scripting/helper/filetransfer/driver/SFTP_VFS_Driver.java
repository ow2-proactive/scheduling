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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerSCP;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;


/**
 * Simple implementation of SFTP protocol based on Apache VFS
 * TODO: Does not work for large Files
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SFTP_VFS_Driver implements FileTransfertDriver {

    private StandardFileSystemManager manager = null;

    DefaultFileSystemManager fsManager;

    //--- Information connection ---
    private String _host;
    private String _user;
    private String _pass;
    private int _port;

    private String sftpUri = "";
    private FileSystemOptions opts;

    public void init(FileTransfertInitializer myInit) {
        debug("init " + this.getClass().getName());
        FileTransfertInitializerSCP connexionParamaters = (FileTransfertInitializerSCP) myInit;
        _host = connexionParamaters.getHost();
        _user = connexionParamaters.getUser();
        _pass = connexionParamaters.getPassword();
        _port = connexionParamaters.getPort();
    }

    public void getFile(String remotePath, String destFolderPath) throws Exception {

        connect();
        debug("Getting file " + remotePath + " to local folder " + destFolderPath);

        String fileName = (new File(remotePath).getName());
        String localPath = destFolderPath + File.separator + fileName;

        // we first set strict key checking off
        FileSystemOptions fsOptions = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
        // now we create a new filesystem manager

        // the url is of form sftp://user:pass@host/remotepath/
        String uri = "sftp://" + _user + ":" + _pass + "@" + _host + "/" + remotePath;
        // get file object representing the local file
        FileObject fo = fsManager.resolveFile(uri, fsOptions);

        // open input stream from the remote file
        BufferedInputStream is = new BufferedInputStream(fo.getContent().getInputStream());
        // open output stream to local file
        OutputStream os = new BufferedOutputStream(new FileOutputStream(localPath));
        int c;
        // do copying
        while ((c = is.read()) != -1) {
            os.write(c);
        }
        os.close();
        is.close();
        // close the file object
        fo.close();

        debug("File copied " + remotePath + " to local folder " + destFolderPath);

        // NOTE: if you close the file system manager, you won't be able to
        // use VFS again in the same VM. If you wish to copy multiple files,
        // make the fsManager static, initialize it once, and close just
        // before exiting the process.
        // fsManager.close();
        //System.out.println("Finished copying the file");
        disconnect();
    }

    public void putFile(String localPathFile, String remoteFolder) throws Exception {
        if (remoteFolder == "")
            remoteFolder = ".";

        debug("Putting file " + localPathFile + " to " + remoteFolder);

        //--Setup the SCP connection
        connect();

        //--Define paths
        //		String localFolder = FileTransfertUtils.getFolderFromPathfile(localPathFile);
        String fileName = new File(localPathFile).getName();

        // we first set strict key checking off
        FileSystemOptions fsOptions = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
        // now we create a new filesystem manager

        // the url is of form sftp://user:pass@host/remotepath/
        String uri = "sftp://" + _user + ":" + _pass + "@" + _host + "/" + remoteFolder + "/" + fileName;
        // get file object representing the local file
        FileObject fo = fsManager.resolveFile(uri, fsOptions);
        fo.createFile();
        OutputStream os = fo.getContent().getOutputStream();
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(new File(localPathFile)));

        int c;
        // do copying
        while ((c = is.read()) != -1) {
            os.write(c);
        }
        os.close();
        is.close();
        fo.close();

        debug("File copied :" + localPathFile + " to " + remoteFolder);

        //--Logout and disconnect
        disconnect();

    }

    public ArrayList<String> list(String remoteFolder) throws Exception {
        //--Setup the FTP connection
        connect();

        //--Set remote folder to current
        /*	if (remoteFolder == "") {
        remoteFolder = ".";
        }
        System.out.println("Reading of = "+remoteFolder);

        FTPFile[] ftpFiles = ftp.listFiles(remoteFolder);*/
        ArrayList<String> files = new ArrayList<String>();
        /*
        for (int i = 0; i < ftpFiles.length; i++) {
        files.add(ftpFiles[i].getName());
        System.out.println("=>" + ftpFiles[i].getName());
        }*/

        return files;
    }

    /************************************************************************
     * Connect and disconnect methods
     ************************************************************************/
    private void connect() throws Exception {
        //		sftpUri = "sftp://"+_user+":"+_pass+"@"+_host;
        //		opts = new FileSystemOptions();
        //		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
        //		manager = new StandardFileSystemManager();
        //		manager.init();

        fsManager = (DefaultFileSystemManager) VFS.getManager();

    }

    private void disconnect() throws Exception {
        if (manager != null)
            manager.close();

    }

    public void getFolder(String remoteFolderPath, String localFolderPath) throws Exception {
        // TODO Auto-generated method stub
        throw new Exception("This method is not implemented by the " + this.getClass() + " driver.");
    }

    public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception {
        // TODO Auto-generated method stub
        throw new Exception("This method is not implemented by the " + this.getClass() + " driver.");
    }

    public void getFiles(List<String> files, String localFolder) throws Exception {
        Iterator<String> it = files.iterator();
        while (it.hasNext()) {
            String file = it.next();
            this.getFile(file, localFolder);
        }
    }

    public void putFiles(List<String> localFilePaths, String remoteFolder) throws Exception {
        Iterator<String> it = localFilePaths.iterator();
        while (it.hasNext()) {
            String file = it.next();
            this.putFile(file, remoteFolder);
        }
    }

    private void debug(String msg) {
        System.out.println(this.getClass().getSimpleName() + ": " + msg);
    }

}
