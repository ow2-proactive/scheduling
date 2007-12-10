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

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.remoteobject.http.util.HttpUtils;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * @author vlegrand
 *
 * This class is used to make a new Thread in the Class server when a request incomes.
 * It calls the right service (or "module") to perform the request and send back the appropriate response.
 * For example, when a request for a class file incomes, the thread calls the FileProcess.
 */
public class HTTPRequestHandler extends Thread {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);
    private Socket socket;
    private String paths;
    private InputStream in;
    private OutputStream out;
    private RequestInfo reqInfo;
    private HttpServletResponse response;

    public HTTPRequestHandler(Socket socket, String paths)
        throws IOException {
        this(socket.getInputStream(), socket.getOutputStream());
        this.socket = socket;
        this.paths = paths;
    }

    public HTTPRequestHandler(InputStream in, OutputStream out,
        RequestInfo reqInfo, HttpServletResponse response) {
        this(in, out, reqInfo);
        this.response = response;
    }

    public HTTPRequestHandler(InputStream in, OutputStream out,
        RequestInfo reqInfo) {
        this(in, out);
        this.reqInfo = reqInfo;
    }

    public HTTPRequestHandler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    //    public void test () {
    //        
    //        /* -------------------------------------------------------------------------- */
    ////      byte[] source = new byte[reqInfo.getContentLength()];
    ////      
    ////      System.out.println("HTTP Request Handler    TEST  Content - Length ");
    //      int b;
    //      try {
    //          
    //          b = in.read();
    //          
    //          System.out.println("B = " + b);
    //          int count = 0;
    //          while (b != -1 ) {
    //              System.out.print((char)b);
    //              b = (byte)in.read();
    //              count++;
    //          }
    //          System.out.println("==> count = " + count);
    //      } catch (IOException e1) {
    //          // TODO Auto-generated catch block
    //          e1.printStackTrace();
    //      }
    //    }
    @Override
    public void run() {
        HTTPInputStream httpIn = null;
        DataOutputStream dOut = null;
        String responseHeaders = "";
        String statusLine = null;
        String contentType = null;
        byte[] bytes = null;

        try {
            dOut = new java.io.DataOutputStream(this.out);

            // Get the headers information in order to determine what is the requested service
            httpIn = new HTTPInputStream(new BufferedInputStream(this.in));

            try {

                /* ----------------- Here we get the request headers  */
                if (this.reqInfo == null) {
                    this.reqInfo = new RequestInfo();
                    reqInfo.read(httpIn);
                }

                if (!reqInfo.hasInfos()) {
                    return;
                }

                /* fix the jini class loader problem */
                if (this.reqInfo.getPreferredList()) {
                    statusLine = "HTTP/1.1 200 OK";
                    bytes = "PreferredResources-Version: 1.0\nPreferred: false".getBytes();
                }
                // If  there is no field ClassFileName then it is a call to the
                // ProActive Request via HTTP
                else if (this.reqInfo.getClassFileName() == null) {
                    HTTPProcess process = new HTTPProcess(httpIn, this.reqInfo);
                    Object o = process.getBytes();
                    bytes = HttpMarshaller.marshallObject(o);

                    /////////////////////////////////
                    statusLine = "HTTP/1.1 200 OK";
                    contentType = HttpUtils.SERVICE_REQUEST_CONTENT_TYPE;
                } else {
                    // ClassServer request
                    FileProcess fp = new FileProcess(paths, this.reqInfo);
                    bytes = fp.getBytes();
                    statusLine = "HTTP/1.1 200 OK";
                    contentType = "application/java";
                }
            } catch (ClassNotFoundException e) {
                logger.info("ClassServer failed to load class " +
                    this.reqInfo.getClassFileName());
                statusLine = "HTTP/1.1 404 " + e.getMessage();
                contentType = "text/plain";
                bytes = new byte[0];
            } catch (IOException e) { // Including HTTPRemoteException & co
                statusLine = "HTTP/1.1 500 " + e.getMessage();
                contentType = "text/plain";
                bytes = new byte[0];
            }

            if (!PAProperties.PA_HTTP_SERVLET.isTrue()) {
                dOut.writeBytes(statusLine + "\r\n");
                dOut.writeBytes("Content-Length: " + bytes.length + "\r\n");
                dOut.writeBytes("Content-Type: " + contentType + "\r\n");
                dOut.writeBytes(responseHeaders);
                dOut.writeBytes("\r\n");
            } else {
                response.setContentLength(bytes.length);
                response.setContentType(contentType);
            }
            dOut.write(bytes);
            dOut.flush();

            if (reqInfo.getClassFileName() != null) {
                if (logger.isDebugEnabled()) {
                    logger.info("ClassServer sent class " +
                        reqInfo.getClassFileName() + " successfully");
                }
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
                dOut.close();
                out.close();

                if (socket != null) {
                    socket.close();
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}
