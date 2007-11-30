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

import org.objectweb.proactive.extensions.scilab.monitor.MSService;
import org.objectweb.proactive.extensions.scilab.util.FutureDoubleMatrix;
import org.objectweb.proactive.extensions.scilab.util.GridMatrix;


public class SciTestParMult {
    public SciTestParMult() {
    }

    public static void main(String[] args) throws Exception {
        MSService service = new MSService();

        if (args.length != 5) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        int nbEngine = Integer.parseInt(args[2]);
        service.deployEngine(args[0], args[1], nbEngine);
        BufferedReader reader = new BufferedReader(new FileReader(args[3]));
        PrintWriter writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(args[4])));

        int nbRow;
        int nbCol;

        double[] m1;
        double[] m2;
        double[] m3;
        FutureDoubleMatrix result;

        double startTime;
        double endTime;
        String line;

        for (int i = 0; (line = reader.readLine()) != null; i++) {
            if (line.trim().startsWith("#")) {
                continue;
            }

            if (line.trim().equals("")) {
                break;
            }

            nbRow = Integer.parseInt(line);
            nbCol = Integer.parseInt(line);
            System.out.println(nbEngine + "  size=" + nbRow);
            m1 = new double[nbRow * nbCol];
            m2 = new double[nbRow * nbCol];
            for (int k = 0; k < (nbRow * nbCol); k++) {
                m1[k] = Math.random() * 10.0;
                m2[k] = Math.random() * 10.0;
            }

            startTime = System.currentTimeMillis();
            result = GridMatrix.mult(service, "mult" + i, m1, nbRow, nbCol, m2,
                    nbRow, nbCol);
            m3 = result.get();

            endTime = System.currentTimeMillis();
            System.out.println(endTime - startTime);

            System.out.println(" ");
            for (int k = 0; k < nbRow; k++) {
                for (int j = 0; j < nbCol; j++) {
                    System.out.print(m3[(k * nbCol) + j] + " ");
                }

                System.out.println(" ");
            }

            writer.println(nbEngine + " " + nbRow + (endTime - startTime));
        }

        reader.close();
        writer.close();
        service.exit();
        System.exit(0);
    }
}
