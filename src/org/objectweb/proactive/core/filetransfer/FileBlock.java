package org.objectweb.proactive.core.filetransfer;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

/**
 * @author ProActive Team 09/2005
 */
public class FileBlock implements Serializable{
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
	 
	public static final int DEFAULT_BLOCK_SIZE=512*1024; //Bytes
	
	private String srcFilename;
	private String dstFilename;
	private byte buffer[];
	private int usage;
	private long offset;
	private int blockSize;
	private boolean hasNextBlock;
	private long numberOfBlocks;
	
	public FileBlock(){
	}
	
	public FileBlock(String filename){
		
		this(filename, 0, DEFAULT_BLOCK_SIZE);
	}
	
	public FileBlock(String filename, long offset){
		
		this(filename, offset, DEFAULT_BLOCK_SIZE);
	}
	
	public FileBlock(String filename, long offset, int blockSize){
		
		this.srcFilename=filename;
		this.dstFilename=filename;
		this.offset=offset;
		this.blockSize=blockSize;
		this.buffer=new byte[blockSize];
		this.numberOfBlocks=0;
		
		this.usage=0;
		this.hasNextBlock=true;
		
		File F = new File(this.srcFilename);
		numberOfBlocks=Math.round(Math.ceil((double)F.length()/this.blockSize));
	}
	
	public String getSrcFilename() {
		return srcFilename;
	}
	
	public String getDstFilename() {
		return dstFilename;
	}
	
	public void setDstFilename(String dstFilename) {
		this.dstFilename = dstFilename;
	}
	
	public long getNumberOfBlocks(){
		
		return numberOfBlocks;
	}
	
	public void loadNexBlock() throws IOException{

		try {
			InputStream is = new FileInputStream(srcFilename);
			long skipped=is.skip(offset);
			if(skipped!=offset) throw new IOException("Error while skipping file offset");
			
			usage=is.read(buffer, 0, blockSize);
			offset+=usage;
			is.close();
			
			if(usage<blockSize)
				hasNextBlock=false;
			
			//File F = new File(this.srcFilename);
			//numberOfBlocks=Math.round(Math.ceil((double)F.length()/this.blockSize));

		} catch (IOException e) {
			hasNextBlock=false;
			usage=0;
			throw e;
		}
	}
	
	/*
	public boolean hasNextBlock(){
		return hasNextBlock;
	}
	*/
	public void saveCurrentBlock(){
		
		if(usage<0) usage=0;
		
		try {
			FileOutputStream fos =
				new FileOutputStream(dstFilename, offset<=usage?false:true);
			fos.write(buffer, 0, usage);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			hasNextBlock=false;
			e.printStackTrace();
		}
		
		//Can't write the same block twice
		usage=0;
	}
	
	/**
	 * @return Returns the offset.
	 */
	public long getOffset() {
		return offset;
	}
	/**
	 * @return Returns the blockSize.
	 */
	public int getBlockSize() {
		return blockSize;
	}
}
