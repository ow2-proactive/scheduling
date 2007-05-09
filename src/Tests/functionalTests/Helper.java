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
package functionalTests;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.util.OperatingSystem;


public class Helper {
    static public void killJVMs() {
        File dir = new File(System.getProperty("proactive.dir"));
        File cmd = new File(dir + "/dev/scripts/killTests");
        if (cmd.exists()) {
            try {
                switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    Runtime.getRuntime()
                           .exec(new String[] { cmd.getAbsolutePath() }, null,
                        dir);
                    break;
                default:
                    System.err.println("TODO: Kill JVMs on Windows also !");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(cmd + "does not exist");
        }
    }
}
