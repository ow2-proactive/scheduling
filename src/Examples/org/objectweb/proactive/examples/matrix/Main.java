/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.matrix;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.gcmdeployment.API;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


public class Main {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("Usage: java " + Main.class.getName() + " <size> <deploiement file>");
            System.exit(0);
        }

        ProActiveConfiguration.load();

        //	
        //	String[] nodesList = readNodesList(args[0]);	
        //	//String targetNode = nodesList[0].substring(0, nodesList[0].length()-1)+"2";
        GCMApplicationDescriptor proActiveDescriptor = null;
        Set<Node> nodesList = null;
        try {
            proActiveDescriptor = API.getGCMApplicationDescriptor(new File(args[1]));
            proActiveDescriptor.startDeployment();
            GCMVirtualNode vn1 = proActiveDescriptor.getVirtualNode("matrixNode");

            //Thread.sleep(15000);
            nodesList = vn1.getCurrentNodes();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Pb when reading descriptor");
        }

        Launcher l = null;

        try {
            l = (Launcher) PAActiveObject.newActive(Launcher.class.getName(), new Object[] { nodesList }); //,targetNode);
        } catch (Exception e) {
            logger.error("\nUnable to create the Launcher !!!!!\n");
            e.printStackTrace();
        }

        int matrixSize = Integer.parseInt(args[0]);

        Matrix m1;
        Matrix m2 = new Matrix(matrixSize, matrixSize);
        m2.initializeWithRandomValues();

        long startTime;
        long endTime;

        // DISTRIBUTION 
        printMessageAndWait("\n\n\n\nReady for distribution");
        startTime = System.currentTimeMillis();

        Matrix m2group = null;
        try {
            m2group = l.distribute(m2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        endTime = System.currentTimeMillis() - startTime;
        logger.info("   Asynchronous Distribution : " + endTime + " millisecondes\n\n");

        // MULTIPLICATION
        int i = 1;
        while (true) {
            m1 = new Matrix(matrixSize, matrixSize);
            m1.initializeWithRandomValues(); //l.createMatrix(matrixSize);

            printMessageAndWait("Ready for distributed multiplication (" + i + ")");
            //startTime= System.currentTimeMillis();
            // System.out.println(m1);
            // System.out.println(m2);
            l.start(m1, m2group, i);

            logger.info("   Operation (" + i + ") launched\n");

            //endTime = System.currentTimeMillis() - startTime;
            //System.out.println("Total Distributed Multiplication : " + endTime + " millisecondes\n\n");
            i++;
        }
    }

    private static void printMessageAndWait(String msg) {
        java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        logger.info(msg);
        logger.info("   --> Press <return> to continue");
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
            if (!f.canRead()) {
                return null;
            }
            byte[] b = getBytesFromInputStream(new java.io.FileInputStream(f));
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(new String(b));
            int n = tokenizer.countTokens();
            if (n == 0) {
                return null;
            }
            String[] result = new String[n];
            int i = 0;
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
    private static byte[] getBytesFromInputStream(java.io.InputStream in) throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }
}
