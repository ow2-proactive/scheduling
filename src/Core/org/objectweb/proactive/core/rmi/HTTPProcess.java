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

import org.objectweb.proactive.core.remoteobject.http.util.HttpMarshaller;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;


/**
 * @author vlegrand
 */
public class HTTPProcess {
    protected DataInputStream in;
    protected RequestInfo info;

    /**
     *
     * @param in
     * @param info
     */
    public HTTPProcess(DataInputStream in, RequestInfo info) {
        this.info = info;
        this.in = in;
    }

    /**
     *
     */
    public Object getBytes() {
        //        test ();
        Object result = null;
        byte[] replyMessage = null;
        String action = null;

        byte[] source = new byte[info.getContentLength()];
        int b;

        try {
            in.readFully(source);
            //                          System.out.println("SOURCE :");
            //                           for (int i=0; i< source.length ; i++) {
            //                               System.out.print((char)source[i]);
            //                           }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            HttpMessage message = (HttpMessage) HttpMarshaller.unmarshallObject(source);

            if (message != null) {
                result = message.processMessage();
            }
            Thread.currentThread().setContextClassLoader(cl);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // catch the exception and returns it
            //  e.printStackTrace();
            result = e;
        }

        return result;
    }
}
