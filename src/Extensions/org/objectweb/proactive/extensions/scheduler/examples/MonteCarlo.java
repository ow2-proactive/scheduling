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
package org.objectweb.proactive.extensions.scheduler.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Random;

import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;


public class MonteCarlo extends JavaExecutable {

    /**  */
    private static final long DEFAULT_STEPS = 10;
    private static final long DEFAULT_ITERATIONS = 10000;
    private long iterations = DEFAULT_ITERATIONS;
    private long steps = DEFAULT_STEPS;
    private String file = null;

    @Override
    public void init(Map<String, Object> args) {
        if (args.containsKey("steps")) {
            try {
                steps = Long.parseLong(args.get("steps").toString());
            } catch (NumberFormatException e) {
            }
        }

        if (args.containsKey("iterations")) {
            try {
                iterations = Long.parseLong(args.get("iterations").toString());
            } catch (NumberFormatException e) {
            }
        }

        if (args.containsKey("file")) {
            file = args.get("file").toString();
        }
    }

    @Override
    public Object execute(TaskResult... results) {
        Random rand = new Random(System.currentTimeMillis());
        long n = iterations;
        long print = iterations / steps;
        int nbPrint = 0;
        double res = 0;

        while (n > 0) {
            if (print < 0) {
                System.out.println("Calcul intermediaire (" +
                    (100 - ((n * 100) / iterations)) + "%) : Pi = " +
                    ((4 * res) / (((++nbPrint) * iterations) / steps)));
                print = iterations / steps;
            }

            double x = rand.nextDouble();
            double y = rand.nextDouble();

            if (((x * x) + (y * y)) < 1) {
                res++;
            }

            print--;
            n--;
        }

        Double result = new Double((4 * res) / iterations);

        if (file != null) {
            FileOutputStream f = null;

            try {
                f = new FileOutputStream(file);

                PrintStream ps = new PrintStream(f);
                ps.println("Le resultat de Pi par Montecarlo est : " + result);
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}
