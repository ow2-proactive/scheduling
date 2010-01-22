/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.exceptions.AuthentificationFailedException;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerSCP;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;


/**
 * SCP_Trilead_Driver...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SCP_Trilead_Driver implements FileTransfertDriver {

    private String host;
    private String user;
    private String pass;
    private int port;

    private Connection con;
    private SCPClient scpClient;

    public void init(FileTransfertInitializer myInit) {
        debug("init " + this.getClass().getName());
        FileTransfertInitializerSCP connexionParamaters = (FileTransfertInitializerSCP) myInit;
        host = connexionParamaters.getHost();
        user = connexionParamaters.getUser();
        pass = connexionParamaters.getPassword();
        port = connexionParamaters.getPort();
    }

    /** Tries to connect through ssh keys
     *  If it fails it will try to connect via user/password
     *  AuthentificationFailedException is thrown if both ssh keys authentification and user/password attempts fail 
     * @throws IOException
     * @throws AuthentificationFailedException 
     */
    public void connect() throws IOException, AuthentificationFailedException {
        //open a connection on the host
        try {
            con = ConnectionTools.authentificateWithKeys(user, host, port);
        } catch (Exception e) {
            debug("Could not authentificate with private/public key, trying user/password");
            con = new Connection(host, port);
            con.connect(null, 4000, 8000);
            //con.connect();
            //authentificate
            if (!con.authenticateWithPassword(user, pass)) {
                throw new AuthentificationFailedException("Authentification failed");
            } else
                debug("Authentificated with user/password");
        }
        scpClient = con.createSCPClient();
    }

    public void disconnect() {
        con.close();
        debug("disconnected from the remote host " + host);
    }

    public void getFiles(List<String> files, String destFolderPath) throws IOException,
            AuthentificationFailedException {
        connect();
        //create destination folder if it doesn't exist
        File destFolder = new File(destFolderPath);
        if (!destFolder.exists())
            destFolder.mkdirs();

        debug("Getting files " + files + " to local folder " + destFolderPath);
        scpClient.get((String[]) files.toArray(new String[0]), destFolder.getAbsolutePath());
        disconnect();
    }

    public void getFile(String remoteFilePath, String localFolder) throws IOException,
            AuthentificationFailedException {
        connect();
        File destFolder = new File(localFolder);
        if (!destFolder.exists())
            destFolder.mkdirs();

        debug("Getting file " + remoteFilePath + " to local folder " + localFolder);
        scpClient.get(remoteFilePath, localFolder);
        disconnect();
    }

    public ArrayList<String> list(String remoteFolder) throws Exception {
        throw new Exception("This method is not implemented by the " + this.getClass() + " driver.");
    }

    public void putFile(String localFilePath, String remoteFolder) throws Exception {
        connect();
        debug("Putting file " + localFilePath + " to remote folder " + remoteFolder);
        scpClient.put(localFilePath, remoteFolder);
        disconnect();
    }

    /**
     * Copy the local files in the remote folder
     * Throws an exception if the remote folder does not exist
     */
    public void putFiles(List files, String remoteFolder) throws Exception {
        //TODO: test this for windows as remote system
        connect();
        Session s = con.openSession();
        s.execCommand("mkdir " + remoteFolder);
        s.close();
        debug("putting files " + files + " to remote folder " + remoteFolder);
        scpClient.put((String[]) files.toArray(new String[0]), remoteFolder);
        disconnect();
    }

    public void getFolder(String remoteFolderPath, String localFolderPath) throws Exception {
        throw new Exception("This method is not implemented by the " + this.getClass() + " driver.");
    }

    public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception {
        // TODO Auto-generated method stub
        throw new Exception("This method is not implemented by the " + this.getClass() + " driver.");
    }

    public void debug(String msg) {
        System.out.println(this.getClass().getSimpleName() + ": " + msg);
    }

}
