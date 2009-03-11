package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.exceptions.AuthentificationFailedException;
import org.ow2.proactive.scripting.helper.filetransfer.exceptions.NotConnectedException;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerSCP;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPException;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;


public class SFTP_Trilead_Driver implements FileTransfertDriver {
    private static final int BUFSIZE = 1024;
    private SFTPv3Client sc = null;
    private Connection conn;

    private String host;
    private String user;
    private String pass;
    private int port;

    public static final int S_IFMT = 0170000; // bitmask for the file type bitfields

    public static final int S_IFDIR = 0040000; // directory

    public void init(FileTransfertInitializer myInit) {

        FileTransfertInitializerSCP connexionParamaters = (FileTransfertInitializerSCP) myInit;
        host = connexionParamaters.getHost();
        user = connexionParamaters.getUser();
        pass = connexionParamaters.getPassword();
        port = connexionParamaters.getPort();
    }

    private void connect() throws IOException, AuthentificationFailedException {
        conn = new Connection(host, port);
        conn.connect();
        if (!conn.authenticateWithPassword(user, pass)) {
            throw new AuthentificationFailedException("username and pasword do not match");
        }
        sc = new SFTPv3Client(conn);

        debug("connected to the remote host " + host + ":" + port);

    }

    private void disconnect() {
        sc.close();
        conn.close();
        debug("disconnected from the remote host " + host);
    }

    public void putFile(String localFilePath, String remoteFolder) throws Exception {
        putFile(localFilePath, remoteFolder, true);
    }

    public void putFile(String localFilePath, String remoteFolder, boolean ownConnection) throws Exception {

        debug("Putting file " + localFilePath + " to remote folder " + remoteFolder);

        if (ownConnection)
            connect();
        this.createRemoteDirs(remoteFolder);

        File localFile = new File(localFilePath);

        String remoteFilePath = remoteFolder + "/" + localFile.getName();
        //	sc.mkdir(remoteFolder, 0777);
        sc.createFile(remoteFilePath);
        SFTPv3FileHandle fh = sc.openFileRW(remoteFilePath);
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(localFile));
        byte[] buf = new byte[BUFSIZE];
        int count = 0;
        int bufsiz = 0;
        while ((bufsiz = is.read(buf)) != -1) {
            //here the writing is made on the remote file
            sc.write(fh, (long) count, buf, 0, bufsiz);
            count += bufsiz;
        }
        is.close();

        if (ownConnection)
            disconnect();
    }

    public void getFile(String remoteFilePath, String localFolder) throws Exception {
        this.getFile(remoteFilePath, localFolder, true);
    }

    public void getFile(String remoteFilePath, String localFolder, boolean ownConnection) throws Exception {
        debug("Getting file " + remoteFilePath + " to local folder " + localFolder);

        if (ownConnection)
            connect();

        String fileName = (new File(remoteFilePath)).getName();
        File localFile = new File(localFolder + File.separator + fileName);

        SFTPv3FileHandle fh = sc.openFileRO(remoteFilePath);
        BufferedOutputStream bfout = new BufferedOutputStream(new FileOutputStream(localFile));
        byte[] buf = new byte[BUFSIZE];
        int count = 0;
        int bufsiz = 0;

        while ((bufsiz = sc.read(fh, count, buf, 0, BUFSIZE)) != -1) {
            bfout.write(buf, 0, bufsiz);
            count += bufsiz;
        }

        bfout.close();

        if (ownConnection)
            disconnect();
    }

    public ArrayList<String> list(String remoteFolder) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void getFolder(String remoteFolderPath, String localFolderPath) throws Exception {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception {
        // TODO Auto-generated method stub
        throw new NotImplementedException();
    }

    public void getFiles(List<String> files, String localFolder) throws Exception {

        connect();
        Iterator<String> it = files.iterator();
        while (it.hasNext()) {
            String file = it.next();
            this.getFile(file, localFolder, false);
        }

        disconnect();
    }

    public void putFiles(List<String> localFilePaths, String remoteFolder) throws Exception {

        connect();
        Iterator<String> it = localFilePaths.iterator();
        while (it.hasNext()) {
            String file = it.next();
            this.putFile(file, remoteFolder, false);
        }
        disconnect();
    }

    /**
     * create folders  on the remote host
     * This considers that a connection has already been opend
     * @param path
     * @throws IOException
     * @throws AuthentificationFailedException
     * @throws NotConnectedException
     */
    private void createRemoteDirs(String path) throws IOException, AuthentificationFailedException,
            NotConnectedException {
        //recurse in parent folders
        if (this.conn == null) {
            throw new NotConnectedException("A connection should be opend in order to call this function");
        }

        String FS = System.getProperty("file.separator");
        int index = path.lastIndexOf(FS);
        if (index > 1) {
            createRemoteDirs(path.substring(0, index));
        }

        try {
            SFTPv3FileAttributes attribs = sc.stat(path);
            if (!((attribs.permissions & S_IFDIR) == S_IFDIR)) {
                throw new IOException(path + " is not a folder");
            }
        } catch (SFTPException e) {
            sc.mkdir(path, 0777);
            debug("Remote folder created " + path);
        }
    }

    private void debug(String msg) {
        System.out.println(this.getClass().getSimpleName() + ": " + msg);
    }

}
