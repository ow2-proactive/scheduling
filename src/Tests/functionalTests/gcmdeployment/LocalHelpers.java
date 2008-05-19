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
package functionalTests.gcmdeployment;

import java.io.File;
import java.io.FileNotFoundException;


public abstract class LocalHelpers {

    static public File getDescriptor(Class<?> cl) throws FileNotFoundException {
        String classname = cl.getSimpleName();
        System.out.println(classname);
        String resource = cl.getResource(classname + ".xml").getFile();
        File desc = new File(resource);
        if (!(desc.exists() && desc.isFile() && desc.canRead())) {
            throw new FileNotFoundException(desc.getAbsolutePath());
        }

        return desc;
    }

    static public File getDescriptor(Object o) throws FileNotFoundException {
        return getDescriptor(o.getClass());
    }

    static public void waitAllocation() {
        wait(10000);
    }

    static public void wait(int sec) {
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
