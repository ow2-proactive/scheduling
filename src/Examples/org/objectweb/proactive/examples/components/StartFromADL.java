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
package org.objectweb.proactive.examples.components;

import org.objectweb.proactive.core.component.adl.Launcher;


/** This is a wrapper to start component applications from their ADL description+deployment descr. */
public class StartFromADL {
    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Parameters : descriptor_file fractal_ADL_file " +
                "\n        The first file describes your deployment of computing nodes." +
                "\n                You may want to try ../../../descriptors/components/C3D_all.xml" +
                "\n        The second file describes your components layout. " +
                "\n                Try org.objectweb.proactive.examples.components.c3d.adl.userAndComposite");
        } else {
            String descriptor = args[0];
            String adl = args[1];
            Launcher.main(new String[] { "-fractal", adl, "m", descriptor });
        }
    }
}
