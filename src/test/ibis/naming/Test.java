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

import ibis.rmi.Naming;
import ibis.rmi.NotBoundException;
import ibis.rmi.RemoteException;

import org.objectweb.proactive.core.util.IbisProperties;

import java.net.MalformedURLException;


public class Test {
    static {
        IbisProperties.load();
    }

    public Test() {
    }

    public static void lookup(String name) {
        try {
            RemoteTest t = (RemoteTest) Naming.lookup(name);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] arguments) {
    	System.out.println(System.getProperty("sun.boot.library.path"));
		System.out.println(System.getProperty("sun.boot.class.path"));
        if (arguments.length < 1) {
            System.err.println("Usage: " + Test.class.getName() +
                " <bindName>");
            System.exit(-1);
        }

        System.out.println("Test: calling lookup ");
        Test.lookup(arguments[0]);
        System.out.println("Test: lookup done");
    }
}
