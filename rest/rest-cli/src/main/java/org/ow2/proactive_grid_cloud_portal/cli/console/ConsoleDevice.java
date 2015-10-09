/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.Console;
import java.io.IOException;
import java.io.Writer;


class ConsoleDevice extends AbstractDevice {
    private Console console;

    public ConsoleDevice(Console console) {
        this.console = console;
    }

    @Override
    public String readLine(String fmt, Object... args) throws IOException {
        return console.readLine(fmt, args);
    }

    @Override
    public char[] readPassword(String fmt, Object... args) throws IOException {
        return console.readPassword(fmt, args);
    }

    @Override
    public Writer getWriter() {
        return console.writer();
    }

    @Override
    public void writeLine(String format, Object... args) {
        console.printf(format, args);
    }

    @Override
    public boolean canRead() throws IOException {
        return console.reader().ready();
    }

    @Override
    public int read() throws IOException {
        return console.reader().read();
    }

    @Override
    public int getHeight() {
        return -1;
    }

    @Override
    public int getWidth() {
        return -1;
    }
}