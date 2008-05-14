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
package org.objectweb.proactive.benchmarks.timit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * An helper class for reading and writing XML files
 *
 * @author The ProActive Team
 */
public class XMLHelper {

    /**
     * Get XML Document thanks to its filename
     *
     * @param filename
     *            the XML file name
     * @return the XML Document
     */
    public static Document readFile(String filename) {
        return readFile(new File(filename));
    }

    /**
     * Get XML Document thanks to its filename
     *
     * @param the XML file
     * @return the XML Document
     */
    public static Document readFile(File file) {
        try {
            return new SAXBuilder().build(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save document into XML file
     *
     * @param document
     *            the Document to save
     * @param filename
     *            the XML file name to save
     */
    public static void writeFile(Document document, String filename) {
        try {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

            FileOutputStream fos = new FileOutputStream(XMLHelper.createFileWithDirs(filename));
            out.output(document, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            System.err.println("Unable to write the XML file: " + filename);
            e.printStackTrace();
        }
    }

    /**
     * Save document into XML file
     *
     * @param document
     *            the Document to save
     * @param filename
     *            the XML file name to save
     */
    public static void writeFile(Document document, File file) {
        try {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            FileOutputStream fos = new FileOutputStream(file);
            out.output(document, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            System.err.println("Unable to write the XML file: " + file);
            e.printStackTrace();
        }
    }

    public static File createFileWithDirs(String filename) {
        try {
            File file = new File(filename);

            String path = file.getParent();
            if (path != null) {
                new File(path).mkdirs();
            }
            return file;
        } catch (Exception e) {
            System.err.println("Unable to create file: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method allow to append a message to an error file
     *
     * @param filename
     *            the error file to write
     * @param message
     *            the message to append to file
     */
    public static void errorLog(String filename, String message) {
        try {
            File file = new File(filename);
            String path = file.getParent();
            if (path != null) {
                new File(path).mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file, true); // true->append
            String time = "" + (new java.sql.Timestamp(System.currentTimeMillis()));
            message = time + "  " + message + "\n";
            fos.write(message.getBytes());
            fos.close();
        } catch (Exception e) {
            System.err.println("Unable to write file: " + filename);
            e.printStackTrace();
        }
    }

    /**
     * Generate a ProActive descriptor file thanks to a pad base file. Variables
     * in this pad will be reassigned. Note: local variables mask global
     * variable
     *
     * @param inFilename
     *            the ProActive descriptor base file
     * @param gvars
     *            the global variables
     * @param lvars
     *            the local variables
     * @param outFilename
     *            the generated pad file name
     */
    public static void generateDescriptor(String inFilename, HashMap<String, String> gvars,
            HashMap<String, String> lvars, String outFilename) {
        // Read and modify ProActive descriptor base
        Document doc = XMLHelper.readFile(inFilename);

        // Get the root namespace in order to provide it when performing a getChild
        Namespace descriptorNamespace = doc.getRootElement().getNamespace();
        Element eVariables = doc.getRootElement().getChild("environment", descriptorNamespace);
        @SuppressWarnings("unchecked")
        Iterator it = eVariables.getChildren().iterator();
        while (it.hasNext()) {
            Element var = (Element) it.next();
            String name = var.getAttributeValue("name");
            if (lvars.containsKey(name)) {
                var.setAttribute("value", lvars.get(name));
            } else if (gvars.containsKey(name)) {
                var.setAttribute("value", gvars.get(name));
            }
        }

        // Write the generated descriptor into file
        XMLHelper.writeFile(doc, outFilename);
    }

    /**
     * Replace all occurences of old by value in all attributes of elt
     *
     * @param elt
     *            the Element to parse
     * @param old
     *            the old value to replace
     * @param value
     *            the new value
     */
    public static void replaceAll(Element elt, String old, String value) {
        @SuppressWarnings("unchecked")
        Iterator itAttr = elt.getAttributes().iterator();
        while (itAttr.hasNext()) {
            Attribute attr = (Attribute) itAttr.next();
            attr.setValue(attr.getValue().replaceAll(old, value));
        }
    }

    /**
     * Replace all variable occurences in serie's list by their value in vars
     *
     * @param serieList
     *            the list of series to parse
     * @param vars
     *            the variables to set
     */
    public static void replaceVariables(List serieList, HashMap<String, String> vars) {
        // Replace variables
        Pattern p = Pattern.compile("[^\\x24\\x7B\\x7D]*\\x24\\x7B" + // *${
            "([^\\x7D]*)" + // A,B,C
            "\\x7D[^\\x7D\\x24\\x7B]*"); // }*
        @SuppressWarnings("unchecked")
        Iterator it = serieList.iterator();
        while (it.hasNext()) {
            Element serie = (Element) it.next();
            // Look for variables in Series attributes
            replaceVariablesAttributes(serie, p, vars);

            // Look for variables in all descendants of Series
            @SuppressWarnings("unchecked")
            Iterator itSerie = serie.getDescendants();
            while (itSerie.hasNext()) {
                Object elt = itSerie.next();
                if (elt instanceof Element) {
                    replaceVariablesAttributes((Element) elt, p, vars);
                }
            }
        }
    }

    /**
     * Scan all attributes of a given Element and replace variable name by their
     * real value
     *
     * @param elt
     *            the Element to scan
     * @param p
     *            the variable Pattern
     * @param vars
     *            the variables values
     */
    private static void replaceVariablesAttributes(Element elt, Pattern p, HashMap<String, String> vars) {
        @SuppressWarnings("unchecked")
        Iterator itAttr = elt.getAttributes().iterator();
        while (itAttr.hasNext()) {
            Attribute attr = (Attribute) itAttr.next();
            String values = attr.getValue();
            Matcher m = p.matcher(values);
            while (m.find()) {
                String var = m.group(1);
                String resolve = vars.get(var);
                values = values.replaceAll("\\x24\\x7B" + var + "\\x7D", // ${*}
                        (resolve.split(",").length == 1) ? resolve : ("#{" + resolve + "}"));
                attr.setValue(values);
            }
        }
    }

    /**
     * Filter elements from value of name attribute<br>
     * Remove all elements which are not in values. If a parent is not in
     * accepted values, its children will not be accepted.
     *
     * @param timit
     *            the Element to modify
     * @param values
     *            names to accept
     */
    public static void tagFiltering(Element eTag, String[] values) {
        Arrays.sort(values);
        List<Element> children = eTag.getChildren();
        int i = 0;

        while (i < children.size()) {
            Element elt = children.get(i);
            Element parent = elt.getParentElement();

            while (!filter(elt, values)) {
                if (!elt.getName().equals(parent.getName())) {
                    children = eTag.getChildren();
                    i--;
                    break;
                }
                elt = parent;
            }
            i++;
        }
    }

    private static boolean filter(Element eTag, String[] values) {
        if (values.length == 0) {
            return true;
        }
        if (Arrays.binarySearch(values, eTag.getAttributeValue("name")) < 0) {
            // not in values --> remove this Element
            eTag.detach();
            return false;
        } else {
            List children = eTag.getChildren();
            for (int i = 0; i < children.size(); i++) {
                Element child = (Element) children.get(i);
                Element parent = child.getParentElement();
                while (!filter(child, values)) {
                    child = parent;
                }
            }
            return true;
        }
    }

    /**
     * Print document on stdout. For debug purpose
     *
     * @param doc
     */
    public static void printOut(Document doc) {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            xmlOutputter.output(doc, System.out);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
