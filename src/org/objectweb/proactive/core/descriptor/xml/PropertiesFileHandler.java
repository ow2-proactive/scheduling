package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.xml.XMLProperties;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.io.Attributes;


public class PropertiesFileHandler extends BasicUnmarshaller {
    PropertiesFileHandler() {
    }

    public void startContextElement(String tag, Attributes attributes)
        throws org.xml.sax.SAXException {
        // First control if it's a file tag
        String file = attributes.getValue("location");
        if (checkNonEmpty(file)) {
            // Specific processing for loading file
            XMLProperties.load(file);
            return;
        }

    }
}
