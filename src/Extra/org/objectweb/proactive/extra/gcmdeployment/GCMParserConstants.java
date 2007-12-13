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
package org.objectweb.proactive.extra.gcmdeployment;

public interface GCMParserConstants {
    static public final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static public final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static public final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String COMMON_TYPES_LOCATION = "/org/objectweb/proactive/extra/gcmdeployment/schema/CommonTypes.xsd";
    public static final String EXTENSION_SCHEMAS_LOCATION = "/org/objectweb/proactive/extra/gcmdeployment/schema/ExtensionSchemas.xsd";
    public static final String DEPLOYMENT_DESC_LOCATION = "/org/objectweb/proactive/extra/gcmdeployment/schema/DeploymentDescriptorSchema.xsd";
    public static final String APPLICATION_DESC_LOCATION = "/org/objectweb/proactive/extra/gcmdeployment/schema/ApplicationDescriptorSchema.xsd";
    public static final String GCM_DESCRIPTOR_NAMESPACE = "http://www-sop.inria.fr/oasis/ProActive/schemas";
    public static final String GCM_DESCRIPTOR_EXTENSION_NAMESPACE = "http://www-sop.inria.fr/oasis/ProActive/schemas/DeploymentDescriptorSchemaExtension";
    public static final String GCM_DESCRIPTOR_NAMESPACE_PREFIX = "pa:";
    public static final String VARIABLES_DESCRIPTOR_TAG = "descriptorVariable";
    public static final String VARIABLES_PROGRAM_TAG = "programVariable";
    public static final String VARIABLES_DESCRIPTOR_DEFAULT_TAG = "descriptorDefaultVariable";
    public static final String VARIABLES_PROGRAM_DEFAULT_TAG = "programDefaultVariable";
    public static final String VARIABLES_JAVAPROPERTY_TAG = "javaPropertyVariable";
    public static final String VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG = "javaPropertyDescriptorDefault";
    public static final String VARIABLES_JAVAPROPERTY_PROGRAM_TAG = "javaPropertyProgramDefault";
    public static final String VARIABLES_INCLUDE_XML_FILE_TAG = "includeXMLFile";
    public static final String VARIABLES_INCLUDE_PROPERTY_FILE_TAG = "includePropertyFile";
    public static final String VARIABLES_DESCRIPTOR = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_DESCRIPTOR_TAG;
    public static final String VARIABLES_PROGRAM = GCM_DESCRIPTOR_NAMESPACE_PREFIX + VARIABLES_PROGRAM_TAG;
    public static final String VARIABLES_DESCRIPTOR_DEFAULT = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_DESCRIPTOR_DEFAULT_TAG;
    public static final String VARIABLES_PROGRAM_DEFAULT = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_PROGRAM_DEFAULT_TAG;
    public static final String VARIABLES_JAVAPROPERTY = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_JAVAPROPERTY_TAG;
    public static final String VARIABLES_JAVAPROPERTY_DESCRIPTOR = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG;
    public static final String VARIABLES_JAVAPROPERTY_PROGRAM = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_JAVAPROPERTY_PROGRAM_TAG;
    public static final String VARIABLES_INCLUDE_XML_FILE = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_INCLUDE_XML_FILE_TAG;
    public static final String VARIABLES_INCLUDE_PROPERTY_FILE = GCM_DESCRIPTOR_NAMESPACE_PREFIX +
        VARIABLES_INCLUDE_PROPERTY_FILE_TAG;
}
