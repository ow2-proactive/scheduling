/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

public enum OperatingSystem {

    WINDOWS(";", "\\"), UNIX(":", "/"), CYGWIN(";", "/");

    /** the path separator, ie. ";" on windows systems and ":" on unix systems */
    public final String ps;

    /** the file path separator, ie. "/" on unix systems and "\" on windows systems */
    public final String fs;

    private OperatingSystem(String ps, String fs) {
        this.fs = fs;
        this.ps = ps;
    }

    /**
     * Returns the operating system corresponding to the provided String parameter: 'LINUX', 'WINDOWS' or 'CYGWIN'
     * @param desc one of 'LINUX', 'WINDOWS' or 'CYGWIN'
     * @return the appropriate Operating System of null if no system is found
     */
    public static OperatingSystem getOperatingSystem(String desc) {
        if (desc == null) {
            throw new IllegalArgumentException("String description of operating system cannot be null");
        }
        desc = desc.toUpperCase();
        if ("LINUX".equals(desc) || "UNIX".equals(desc)) {
            return OperatingSystem.UNIX;
        }
        if ("WINDOWS".equals(desc)) {
            return OperatingSystem.WINDOWS;
        }
        if ("CYGWIN".equals(desc)) {
            return OperatingSystem.CYGWIN;
        }
        return null;
    }
}
