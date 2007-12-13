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
package org.objectweb.proactive.extensions.scilab.util;

import javasci.SciDoubleMatrix;

import org.objectweb.proactive.extensions.scilab.AbstractGeneralTask;
import org.objectweb.proactive.extensions.scilab.SciTask;
import org.objectweb.proactive.extensions.scilab.monitor.MSEventListener;
import org.objectweb.proactive.extensions.scilab.monitor.MSService;


/**
 * This class represents a set of matrix operations for a parallel computation
 */
public class GridMatrix {

    /**
     * Mandelbrot set calculation
     * @param service
     * @param name name of matrix result
     * @param xres x-axis resolution
     * @param yres y-axis resolution
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     * @param precision max number of iterations
     * @param nbBloc number of blocks
     * @return future of the matrix result
     */
    public static FutureDoubleMatrix calMandelbrot(MSService service, String name, int xres, int yres,
            double xmin, double xmax, double ymin, double ymax, int precision, int nbBloc) {
        FutureDoubleMatrix res = new FutureDoubleMatrix(name, xres, yres);

        service.addEventListenerTask(new MandelbrotEventListener(service, nbBloc, res));

        AbstractGeneralTask sciTask;
        int nbRow = yres / nbBloc;
        double sizeBloc = ((ymax - ymin) / nbBloc);

        double y1 = ymin;
        double y2 = ymin + sizeBloc;
        for (int i = 0; i < nbBloc; i++) {
            sciTask = new SciTask(name + i);
            sciTask.addDataOut(name + i);
            sciTask.setJob(SciMath.formulaMandelbrot(name + i, nbRow, xres, xmin, xmax, y1, y2, precision));
            service.sendTask(sciTask);
            y1 = y2;
            y2 += sizeBloc;
        }

        return res;
    }

    /**
     * Pi calculation
     * @param service
     * @param name name of matrix result
     * @param precision max of iterations
     * @param nbBloc number of blocks
     * @return future of the matrix result
     */
    public static FutureDoubleMatrix calPi(MSService service, String name, int precision, int nbBloc) {
        FutureDoubleMatrix res = new FutureDoubleMatrix(name, 1, 1);
        int sizeBloc = precision / nbBloc;
        service.addEventListenerTask(new PiEventListener(service, nbBloc, res));

        AbstractGeneralTask sciTask;
        for (int i = 0; i < nbBloc; i++) {
            sciTask = new SciTask(name + i);
            sciTask.addDataOut("pi" + i);
            sciTask.setJob(SciMath.formulaPi("pi" + i, i, sizeBloc));
            service.sendTask(sciTask);
        }

        return res;
    }

    /**
     * Matrix multiplication
     * @param service
     * @param name name of matrix result
     * @param matrix1
     * @param nbRow1
     * @param nbCol1
     * @param matrix2
     * @param nbRow2
     * @param nbCol2
     * @return future of the matrix result
     */
    public static FutureDoubleMatrix mult(MSService service, String name, double[] matrix1, int nbRow1,
            int nbCol1, double[] matrix2, int nbRow2, int nbCol2) {
        FutureDoubleMatrix res = new FutureDoubleMatrix(name, nbRow2, nbCol2);

        MSEventListener eventListener = new MultEventListener(service, res);
        service.addEventListenerTask(eventListener);
        int nbTask = service.getNbEngine();

        if ((matrix1.length != (nbRow1 * nbCol1)) || (matrix2.length != (nbRow2 * nbCol2)) ||
            (nbCol1 != nbRow2)) {
            System.out.println("---------------INVALID MATRIX FOR MULTIPLICATION-----------------");
            return null;
        }

        SciDoubleMatrix sciMatrix = new SciDoubleMatrix("M", nbRow1, nbCol1, matrix1);

        int nbRow = nbRow2;
        int nbCol = nbCol2 / nbTask;
        int sizeSubMatrix = nbRow * nbCol;

        for (int i = 0; i < nbTask; i++) {
            double[] subMatrix = new double[sizeSubMatrix];

            for (int j = 0; j < sizeSubMatrix; j++) {
                subMatrix[j] = matrix2[j + (i * sizeSubMatrix)];
            }

            SciDoubleMatrix sciSubMatrix = new SciDoubleMatrix("M" + i, nbRow, nbCol, subMatrix);
            SciTask sciTask = new SciTask(name + i);
            sciTask.addDataIn(sciMatrix);
            sciTask.addDataIn(sciSubMatrix);
            sciTask.addDataOut("M" + i);
            sciTask.setJob(sciSubMatrix.getName() + "=" + sciMatrix.getName() + "*" + sciSubMatrix.getName() +
                ";");
            service.sendTask(sciTask);
        }

        return res;
    }
}
