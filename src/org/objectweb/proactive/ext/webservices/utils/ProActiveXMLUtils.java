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
package org.objectweb.proactive.ext.webservices.utils;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.http.BodyRequest;
import org.objectweb.proactive.core.body.http.HttpMessage;
import org.objectweb.proactive.core.runtime.http.RuntimeReply;
import org.objectweb.proactive.core.runtime.http.RuntimeRequest;

import sun.rmi.server.MarshalInputStream;
import sun.rmi.server.MarshalOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 *  ProActive HTTP Utilities class
 * @author virginie
 */

public class ProActiveXMLUtils {
    public static final String MESSAGE = "Message";
    public static final String RUNTIME_REQUEST = "RuntimeRequest";
    public static final String RUNTIME_REPLY = "RuntimeReply";
    public static final String PROACTIVE_MESSAGE = "ProActiveMessage";
    public static final String PROACTIVE_ACTION = "Action";
    public static final String PROACTIVE_OBJECT = "ProActiveObject";
    public static final String PROACTIVE_OAID = "ProActiveOAID";
    public static final String OK = "OK";
    public static final String NO_SUCH_OBJECT = "No Such Object Exception";
    private static final String LOOKUP = "Lookup";
    private static final String PROACTIVE_LOOKUP_RUNTIME = "Lookup_runtime";
    private static final String PROACTIVE_LOOKUP_RUNTIME_RESULT = "Lookup_runtime_result";
    private static final String PROACTIVE_LOOKUP = "Lookup";
    private static final String PROACTIVE_LOOKUP_RESULT = "LookupResult";
    private static final String PROACTIVE_ERROR = "Error";
    private static int tries = 0;
    public static final String ACTION_EXCEPTION = "Exception";
    private static transient Logger logger = Logger.getLogger("XML_HTTP");
    public static final String SERVICE_REQUEST_URI = "/ProActiveHTTP";
    public static final String SERVICE_REQUEST_CONTENT_TYPE = "application/java";

    /**
    * Serialize an object into a byte array
    * @param o The object you want to serialize
    */
    public static byte[] serializeObject(Object o) {
        String result = null;
        byte[] buffer = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MarshalOutputStream oos = null;

        try {
            oos = new MarshalOutputStream(out);
            oos.writeObject(o);
            buffer = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        result = new String(buffer);

        return buffer;
    }

    /**
     * Unmarshall an object from a byte array
     * @param buffer The byte array containing the serialized object
     */
    public static Object deserializeObject(byte[] buffer) {
        Object o = null;
        MarshalInputStream in = null;

        try {
            in = new MarshalInputStream(new ByteArrayInputStream(buffer));
            o = in.readObject();

            return o;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }


    public static byte[] getMessage(Object obj) {
        //        String message = createMessage(action);
        return serializeObject(obj);

        //        message += ("\t\t\t<" + PROACTIVE_OBJECT + ">");
        //        message += serializeObject(obj);
        //        message += ("\t\t\t</" + PROACTIVE_OBJECT + ">");
        //        
        //        message += endMessage();
        //        return message;
    }

    
    private static String getName() {
        try {
            return java.net.InetAddress.getLocalHost() + "";
        } catch (Exception e) {
            return "java.net.InetAddress.getLocalHost() IMPOSSIBLE";
        }
    }

    /**
     * Sends a message to the given url This message contains a serialized object.
     * @param url The targeted url
     * @param port The destination port
     * @param obj The objet contained in the message
     * @param action What to do with this object ?
     */
    public static Object sendMessage(String url, int port, Object obj,
        String action) throws Exception, HTTPRemoteException {
        byte[] message = getMessage(obj);
        
        //logger.info("sending Message to " + url);
        
        return sendMessage(url, port, message, action);
    }

    private static Object sendMessage(String url, int port, byte[] message,
        String action) throws Exception, HTTPRemoteException {
        try {
        	String nodename = null;
        	   
            if (!url.startsWith("http:")) {
                url = "http:" + url;
            }

            int lastIndex = url.lastIndexOf(":");

            int lastslash = url.lastIndexOf('/');
            if ( lastslash > 6) {
            	nodename = url.substring(lastslash);
            	url = url.substring(0, lastslash);
            }

            if (port == 0) {
                port = Integer.parseInt(url.substring(lastIndex + 1,
                            lastIndex + 5));
            }

            if (lastIndex == 4) {
                //    	 	url = url.substring(0,lastIndex);
                //    	 else
                url = url + ":" + port;
            }
            
            // Nodename is never used on the server
            //if(nodename != null ){
            //	url = url + nodename;
            //	
            //}

            URL u = new URL(url + SERVICE_REQUEST_URI);

            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" +
                message.length);
            connection.setRequestProperty("Content-Type", SERVICE_REQUEST_CONTENT_TYPE);
            connection.setRequestProperty("ProActive-Action", action);
            connection.setUseCaches(false);

            connection.connect();

            BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(message);
            out.flush();
            out.close();

            DataInputStream in = null;

            in = new DataInputStream(new BufferedInputStream(
                        connection.getInputStream()));

            int a = connection.getContentLength();
            byte[] b = new byte[connection.getContentLength()];

            //int totalRead = 0;
            //while (totalRead != b.length) {
            //	totalRead = in.read(b, totalRead, b.length - totalRead);
            //}
            in.readFully(b);

            Object rep = ProActiveXMLUtils.unwrapp(b,
                    connection.getHeaderField("ProActive-Action"));

            return rep;
        } catch (ConnectException e) {
            throw new HTTPRemoteException("Error while connecting the remote host: " + url, e);
        } catch (UnknownHostException e) {
            throw new HTTPRemoteException("Unknown remote host: " + url, e);
        } catch (IOException e) {
            throw new HTTPRemoteException("Error during connection with remote host" + url, e);
        }
    }

    /**
     *  Unmarshalles a message and performs an action according to the action field
     * @param msg The message contained in a byte array
     * @param action The action to perform with this message
     * @throws Exception
     */
    public static Object unwrapp(byte[] msg, String action)
        throws Exception {
        //InputStream in = new ByteArrayInputStream(msg);
        //Request paRequest = null;
        //Reply paReply = null;
        //Body body = null;
        //parser = new ProActiveXMLParser(in);
        //Object[] result;
        //String msg_ = new String(msg);
        //            try {
        //                result = (Object[]) parser.getResultObject();
        //            } catch (SAXException e1) {
        //                throw new ProActiveException(e1.getMessage());
        //            }
        //String action = (String) result[0];
        //String objectValue = (String) result[1];
        Object obj = deserializeObject(msg);

        if (action.equals(MESSAGE)) {
        	HttpMessage message = (HttpMessage) obj;

            return message;
        } else if (action.equals(RUNTIME_REQUEST)) {
        	RuntimeReply reply = null;
        	if( obj instanceof RuntimeRequest ){
        		RuntimeRequest rr = (RuntimeRequest) obj;
        		reply = rr.process();
        	}else	{
        		BodyRequest rr = (BodyRequest) obj;
        		reply = rr.process();
        	
        	} 

            return reply;
        } else if (action.equals(RUNTIME_REPLY)) {
            return (RuntimeReply) obj;
        } else if (action.equals(PROACTIVE_LOOKUP_RESULT)) {
            //System.out.println("ub = " + obj.getClass());
            if (obj instanceof UniversalBody) {
                return (UniversalBody) obj;
            } else {
                throw new HTTPRemoteException("Transfered class is not an instance of UniversalBody", new ProActiveException(
                        (String) obj));
            }
        } else if (action.equals(ACTION_EXCEPTION)) {
            throw (Exception) obj;
        }

        return null;
    }

    /**
     *  Search a Body matching with a given unique ID
     * @param id The unique id of the body we are searching for
     * @return The body associated with the ID
     */
    public static Body getBody(UniqueID id) {
  
    	LocalBodyStore bodyStore = LocalBodyStore.getInstance();

        
        Body body = bodyStore.getLocalBody(id);

        if (body == null) {
            body = LocalBodyStore.getInstance().getLocalHalfBody(id);
        }

        if (body == null) {
            body = LocalBodyStore.getInstance().getForwarder(id);

        }
        
   
        return body;
    }
}
