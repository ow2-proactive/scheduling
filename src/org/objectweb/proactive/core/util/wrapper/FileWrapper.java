/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.util.wrapper;

/*
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
*/
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>An reifiable object for Java type <code>File</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author Mario Leyton
 *
 * Created on Feb 18, 2006
 */
public class FileWrapper implements Serializable {
	protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
	
	ArrayList fileList;
	ArrayList wrapperList;
	
	public FileWrapper(){

		fileList = new ArrayList();
		wrapperList = new ArrayList();
	}
	
	/**
	 * Returns an array of the files represented by this wrapper.
	 * If the files are being transfered, then this method will block
	 * until the transfers have finished
	 * @return An array with the Files
	 */
	public File[] getFiles(){
		
		Collection alist= getFileCollection();

		return (File [])alist.toArray( new File[alist.size()]);
	}
	
	/**
	 * Like getFiles(), but returns a Collection.
	 * @return A collection with the Files represented by this wrapper,
	 */
	public Collection getFileCollection(){

		//no need to wait for the files
		ArrayList alist= new ArrayList();
		alist.addAll(fileList);
		
		Iterator it = wrapperList.iterator();
		while(it.hasNext()){
			FileWrapper fw= (FileWrapper) it.next();
			alist.addAll(fw.getFileCollection()); //wait-by-necessity
		}
		
		return alist;
	}
	
	public void addFile(File f){
	
		fileList.add(f);
	}
	
	public void addFileWrapper(FileWrapper fw){

		wrapperList.add(fw);
	}
}
