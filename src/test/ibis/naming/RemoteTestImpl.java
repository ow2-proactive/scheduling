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
package test.ibis.naming;

import ibis.rmi.AlreadyBoundException;
import ibis.rmi.Naming;
import ibis.rmi.RemoteException;
import ibis.rmi.server.UnicastRemoteObject;

import java.net.MalformedURLException;

import org.objectweb.proactive.core.util.IbisProperties;


public class RemoteTestImpl extends UnicastRemoteObject implements RemoteTest {
	
	static {
			IbisProperties.load();
		}
	
    public RemoteTestImpl() throws RemoteException {
    }

    public void bind(String name) {
        try {
            Naming.bind(name, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] arguments) {
        if (arguments.length < 1) {
            System.err.println("Usage: " + RemoteTestImpl.class.getName() +
                " <bindName>");
            System.exit(-1);
        }

        RemoteTestImpl t = null;

        try {
            t = new RemoteTestImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        t.bind(arguments[0]);
    }
}
