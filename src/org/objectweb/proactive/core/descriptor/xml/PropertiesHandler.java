package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.xml.XMLProperties;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.io.Attributes;


public class PropertiesHandler extends BasicUnmarshaller {
    PropertiesHandler() {
    }

    public void startContextElement(String tag, Attributes attributes)
        throws org.xml.sax.SAXException {
        // First control if it's a file tag
//        String file = attributes.getValue("propertiesFile");
//        if (checkNonEmpty(file)) {
//            // Specific processing for loading file
//            XMLProperties.load(file);
//            return;
//        }

        // get datas
        String name = attributes.getValue("name");
        if (!checkNonEmpty(name)) {
            throw new org.xml.sax.SAXException("Tag property have no name !");
        }
        String type = attributes.getValue("type");
        if (!checkNonEmpty(type)) {
            throw new org.xml.sax.SAXException("Tag property " + name +
                " have no type !");
        }
        String value = attributes.getValue("value");
        if ((!checkNonEmpty(value)) && (!type.equalsIgnoreCase("programset"))) {
            throw new org.xml.sax.SAXException("Tag property " + name +
                " have no value !");
        }

        // add property informations to list
        try {
            XMLProperties.setDescriptorVariable(name, value, type);
        } catch (org.xml.sax.SAXException ex) {
            throw ex;
        }
    }
}
