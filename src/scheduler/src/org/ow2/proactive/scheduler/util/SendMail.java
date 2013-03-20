package org.ow2.proactive.scheduler.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;


public class SendMail {

    private static Logger logger = Logger.getLogger(SendMail.class);
    private static final String SMTP_HOST_PROPERTY_NAME = "mail.smtp.host";
    private static final String DEFAULT_SMTP_HOST = "localhost";
    private static final Session session;

    static {
        Properties properties = System.getProperties();
        if (properties.getProperty(SMTP_HOST_PROPERTY_NAME) == null) {
            properties.setProperty(SMTP_HOST_PROPERTY_NAME, DEFAULT_SMTP_HOST);
        }
        session = Session.getDefaultInstance(properties);
    }

    public void send(String from, String to, String subject, String body) throws AddressException,
            MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }
}