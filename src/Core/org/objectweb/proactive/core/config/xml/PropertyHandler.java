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
package org.objectweb.proactive.core.config.xml;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PropertyHandler extends DefaultHandler {
    public static final String MASTER_TAG = "ProActiveUserProperties";
    public static final String PROPERTIES_TAG = "properties";
    public static final String PROP_TAG = "prop";
    private Properties properties = null;

    public PropertyHandler(Properties properties) {
        this.properties = properties;
    }

    /**
     * Create a SAX parser on the specified file
     * @param filename the full path to the file
     */
    public static Properties createMasterFileHandler(String filename,
        Properties properties) {
        if (properties == null) {
            properties = new Properties();
        }

        try {
            InputSource source = null;

            //System.out.println("FILENAME = " + filename);
            if (filename.startsWith("bundle://")) {
                /* osgi mode, get the ProActiveConfiguration in the jar root */
                filename = "/ProActiveConfiguration.xml";
                //filename = "/org/objectweb/proactive/core/config/ProActiveConfiguration.xml";
                source = new InputSource(PropertyHandler.class.getResourceAsStream(
                            filename));
            } else {
                source = new org.xml.sax.InputSource(filename);
            }
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(source, new PropertyHandler(properties));

            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return new Properties();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) {
        if (qName.equals(PROP_TAG)) {
            String key = attributes.getValue("key");
            String value = attributes.getValue("value");

            this.properties.setProperty(key, value);
        }
    }
}
