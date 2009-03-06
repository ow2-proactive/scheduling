package org.ow2.proactive.scripting.helper.filetransfer.initializer;

import org.ow2.proactive.scripting.helper.filetransfer.driver.FileTransfertDriver;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertProtocols.Protocol;

public interface FileTransfertInitializer {

	Class<? extends FileTransfertDriver> getDriverClass();

	Protocol getProtocol();
	
	public String getHost();
	
	public int getPort();
	
	public String getUser();
	
}
