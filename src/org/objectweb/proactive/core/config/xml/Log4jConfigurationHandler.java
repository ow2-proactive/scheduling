package org.objectweb.proactive.core.config.xml;

import org.apache.log4j.PropertyConfigurator;

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;


public class Log4jConfigurationHandler extends AbstractUnmarshallerDecorator
    implements MasterFileConstants {
    public Log4jConfigurationHandler() {
        super();
    }

    public Object getResultObject() throws org.xml.sax.SAXException {
        return null;
    }

    public void startContextElement(String name, Attributes attributes)
        throws org.xml.sax.SAXException {
        String file = attributes.getValue("file");
        PropertyConfigurator.configure(relativeToAbsolute(file));
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    }

    protected String relativeToAbsolute(String path) {
        String s2 = path;

        //this test doesn't look good
        //maybe we should rather try to open the file using the path
        //and convert it if we cannot
        if (path.indexOf("/") != 0) {
            //we have a relative path, we should turn it into 
            //an absolute one
            String s = ProActiveConfiguration.class.getResource("/" +
                    ProActiveConfiguration.class.getName().replace('.', '/') +
                    ".class").getPath();
            s2 = s.replaceAll("ProActiveConfiguration.class", path);
        }

        return s2;
    }
}
