/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.HashMap;
import java.util.Map;


/**
 * XMLAttributes defines attributes allowed in XML job descriptors.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public enum XMLAttributes {

    // JOBS
    JOB_PRIORITY("priority"),
    JOB_PROJECT_NAME("projectName"),

    // COMMON
    COMMON_CANCEL_JOB_ON_ERROR("cancelJobOnError"),
    COMMON_ON_TASK_ERROR("onTaskError"),
    COMMON_MAX_NUMBER_OF_EXECUTION("maxNumberOfExecution"),
    COMMON_NAME("name"),
    COMMON_RESTART_TASK_ON_ERROR("restartTaskOnError"),
    COMMON_VALUE("value"),

    // VARIABLE
    VARIABLE_NAME("name"),
    VARIABLE_VALUE("value"),
    VARIABLE_MODEL("model"),
    VARIABLE_JOB_INHERITED("inherited"),

    // TASKS
    TASK_CLASS_NAME("class"),
    TASK_DEPENDS_REF("ref"),
    TASK_PRECIOUS_LOGS("preciousLogs"),
    TASK_PRECIOUS_RESULT("preciousResult"),
    TASK_RUN_AS_ME("runAsMe"),
    TASK_WALLTIME("walltime"),

    // NATIVE TASK ATTRIBUTES
    TASK_COMMAND_VALUE("value"),
    TASK_NB_NODES("numberOfNodes"),
    TASK_PARAMETER_NAME("name"),
    TASK_PARAMETER_VALUE("value"),
    TASK_WORKDING_DIR("workingDir"),

    // TOPOLOGY
    TOPOLOGY_THRESHOLD("threshold"),

    // SCRIPTS
    SCRIPT_URL("url"),

    // FORK ENVIRONMENT
    FORK_JAVA_HOME("javaHome"),

    // FLOW CONTROL
    FLOW_BLOCK("block"),
    FLOW_CONTINUATION("continuation"),
    FLOW_ELSE("else"),
    FLOW_TARGET("target"),

    // DATASPACES
    DS_ACCESS_MODE("accessMode"),
    DS_EXCLUDES("excludes"),
    DS_INCLUDES("includes"),
    DS_URL("url"),

    // NOT USED IN XML FACTORY BUT USED IN XML DESCRIPTION
    PATH("path"),
    LANGUAGE("language");

    private String xmlName;

    XMLAttributes(String xmlName) {
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
            namesToEnum = new HashMap<>();
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
