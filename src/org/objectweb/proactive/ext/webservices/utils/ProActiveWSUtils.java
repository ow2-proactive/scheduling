package org.objectweb.proactive.ext.webservices.utils;

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


/*
 * Created on Feb 11, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author vlegrand
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
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
    public static SOAPMessage attachFile(SOAPMessage message, File file,
        String contentId) {
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
