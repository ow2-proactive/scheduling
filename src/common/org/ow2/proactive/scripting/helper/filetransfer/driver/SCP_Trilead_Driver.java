package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.exceptions.AuthentificationFailedException;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerSCP;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

public class SCP_Trilead_Driver implements FileTransfertDriver {
	
	private String host;
	private String user;
	private String pass;
	private int port;
	
	private Connection con;
	private SCPClient scpClient;
	
	
	
	@Override
    /** Retrieve connection parameters contains into the Initializer
     * @param myInit the Initializer used to set connection informations
     */
	public void init(FileTransfertInitializer myInit) {
		debug("init "+this.getClass().getName());
		FileTransfertInitializerSCP connexionParamaters = (FileTransfertInitializerSCP) myInit;
		host = connexionParamaters.getHost();
		user = connexionParamaters.getUser();
		pass = connexionParamaters.getPassword();
		port = connexionParamaters.getPort();
	}
	
	
	public void connect() throws IOException, AuthentificationFailedException
	{
		//open a connection on the host
		con = new Connection(host,port);
		con.connect(null,4000,8000);
		//con.connect();
		//authentificate
		if (!con.authenticateWithPassword(user, pass))
		 {
			 throw new AuthentificationFailedException("username and pasword do not match");
		 }
		 scpClient =con.createSCPClient();
		 
	}

	
	public void disconnect()
	{
		con.close();
		debug ("disconnected from the remote host " + host);
	}

	
	
  public void getFiles(List<String> files, String destFolderPath) throws IOException, AuthentificationFailedException
	{
		connect();
	  //create destination folder if it doesn't exist
		File destFolder = new File(destFolderPath);
		if (!destFolder.exists())
			destFolder.mkdirs();

		debug ("Getting files "+files+" to local folder "+destFolderPath);
		scpClient.get((String[])files.toArray(new String[0]), destFolder.getAbsolutePath());
		disconnect();
	}
	
	
	@Override
	public void getFile(String remoteFilePath, String localFolder) throws IOException, AuthentificationFailedException {
		connect();
		File destFolder = new File(localFolder);
		if (!destFolder.exists())
			destFolder.mkdirs();
		
		debug("Getting file "+remoteFilePath+" to local folder "+localFolder);
		scpClient.get(remoteFilePath, localFolder);
		disconnect();
	}

	@Override
	public ArrayList<String> list(String remoteFolder) throws Exception {
		throw new NotImplementedException();
	}

	
	@Override
	/**
	 * Copy the local file in the remote folder
	 * Throws an exception if the remote folder does not exist
	 */
	public void putFile(String localFilePath, String remoteFolder)
			throws Exception {
		connect();
		debug ("Putting file "+localFilePath+" to remote folder "+remoteFolder);
		scpClient.put(localFilePath, remoteFolder);
		disconnect();
	}
	

	/**
	 * Copy the local files in the remote folder
	 * Throws an exception if the remote folder does not exist
	 */
	public void putFiles(List files, String remoteFolder)
			throws Exception {
		
		connect();
		Session s= con.openSession();
		s.execCommand("mkdir "+remoteFolder);
	
//		InputStream stdout = new StreamGobbler(s.getStdout());
//
//		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//		String line = br.readLine();
//		
//		while (line!=null)
//		{
//			System.out.println(line);
//			line = br.readLine();
//	
//		}
		s.close();
		debug ("putting files "+files+" to remote folder "+remoteFolder);
		scpClient.put((String[])files.toArray(new String[0]), remoteFolder);
		disconnect();
	}

	
	
	
	@Override
	public void getFolder(String remoteFolderPath, String localFolderPath)
			throws Exception {
		throw new Exception("This method is not implemented by the "+this.getClass()+" driver.");
	}

	@Override
	public void putFolder(String localFolderPath, String remoteFolderPath)
			throws Exception {
		// TODO Auto-generated method stub
		throw new Exception("This method is not implemented by the "+this.getClass()+" driver.");
	}

	
	

	private void debug (String msg)
	{
		System.out.println(this.getClass().getSimpleName()+": "+msg);
	}
	
}
