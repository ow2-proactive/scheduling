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
package trywithcatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class Catcher {
    private String filename;
    private List<Anything> parseData;
    private InputStream input;
    private OutputStream output;
    private int offsetDelta = 0;
    private int inputOffset = 0;
    public static final String INDENT = "    ";

    public Catcher(String f, OutputStream out, List<Anything> p) {
        filename = f;
        parseData = p;
        output = out;
    }

    public void work() {
        File inputFile = new File(filename);
        try {
            input = new FileInputStream(inputFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        try {
            for (Anything a : parseData) {
                a.work(this);
            }
            completeFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void catchUpFile(int newInputOffset) throws IOException {
        if (newInputOffset <= inputOffset) {
            return;
        }

        int delta = newInputOffset - inputOffset;
        byte[] data = new byte[delta];
        if (input.read(data) != delta) {
            throw new IOException();
        }
        inputOffset = newInputOffset;

        output.write(data);
    }

    public void addAtOffset(int offset, String str) throws IOException {
        catchUpFile(offset);

        byte[] data = str.getBytes();
        output.write(data);
        offsetDelta += data.length;
    }

    private void completeFile() throws IOException {
        byte[] data = new byte[4096];
        int len;
        while ((len = input.read(data)) > 0) {
            output.write(data, 0, len);
        }
    }

    public static String getNewline(Terminal align) {
        int indentLevel = align.getColumn();
        String str = "\n";
        while (indentLevel > 0) {
            str += " ";
            indentLevel--;
        }

        return str;
    }

    private void removeUntil(char b) throws IOException {
        int r;
        do {
            r = input.read();
            if (r == -1) {
                throw new IOException();
            }

            inputOffset++;
            offsetDelta--;
        } while (r != b);
    }

    public void removeCallAtOffset(int offset) throws IOException {
        catchUpFile(offset);

        removeUntil(';');
        removeUntil('\n');
    }
}
