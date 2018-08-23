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
package org.ow2.proactive.scheduler.common.util;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class LogFormatter {

    public static String displayMap(String name, Map<?, ?> content) {
        if (content.isEmpty()) {
            return name + " = {}";
        } else {
            return name + " = {" + System.lineSeparator() +
                   content.entrySet()
                          .stream()
                          .map(pair -> pair.getKey() + " = " + pair.getValue())
                          .map(s -> "\t" + s)
                          .collect(Collectors.joining(System.lineSeparator())) +
                   System.lineSeparator() + "}";
        }
    }

    public static String addIndent(String content) {
        return addIndent(content, 1);
    }

    public static String addIndent(String content, int numberOfIndents) {
        if (numberOfIndents <= 0) {
            return content;
        } else {
            final String lineIndent = IntStream.range(0, numberOfIndents).mapToObj(i -> "\t").reduce("",
                                                                                                     String::concat);
            return Arrays.stream(content.split(System.lineSeparator()))
                         .map(s -> lineIndent + s)
                         .collect(Collectors.joining(System.lineSeparator()));
        }
    }

}
