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



import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.group.ProActiveGroup;


public class Main {

    public static void main (String args[]) {

	if (args.length != 2) {
	    System.err.println("missing argument : <nodesListFile> <MatrixSize>");
	    System.exit(0);
	}
	
	String[] nodesList = readNodesList(args[0]);	
	//String targetNode = nodesList[0].substring(0, nodesList[0].length()-1)+"2";
	Launcher l = null;

	try {
	    l = (Launcher) ProActive.newActive("org.objectweb.proactive.examples.matrix.Launcher", new Object[] {nodesList});//,targetNode);
	} catch (Exception e) {System.err.println("\nUnable to create the Launcher !!!!!\n"); e.printStackTrace();}

	int matrixSize=Integer.parseInt(args[1]);

	Matrix m1;
	Matrix m2 = new Matrix(matrixSize,matrixSize);
	m2.initializeWithRandomValues();

 	long startTime;
 	long endTime;


	// DISTRIBUTION 
	printMessageAndWait("\n\n\n\nReady for distribution");
	startTime= System.currentTimeMillis();

	Matrix m2group = null;
	try {
	    m2group = l.distribute(m2); }
	catch (Exception e) { e.printStackTrace();}

	endTime = System.currentTimeMillis() - startTime;
	System.out.println("   Asynchronous Distribution : " + endTime + " millisecondes\n\n");


	// MULTIPLICATION
	int i=1;
	while (true) {
	    m1 = new Matrix(matrixSize,matrixSize);
	    m1.initializeWithRandomValues();//l.createMatrix(matrixSize);

	    printMessageAndWait("Ready for distributed multiplication (" +i+ ")");
	    //startTime= System.currentTimeMillis();

	    // System.out.println(m1);
	    // System.out.println(m2);

	    l.start(m1,m2group,i);

	    System.out.println("   Operation ("+i+") launched\n");

	    //endTime = System.currentTimeMillis() - startTime;
	    //System.out.println("Total Distributed Multiplication : " + endTime + " millisecondes\n\n");
	    i++;
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
