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
package org.objectweb.proactive.examples.matrix;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;


import org.objectweb.proactive.core.group.*;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.group.ProxyForGroup;

public class Main {

    public static void main (String args[]) {

	if (args.length != 2) {
	    System.err.println("missing argument : <nodesListFile> <MatrixSize>");
	    System.exit(0);
	}
	
	String[] nodesList = readNodesList(args[0]);	
	String targetNode = nodesList[0].substring(0, nodesList[0].length()-1)+"2";
	Launcher l = null;
	try {
	    l = (Launcher) ProActive.newActive("org.objectweb.proactive.examples.matrix.Launcher", new Object[] {nodesList}, targetNode); 
	} catch (Exception e) {e.printStackTrace();}

	int matrixSize=Integer.parseInt(args[1]);
	Matrix m1;
	Matrix m2 = l.createMatrix(matrixSize);

 	long startTime;
 	long endTime;


	// DISTRIBUTION 
	printMessageAndWait("Ready for distribution");
	startTime= System.currentTimeMillis();

 	Matrix m2group = l.distribute(m2);

	endTime = System.currentTimeMillis() - startTime;
	System.out.println("   Distribution : " + endTime + " millisecondes\n\n");


	// MULTIPLICATION
	while (true) {
	    printMessageAndWait("Ready for distributed multiplication");
	    startTime= System.currentTimeMillis();
	    
	    m1 = l.createMatrix(matrixSize);




	    System.out.println(m1);
	    System.out.println(m2);




	    l.start(m1,m2group);

	    endTime = System.currentTimeMillis() - startTime;
	    System.out.println("   Distributed Multiplication : " + endTime + " millisecondes\n\n");
	}
    } 
    
    
    private static void printMessageAndWait(String msg) {
	java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	System.out.print(msg);
	System.out.println("   --> Press <return> to continue");
	try { 
	    d.readLine(); 
	} catch (Exception e) {
	}
	//     System.out.println("---- GO ----");
    } 
    
    
    /**
     * go
     */
    private static String[] readNodesList(String filename) {
	try {
	    java.io.File f = new java.io.File(filename);
	    if (! f.canRead()) return null;
	    byte[] b = getBytesFromInputStream(new java.io.FileInputStream(f));
	    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(new String(b));
	    int n = tokenizer.countTokens();
	    if (n == 0) return null;
	    String[] result = new String[n];
	    int i=0;
	    while (tokenizer.hasMoreTokens()) {
		result[i++] = tokenizer.nextToken();
	    }
	    return result;
	} catch (java.io.IOException e) {
	    return null;
	}
    }


    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in)  throws java.io.IOException {
	java.io.DataInputStream din = new java.io.DataInputStream(in);
	byte[] bytecodes = new byte[in.available()];
	try {
	    din.readFully(bytecodes);
	} finally {
	    if (din != null) din.close();
	}
	return bytecodes;
    }
    
}
