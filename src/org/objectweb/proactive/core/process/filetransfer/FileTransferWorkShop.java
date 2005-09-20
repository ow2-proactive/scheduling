/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.core.process.filetransfer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;


/**
 * This class stores the FileTransfer arquitecture specific 
 * information. It also has a reference on the abstract
 * FileTransfer definitions.
 * 
 * The tools for mergin the abstract and specific information
 * are also provided through this class. 
 *
 * @author  ProActive Team
 * @version 1.0,  2005/08/26
 * @since   ProActive 2.3
 */
public class FileTransferWorkShop implements Serializable{
	public static final String PROCESSDEFAULT_KEYWORD = "processDefault";
	public static final String IMPLICIT_KEYWORD = "implicit";
	public static final String[] ALLOWED_COPY_PROTOCOLS = {PROCESSDEFAULT_KEYWORD, "scp", "unicore", "rcp"};
	
	protected static Logger logger = Logger.getLogger(Loggers.FILETRANSFER);
	
	/* Reference to filetransfer definitions */
	private HashMap fileTransfers;
			
	/*Array with protocols to try*/
	private String copyProtocol[];
	
	private String processDefault;
	boolean isImplicit;
	public StructureInformation srcInfoParams, dstInfoParams;
	
	public FileTransferWorkShop(String processDefault){
		
		//Verification of ilegal name for processDefault=="processDefault"
		if(processDefault == null || processDefault.length()<=0 ||
				processDefault.equalsIgnoreCase(PROCESSDEFAULT_KEYWORD)){
			logger.error("Illegal processDefault value="+processDefault+" in "+
					this.getClass()+". Falling back to dummy.");
			this.processDefault="dummy";
		}
		else
			this.processDefault=processDefault;
		
		isImplicit=false;
		fileTransfers = new HashMap();
		copyProtocol = new String[0];
		srcInfoParams = new StructureInformation();
		dstInfoParams = new StructureInformation();
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();

		//Queue variables
		sb.append("isImplicit     = ").append(isImplicit).append("\n");
		sb.append("processDefault = ").append(processDefault).append("\n");
		
		//Copy Protocol
		sb.append("copyProtocols  = ");
		for(int i=0; i<copyProtocol.length ;i++)
			sb.append(copyProtocol[i]).append(",");
		sb.append("\n");
		
		//SrcInfo
		sb.append("Src Information Parameters\n");
		sb.append(srcInfoParams).append("\n");
		
		//DstInfo
		sb.append("Dst Information Parameters\n");
		sb.append(dstInfoParams).append("\n");
		
		//FileTransfers
		Iterator it = fileTransfers.keySet().iterator();
		while(it.hasNext()){
			FileTransfer ft=
				(FileTransfer)fileTransfers.get(it.next());
			
			sb.append(ft.toString()).append("\n");
		}
	
		while(sb.length()>0 && sb.charAt(sb.length()-1) == '\n')
			sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
	
	public synchronized void addFileTransfer(FileTransfer ft){
		if(ft ==null) return;
		if(ft.getId().equalsIgnoreCase(IMPLICIT_KEYWORD)){
			logger.warn("Warning, ignoring addFileTransfer with keyword id="+
					IMPLICIT_KEYWORD);
			return;
		}
		
		fileTransfers.put(ft.getId(),ft);
	}
	
	public void setFileTransferCopyProtocol(String copyProtocolString){
		copyProtocol=copyProtocolString.split("\\s*,\\s*");
	}
	
	/**
	 * This method returns an array of CopyProtocol instances.
	 * This instances are based on the value configured through
	 * the FileTransferStructure.setCopyProtocols()
	 * @return An array of CopyProtocol[].
	 */
	public CopyProtocol[] getCopyProtocols(){
		ArrayList alist=new ArrayList();
		
		StringBuffer skippedProtocols= new StringBuffer();
		for(int i=0; i<copyProtocol.length;i++){
			if(!isAllowedProtocol(copyProtocol[i])){
				skippedProtocols.append(copyProtocol[i]).append(" ");
				continue;
			}
			alist.add(copyProtocolFactory(copyProtocol[i]));
		}
		
		if(skippedProtocols.length()>0)
			logger.warn("Unknown copyprotocols will be skipped:"+skippedProtocols.toString());
		
		//if no Protocol is defined use the default
		if(alist.size()<=0){
			if(logger.isDebugEnabled())
				logger.debug("No CopyProtocols found, using default protocol:"+processDefault);
			
			alist.add(copyProtocolFactory(PROCESSDEFAULT_KEYWORD));
		}
		
		return (CopyProtocol[])alist.toArray(new CopyProtocol[0]);
	}
	
	/**
	 * This methods creates a CopyProtocol instance, given by it's
	 * name as a parameter. If the name is unknown, then a 
	 * DummyCopyProtocol is returned. 
	 * 
	 * If the name of the protocol is "processDefault" then a 
	 * corresponding instance will be returned with the flag:
	 * CopyProtocol.isDefaultProtocol() set to true. 
	 * 
	 * Note that in this last case it is possible to have a 
	 * DummyProtocol with the flag set to true if the factory 
	 * doesn't know how to make the default protocol.
	 *  
	 * @param protocolname The name of the desired transfer protocol
	 * @return An instance of a class that implements CopyProtocol.
	 */
	public CopyProtocol copyProtocolFactory(String protocolname){
		
		CopyProtocol cp;
		
		if(protocolname.equalsIgnoreCase("scp"))
			cp = new SecureCopyProtocol(protocolname);
		else if(protocolname.equalsIgnoreCase(PROCESSDEFAULT_KEYWORD)){
			//Note: this will produce an infinit recursion if
			// processDefault==PROCESSDEFAULT_KEYWORD
			cp = copyProtocolFactory(processDefault); //cool recursive call
			cp.setDefaultProtocol(true);
			return cp;
		}
		else if(protocolname.equalsIgnoreCase("rcp")){			
			cp = new RemoteFileCopy(protocolname);
		}
		else
			cp = new DummyCopyProtocol(protocolname);
		
		//Default values for almost all copy protocols
		cp.setFileTransferDefinitions(getAllFileTransferDefinitions());
		cp.setSrcInfo(srcInfoParams);
		cp.setDstInfo(dstInfoParams);
		
		return cp;
	}
	
	public synchronized FileTransfer[] getAllFileTransferDefinitions(){
		
		ArrayList ftList = new ArrayList();
		
		Iterator it = fileTransfers.keySet().iterator();
		while(it.hasNext()){
			FileTransfer ft=
				(FileTransfer)fileTransfers.get(it.next());
			
			ftList.add(ft);
		}
		
		return (FileTransfer[])ftList.toArray(new FileTransfer[0]);
	}
	
	/**
	 * Sets the source information for a given Queue (Deploy, Retrieve).
	 * @param name  The name of the parameter from: prefix, hostname, username, password
	 * @param value The value of the parameter.
	 */
	public void setFileTransferStructureSrcInfo(String name, String value){
		
		srcInfoParams.setInfoParameter(name, value);
	}
	
	/**
	 * Sets the destination information for a given Queue (Deploy, Retrieve).
	 * @param name  The name of the parameter from: prefix, hostname, username, password
	 * @param value The value of the parameter.
	 */
	public void setFileTransferStructureDstInfo(String name, String value){

		dstInfoParams.setInfoParameter(name, value);
	}
	
	/**
	 * Checks different things. For now, it prints a warning
	 * when an empty FileTransfer definition is found.
	 * @return true if everything is OK, false if there is a
	 * problem.
	 */
	public boolean check(){
		FileTransfer ft;
		
		if(fileTransfers.size()<=0){
			if(logger.isDebugEnabled()) logger.debug("No file transfer required.");
			return false;
		}
		
		//Checking FileTransfer definitions
		boolean retval=false;
		Iterator it = fileTransfers.keySet().iterator();
		while(it.hasNext()){
			ft = (FileTransfer)fileTransfers.get(it.next());
			if(ft.isEmpty()){
				logger.warn("Warning: FileTransfer definition id="+
						ft.getId()+" is empty or undefined.");
				continue;
			}
			
			//Obs: this might be a problem with Retrieve
			if(checkLocalFileExistance(ft))
				retval=true; //At least one file to transfer
		}		
		return retval;
	}
	
	public boolean isImplicit() {
		
		return isImplicit;
	}
	
	public void setImplicit(boolean implicit) {
		
		this.isImplicit=implicit;
	}
	
	public boolean isAllowedProtocol(String protocol){
		for(int i=0; i<ALLOWED_COPY_PROTOCOLS.length; i++)
			if(ALLOWED_COPY_PROTOCOLS[i].equalsIgnoreCase(protocol))
				return true;
			
		return false;
	}
	
	public String buildSrcFilePathString(String filename){
		
		return buildFilePathString(srcInfoParams.getPrefix(),
									srcInfoParams.getFileSeparator(),
									filename);
	}
	
	public static String buildFilePathString(StructureInformation infoParam, String filename){
		
		return buildFilePathString(infoParam.getPrefix(), 
									infoParam.getFileSeparator(),
									filename );
	}
	
	public String buildDstFilePathString(String filename){
		
		return buildFilePathString(dstInfoParams.getPrefix(),
									dstInfoParams.getFileSeparator(),
									filename);
	}
	
	public static String buildFilePathString(String prefix, String fileSep, String filename){
		
		/*
		 *BORDER CONDITIONS 
		 */
		
		//Trim white spaces
		prefix= prefix.trim();
		fileSep = fileSep.trim();
		filename = filename.trim();
		
		//Asign a default filesep if needed
		if(fileSep.length()<=0) fileSep="/"; 
		
		//Remove trailing slash of filename if needed
		if(filename.endsWith(fileSep))
			filename.substring(0, filename.length()-1);
		
		//Remove trailing slash from prefix if needed
		if(prefix.endsWith(fileSep))
			prefix.substring(0, prefix.length()-1);
		
		/*
		 * CASES EVALUATION 
		 */
		
		// -case1: filename starts from root path, nothing to do
		// -case2: no prefix, nothing to do
		if(filename.charAt(0)==fileSep.charAt(0) ||
				prefix.length()<=0)
			return filename; 
		
		//-case3: prefix is defined
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append(fileSep).append(filename);
		return sb.toString();
	}
	
	public boolean checkLocalFileExistance(FileTransfer ft){
		
		boolean atLeastOneFile=false;
		
		FileTransfer.FileDescription files[] = ft.getAll();
		
		File f; String filefullname;
		for(int i=0;i<files.length;i++){
			
			filefullname = buildSrcFilePathString(files[i].getSrcName());
			
			f= new File(filefullname);

			if(!f.exists())
				logger.warn("Warning, nonexistent: "+filefullname);
			else if(!f.canRead())
				logger.warn("Warning, unreadble: "+filefullname);
			else
				atLeastOneFile=true; //at least one dir is found
			
			if(files[i].isDir() && !f.isDirectory())
				logger.warn("Warning, not a directory: "+filefullname);
		}
		
		return atLeastOneFile;
	}
	
	public static boolean isLocalReadable(String filenamepath){
					
			File f = new File(filenamepath);
			
			return f.canRead();
	}
	
	public class StructureInformation implements Serializable{
		
		/* FileTransferQueue specific information */
		String prefix;
		String hostname;
		String username;
		String password;
		String fileSeparator;
		
		public StructureInformation(){
			
			prefix="";
			hostname="";
			username="";
			password="";
			fileSeparator="/";
		}
		
		public void setInfoParameter(String name, String value){
			
			if(name.equalsIgnoreCase("prefix")){
				
				value=value.trim();
				//delete ending file separators
				while(value.length()>0 && (value.endsWith("/") || value.endsWith("\\")))
					value = value.substring(0,value.length()-1);
				
				if(value.length()>0)
					prefix=value;
			}
			else if(name.equalsIgnoreCase("hostname"))
				hostname=value;
			else if(name.equalsIgnoreCase("username"))
				username=value;
			else if(name.equalsIgnoreCase("password"))
				password=value;
			else if(name.equalsIgnoreCase("fileseparator"))
				fileSeparator=value;
			else
				logger.warn("Skipping:"+name+"="+value+". Unknown FileTransfer information parameter.");
		}
		
		public String toString(){
			
			StringBuffer sb= new StringBuffer();
			
			sb.append("prefix        = ").append(prefix).append("\n");
			sb.append("hostname      = ").append(hostname).append("\n");
			sb.append("username      = ").append(username).append("\n");
			sb.append("password      = ").append(password).append("\n");
			sb.append("fileSeparator = ").append(fileSeparator);
			
			return sb.toString();
		}	
		/**
		 * @return Returns the fileSeparator.
		 */
		public String getFileSeparator() {
			return fileSeparator;
		}
		/**
		 * @param fileSeparator The fileSeparator to set.
		 */
		public void setFileSeparator(String fileSeparator) {
			this.fileSeparator = fileSeparator;
		}
		/**
		 * @return Returns the prefix.
		 */
		public String getPrefix() {
			return prefix;
		}
		/**
		 * @param prefix The prefix to set.
		 */
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
		/**
		 * @return Returns the hostname.
		 */
		public String getHostname() {
			return hostname;
		}
		/**
		 * @return Returns the username.
		 */
		public String getUsername() {
			return username;
		}
		/**
		 * @param hostname The hostname to set.
		 */
		public void setHostname(String hostname) {
			this.hostname = hostname;
		}
		/**
		 * @param username The username to set.
		 */
		public void setUsername(String username) {
			this.username = username;
		}
	}
}
