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
package nonregressiontest.nfe;

import java.io.Serializable;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.exceptions.NonFunctionalException;


/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class A implements Serializable {
    String name;

    public A() {
    }

    public A(String name) {
        this.name = name;
		
		/*InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Object A sur //" + inet.getCanonicalHostName() );
        */
    }

    public String getName() {
        return name;
    }

    public String getNodeUrl() {
        return ProActive.getBodyOnThis().getNodeURL();
    }
    
    public boolean protectedFrom(NonFunctionalException nfe) {
    	try {
    		if  (!ProActive.getBodyOnThis().getHandlersLevel().isEmpty()) {
				return (ProActive.searchExceptionHandler(nfe, this) != null);
    		} else {
				return false;
    		}
    	} catch (ProActiveException e) {
    		return false;
    	}
    }
    
}
