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
