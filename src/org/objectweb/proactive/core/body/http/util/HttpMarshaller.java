/*
 * Created on Apr 8, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.core.body.http.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sun.rmi.server.MarshalInputStream;
import sun.rmi.server.MarshalOutputStream;


/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpMarshaller {

    /**
     *
     * @param o
     * @return
     */
    public static byte[] marshallObject(Object o) {
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

    public static Object unmarshallObject(byte[] bytes) {
        Object o = null;
        MarshalInputStream in = null;

        try {
            in = new MarshalInputStream(new ByteArrayInputStream(bytes));
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
}
