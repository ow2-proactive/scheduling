package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.ow2.proactive.scripting.helper.filetransfer.FileTransfertUtils;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerSCP;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;



/***
 * Simple implementation of SFTP protocol based on Apache VFS
 * TODO: Does not work for large Files
 * @author esalagea
 *
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
	
	
	@Override
    /** Retrieve connection parameters contains into the Initializer
     * @param myInit the Initializer used to set connection informations
     */
	public void init(FileTransfertInitializer myInit) {
		debug("init "+this.getClass().getName());
		FileTransfertInitializerSCP connexionParamaters = (FileTransfertInitializerSCP) myInit;
		_host = connexionParamaters.getHost();
		_user = connexionParamaters.getUser();
		_pass = connexionParamaters.getPassword();
		_port = connexionParamaters.getPort();
	}

	
	@Override
    /** get a file from remote to local
     * @param remotePathFile the remote path like "myRmFld/myFile.txt"
     * @param localFolder the destination path where the file must be copied (empty string considered like current folder)
     * @param value will be compared
     */
	public void getFile(String remotePath, String destFolderPath) throws Exception{
		
		connect();
		debug ("Getting file "+remotePath+ " to local folder "+destFolderPath);
		
				
			String fileName = (new File(remotePath).getName());
			String localPath = destFolderPath+File.separator+fileName;
		
		
		
		    // we first set strict key checking off
		    FileSystemOptions fsOptions = new FileSystemOptions();
		    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
		            fsOptions, "no");
		    // now we create a new filesystem manager
		    
		    // the url is of form sftp://user:pass@host/remotepath/
		    String uri = "sftp://" + _user + ":" + _pass + "@" + _host
		            + "/" 
		            + remotePath;
		    // get file object representing the local file
		    FileObject fo = fsManager.resolveFile(uri, fsOptions);

		    
		    // open input stream from the remote file
		    BufferedInputStream is = new BufferedInputStream(fo.getContent().getInputStream());
		    // open output stream to local file
		    OutputStream os = new BufferedOutputStream(new FileOutputStream(
		            localPath));
		    int c;
		    // do copying
		    while ((c = is.read()) != -1) {
		        os.write(c);
		    }
		    os.close();
		    is.close();
		    // close the file object
		    fo.close();
		
		    debug ("File copied "+remotePath+ " to local folder "+destFolderPath);
		    
		    // NOTE: if you close the file system manager, you won't be able to 
		    // use VFS again in the same VM. If you wish to copy multiple files,
		    // make the fsManager static, initialize it once, and close just
		    // before exiting the process.
		   // fsManager.close();
		    //System.out.println("Finished copying the file");
		    disconnect();
		}
	
	
	
	
	
	@Override
	public void putFile(String localPathFile, String remoteFolder) throws Exception {
		if(remoteFolder == "")
			remoteFolder = ".";
		
		debug("Putting file " + localPathFile + " to " + remoteFolder);
		
		//--Setup the SCP connection
		connect();
		
		
		//--Define paths
//		String localFolder = FileTransfertUtils.getFolderFromPathfile(localPathFile);
		String fileName = new File(localPathFile).getName();
		

		
	    // we first set strict key checking off
	    FileSystemOptions fsOptions = new FileSystemOptions();
	    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
	            fsOptions, "no");
	    // now we create a new filesystem manager
	    
	    // the url is of form sftp://user:pass@host/remotepath/
	    String uri = "sftp://" + _user + ":" + _pass + "@" + _host
	            + "/" 
	            + remoteFolder
	            + "/"
	            +fileName;
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
	    
	    
		debug ("File copied :"+ localPathFile + " to " + remoteFolder);  
		
		
		

		//--Logout and disconnect
		disconnect();
		
				
	}
	
	@Override
    /** List of files & folders name which a directory contains (dont change the working directory)
     * @param remoteFolder folder which must be read
     * @return List of files & folders name which a directory contains
     */
	public ArrayList<String> list(String remoteFolder) throws Exception {		
		//--Setup the FTP connection
		connect();
		
		//--Set remote folder to current
	/*	if (remoteFolder == "") {
			remoteFolder = ".";
		}
		System.out.println("Reading of = "+remoteFolder);

		FTPFile[] ftpFiles = ftp.listFiles(remoteFolder);*/
		ArrayList<String>files = new ArrayList<String>();
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
	private void connect() throws Exception{
//		sftpUri = "sftp://"+_user+":"+_pass+"@"+_host;
//		opts = new FileSystemOptions();
//		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
//		manager = new StandardFileSystemManager();
//		manager.init();   

			fsManager= (DefaultFileSystemManager) VFS.getManager();
	
	}
	
	private void disconnect() throws Exception{
		if (manager!=null)
			manager.close();
	
	
	}

	@Override
	public void getFolder(String remoteFolderPath, String localFolderPath)
			throws Exception {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public void putFolder(String localFolderPath, String remoteFolderPath)
			throws Exception {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}


	@Override
	public void getFiles(List<String> files, String localFolder)
			throws Exception {
		Iterator<String> it = files.iterator();
		while (it.hasNext())
		{
			String file = it.next();
			this.getFile(file,localFolder);
		}
	}


	@Override
	public void putFiles(List<String> localFilePaths, String remoteFolder)
			throws Exception {
		Iterator<String> it = localFilePaths.iterator();
		while (it.hasNext())
		{
			String file = it.next();
			this.putFile(file, remoteFolder);
		}
	}

	
	private void debug (String msg)
	{
		System.out.println(this.getClass().getSimpleName()+": "+msg);
	}
	
	
}
