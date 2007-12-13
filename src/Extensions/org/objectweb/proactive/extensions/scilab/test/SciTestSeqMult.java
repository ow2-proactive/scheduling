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
package org.objectweb.proactive.extensions.scilab.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javasci.SciData;
import javasci.SciDoubleMatrix;
import javasci.Scilab;


public class SciTestSeqMult {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));

        double[] m1;
        double[] m2;

        String line;
        int nbRow;
        int nbCol;

        double startTime;
        double endTime;

        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#")) {
                continue;
            }

            if (line.trim().equals("")) {
                break;
            }

            nbRow = Integer.parseInt(line);
            nbCol = Integer.parseInt(line);

            m1 = new double[nbRow * nbCol];
            m2 = new double[nbRow * nbCol];
            for (int i = 0; i < (nbRow * nbCol); i++) {
                m1[i] = Math.random() * 10.0;
                m2[i] = Math.random() * 10.0;
            }

            startTime = System.currentTimeMillis();
            Scilab.sendData(new SciDoubleMatrix("A", nbRow, nbCol, m1));
            Scilab.sendData(new SciDoubleMatrix("B", nbRow, nbCol, m2));
            Scilab.sendData(new SciData("C"));
            Scilab.Exec("C=A*B;");

            SciData sciResult = Scilab.receiveDataByName("C");
            System.out.println(sciResult);
            endTime = System.currentTimeMillis();

            System.out.println(endTime - startTime);
            //System.out.println(sciResult);
            writer.println(nbRow + " " + (endTime - startTime));
        }

        reader.close();
        writer.close();
    }
}
