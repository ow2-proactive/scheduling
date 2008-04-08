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
package org.objectweb.proactive.extensions.gcmdeployment;

public interface GCMParserConstants {
    static public final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static public final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static public final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    public static final String COMMON_TYPES_LOCATION = "/org/objectweb/proactive/extensions/gcmdeployment/schema/CommonTypes.xsd";

    public static final String EXTENSION_SCHEMAS_LOCATION = "/org/objectweb/proactive/extensions/gcmdeployment/schema/ExtensionSchemas.xsd";
    public static final String DEPLOYMENT_DESC_LOCATION = "/org/objectweb/proactive/extensions/gcmdeployment/schema/DeploymentDescriptorSchema.xsd";
    public static final String GCM_DEPLOYMENT_NAMESPACE = "urn:gcm:deployment:1.0";
    public static final String GCM_DEPLOYMENT_NAMESPACE_PREFIX = "dep";

    public static final String APPLICATION_DESC_LOCATION = "/org/objectweb/proactive/extensions/gcmdeployment/schema/ApplicationDescriptorSchema.xsd";
    public static final String GCM_APPLICATION_NAMESPACE = "urn:gcm:application:1.0";
    public static final String GCM_APPLICATION_NAMESPACE_PREFIX = "app";

    public static final String[] VARIABLES_TAGS = { "descriptorVariable", "programVariable",
            "descriptorDefaultVariable", "programDefaultVariable", "javaPropertyVariable",
            "javaPropertyDescriptorDefault", "javaPropertyProgramDefault", "includeXMLFile",
            "includePropertyFile" };
}
