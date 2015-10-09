package org.ow2.proactive.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


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