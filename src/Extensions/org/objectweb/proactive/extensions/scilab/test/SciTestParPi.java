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


public class SciTestParPi {
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

        int precision;
        int nbBloc;
        String line;
        String[] arrayLine;
        FutureDoubleMatrix piResult;

        double startTime;
        double endTime;
        double result;

        for (int i = 0; (line = reader.readLine()) != null; i++) {
            if (line.trim().startsWith("#")) {
                continue;
            }

            if (line.trim().equals("")) {
                break;
            }

            arrayLine = line.split(" ");
            nbBloc = Integer.parseInt(arrayLine[0]);
            precision = Integer.parseInt(arrayLine[1]);

            startTime = System.currentTimeMillis();
            piResult = GridMatrix.calPi(service, "calPi" + i, precision, nbBloc);
            result = piResult.get()[0];
            service.removeAllEventListenerTask();
            endTime = System.currentTimeMillis();
            System.out.println("Pi = " + result);
            writer.println(nbEngine + " " + precision + " " + nbBloc + " " +
                (endTime - startTime));
        }

        reader.close();
        writer.close();
        service.exit();
        System.exit(0);
    }
}
