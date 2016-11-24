package org.ow2.proactive.scheduler.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;


import org.apache.log4j.Logger;

import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class SendMail {

    public void sender(String to, String subject, String body) throws MessagingException {
        final Properties properties = EmailConfiguration.getConfiguration().getProperties();

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("mail.smtp.username"),
                        properties.getProperty("mail.smtp.password"));
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.getValueAsString()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

}

class EmailConfiguration{
    private String path;
    private static EmailConfiguration configuration;
    private static Logger logger = Logger.getLogger(EmailConfiguration.class);

    private EmailConfiguration() {
        path = PASchedulerProperties.EMAIL_NOTIFICATIONS_CONFIGURATION.getValueAsString();
    }

    public static synchronized EmailConfiguration getConfiguration() {
        if(configuration == null){
            configuration = new EmailConfiguration();
        }
        return configuration;
    }

    public String getPath(){
        if(path!=null && path.length() > 0){
            path = PASchedulerProperties.getAbsolutePath(path);
        }
        return path;
    }

    public Properties getProperties(){
        Properties props = new Properties();
        try{
            InputStream fis = new FileInputStream(getPath());
            props.load(fis);
        }catch(IOException e){
            logger.warn("Email Configuration file: "+getPath()+" not found!", e);
        }
        return props;
    }
}