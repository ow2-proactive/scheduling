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

package org.objectweb.proactive.examples.handlers;

// Exceptions and handlers
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.exceptions.communication.*;
import org.objectweb.proactive.core.exceptions.creation.*;
import org.objectweb.proactive.core.exceptions.group.*;
import org.objectweb.proactive.core.exceptions.migration.*;
import org.objectweb.proactive.core.exceptions.security.*;
import org.objectweb.proactive.core.exceptions.service.*;
import org.objectweb.proactive.core.exceptions.handler.*;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Space {

  
    // Create the space complexity test with a given number of handlers
    public Space(int nbHandlers) {

	// We add the requested number of handler to VM level
	for (int i = 0; i<nbHandlers; i++) {
	    ProActive.setExceptionHandler(ProActive.VMLevel, HandlerProActiveException.class.getName(), ProActiveException.class.getName() + i);
	}

	// We serialize the level and its referenced handlers
	try {

	    FileOutputStream fos1 = new FileOutputStream("time-test-default");
	    ObjectOutputStream out1 = new ObjectOutputStream(fos1);

	    FileOutputStream fos2 = new FileOutputStream("time-test-VM");
	    ObjectOutputStream out2 = new ObjectOutputStream(fos2);

	    System.out.println("Serialize Default Level");
	    out1.writeObject(ProActive.defaultLevel);
	    out1.close();

	    System.out.println("Serialize VM Level");
	    out2.writeObject(ProActive.VMLevel);
	    out2.close();

	} catch(IOException ex) {
	    ex.printStackTrace();
	}
    }

    // Main program
    public static void main(String[] args) {

	Space space = null;
	if (args.length == 1)
	    space = new Space((new Integer(args[0])).intValue());
    }
}
