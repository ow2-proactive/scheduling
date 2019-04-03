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

import com.google.common.base.Preconditions;


/**
 * XMLTags defines all tags that can be used in an XML job descriptor.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public enum XMLTags {

    // Only needed tags are defined. If more are required, just create them.

    // JOBS
    JOB("job"),
    JOB_CLASSPATHES("jobClasspath"),
    JOB_PATH_ELEMENT("pathElement"),
    TASK_FLOW("taskFlow"),

    // COMMON
    COMMON_DESCRIPTION("description"),
    COMMON_GENERIC_INFORMATION("genericInformation"),
    COMMON_INFO("info"),

    // VARIABLES
    VARIABLE("variable"),
    VARIABLES("variables"),

    // TASKS
    JAVA_EXECUTABLE("javaExecutable"),
    NATIVE_EXECUTABLE("nativeExecutable"),
    NATIVE_TASK_ARGUMENT("argument"),
    NATIVE_TASK_ARGUMENTS("arguments"),
    NATIVE_TASK_STATIC_COMMAND("staticCommand"),
    SCRIPT_EXECUTABLE("scriptExecutable"),
    TASK("task"),
    TASK_DEPENDENCES("depends"),
    TASK_DEPENDENCES_TASK("task"),
    TASK_PARAMETER("parameter"),
    TASK_PARAMETERS("parameters"),

    // TOPOLOGY
    PARALLEL_ENV("parallel"),
    TOPOLOGY("topology"),
    TOPOLOGY_ARBITRARY("arbitrary"),
    TOPOLOGY_BEST_PROXIMITY("bestProximity"),
    TOPOLOGY_DIFFERENT_HOSTS_EXCLUSIVE("differentHostsExclusive"),
    TOPOLOGY_MULTIPLE_HOSTS_EXCLUSIVE("multipleHostsExclusive"),
    TOPOLOGY_SINGLE_HOST("singleHost"),
    TOPOLOGY_SINGLE_HOST_EXCLUSIVE("singleHostExclusive"),
    TOPOLOGY_THRESHOLD_PROXIMITY("thresholdProximity"),

    // SCRIPTS
    SCRIPT_ARGUMENT("argument"),
    SCRIPT_ARGUMENTS("arguments"),
    SCRIPT_CLEANING("cleaning"),
    SCRIPT_CODE("code"),
    SCRIPT_FILE("file"),
    SCRIPT_PRE("pre"),
    SCRIPT_POST("post"),
    SCRIPT_SCRIPT("script"),
    SCRIPT_SELECTION("selection"),

    // FORK ENVIRONMENT
    FORK_ADDITIONAL_CLASSPATH("additionalClasspath"),
    FORK_ENVIRONMENT("forkEnvironment"),
    FORK_JVM_ARG("jvmArg"),
    FORK_JVM_ARGS("jvmArgs"),
    FORK_PATH_ELEMENT("pathElement"),
    FORK_SYSTEM_PROPERTIES("SystemEnvironment"),
    FORK_SYSTEM_PROPERTY("variable"),
    SCRIPT_ENV("envScript"),

    // FLOW CONTROL
    FLOW("controlFlow"),
    FLOW_IF("if"),
    FLOW_LOOP("loop"),
    FLOW_REPLICATE("replicate"),

    // DATASPACES
    DS_FILES("files"),
    DS_GLOBAL_SPACE("globalSpace"),
    DS_INPUT_FILES("inputFiles"),
    DS_INPUT_SPACE("inputSpace"),
    DS_OUTPUT_FILES("outputFiles"),
    DS_OUTPUT_SPACE("outputSpace"),
    DS_USER_SPACE("userSpace"),

    // METADATA
    METADATA("metadata"),
    METADATA_VISUALIZATION("visualization");

    /**
     * pattern that matches for open tag for provided tag name.
     * e.g.: <code>String.format(OPEN_TAG_PATTERN, XMLTags.VARIABLES</code> seeks
     * for the string like this: &lt;variables&gt;, &lt;   variables&gt;, &lt;variables   &gt;, etc.
     */
    public static final String OPEN_TAG_PATTERN = "<[ ]*%s[^>]*>";

    /**
     * pattern that matches for open tag for provided tag name.
     * e.g.: <code>String.format(CLOSE_TAG_PATTERN, XMLTags.VARIABLES</code> seeks
     * for the string like this: &lt;/variables&gt;, &lt;/   variables&gt;, &lt;  /  variables   &gt;, etc.
     */
    public static final String CLOSE_TAG_PATTERN = "<[ ]*/[ ]*%s[ ]*>";

    private String xmlName;

    XMLTags(String xmlName) {
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

    public String getOpenTagPattern() {
        return String.format(OPEN_TAG_PATTERN, this.xmlName);
    }

    public String getCloseTagPattern() {
        return String.format(CLOSE_TAG_PATTERN, this.xmlName);
    }

    private static Map<String, XMLTags> namesToEnum = null;

    /**
     * Get the XMLTags enum corresponding to the given xml tag name. This method
     * ignores the case. Argument cannot be {@code null}.
     *
     * @param xmlName the XML tag name as a string.
     * @return the corresponding XML tag.
     * @throws IllegalArgumentException if the tag name does not exist.
     */
    public static XMLTags getFromXMLName(String xmlName) {
        Preconditions.checkNotNull(xmlName);

        String toCheck = xmlName.toUpperCase();

        if (namesToEnum == null) {
            XMLTags[] values = values();
            Map<String, XMLTags> result = new HashMap<>(values.length);

            for (XMLTags tag : values) {
                result.put(tag.getXMLName().toUpperCase(), tag);
            }

            namesToEnum = result;
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
     * @param xmlName the XML tag name as a String.
     * @return true only if the given XML name matches this XMLTags
     */
    public boolean matches(String xmlName) {
        return xmlName.equalsIgnoreCase(this.xmlName);
    }

    public String withContent(String content) {
        return "  <" + this.xmlName + ">\n" + content + "  </" + this.xmlName + ">";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getXMLName();
    }

}
