/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.process.globus;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;

/**
 * This class is a utility class, to use when launching globus processes
 */

public class StartGlobus
{

	private static final String FS = System.getProperty("file.separator");
  private static final String XML_LOCATION = System.getProperty("user.home")+FS+"ProActive"+FS+"descriptors"+FS+"LocalGlobusSetup.xml";
  
	public static void main(String[] args)
	{
		try{
		ProActiveDescriptor pad = ProActive.getProactiveDescriptor("file:"+XML_LOCATION);
		GlobusProcess gp = (GlobusProcess)pad.getProcess("globusProcess");
		//gp.setId(args[0]);
		gp.startNodeWithGlobus(args[0]);
		}catch(ProActiveException e){
    		e.printStackTrace();
    	}
	}
}
