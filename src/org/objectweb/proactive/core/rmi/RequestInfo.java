/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.rmi;

import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;

import java.io.IOException;

import java.util.regex.Pattern;


public class RequestInfo {
    /* Reusable variables */
    
    private static Pattern pSpace = Pattern.compile(" ");
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private String contentType;
    private int contentLength;
    private String classFileName;
    private String action;
    private boolean begun;

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getClassFileName() {
        return classFileName;
    }

    public String getAction() {
        return action;
    }

    public boolean isBegun() {
        return begun;
    }

    /**
     * Read the InputStream, which points to the beginning of a HTTP Request,
     * and extract the necessary information
     * @return true on success, false if the connection was closed it could read anything
     **/
    public void read(HTTPInputStream in) throws IOException {
        String line;

        /* Clear the previous information */
        classFileName = null;
        contentLength = 0;
        contentType = null;
        action = null;

        /* Read request line */
        if ((line = in.getLine()) == null) {
            // Reused connection must have been closed by the client
            begun = false;
            return;
        } else {
        	begun = true;
        }
        
        String[] triplet = pSpace.split(line, 3);
        String method = null;
        String requestURI = null;

        try {
            method = triplet[0];
            requestURI = triplet[1];

            String HTTPVersion = triplet[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new java.io.IOException(
                "Malformed Request Line, expected 3 elements: " + line);
        }

        if (method.equals(METHOD_GET)) {
            // Extract the Path information
            int index = requestURI.indexOf(".class");

            if (index > 1) {
                this.classFileName = requestURI.substring(1, index).replace('/',
                        '.');
            } else {
                throw new java.io.IOException(
                    "Malformed Request Line, expected a path to a .class file: " +
                    line);
            }

            // Eat up headers, the ClassServer do not need them
            do {
                if ((line = in.getLine()) == null) {
                    throw new IOException(
                        "Connection ended before reading all headers");
                }
            } while (line.length() > 0); // empty line, end of headers
        } else if (method.equals(METHOD_POST)) {
            if (!requestURI.equals(ProActiveXMLUtils.SERVICE_REQUEST_URI)) {
                throw new java.io.IOException(
                    "Malformed Request Line, expected " +
                    ProActiveXMLUtils.SERVICE_REQUEST_URI + " as path: " +
                    line);
            }

            /* Read message headers */
            in.parseHeaders();
            
            // ProActive specific processing
            this.contentLength = Integer.parseInt(in.getHeader("Content-Length"));
            this.contentType = in.getHeader("Content-Type");

            if (!contentType.equals(
                        ProActiveXMLUtils.SERVICE_REQUEST_CONTENT_TYPE)) {
                throw new java.io.IOException(
                    "Malformed header, expected Content-Type = " +
                    ProActiveXMLUtils.SERVICE_REQUEST_CONTENT_TYPE + ": " +
                    contentType);
            }

            this.action = in.getHeader("ProActive-Action");
        } else {
            throw new java.io.IOException(
                "Malformed Request Line, expected method GET or POST: " + line);
        }
    }
}
