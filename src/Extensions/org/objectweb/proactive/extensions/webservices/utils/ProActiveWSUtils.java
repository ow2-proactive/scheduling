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
package org.objectweb.proactive.extensions.webservices.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.activation.DataHandler;
import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;


/**
 * Web Services utilities
 * @author vlegrand
 */
public class ProActiveWSUtils {
    private static MessageFactory messageFactory;
    private static SOAPConnection connection;
    private static SOAPConnectionFactory soapConnFactory;
    private static SOAPFactory soapFactory;
    public static final String PA_WEBSERVICE = "ProActiveWebService";
    public static final String NAME = "name";
    public static final String UNDEPLOY = "undeploy";
    public static final String ACTION = "Action";
    public static final String ACTION_NAME = "actionName";
    public static final String WSDL = "wsdl";
    public static final String DEPLOY = "deploy";
    public static final String URN = "urn";
    public static final String OAID = "ObjectID";
    public static final String VN_NAME = "VirtualNodeName";
    public static final String RT_URL = "RuntimeUrl";
    private static boolean init = false;

    /**
     *
     *
     */
    private static void init() {
        if (!init) {
            try {
                soapFactory = SOAPFactory.newInstance();
                soapConnFactory = SOAPConnectionFactory.newInstance();
                connection = soapConnFactory.createConnection();
                messageFactory = MessageFactory.newInstance();
            } catch (SOAPException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param message
     * @param url
     */
    public static SOAPMessage sendMessage(String url, SOAPMessage message) {
        init();

        SOAPMessage reponse = null;

        try {
            URLEndpoint destination = new URLEndpoint(url);

            reponse = connection.call(message, destination);
            connection.close();
        } catch (SOAPException e) {
            return null;
        }

        return reponse;
    }

    /**
     *
     * @param message
     * @param file
     */
    public static SOAPMessage attachFile(SOAPMessage message, File file, String contentId) {
        init();

        String pathUrl = "file://" + file.getAbsoluteFile();

        try {
            URL url = new URL(pathUrl);
            DataHandler handler = new DataHandler(url);

            AttachmentPart attachment = message.createAttachmentPart(handler);
            attachment.setContentId(contentId);
            message.addAttachmentPart(attachment);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return null;
        }

        return message;
    }

    /**
     *
     */
    public static SOAPMessage createMessage() throws SOAPException {
        init();

        SOAPMessage message = messageFactory.createMessage();

        //message. 
        return message;
    }

    /**
     * @param string
     */
    public static Name createName(String string) throws SOAPException {
        init();

        return soapFactory.createName(string);
    }
}
