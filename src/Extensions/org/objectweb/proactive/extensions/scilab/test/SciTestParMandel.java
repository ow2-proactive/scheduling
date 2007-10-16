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

import org.objectweb.proactive.extensions.scilab.monitor.ScilabService;
import org.objectweb.proactive.extensions.scilab.util.FutureDoubleMatrix;
import org.objectweb.proactive.extensions.scilab.util.GridMatrix;


public class SciTestParMandel {
    public static void main(String[] args) throws Exception {
        ScilabService service = new ScilabService();

        if (args.length != 11) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        int nbEngine = Integer.parseInt(args[2]);
        service.deployEngine(args[0], args[1], nbEngine);
        Thread.sleep(nbEngine * 4000);

        BufferedReader reader = new BufferedReader(new FileReader(args[9]));
        PrintWriter writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(args[10])));

        int xres = Integer.parseInt(args[3]);
        int yres = Integer.parseInt(args[4]);
        double xmin = Double.parseDouble(args[5]);
        double xmax = Double.parseDouble(args[6]);
        double ymin = Double.parseDouble(args[7]);
        double ymax = Double.parseDouble(args[8]);
        int precision;
        int nbBloc;

        String line;
        String[] arrayLine;
        FutureDoubleMatrix mandelResult;

        double startTime;
        double endTime;
        double[] result;

        for (int i = 0; (line = reader.readLine()) != null; i++) {
            if (line.trim().startsWith("#")) {
                continue;
            }

            if (line.trim().equals("")) {
                break;
            }

            arrayLine = line.split(" ");

            precision = Integer.parseInt(arrayLine[1].trim());
            nbBloc = Integer.parseInt(arrayLine[0].trim());

            startTime = System.currentTimeMillis();
            mandelResult = GridMatrix.calMandelbrot(service, "mandel" + i,
                    xres, yres, xmin, xmax, ymin, ymax, precision, nbBloc);
            result = mandelResult.get();
            service.removeAllEventListenerTask();
            endTime = System.currentTimeMillis();

            System.out.println(nbEngine + " " + xres + " " + yres + " " +
                precision + " " + nbBloc + " " + (endTime - startTime));
            writer.println(nbEngine + " " + xres + " " + yres + " " +
                precision + " " + nbBloc + " " + (endTime - startTime));

            for (int k = 0; k < mandelResult.getNbRow(); k++) {
                System.out.println("");
                for (int j = 0; j < mandelResult.getNbCol(); j++) {
                    System.out.print(result[(k * mandelResult.getNbCol()) + j] +
                        " ");
                }
            }
        }

        reader.close();
        writer.close();
        service.exit();
        System.exit(0);
    }
}
