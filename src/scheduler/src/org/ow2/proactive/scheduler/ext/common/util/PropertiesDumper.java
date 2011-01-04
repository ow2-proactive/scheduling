/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
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
