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

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;


/**
 *
 * @author vlegrand
 *
 * This class is used to make a new Thread in the Class server when a request incomes.
 * It calls the right service (or "module") to perform the request and send back the appropriate response.
 * For example, when a request for a class file incomes, the thread calls the FileProcess.
 */
public class ProActiveService extends Thread {
    protected static Logger logger = Logger.getLogger(ClassServer.class.getName());
    private final Socket socket;
    private String paths;

    public ProActiveService(Socket socket, String paths) {
        this.socket = socket;
        this.paths = paths;
    }

    public void run() {
        HTTPInputStream in = null;
        DataOutputStream out = null;
        RequestInfo info = new RequestInfo();

        String responseHeaders = "";
        String statusLine = null;
        String contentType;
        byte[] bytes = null;

        try {
            out = new java.io.DataOutputStream(socket.getOutputStream());

            // Get the headers information in order to determine what is the requested service
            in = new HTTPInputStream(new BufferedInputStream(
                        socket.getInputStream()));


// Process several requests in a row if needed (HTTP/1.1 persistent connection)
process_request: 
            while (true) {
                try {
                    info.read(in);

                    if (!info.isBegun()) {
                        break process_request; // connection was closed by the client
                    }
                    // else we successfully read the request information
                    
                    // If  there is no field ClassFileName then it is a call to the
                    // ProActive Request via HTTP
                    if (info.getClassFileName() == null) {
                        HTTPProcess process = new HTTPProcess(in, info);
                        MSG msg = process.getBytes();
                        String actionType = msg.getAction();
                        bytes = msg.getMessage();

                        statusLine = "HTTP/1.1 200 OK";
                        contentType = "text/xml";
                        responseHeaders = "ProActive-Action: " + actionType +
                            "\r\n";
                    } else {
                        // ClassServer request
                        FileProcess fp = new FileProcess(paths, info);
                        bytes = fp.getBytes();
                        statusLine = "HTTP/1.1 200 OK";
                        contentType = "application/java";
                        logger.info("ClassServer sent class " + info.getClassFileName() + " successfully");
                    }
                } catch (ClassNotFoundException e) {
                    logger.info("ClassServer failed to load class " +
                        info.getClassFileName());
                    statusLine = "HTTP/1.1 404 " + e.getMessage();
                    contentType = "text/plain";
                    bytes = new byte[0];
                } catch (IOException e) { // Including HTTPRemoteException & co
                    statusLine = "HTTP/1.1 500 " + e.getMessage();
                    contentType = "text/plain";

                    // Time-consuming and not very useful:
                    // StringBuffer buf = new StringBuffer();
                    // StackTraceElement[] trace = e.getStackTrace();
                    // for (int i = 0; i < trace.length; i++) {
                    // 	buf.append(trace[i].toString());
                    // 	buf.append("\n");
                    // }
                    // bytes = buf.toString().getBytes();
                    bytes = new byte[0];
                }

                out.writeBytes(statusLine + "\r\n");
                out.writeBytes("Content-Length: " + bytes.length + "\r\n");

                int a = bytes.length;
                String b = "Content-Length: " + bytes.length + "\r\n";
                out.writeBytes("Content-Type: " + contentType + "\r\n");
                out.writeBytes(responseHeaders);
                out.writeBytes("\r\n");
                out.write(bytes);
                out.flush();
            }
        } catch (IOException e) {
            // If there is an error when writing the reply,
            // nothing can be told to the caller...
            e.printStackTrace();
        } finally {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Closing socket " + this.socket);
                }

                out.close();
                in.close();
                socket.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
