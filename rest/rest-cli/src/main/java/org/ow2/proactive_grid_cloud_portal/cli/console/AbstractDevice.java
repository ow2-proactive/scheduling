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
package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.IOException;
import java.io.Writer;


public abstract class AbstractDevice {

    public static final short STARDARD = 1;

    public static final short JLINE = 2;

    public abstract boolean canRead() throws IOException;

    public abstract int read() throws IOException;

    public abstract String readLine(String fmt, Object... args) throws IOException;

    public abstract char[] readPassword(String fmt, Object... args) throws IOException;

    public abstract void writeLine(String fmtm, Object... args) throws IOException;

    public abstract Writer getWriter();

    public abstract int getWidth();

    public abstract int getHeight();

    public static AbstractDevice getConsole(int type) throws IOException {
        switch (type) {
            case STARDARD:
                return (System.console() != null) ? new ConsoleDevice(System.console())
                                                  : new CharacterDevice(System.in, System.out);
            case JLINE:
                return new JLineDevice(System.in, System.out);
            default:
                throw new IllegalArgumentException("Unknown console type [" + type + "]");
        }
    }
}
