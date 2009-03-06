/*********************************************************
 * User API
 ********************************************************/
package org.ow2.proactive.scripting.helper.filetransfer;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.driver.FileTransfertDriver;
import org.ow2.proactive.scripting.helper.filetransfer.exceptions.NotInitializedFileTransfertSessionException;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;

public class FileTransfertSession {
	
	//--Driver use to process commands
	private FileTransfertDriver ftDriver;
	
	
	public FileTransfertSession(FileTransfertInitializer myInit)
	{
			ftDriver = FileTransfertFactory.getDriver(myInit);
	}
	
	
	/***** Use the appropriate driver to perform *****
	 * @throws Exception */
	public void getFile(String remoteFilePath, String localFolder) throws Exception{
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");
			ftDriver.getFile(remoteFilePath, localFolder);
	}
	
	
	
	public void getFiles(List<String> files, String localFolder) throws Exception
	{
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");
			ftDriver.getFiles(files, localFolder);
	}
	
	
	
	public void putFile(String localPathFile, String remoteFolder) throws Exception {
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");
		
	
			ftDriver.putFile(localPathFile, remoteFolder);
		
	}
	
	
	public void putFiles(List<String> localFilePaths, String remoteFolder) throws Exception
	{
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");

			//--Return the files list
			ftDriver.putFiles(localFilePaths, remoteFolder);
	
	}
	
	
	public ArrayList<String> list(String remoteFolder) throws Exception{
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");

		return ftDriver.list(remoteFolder);
		
		//--Return an empty ArrayList
		//return new ArrayList<String>();
	}

	
	public void getFolder(String remoteFolderPath, String localFolderPath) throws NotInitializedFileTransfertSessionException, Exception
	{
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");
		
		
			ftDriver.getFolder(remoteFolderPath, localFolderPath);
	}
	
	
	public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception
	{
		if(ftDriver == null)
			throw new NotInitializedFileTransfertSessionException("Session is not correctly initialized");
		ftDriver.putFolder(localFolderPath, remoteFolderPath);
	}
	
	

	
	
	
	
}
