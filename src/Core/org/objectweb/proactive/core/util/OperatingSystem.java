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
package org.objectweb.proactive.core.util;

import java.io.Serializable;


public enum OperatingSystem implements Serializable {
    windows(';', '\\'), unix(':', '/');
    protected char pathSeparator;
    protected char fileSeparator;

    OperatingSystem(char pathSeparator, char fileSeparator) {
        this.pathSeparator = pathSeparator;
        this.fileSeparator = fileSeparator;
    }

    public char pathSeparator() {
        return pathSeparator;
    }

    public char fileSeparator() {
        return fileSeparator;
    }

    static public OperatingSystem getOperatingSystem() {
        String val = System.getProperty("os.name");
        if (val.contains("Windows")) {
            return windows;
        }

        return unix;
    }
}
