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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;


public class BoundedStringWriterTest {

    @Test
    public void append_empty_string() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BoundedStringWriter writer = new BoundedStringWriter(new PrintStream(new ByteArrayOutputStream()), 5);
        writer.append("");

        assertEquals("", output.toString());
        assertEquals("", writer.toString());
        assertEquals(0, writer.getBuilder().length());
    }

    @Test
    public void multiple_appends() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BoundedStringWriter writer = new BoundedStringWriter(new PrintStream(output), 10);
        writer.append("123");

        assertEquals("123", output.toString());
        assertEquals("123", writer.toString());
        assertEquals(3, writer.getBuilder().length());

        writer.append("456");

        assertEquals("123456", output.toString());
        assertEquals("123456", writer.toString());
        assertEquals(6, writer.getBuilder().length());
    }

    @Test
    public void append_over_boundaries() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BoundedStringWriter writer = new BoundedStringWriter(new PrintStream(output), 3);
        writer.append("123");

        assertEquals("123", output.toString());
        assertEquals("123", writer.toString());
        assertEquals(3, writer.getBuilder().length());

        writer.append("456");

        assertEquals("123456", output.toString());
        assertEquals("456", writer.toString());
        assertEquals(3, writer.getBuilder().length());

        writer.append("789789");

        assertEquals("123456789789", output.toString());
        assertEquals("789", writer.toString());
        assertEquals(3, writer.getBuilder().length());
    }
}
