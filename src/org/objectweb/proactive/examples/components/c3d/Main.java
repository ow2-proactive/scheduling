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
    public static void main(final String[] args)
        throws Exception {
        if (args.length != 1) {
            System.out.println("Usage : you need to pass one descriptor file name as parameter.");
            return;
        }
        String arg0 = "-fractal"; // using the fractal component model
        String arg1 = Main.class.getPackage().getName() + ".fractal.distributed"; // the bindings description
        String arg2 = "m";
        String arg3 = args[0];
        Launcher.main(new String[] { arg0, arg1, arg2, arg3 });
    }
}
