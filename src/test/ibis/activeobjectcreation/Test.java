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
package test.ibis.activeobjectcreation;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.IbisProperties;


public class Test implements RunActive {
	
	static {
		IbisProperties.load();
	}
	
    public Test() {
    }

    public void runActivity(Body body) {
        System.out.println(" >>>> Active object up and running <<< ");
    }

//    public static void setNativePath() {
//        ClassLoader cl = Test.class.getClassLoader();
//        //System.out.println("cl = " + cl);
//        //System.out.println(Test.class.getName().replace('.','/').concat(".class"));
//        //java.net.URL u = cl.getResource(Test.class.getName().replace('.','/').concat(".class"));
//        //	java.net.URL u = cl.getResource("libconversion.so");
//        //	String tmp=u.toString();
//        System.setProperty("name_server", "localhost");
//        System.setProperty("name_server_pool", "rutget");
//        System.setProperty("pool_host_number", "1");
//        //	System.out.println("u = " + u.getPath().toString().substring(0,u.getPath().toString().lastIndexOf('/')));
//        //	System.out.println(u.getFile());
//    }

    public static void main(String[] arguments) {

        Test test = null;
    //    Test.setNativePath();

        try {
            test = (Test) ProActive.newActive(Test.class.getName(), null, null,
                    null, ProActiveMetaObjectFactory.newInstance());
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }
}
