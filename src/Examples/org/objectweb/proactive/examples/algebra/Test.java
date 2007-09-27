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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.algebra;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


public class Test extends Object {
    public static void main(String[] args) {
        int n = 1000;
        int m = n / 2;
        if (args.length == 1) {
            m = Integer.decode(args[0]).intValue();
        }

        ProActiveConfiguration.load();

        Matrix m0;
        Matrix m1;
        Matrix m2;
        Vector v0;
        Vector v1;
        Vector v2;

        // Creates and randomly fills in both matrix and vector
        m0 = new Matrix(n, n);
        v0 = new Vector(n);

        m0.randomizeFillIn();
        v0.randomizeFillIn();

        // Creates both submatrixs, with sizes m and n-m
        try {
            Object[] parameters1 = { m0.getBlock(0, 0, m, n - 1) };

            //System.out.println("Le parametre est " + parameters1[0]);
            m1 = (Matrix) org.objectweb.proactive.api.ProActiveObject.newActive(Matrix.class.getName(),
                    parameters1);
            //m1 = (Matrix) org.objectweb.proactive.ProActive.newActive(Matrix.class.getName(), null,null);
            Object[] parameters2 = { m0.getBlock(m + 1, 0, n - 1, n - 1) };
            m2 = (Matrix) org.objectweb.proactive.api.ProActiveObject.newActive(Matrix.class.getName(),
                    parameters2);

            // Computes both products
            long begin = System.currentTimeMillis();
            ;
            v1 = m1.rightProduct(v0);
            v2 = m2.rightProduct(v0);
            // Creates result vector
            v1.concat(v2);
            long end = System.currentTimeMillis();
            ;

            System.out.println("Elapsed time = " + (end - begin) + " ms");
        } catch (Exception e) {
            System.out.println("Exception while creating matrixes:\n" + e);
            e.printStackTrace();
        }
        System.exit(0);
    }
}
