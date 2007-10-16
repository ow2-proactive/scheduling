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
package org.objectweb.proactive.core.descriptor.legacyparser;

import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.InputSource;


/**
 * This class handles all the parsing of Variable Contract XML tags.
 * @author The ProActive Team
 *
 */
public class VariablesHandler extends PassiveCompositeUnmarshaller
    implements ProActiveDescriptorConstants {
    protected VariableContract variableContract;

    public VariablesHandler(VariableContract variableContract) {
        super(false);
        this.variableContract = variableContract;

        this.addHandler(VARIABLES_DESCRIPTOR_TAG,
            new VariableHandler(VARIABLES_DESCRIPTOR_TAG));
        this.addHandler(VARIABLES_PROGRAM_TAG,
            new VariableHandler(VARIABLES_PROGRAM_TAG));
        this.addHandler(VARIABLES_JAVAPROPERTY_TAG,
            new VariableHandler(VARIABLES_JAVAPROPERTY_TAG));
        this.addHandler(VARIABLES_PROGRAM_DEFAULT_TAG,
            new VariableHandler(VARIABLES_PROGRAM_DEFAULT_TAG));
        this.addHandler(VARIABLES_DESCRIPTOR_DEFAULT_TAG,
            new VariableHandler(VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        this.addHandler(VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG,
            new VariableHandler(VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
        this.addHandler(VARIABLES_JAVAPROPERTY_PROGRAM_TAG,
            new VariableHandler(VARIABLES_JAVAPROPERTY_PROGRAM_TAG));

        this.addHandler(VARIABLES_INCLUDE_XML_FILE_TAG,
            new IncludeXMLFileHandler());
        this.addHandler(VARIABLES_INCLUDE_PROPERTY_FILE_TAG,
            new IncludePropertiesFileHandler());
    }

    @Override
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        //Once the variables have been defined, we load pending values from the javaproperties
        variableContract.setJavaPropertiesValues();
    }

    /**
     * Creates a SAX parser on the specified file for variables. This is used
     * when including a variable contract defined on a different file.
     * @param filename the full path to the file
     */
    public static void createVariablesHandler(String filename,
        VariableContract variableContract) {
        VariablesFileHandler vfh = new VariablesFileHandler(variableContract);

        org.objectweb.proactive.core.xml.io.StreamReader sr;

        //String file = VariablesHandler.class.getResource(filename).getPath();
        InputSource source = new org.xml.sax.InputSource(filename);
        try {
            sr = new org.objectweb.proactive.core.xml.io.StreamReader(source,
                    vfh);
            sr.read();
            //return (cast) vh.getResultObject();
        } catch (Exception e) {
            logger.error("Unable to load Variable Contract from:" + filename);
            e.printStackTrace();
        }
    }

    public static class VariablesFileHandler
        extends PassiveCompositeUnmarshaller {
        VariablesFileHandler(VariableContract variableContract) {
            super(false);
            this.addHandler(VARIABLES_TAG,
                new VariablesHandler(variableContract));
        }
    }

    private class VariableHandler extends BasicUnmarshaller {
        VariableContractType varType;
        String varStringType;

        VariableHandler(String varStringType) {
            this.varType = VariableContractType.getType(varStringType);
            this.varStringType = varStringType;
        }

        @Override
        public void startContextElement(String tag, Attributes attributes)
            throws org.xml.sax.SAXException {
            if (this.varType == null) {
                throw new org.xml.sax.SAXException(
                    "Ilegal Descriptor Variable Type: " + varStringType);
            }

            // Variable Name
            String name = attributes.getValue("name");
            if (!checkNonEmpty(name)) {
                throw new org.xml.sax.SAXException("Variable has no name");
            }

            String value = attributes.getValue("value");
            if (value == null) {
                value = "";
            }
            // Define and set variables into the contract
            variableContract.setDescriptorVariable(name, value, varType);
        }
    }

    private class IncludeXMLFileHandler extends BasicUnmarshaller {
        IncludeXMLFileHandler() {
        }

        @Override
        public void startContextElement(String tag, Attributes attributes)
            throws org.xml.sax.SAXException {
            String file = attributes.getValue("location");
            if (checkNonEmpty(file)) {
                // Specific processing for loading an xml file
                variableContract.loadXML(file);
                return;
            }
        }
    }

    private class IncludePropertiesFileHandler extends BasicUnmarshaller {
        IncludePropertiesFileHandler() {
        }

        @Override
        public void startContextElement(String tag, Attributes attributes)
            throws org.xml.sax.SAXException {
            String file = attributes.getValue("location");
            if (checkNonEmpty(file)) {
                // Specific processing for loading a file
                variableContract.load(file);
                return;
            }
        }
    }
}
