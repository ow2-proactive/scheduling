package org.ow2.proactive.scheduler.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ow2.proactive.addons.email.EmailSender;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class SendMail {

    public void sender(String to, String subject, String body) {
        final Properties properties = EmailConfiguration.getConfiguration().getProperties();

        EmailSender.Builder builder = new EmailSender.Builder(properties);
        builder.setFrom(PASchedulerProperties.EMAIL_NOTIFICATIONS_SENDER_ADDRESS.getValueAsString());
        builder.addRecipient(to);
        builder.setSubject(subject);
        builder.setBody(body);
        builder.build().sendPlainTextEmail();
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