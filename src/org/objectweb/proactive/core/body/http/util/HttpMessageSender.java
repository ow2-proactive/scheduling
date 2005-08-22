/*
 * Created on Apr 8, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.body.http.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import org.objectweb.proactive.core.body.http.util.exceptions.HTTPRemoteException;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpMessageSender {
    public static final String SERVICE_REQUEST_URI = "/ProActiveHTTP";
    public static final String SERVICE_REQUEST_CONTENT_TYPE = "application/java";
    private String url;

    //    private int port

    /**
     *
     * @param url
     */
    public HttpMessageSender(String url) {
        this.url = url;
        //        this.port =  port;       
    }

    /**
     *
     * @param message
     * @return
     */
    public Object sendMessage(HttpMessage message) throws HTTPRemoteException {
        byte[] bytes = HttpMarshaller.marshallObject(message);

        String url_;

        //        try {
        //            url_ = UrlBuilder.checkUrl(url);
        //            System.out.println("************* " + url_);
        //        } catch (UnknownHostException e1) {
        //            // TODO Auto-generated catch block
        //            e1.printStackTrace();
        //        }
        try {
            String nodename = null;
            if (!url.startsWith("http:")) {
                url = "http:" + url;
            }
            int lastslash = url.lastIndexOf('/');
            if (lastslash > 6) {
                nodename = url.substring(lastslash);
                url = url.substring(0, lastslash);
            }
            int lastIndex = url.lastIndexOf(":");

            //            if (port == 0) {
            //                 System.out.println(url.length() + " -- " + url);
            //                 port = Integer.parseInt(url.substring(lastIndex + 1,
            //                                lastIndex + 5));
            //                }
            //
            //            if (lastIndex == 4) {
            //                  url = url + ":" + port;
            //        }
            URL u = new URL(url + SERVICE_REQUEST_URI);

            //connection to the specified url
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Length", "" + bytes.length);
            connection.setRequestProperty("Content-Type",
                SERVICE_REQUEST_CONTENT_TYPE);
            connection.setUseCaches(false);
            connection.connect();

            //write data in the stream
            BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());

            out.write(bytes);
            out.flush();
            out.close();

            //Get data returned in the connection
            DataInputStream in = null;

            in = new DataInputStream(new BufferedInputStream(
                        connection.getInputStream()));

            int a = connection.getContentLength();

            byte[] b = new byte[a];

            in.readFully(b);
            Object returnedObject = HttpMarshaller.unmarshallObject(b);
            return returnedObject;

            //            if (returnedObject instanceof Exception)
            //                throw (Exception)returnedObject;
        } catch (ConnectException e) {
            throw new HTTPRemoteException(
                "Error while connecting the remote host: " + url, e);
        } catch (UnknownHostException e) {
            throw new HTTPRemoteException("Unknown remote host: " + url, e);
        } catch (IOException e) {
            throw new HTTPRemoteException(
                "Error during connection with remote host" + url, e);
        }
    }
}
