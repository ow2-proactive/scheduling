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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.job.factories;

import java.util.HashMap;
import java.util.Map;


/**
 * XMLTags represents all the tags used in XML job descriptor
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public enum XMLTags {
    // Are define only the needed tags. If more are needed, just create them.
    // JOBS
    JOB("job"), TASKFLOW("taskFlow"), JOB_CLASSPATHES("jobClasspath"), JOB_PATH_ELEMENT("pathElement"),
    // COMMON
    COMMON_DESCRIPTION("description"), COMMON_GENERIC_INFORMATION("genericInformation"), COMMON_INFO("info"),
    // VARIABLES
    VARIABLES("variables"), VARIABLE("variable"),
    // TASKS
    TASK("task"), JAVA_EXECUTABLE("javaExecutable"), NATIVE_EXECUTABLE("nativeExecutable"), SCRIPT_EXECUTABLE(
            "scriptExecutable"), TASK_DEPENDENCES("depends"), TASK_DEPENDENCES_TASK("task"), TASK_PARAMETERS(
            "parameters"), TASK_PARAMETER("parameter"), NATIVE_TASK_STATICCOMMAND("staticCommand"),
            NATIVE_TASK_ARGUMENTS("arguments"), NATIVE_TASK_ARGUMENT("argument"),
    // TOPOLOGY
    PARALLEL_ENV("parallel"), TOPOLOGY("topology"), TOPOLOGY_ARBITRARY("arbitrary"), TOPOLOGY_BEST_PROXIMITY(
            "bestProximity"), TOPOLOGY_THRESHOLD_PROXIMITY("thresholdProximity"), TOPOLOGY_SINGLE_HOST(
            "singleHost"), TOPOLOGY_SINGLE_HOST_EXCLUSIVE("singleHostExclusive"), TOPOLOGY_MULTIPLE_HOSTS_EXCLUSIVE(
            "multipleHostsExclusive"), TOPOLOGY_DIFFERENT_HOSTS_EXCLUSIVE("differentHostsExclusive"),

    // SCRIPTS
    SCRIPT_SELECTION("selection"), SCRIPT_PRE("pre"), SCRIPT_POST("post"), SCRIPT_CLEANING("cleaning"), SCRIPT_SCRIPT(
            "script"), SCRIPT_ARGUMENTS("arguments"), SCRIPT_ARGUMENT("argument"), SCRIPT_FILE("file"), SCRIPT_CODE(
            "code"),
    // FORK ENVIRONMENT
    FORK_ENVIRONMENT("forkEnvironment"), FORK_SYSTEM_PROPERTIES("SystemEnvironment"), FORK_SYSTEM_PROPERTY(
            "variable"), FORK_JVM_ARGS("jvmArgs"), FORK_JVM_ARG("jvmArg"), FORK_ADDITIONAL_CLASSPATH(
            "additionalClasspath"), FORK_PATH_ELEMENT("pathElement"), SCRIPT_ENV("envScript"),
    // FLOW CONTROL
    FLOW("controlFlow"), FLOW_IF("if"), FLOW_REPLICATE("replicate"), FLOW_LOOP("loop"),
    // DATASPACES
    DS_INPUTSPACE("inputSpace"), DS_OUTPUTSPACE("outputSpace"), DS_GLOBALSPACE("globalSpace"), DS_USERSPACE(
            "userSpace"), DS_INPUTFILES("inputFiles"), DS_OUTPUTFILES("outputFiles"), DS_FILES("files");

    private String xmlName;

    private XMLTags(String xmlName) {
        this.xmlName = xmlName;
    }

    /**
     * Return the XML tag name of the element as a String.
     *
     * @return the XML tag name of the element as a String.
     */
    public String getXMLName() {
        return this.xmlName;
    }

    private static Map<String, XMLTags> namesToEnum = null;

    /**
     * Get the XMLTags enum corresponding to the given xml tag name. This method
     * ignores the case. Argument cannot be null.
     *
     * @param xmlName
     *            the XML tag name as a string
     * @return the corresponding XML tag.
     * @throws IllegalArgumentException
     *             if the tag name does not exist
     */
    public static XMLTags getFromXMLName(String xmlName) {
        if (xmlName == null) {
            throw new IllegalArgumentException("xmlName argument cannot be null");
        }
        String toCheck = xmlName.toUpperCase();
        if (namesToEnum == null) {
            namesToEnum = new HashMap<String, XMLTags>();
            for (XMLTags tag : values()) {
                namesToEnum.put(tag.getXMLName().toUpperCase(), tag);
            }
        }
        XMLTags tag = namesToEnum.get(toCheck);
        if (tag == null) {
            throw new IllegalArgumentException("XML tag name '" + xmlName + "' does not exist");
        } else {
            return tag;
        }
    }

    /**
     * Return true if the given XML name matches this XMLAttributes
     *
     * @param xmlName
     *            the XML tag name as a String.
     * @return true only if the given XML name matches this XMLTags
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
