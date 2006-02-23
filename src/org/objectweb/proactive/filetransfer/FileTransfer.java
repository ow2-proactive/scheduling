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
package org.objectweb.proactive.filetransfer;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.filetransfer.FileTransferService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.FileWrapper;

/**
 * This class provides a standard entry point for API FileTransfer tools.
 * @author The ProActive Team
 * @since ProActive 3.0.2 (Feb 2006)
 */
public class FileTransfer {


	/**
	 * Pulls a file from a remote node. This method behaves asynchronously.
	 * @param node The remote ProActive node.
	 * @param srcFile The source file in the remote node.
	 * @param dstFile The destination file in the local node.
	 * @return A FileWrapper instance containing a File object. This return value is a future,
	 * and to wait on it simply call the FileWrapper.getFiles() method.
	 * @throws IOException Problem with permissions, files not found, etc.
	 * @throws ProActiveException Problems with communication like node unreachable, etc.
	 */
	public static FileWrapper pullFile(Node node, File srcFile, File dstFile) throws IOException, ProActiveException{
		
		return FileTransferService.pullFile(node, srcFile, dstFile);
	}
	
	/**
	 * This method behaves like pullFile(Node, File, File), with the difference that it transfers multiple files.
	 * When performing a FileWrapper.getFiles() on the returned object, the wait-by-necessity mechanism will block
	 * the calling thread until all files have been pulled. 
	 */
	public static FileWrapper pullFile(Node node, File srcFile[], File dstFile[]) throws IOException, ProActiveException{
		return FileTransferService.pullFiles(node, srcFile, dstFile);
	}
	
	/**
	 * Pushs a file from the local node to a remote node. This method behaves asynchronously.
	 * @param node The remote ProActive node.
	 * @param srcFile The source file in the local node.
	 * @param dstFile The destination file in the remote node.
	 * @return A future of a BooleanWrapper. Accessing this variable will cause the block of the calling thread 
	 * until the file has been fully received at the remote node. 
	 * @throws IOException Problem with permissions, files not found, etc.
	 * @throws ProActiveException Problems with communication like node unreachable, etc.
	 */
	public static BooleanWrapper pushFile(Node node, File srcFile, File dstFile) throws IOException, ProActiveException{
	
		return FileTransferService.pushFile(node, srcFile, dstFile);
	}
	
	/**
	 * This method behaves like pushFile(Node, File, File),  with the difference that it transfers multiple files.
	 * Accessing the future BooleanWrapper will block the thread, until all files have been pushed to the remote node.
	 */
	public static BooleanWrapper pushFile(Node node, File srcFile[], File dstFile[]) throws IOException, ProActiveException{
		return FileTransferService.pushFiles(node, srcFile, dstFile);
	}
		
}
