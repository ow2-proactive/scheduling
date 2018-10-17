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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ow2.proactive.scheduler.common.job.JobVariable;


/**
 * Factory used to create a job content as merge between submitted xml
 * and provided <code>variables</code> and <code>genericInfo</code> maps.
 */
public class GetJobContentFactory {

    private static final String FOUT_SPACES_INDENT = "    ";

    private static final String TWO_SPACES_INDENT = "  ";

    /**
     * @param jobContent xml representation of the submitted job
     * @param variables provided during job submit
     * @param genericInformation provided during job submit
     * @return new job content where 'variables' and 'genericInformation' tags content are replaced
     *  by provided <code>variables</code> and <code>genericInformation</code> respectively.
     */
    public String replaceVarsAndGenericInfo(String jobContent, Map<String, JobVariable> variables,
            Map<String, String> genericInformation) {
        final int end = jobContent.indexOf(XMLTags.TASK_FLOW.getXMLName());

        String replacedJobContent = replaceTagContent(jobContent,
                                                      XMLTags.VARIABLES,
                                                      newVariablesContent(variables),
                                                      end);
        replacedJobContent = replaceTagContent(replacedJobContent,
                                               XMLTags.COMMON_GENERIC_INFORMATION,
                                               newGenericInfoContent(genericInformation),
                                               end);

        return replacedJobContent;
    }

    private String newGenericInfoContent(Map<String, String> genericInformation) {
        return genericInformation.entrySet()
                                 .stream()
                                 .map(this::genericInfoContent)
                                 .map(s -> s + System.lineSeparator())
                                 .reduce("", String::concat);
    }

    private String genericInfoContent(Map.Entry<String, String> pair) {
        return String.format(FOUT_SPACES_INDENT + "<%s %s=\"%s\" %s=\"%s\"/>",
                             XMLTags.COMMON_INFO,
                             XMLAttributes.COMMON_NAME,
                             pair.getKey(),
                             XMLAttributes.COMMON_VALUE,
                             pair.getValue());
    }

    private String replaceTagContent(String jobContent, XMLTags tag, String newContent, int end) {
        final String firstHalf = jobContent.substring(0, end);

        /*
         * because tasks can contain variables as well we modify original string
         * only till the provided end
         */
        final String untouchablePart = jobContent.substring(end);

        final Optional<Matcher> openMatcher = indexOfPattern(firstHalf, tag.getOpenTagPattern());
        if (!openMatcher.isPresent()) {
            return jobContent;
        }
        final int afterOpenTag = openMatcher.get().end();

        final Optional<Matcher> closeMatcher = indexOfPattern(firstHalf, tag.getCloseTagPattern());
        if (!closeMatcher.isPresent()) {
            return jobContent;
        }
        final int beforeCloseTag = closeMatcher.get().start();

        final String beforeNewContent = firstHalf.substring(0, afterOpenTag) + System.lineSeparator();
        final String afterNewContent = TWO_SPACES_INDENT + firstHalf.substring(beforeCloseTag);

        return beforeNewContent + newContent + afterNewContent + untouchablePart;
    }

    private Optional<Matcher> indexOfPattern(String content, String pattern) {
        Pattern aPattern = Pattern.compile(pattern);
        Matcher matcher = aPattern.matcher(content);

        if (matcher.find()) {
            return Optional.of(matcher);
        } else {
            return Optional.empty();
        }
    }

    private String newVariablesContent(Map<String, JobVariable> variables) {
        return variables.values()
                        .stream()
                        .map(this::variableContent)
                        .map(s -> s + System.lineSeparator())
                        .reduce("", String::concat);
    }

    private String variableContent(JobVariable jobVariable) {
        if (jobVariable.getModel() != null && !jobVariable.getModel().trim().isEmpty()) {
            return String.format(FOUT_SPACES_INDENT + "<%s %s=\"%s\" %s=\"%s\" %s=\"%s\" />",
                                 XMLTags.VARIABLE.getXMLName(),
                                 XMLAttributes.VARIABLE_NAME,
                                 jobVariable.getName(),
                                 XMLAttributes.VARIABLE_VALUE,
                                 jobVariable.getValue(),
                                 XMLAttributes.VARIABLE_MODEL,
                                 jobVariable.getModel());
        } else {
            return String.format(FOUT_SPACES_INDENT + "<%s %s=\"%s\" %s=\"%s\" />",
                                 XMLTags.VARIABLE.getXMLName(),
                                 XMLAttributes.VARIABLE_NAME,
                                 jobVariable.getName(),
                                 XMLAttributes.VARIABLE_VALUE,
                                 jobVariable.getValue());
        }
    }

}
