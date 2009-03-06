package org.ow2.proactive.scripting.helper.filetransfer.initializer;

import org.ow2.proactive.scripting.helper.filetransfer.driver.FTP_VFS_Driver;
import org.ow2.proactive.scripting.helper.filetransfer.driver.FileTransfertDriver;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertProtocols.Protocol;

/**
 * Initializer class for an ftp connection 
 * By default the port is 21, the file transfer type is ASCII (binaryType is set to false), and the connection mode is active
 * @author esalagea
 *
 */
public class FileTransfertInitializerFTP implements FileTransfertInitializer {
	private String host = "";
	private String user = "";
	private String pass = "";
	
	
	private int _port = 21;
	private boolean useBinaryType=false;
	
	private boolean usePassiveMode=false;
	
	//--Trilead is the default driver

	private Class<? extends FileTransfertDriver> _driverClass =  FTP_VFS_Driver.class;
	
	
	public FileTransfertInitializerFTP(String _host, String _user, String _pass){
		host = _host;
		user = _user;
		pass = _pass;
		
	}		
	
	public FileTransfertInitializerFTP(String _host, String _user, String _pass, Class<? extends FileTransfertDriver> driver){
		host = _host;
		user = _user;
		pass = _pass;
		_driverClass = driver;
		
	}		
	
	
	
	public FileTransfertInitializerFTP(String host, String user, String pass, int port){			
		this(host, user, pass);
		_port = port;
	}
	
	public FileTransfertInitializerFTP(String host, String user, String pass, int port, Class<? extends FileTransfertDriver> driver){
		this(host, user, pass, port);
		_driverClass = driver;
	}

	public Class<? extends FileTransfertDriver> getDriverClass() {
		return _driverClass;
	}

	public Protocol getProtocol() {
		return Protocol.FTP;
	}
	
	public String getHost(){
		return host;
	}
	
	public int getPort(){
		return _port;
	}

	public String getUser() {
		return user;
	}
	
	public String getPassword(){
		return pass;
	}

	
	
	public boolean isPassiveMode() {
		return usePassiveMode;
	}

	
	public void setUsePassiveMode(boolean usePassiveMode) {
		this.usePassiveMode = usePassiveMode;
	}


	
	public void setUseBinaryType(boolean useBinaryType) {
		this.useBinaryType = useBinaryType;
	}

	public boolean isBinaryType() {
		return useBinaryType;
	}

	
	
	
}
