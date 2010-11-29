package org.ow2.proactive.scheduler.ext.common.util;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.PAProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * PropertiesDumper
 *
 * @author The ProActive Team
 */
public class PropertiesDumper {

    static String[] excludePropertiesArray = new String[] { "java.rmi.server.codebase",
            "java.rmi.server.codebase", "proactive.http.port" };

    static HashSet<String> excludeProperties = new HashSet<String>();

    static {
        for (String p : excludePropertiesArray) {
            excludeProperties.add(p);
        }
    }

    public static void dumpProperties(File file) throws IOException {
        if (file.exists()) {
            if (!file.canWrite()) {
                throw new IllegalArgumentException("File " + file + " exists and is write-protected.");
            }
            file.delete();
        }
        Element root = new Element("ProActiveUserProperties");
        Document document = new Document(root);
        Element props = new Element("properties");
        root.addContent(props);

        Map<Class<?>, List<PAProperty>> allProperties = PAProperties.getAllProperties();
        for (Class<?> cl : allProperties.keySet()) {

            for (PAProperty prop : allProperties.get(cl)) {
                if ((prop.getValueAsString() != null) && (!excludeProperties.contains(prop.getName()))) {

                    Element propel = new Element("prop");
                    Attribute key = new Attribute("key", prop.getName());
                    Attribute value = new Attribute("value", prop.getValueAsString());
                    propel.setAttribute(key);
                    propel.setAttribute(value);
                    props.addContent(propel);
                }
            }
        }
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        out.output(document, new FileOutputStream(file));
        file.deleteOnExit();

    }
}
