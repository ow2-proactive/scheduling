/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.HashMap;
import java.util.Map;


/**
 * XMLAttributes represents all the attributes of XML job descriptors
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public enum XMLAttributes {

    //JOBS
    JOB_PRIORITY("priority"), JOB_PROJECTNAME("projectName"), JOB_LOGFILE("logFile"),
    //COMMON
    COMMON_CANCELJOBONERROR("cancelJobOnError"), COMMON_RESTARTTASKONERROR("restartTaskOnError"), COMMON_MAXNUMBEROFEXECUTION(
            "maxNumberOfExecution"), COMMON_NAME("name"),
    //TASKS
    TASK_RESULTPREVIEW("resultPreviewClass"), TASK_PRECIOUSRESULT("preciousResult"), TASK_CLASSNAME("class"), TASK_WALLTIME(
            "walltime"), TASK_FORK("fork"), TASK_RUNASME("runAsMe"),
    //NATIVE TASK ATTRIBUTES
    TASK_NB_NODES("numberOfNodes"), TASK_COMMAND_VALUE("value"), TASK_WORKDING_DIR("workingDir"),
    //SCRIPTS
    SCRIPT_URL("url"),
    //FORK ENVIRONMENT
    FORK_JAVAHOME("javaHome"), FORK_JVMPARAMETERS("jvmParameters"),
    // FLOW CONTROL
    FLOW_BLOCK("block"), FLOW_TARGET("target"), FLOW_ELSE("else"), FLOW_JOIN("join"),
    //DATASPACES
    DS_INCLUDES("includes"), DS_EXCLUDES("excludes"), DS_ACCESSMODE("accessMode"),
    //NOT USED IN XML FACTORY BUT USED IN XML DESCRIPTION
    PATH("path"), LANGUAGE("language");

    private String xmlName;

    private XMLAttributes(String xmlName) {
        this.xmlName = xmlName;
    }

    /**
     * Return the XML tag name of the attribute as a String.
     *
     * @return the XML tag name of the attribute as a String.
     */
    public String getXMLName() {
        return this.xmlName;
    }

    private static Map<String, XMLAttributes> namesToEnum = null;

    /**
     * Get the XMLAttributes enum corresponding to the given xml attribute name.
     * This method ignores the case.
     *
     * @param xmlName the XML attribute name as a string
     * @return the corresponding XML attribute.
     * @throws IllegalArgumentException if the attribute name does not exist
     */
    public static XMLAttributes getFromXMLName(String xmlName) {
        String toCheck = xmlName.toUpperCase();
        if (namesToEnum == null) {
            namesToEnum = new HashMap<String, XMLAttributes>();
            for (XMLAttributes attr : values()) {
                namesToEnum.put(attr.getXMLName().toUpperCase(), attr);
            }
        }
        XMLAttributes attr = namesToEnum.get(toCheck);
        if (attr == null) {
            throw new IllegalArgumentException("XML attribute name '" + xmlName + "' does not exist");
        } else {
            return attr;
        }
    }

    /**
     * Return true if the given XML name matches this XMLAttributes
     *
     * @param xmlName the XML attribute name as a String.
     * @return true only if the given XML name matches this XMLAttributes
     */
    public boolean matches(String xmlName) {
        return xmlName.equalsIgnoreCase(this.xmlName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getXMLName();
    }

}
