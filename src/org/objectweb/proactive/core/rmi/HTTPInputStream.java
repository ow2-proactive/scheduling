package org.objectweb.proactive.core.rmi;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;


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
    public HTTPInputStream(InputStream is) {
        super(is);
        in = in = new PushbackInputStream(in);
    }

    /**
     * It is a reimplementation of readLine, renamed getLine since that method
     * is final in DataOutputStream
     */
    public String getLine() throws IOException {
        StringBuffer sb = new StringBuffer();
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
}
