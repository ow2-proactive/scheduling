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
package nonregressiontest.activeobject.acontinuation;

import java.util.Vector;

import org.objectweb.proactive.ProActive;

import testsuite.test.FunctionalTest;


/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Test extends FunctionalTest {
    A a;
    A b;
    Id idPrincipal;
    Id idDeleguate;
    boolean futureByResult;

    public Test() {
        super("Automatic Continuations",
            "Test automatic continuations by results and parameteres");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
       	//System.out.println( "Property "+System.getProperty("proactive.future.ac"));
        String initial_ca_setting = System.getProperty("proactive.future.ac");
        if (!"enable".equals(initial_ca_setting)) {
    		System.setProperty("proactive.future.ac","enable");
        }
		ACThread acthread = new ACThread();
		acthread.start();
		acthread.join();
        System.setProperty("proactive.future.ac",initial_ca_setting);
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    	
    }

    public boolean postConditions() throws Exception {
        return (futureByResult && a.isSuccessful() &&
        a.getFinalResult().equals("dummy"));
    }
    
    private class ACThread extends Thread {
    	
    	public void run() {
    		try{
    		a = (A) ProActive.newActive(A.class.getName(),
                new Object[] { "principal" });
        a.initFirstDeleguate();
        idDeleguate = a.getId("deleguate2");
        idPrincipal = a.getId("principal");
        Vector v = new Vector(2);
        v.add(idDeleguate);
        v.add(idPrincipal);
        if (ProActive.waitForAny(v) == 0) {
            futureByResult = false;
        } else {
            futureByResult = true;
        }
        b = (A) ProActive.newActive(A.class.getName(), new Object[] { "dummy" });
        idPrincipal = b.getIdforFuture();
        a.forwardID(idPrincipal);
    		}catch (Exception e){
    			e.printStackTrace();
    		}
    		
    	}
    }
}
