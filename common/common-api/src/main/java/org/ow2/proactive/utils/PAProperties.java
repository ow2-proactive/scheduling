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
package org.ow2.proactive.utils;

import java.nio.charset.Charset;


/**
 * Contain definitions related to ProActive properties which are common
 * to Scheduler, Resource Manager and Nodes.
 *
 * @author ProActive Team
 */
public final class PAProperties {

    public static final String KEY_PA_FILE_ENCODING = "pa.file.encoding";

    /**
     * Returns value associated to Java property {@value #KEY_PA_FILE_ENCODING}
     * or the current JVM charset name if the previous property is not defined.
     *
     * @return value associated to Java property {@value #KEY_PA_FILE_ENCODING}
     * or the current JVM charset name if the previous property is not defined.
     */
    public static String getFileEncoding() {
        String property = System.getProperty(KEY_PA_FILE_ENCODING);

        if (property != null) {
            return property;
        }

        return Charset.defaultCharset().name();
    }

}
