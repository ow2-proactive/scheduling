/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.objectweb.proactive.core.descriptor.xml.MasterPropertiesFileHandler;


/**
 *
 * @author The ProActive Team
 * The  class
 */
public class XMLProperties {
    private class PropertiesDatas {
        public String value;
        public int type;
    }

    private static XMLProperties instance = new XMLProperties();
    private HashMap list;

    private XMLProperties() {
        list = new HashMap();
    }

    //	public static XMLProperties getInstance() { return instance;}

    /**
     * Call this static method to remove all XML properties.
     * When a descriptor parsing fail with throwing a exception, you must clean properties manually.
     */
    public static void clean() {
        instance.list.clear();
    }

    /**
     * Static method to transform a string with properties to decoded version.
     * @param         value        Text with properties inside to translates.
     * @return        string with properties swapped to their text value.
     */
    public static String transform(String value) {
        return instance.transformValue(value);
    }

    /**
     * Static method to add a new program set property.
     * @param name        The name of the property.
     * @param value        Text to swap with coding property name.
     * @throws org.xml.sax.SAXException
     */
    public static void setVariableValue(String name, String value,
        String typeName) throws org.xml.sax.SAXException {
        int type = instance.type(typeName);
        if ((type != XMLPropertiesType.PROGRAM_SET) &&
                (type != XMLPropertiesType.OVERRIDABLE_IN_PROG) &&
                (type != XMLPropertiesType.OVERRIDABLE_IN_XML)) {
            throw new org.xml.sax.SAXException("Property XML " + name +
                " have no valid type for using in program !");
        }
        try {
            instance.add(name, value, type);
        } catch (org.xml.sax.SAXException ex) {
            throw ex;
        }
    }

    /**
     * Static method to add a table of program set properties.
     * @param map        HashMap with key=name:String and value=textToSwap:String.
     * @throws org.xml.sax.SAXException
     */
    public static void setVariableValue(HashMap map, String type)
        throws org.xml.sax.SAXException {
        String name;
        java.util.Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            name = (String) it.next();
            XMLProperties.setVariableValue(name, (String) map.get(name), type);
        }
    }

    /**
     * Static method to add a property.
     * It's possible to redefine property but only with same type.form descriptor.
     * @param name        The name of the property.
     * @param value        Text to swap with coding property name.
     * @param type        Type of property must be "constant", "overridable" or "programset".
     * <P style="margin-left: 1.3cm">If type is "constant", value must be set in descriptor, and it's impossible to redefine the value of the proterty.</P>
     * <P style="margin-left: 1.3cm">If type is "overridable" value must be set in descriptor, and it's possible to redifine it with the API.</P>
     * <P style="margin-left: 1.3cm">If type is "programset", value cannot be set in descriptor, value has to be defined with the API.</P>
     * @throws org.xml.sax.SAXException
     */
    public static void setDescriptorVariable(String name, String value,
        String type) throws org.xml.sax.SAXException {
        int intType = instance.type(type);
        instance.addFromDescriptor(name, value, intType);
    }

    public static void load(String file) throws org.xml.sax.SAXException {
        Properties properties = new Properties();

        // Open the file
        try {
            FileInputStream stream = new FileInputStream(file);
            properties.load(stream);
        } catch (FileNotFoundException ex) {
            throw new org.xml.sax.SAXException(
                "Tag property cannot open file : [" + file + "]");
        } catch (java.io.IOException ex) {
            throw new org.xml.sax.SAXException(
                "Tag property cannot read file : [" + file + "]");
        } catch (java.lang.IllegalArgumentException ex) {
            throw new org.xml.sax.SAXException(
                "Tag property cannot parse file : [" + file + "]");
        }

        String name;
        String value;
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            name = (String) it.next();
            value = properties.getProperty(name);
            instance.addFromDescriptor(name, value, XMLPropertiesType.CONSTANT);
        }
    }

    public static void loadXML(String file) throws org.xml.sax.SAXException {
        MasterPropertiesFileHandler.createMasterFileHandler(file);
    }

    private String transformValue(String text) {
        //if ( text==null || text.equals("")) return text;
        do {
            // try to find the begining of property
            int begin = text.indexOf("${");
            if (begin < 0) {
                break;
            }
            begin += 2;
            // find the end of proprety name
            int end = text.indexOf("}");
            if (end < 0) {
                break;
            }
            String endText = text.substring(end + 1, text.length());

            // the name is empty ?
            if (begin == end) {
                if (begin > 2) {
                    text = text.substring(0, begin - 2) + endText;
                } else {
                    text = endText;
                }
                continue;
            }

            // build the name of the proterty
            String name = text.substring(begin, end);

            // if the property name does'n exist just return.
            if (list.containsKey(name) != true) {
                break;
            }
            PropertiesDatas datas = (PropertiesDatas) list.get(name);

            // check if there are some chars to keep at the begining of text.  
            if (begin > 2) {
                text = text.substring(0, begin - 2) + datas.value + endText;
            } else {
                text = datas.value + endText;
            }
        } while (true);

        return text;
    }

    private void addFromDescriptor(String name, String value, int type)
        throws org.xml.sax.SAXException {
        String message = " have no valid type for using in descriptor !";

        // Check if parameter type is valide.
        switch (type) {
        case XMLPropertiesType.CONSTANT:
            // if the new property have type CONSTANT and name is already in the list
            // => exception
            if (list.containsKey(name)) {
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " already exist in the list of properties !");
            }
            break;
        case XMLPropertiesType.JAVA_PROPERTY:
            if (list.containsKey(name)) {
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " already exist in the list of properties !");
            }
            try {
                String jvmValue = System.getProperty(name);
                value = new String(jvmValue);
            } catch (Exception ex) {
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " is an unknown Java properties !");
            }
            break;
        case XMLPropertiesType.OVERRIDABLE_IN_XML:
            if (!list.containsKey(name)) {
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " with type overridableInXML not exist in the list of properties !");
            }
            if (value.equals("")) {
                return;
            }
            break;
        case XMLPropertiesType.OVERRIDABLE_IN_PROG:
            if (!list.containsKey(name)) {
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " with type overridableInProgram not exist in the list of properties !");
            }
            if (value.equals("")) {
                return;
            }
            PropertiesDatas datas = (PropertiesDatas) list.get(name);
            if (!datas.value.equals("")) {
                return;
            }
            break;
        case XMLPropertiesType.PROGRAM_SET:
            // if the property is not in the list of properties
            // => exception
            if (!list.containsKey(name)) {
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " with type PROGRAM SET not exist in the list of properties !");
            }
            return;
        default:
            throw new org.xml.sax.SAXException("Property XML " + name +
                " with value " + value + message);
        }

        add(name, value, type);
    }

    private void add(String name, String value, int type)
        throws org.xml.sax.SAXException {
        // first look if name already in list.
        if (list.containsKey(name)) {
            PropertiesDatas datas = (PropertiesDatas) list.get(name);
            if (datas.type == XMLPropertiesType.CONSTANT) {
                // the property name already exist in the list and his type is CONSTANT
                // => exception
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " already exist and is CONSTANT !");
            }
            if (datas.type == XMLPropertiesType.PROGRAM_SET) {
                // the property name already exist in the list and his type is PROGRAM SET
                // => exception
                throw new org.xml.sax.SAXException("Property XML " + name +
                    " already exist and is PROGRAM SET !");
            }

            // Modification au property value is available.
            datas.value = value;
            datas.type = type;
        } else { // The name is unknown so it add to the list
            PropertiesDatas datas = new PropertiesDatas();
            datas.value = value;
            datas.type = type;
            list.put(name, datas);
        }
    }

    private int type(String text) {
        if (text.equals("constant")) {
            return XMLPropertiesType.CONSTANT;
        } else if (text.equals("javaProperty")) {
            return XMLPropertiesType.JAVA_PROPERTY;
        } else if (text.equals("overridableInXML")) {
            return XMLPropertiesType.OVERRIDABLE_IN_XML;
        } else if (text.equals("overridableInProgram")) {
            return XMLPropertiesType.OVERRIDABLE_IN_PROG;
        } else if (text.equals("setInProgram")) {
            return XMLPropertiesType.PROGRAM_SET;
        }
        return -1;
    }
}
