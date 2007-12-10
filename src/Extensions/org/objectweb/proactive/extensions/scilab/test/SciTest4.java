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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.scilab.GeneralResult;
import org.objectweb.proactive.extensions.scilab.MSEngineWorker;
import org.objectweb.proactive.extensions.scilab.SciTask;


public class SciTest4 {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        MSEngineWorker mSEngineWorker = (MSEngineWorker) PAActiveObject.newActive(MSEngineWorker.class.getName(),
                null);

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        PrintWriter writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(args[1])));

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
                m1[i] = (Math.random() * 10);
                m2[i] = (Math.random() * 10);
            }

            SciDoubleMatrix sciMatrix1 = new SciDoubleMatrix("M1", nbRow,
                    nbCol, m1);
            SciDoubleMatrix sciMatrix2 = new SciDoubleMatrix("M2", nbRow,
                    nbCol, m2);

            SciTask sciTask = new SciTask("mult");
            sciTask.addDataIn(sciMatrix1);
            sciTask.addDataIn(sciMatrix2);
            sciTask.addDataOut("M3");
            sciTask.setJob("M3=" + sciMatrix1.getName() + "*" +
                sciMatrix1.getName() + ";");

            startTime = System.currentTimeMillis();

            GeneralResult sciResult = mSEngineWorker.execute(sciTask);
            SciData sciMatrix3 = (SciData) sciResult.getList().get(0).getData();

            endTime = System.currentTimeMillis();

            writer.println(sciMatrix3);
            writer.println(nbRow + " " + (endTime - startTime));
        }

        reader.close();
        writer.close();

        System.exit(0);
    }
}
