/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.examples.components.c3d;

import org.objectweb.proactive.core.component.adl.Launcher;


/**
 * This example is a C3D Component version.
 */
public class Main {
    private static final String DEFAULT_ADL = Main.class.getPackage().getName() +
        ".adl.userAndComposite";

    public static void main(final String[] args) throws Exception {
        if ((args.length != 2) && (args.length != 1)) {
            System.out.println(
                "Parameters : [fractal_ADL_file] descriptor_file" +
                "\n        the first file describes your components layout. " +
                "\n                Default is " + DEFAULT_ADL +
                "\n        the second file describes your deployment of computing nodes." +
                "\n                You may want to try ../../../descriptors/components/C3D_all.xml");

            return;
        }

        String adl;
        String descriptor;

        if (args.length == 1) {
            adl = DEFAULT_ADL;
            descriptor = args[0];
        } else {
            adl = args[0];
            descriptor = args[1];
        }

        Launcher.main(new String[] { "-fractal", adl, "m", descriptor });
    }
}
