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
package org.objectweb.proactive.core.rmi;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.regex.Pattern;


/*
 * Created on Jul 29, 2004
 */

/**
 * @author Sylvain Beucler
 *
 * Simple extension of the DataInputStream class, providing a LGPL
 * non-deprecated version of readLine
 */
public class HTTPInputStream extends DataInputStream {
    private static Pattern pColon = Pattern.compile(": *");
    HashMap headers = new HashMap();

    public HTTPInputStream(InputStream is) {
        super(is);
        in = new PushbackInputStream(in);
    }

    /**
     * It is a reimplementation of readLine, renamed getLine since that method
     * is final in DataOutputStream
     */
    public String getLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        boolean readAtLeastOneChar = false;

        // read until meet '\r', '\n' or '\r\n'
        while (((c = in.read()) > 0) && (c != '\n')) {
            readAtLeastOneChar = true;

            if (c == '\r') {
                int c2 = in.read();

                if ((c2 > 0) && (c2 != '\n')) {
                    // insert the character back in the stream
                    ((PushbackInputStream) in).unread(c2);
                }

                // else { ignoreLF(); }
                break;
            } else {
                sb.append((char) c);
            }
        }

        if (readAtLeastOneChar == false) {
            return null; // read nothing
        } else {
            return sb.toString();
        }
    }

    /* Read message headers */
    public void parseHeaders() throws IOException {
        String line;
        String prevFieldName = null;
        headers.clear();

        do {
            if ((line = getLine()) == null) {
                throw new IOException("Connection ended before reading all headers");
            }

            if ((line.length() > 0)) { // we don't care about headers in GET requests
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    headers.put(prevFieldName, headers.get(prevFieldName) + line); // folding
                }

                String[] pair = pColon.split(line, 2);
                String fieldName = prevFieldName = pair[0].toLowerCase();
                String fieldValue = pair[1];

                String storedFieldValue = (String) headers.get(fieldName);

                if (storedFieldValue == null) {
                    headers.put(fieldName, fieldValue);
                } else {
                    headers.put(fieldName, storedFieldValue + "," + fieldValue);
                }

                headers.put(fieldName, fieldValue);
            }
        } while (line.length() > 0); // empty line, end of headers
    }

    public String getHeader(String fieldName) {
        return (String) headers.get(fieldName.toLowerCase());
    }
}
