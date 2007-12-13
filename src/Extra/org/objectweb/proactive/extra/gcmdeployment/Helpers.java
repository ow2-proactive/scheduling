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
package org.objectweb.proactive.extra.gcmdeployment;

import java.io.File;


public class Helpers {

    /**
     * Checks that descriptor exist, is a file and is readable
     * @param descriptor The File to be checked
     * @throws IllegalArgumentException If the File is does not exist, is not a file or is not readable
     */
    public static File checkDescriptorFileExist(File descriptor) throws IllegalArgumentException {
        if (!descriptor.exists()) {
            throw new IllegalArgumentException(descriptor.getName() + " does not exist");
        }
        if (!descriptor.isFile()) {
            throw new IllegalArgumentException(descriptor.getName() + " is not a file");
        }
        if (!descriptor.canRead()) {
            throw new IllegalArgumentException(descriptor.getName() + " is not readable");
        }

        return descriptor;
    }

    static public String escapeCommand(String command) {
        // At each step, the command must be protected with " or ' and the command
        // passed as parameter must be escaped. This can be quite difficult since
        // Runtime.getRuntime().exec() only take an array of String as parameter...    	
        String res = command.replaceAll("'", "'\\\\''");
        return "'" + res + "'";
    }
}
