package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.ow2.proactive.scripting.helper.filetransfer.FileTransfertUtils;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializerSCP;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.trilead.ssh2.Connection;


public class VFSSCPDriverOld implements FileTransfertDriver {
	//--- Required objects to the connection ---
	private Connection conn = null;
	private StandardFileSystemManager manager = null;
	
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
		System.out.println("init vfs scp");
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
	public void getFile(String remotePathFile, String localFolder)throws Exception {
		System.out.println("Use FileTransfertDriverVFSSCP to get " + remotePathFile + " in " + localFolder);
		
		//--Setup the SCP connection
		connect();
		
		//--Define paths
		//String remoteFolder = FileTransfertUtils.getFolderFromPathfile(remotePathFile);
		String fileName = FileTransfertUtils.getFilenameFromPathfile(remotePathFile);
		
		
		FileObject localFileObject = manager.resolveFile(localFolder + File.separator + fileName);
		
		FileObject remoteFileObject = manager.resolveFile(sftpUri + "/" + remotePathFile, opts);
			     
		localFileObject.copyFrom(remoteFileObject, Selectors.SELECT_SELF);
	
		System.out.println("getFile SCP");
		//--Logout and disconnect
		disconnect();
	}

	

	

	

	
	
	@Override
	public void putFile(String localPathFile, String remoteFolder) throws Exception {
		if(remoteFolder == "")
			remoteFolder = ".";
		
		System.out.println("Use FileTransfertDriverVFSFTP to put " + localPathFile + " to " + remoteFolder);
		
		//--Setup the SCP connection
		connect();
		
		//--Define paths
		String localFolder = FileTransfertUtils.getFolderFromPathfile(localPathFile);
		String fileName = FileTransfertUtils.getFilenameFromPathfile(localPathFile);
		
	

		//--Logout and disconnect
		disconnect();
		
		System.out.println("end putFile");		
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
		sftpUri = "sftp://"+_user+":"+_pass+"@"+_host;
		opts = new FileSystemOptions();
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
		manager = new StandardFileSystemManager();
		manager.init();   
	}
	
	private void disconnect() throws Exception{
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
		// TODO Auto-generated method stub
		throw new Exception("This method is not implemented by the "+this.getClass()+" driver.");
	}


	@Override
	public void putFiles(List<String> localFilePaths, String remoteFolder)
			throws Exception {
		// TODO Auto-generated method stub
		throw new Exception("This method is not implemented by the "+this.getClass()+" driver.");
	}

}
