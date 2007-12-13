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
package org.objectweb.proactive.examples.plugtest;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


/**
 * @author mozonne
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MTest {
    public static void main(String[] args) {
        try {
            //lecture du descripteur
            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(args[0]);
            VirtualNode mTest = pad.getVirtualNode("plugtest");
            mTest.activate();

            Node[] noeuds = mTest.getNodes();
            System.out.println("Il y a " + noeuds.length + " noeuds.");
            ObjA[] arrayA = new ObjA[noeuds.length];
            ObjB b = (ObjB) org.objectweb.proactive.api.PAActiveObject.newActive(ObjB.class.getName(),
                    new Object[] { "B" });

            for (int i = 0; i < noeuds.length; i++) {
                arrayA[i] = (ObjA) org.objectweb.proactive.api.PAActiveObject.newActive(ObjA.class.getName(),
                        new Object[] { "object" + i, b }, noeuds[i]);
            }

            ObjA a = arrayA[0];
            System.out.println("Getting information " + a.getInfo());
            System.out.println("Calling toString " + a.toString());
            System.out.println("Calling getNumber " + a.getNumber());
            System.out.println("Calling getB " + a.getB().sayHello());
            System.out.println("Calling sayHello " + a.sayHello());

            for (int i = 0; i < arrayA.length; i++)
                System.out.println("\nI'm " + arrayA[i].getInfo() + " and I say " + arrayA[i].sayHello() +
                    " on " + arrayA[i].getNode());

            printMessageAndWait(pad);
            pad.killall(false);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printMessageAndWait(ProActiveDescriptor pad) {
        java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        System.out.println("   --> Press <return> to continue");

        try {
            d.readLine();
            pad.killall(false);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //     System.out.println("---- GO ----");
    }
}
