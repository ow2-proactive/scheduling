/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.utils;

/**
 * OperatingSystem
 *
 * @author The ProActive Team
 */
public enum OperatingSystem {
    MAC_OSX("Mac OS X", OperatingSystemFamily.MAC),
    WINDOWS_95("Windows 95", OperatingSystemFamily.WINDOWS),
    WINDOWS_98("Windows 98", OperatingSystemFamily.WINDOWS),
    WINDOWS_ME("Windows Me", OperatingSystemFamily.WINDOWS),
    WINDOWS_NT("Windows NT", OperatingSystemFamily.WINDOWS),
    WINDOWS_2000("Windows 2000", OperatingSystemFamily.WINDOWS),
    WINDOWS_XP("Windows XP", OperatingSystemFamily.WINDOWS),
    WINDOWS_7("Windows 7", OperatingSystemFamily.WINDOWS),
    WINDOWS_10("Windows 10", OperatingSystemFamily.WINDOWS),
    WINDOWS_2003("Windows 2003", OperatingSystemFamily.WINDOWS),
    WINDOWS_2008("Windows 2008", OperatingSystemFamily.WINDOWS),
    SUN_OS("SunOS", OperatingSystemFamily.UNIX),
    MPE_IX("MPE/iX", OperatingSystemFamily.UNIX),
    HP_UX("HP-UX", OperatingSystemFamily.UNIX),
    AIX("AIX", OperatingSystemFamily.UNIX),
    OS_390("OS/390", OperatingSystemFamily.UNIX),
    FREEBSD("FreeBSD", OperatingSystemFamily.UNIX),
    IRIX("Irix", OperatingSystemFamily.UNIX),
    DIGITAL_UNIX("Digital Unix", OperatingSystemFamily.UNIX),
    NETWARE("NetWare", OperatingSystemFamily.UNIX),
    OSF1("OSF1", OperatingSystemFamily.UNIX),
    OPENVMS("OpenVMS", OperatingSystemFamily.DEC_OS),
    WINDOWS_GENERIC("Windows", OperatingSystemFamily.WINDOWS),
    LINUX_OS("Linux", OperatingSystemFamily.LINUX),
    MAC_OS("Mac OS", OperatingSystemFamily.MAC);

    final private String label;

    final private OperatingSystemFamily family;

    private OperatingSystem(String label, OperatingSystemFamily family) {
        this.label = label;
        this.family = family;
    }

    public String getLabel() {
        return label;
    }

    public OperatingSystemFamily getFamily() {
        return family;
    }

    static public OperatingSystem resolve(String osName) {
        for (OperatingSystem os : OperatingSystem.values()) {
            if (osName.startsWith(os.label))
                return os;
        }
        return null;
    }

    static public OperatingSystem resolveOrError(String osName) {
        for (OperatingSystem os : OperatingSystem.values()) {
            if (osName.startsWith(os.label))
                return os;
        }
        throw new IllegalArgumentException("Unknown Operating System " + osName);
    }

}
