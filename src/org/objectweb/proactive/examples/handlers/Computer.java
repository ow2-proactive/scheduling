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


public class Computer implements java.io.Serializable {

    private Integer delta;
    private Double x1;
    private Double x2;
    
    public Computer() {
	delta = new Integer(0);
	x1 = new Double(0.0);
	x2 = new Double(0.0);
    }

    public String getLastSolution() {
	return ("Solution : Delta = " + delta.intValue() + " / x1 = " + x1.doubleValue() + " / x2 = " + x2.doubleValue());
    }

    public boolean secondDegreeEquation(int ca, int cb, int cc) {

	// Check coefficients
	System.out.println("\n*** Resolution of " + ca + "x² + " + cb + "x + " + cc + " = 0");
	if (ca == 0) {
	    System.out.println(" -> Equation is not from second degree !");
	    return false;
	}

	// Compute delta
	delta = new Integer(cb*cb - 4*ca*cc);
	int d = delta.intValue();
	System.out.println("*** Delta = (b*b - 4(a*c)) = " + delta);

	// Search roots according to delta
	if (d > 0) {
	    x1 = new Double((-cb + Math.sqrt(delta.doubleValue())) / (2*ca));
	    x2 = new Double((-cb - Math.sqrt(delta.doubleValue())) / (2*ca));
	    return true;
	} else if (d == 0) {
	    x1 = new Double(-cb / (2*ca));
	    x2 = new Double(-cb / (2*ca));
	    return true;
	} else {
	    x1 = new Double(0.0);
	    x2 = new Double(0.0);
	    return true;
	} 
    }

    // Register the class on the local node
    public static void main(String[] args) {

	try {
	    //Creates an active instance of the class on the local node
	    Computer computer = (Computer) org.objectweb.proactive.ProActive.newActive(Computer.class.getName(), null);
	    java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
	    org.objectweb.proactive.ProActive.register(computer, "//" + localhost.getHostName() + "/Computer");
	} catch (Exception e) {
	    System.err.println("*** Error: " + e.getMessage());
	    e.printStackTrace();
	}
    }
}
