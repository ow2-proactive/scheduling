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
package org.objectweb.proactive.tools;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.filetransfer.FileTransferService;
import org.objectweb.proactive.core.node.Node;

/**
 * This class provides a standard entry point for API FileTransfer tools.
 * @author The ProActive Team
 */
public class FileTransfer {

	/**
	 * API method for pulling files from a Node.
	 */
	public static File pullFile(Node node, File srcFile, File dstFile) throws IOException, ProActiveException{
		
		return FileTransferService.pullFile(node, srcFile, dstFile);
	}
	
	/**
	 * API method for pushing files from a Node.
	 */
	public static void pushFile(Node node, File srcFile, File dstFile) throws IOException, ProActiveException{
	
		FileTransferService.pushFile(node, srcFile, dstFile);
	}
}
