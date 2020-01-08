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

import org.apache.commons.lang3.StringEscapeUtils;
import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.ModelType;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;


/**
 * Factory used to create a job content as merge between submitted xml
 * and provided <code>variables</code> and <code>genericInfo</code> maps.
 */
public class GetJobContentGenerator {

    private static final String FOUR_SPACES_INDENT = "    ";

    private static final String TWO_SPACES_INDENT = "  ";

    private static final String TAG_WITH_TWO_ATTRIBUTES = "<%s %s=\"%s\" %s=\"%s\" />";

    private static final String TAG_WITH_THREE_ATTRIBUTES = "<%s %s=\"%s\" %s=\"%s\" %s=\"%s\" />";

    /**
     * @param jobContent         xml representation of the submitted job
     * @param variables          provided during job submit
     * @param genericInformation provided during job submit
     * @return new job content where 'variables' and 'genericInformation' tags content are replaced
     * by provided <code>variables</code> and <code>genericInformation</code> respectively.
     */
    public String replaceVarsAndGenericInfo(String jobContent, Map<String, JobVariable> variables,
            Map<String, String> genericInformation) {
        int sizeOfFirstPart = jobContent.indexOf(XMLTags.TASK_FLOW.getXMLName());
        String replacedJobContent = replaceVarsContent(jobContent, newVariablesContent(variables), sizeOfFirstPart);
        sizeOfFirstPart = replacedJobContent.indexOf(XMLTags.TASK_FLOW.getXMLName());
        replacedJobContent = replaceGenericInfoContent(replacedJobContent,
                                                       newGenericInfoContent(genericInformation),
                                                       sizeOfFirstPart);

        return replacedJobContent;
    }

    private String newGenericInfoContent(Map<String, String> genericInformation) {
        if (!genericInformation.isEmpty()) {
            return genericInformation.entrySet()
                                     .stream()
                                     .map(this::genericInfoContent)
                                     .map(s -> s + System.lineSeparator())
                                     .reduce("", String::concat);
        } else {
            return "";
        }
    }

    private String genericInfoContent(Map.Entry<String, String> pair) {
        return String.format(FOUR_SPACES_INDENT + TAG_WITH_TWO_ATTRIBUTES,
                             XMLTags.COMMON_INFO,
                             XMLAttributes.COMMON_NAME,
                             pair.getKey(),
                             XMLAttributes.COMMON_VALUE,
                             StringEscapeUtils.escapeXml11(pair.getValue()));
    }

    private String replaceGenericInfoContent(String jobContent, String newContent, int end) {
        final String firstHalf = jobContent.substring(0, end);

        String untouchablePart = jobContent.substring(end);

        Optional<Matcher> openMatcher = indexOfPattern(firstHalf,
                                                       XMLTags.COMMON_GENERIC_INFORMATION.getOpenTagPattern());
        Optional<Matcher> closeMatcher = indexOfPattern(firstHalf,
                                                        XMLTags.COMMON_GENERIC_INFORMATION.getCloseTagPattern());

        // when job already had generic info, then just replace it with new content
        if (openMatcher.isPresent() && closeMatcher.isPresent()) {
            int beforeOpenTag = openMatcher.get().start();
            int afterOpenTag = openMatcher.get().end();
            int beforeCloseTag = closeMatcher.get().start();
            int afterCloseTag = closeMatcher.get().end();
            if (!newContent.isEmpty()) {
                return insertContent(firstHalf, newContent, afterOpenTag, beforeCloseTag) + untouchablePart;
            } else {
                String left = firstHalf.substring(0, beforeOpenTag);
                String right = firstHalf.substring(afterCloseTag + 1).trim();
                return left + right + untouchablePart;
            }
        } else {
            if (!newContent.isEmpty()) { // if job did not have generic info before
                // we try to put it after job description if it exsits
                // then we try to put after variables, if they exist
                XMLTags[] xmlTags = { XMLTags.COMMON_DESCRIPTION, XMLTags.VARIABLES };
                for (XMLTags xmlTag : xmlTags) {
                    final Optional<Integer> afterCloseTag = afterCloseTag(firstHalf, xmlTag);
                    if (afterCloseTag.isPresent()) {
                        return insertContent(firstHalf,
                                             XMLTags.COMMON_GENERIC_INFORMATION.withContent(newContent),
                                             afterCloseTag.get()) +
                               untouchablePart;
                    }
                }

                // if neither variables not job description exist then we add just after job tag
                Optional<Integer> afterOpenTag = afterOpenTag(firstHalf, XMLTags.JOB);

                return afterOpenTag.map(index -> insertContent(firstHalf,
                                                               XMLTags.COMMON_GENERIC_INFORMATION.withContent(newContent),
                                                               index) +
                                                 untouchablePart)
                                   .orElse(jobContent);
            } else {
                return jobContent;
            }

        }

    }

    private String replaceVarsContent(String jobContent, String newContent, int end) {
        final String firstHalf = jobContent.substring(0, end);

        /*
         * because tasks can contain variables as well we modify original string
         * only till the provided end
         */
        String untouchablePart = jobContent.substring(end);

        Optional<Matcher> openMatcher = indexOfPattern(firstHalf, XMLTags.VARIABLES.getOpenTagPattern());
        Optional<Matcher> closeMatcher = indexOfPattern(firstHalf, XMLTags.VARIABLES.getCloseTagPattern());

        // if job already had variables - we just replace their content
        if (openMatcher.isPresent() && closeMatcher.isPresent()) {
            int beforeOpenTag = openMatcher.get().start();
            int afterOpenTag = openMatcher.get().end();
            int beforeCloseTag = closeMatcher.get().start();
            int afterCloseTag = closeMatcher.get().end();

            if (!newContent.isEmpty()) {
                String beforeNewContent = firstHalf.substring(0, afterOpenTag) + System.lineSeparator();
                String afterNewContent = TWO_SPACES_INDENT + firstHalf.substring(beforeCloseTag);
                return beforeNewContent + newContent + afterNewContent + untouchablePart;
            } else {
                String beforeNewContent = firstHalf.substring(0, beforeOpenTag);
                String afterNewContent = firstHalf.substring(afterCloseTag + 1).trim();
                return beforeNewContent + afterNewContent + untouchablePart;
            }
        } else {
            if (!newContent.isEmpty()) {
                Optional<Integer> afterOpenTag = afterOpenTag(firstHalf, XMLTags.JOB);
                return afterOpenTag.map(index -> insertContent(firstHalf,
                                                               XMLTags.VARIABLES.withContent(newContent),
                                                               index) +
                                                 untouchablePart)
                                   .orElse(jobContent);
            } else {
                return jobContent;
            }
        }
    }

    private String insertContent(String all, String content, int index) {
        String left = all.substring(0, index) + System.lineSeparator();
        String right = TWO_SPACES_INDENT + all.substring(index);
        StringBuilder result = new StringBuilder();
        result.append(left);
        result.append(content);
        result.append(right);
        return result.toString();
    }

    private String insertContent(String all, String content, int index, int index2) {
        String left = all.substring(0, index);
        String right = TWO_SPACES_INDENT + all.substring(index2);
        return left + System.lineSeparator() + content + right;
    }

    private Optional<Integer> afterOpenTag(String content, XMLTags tag) {
        return indexOfPattern(content, tag.getOpenTagPattern()).map(Matcher::end);
    }

    private Optional<Integer> afterCloseTag(String content, XMLTags tag) {
        return indexOfPattern(content, tag.getCloseTagPattern()).map(Matcher::end);
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
        if (!variables.isEmpty()) {
            return variables.values()
                            .stream()
                            .map(this::variableContent)
                            .map(s -> s + System.lineSeparator())
                            .reduce("", String::concat);
        } else {
            return "";
        }
    }

    private String variableContent(JobVariable jobVariable) {
        if (jobVariable.getModel() != null && !jobVariable.getModel().trim().isEmpty()) {
            boolean encryptionNeeded = jobVariable.getModel()
                                                  .trim()
                                                  .equalsIgnoreCase(ModelValidator.PREFIX + ModelType.HIDDEN.name());
            return String.format(FOUR_SPACES_INDENT + TAG_WITH_THREE_ATTRIBUTES,
                                 XMLTags.VARIABLE.getXMLName(),
                                 XMLAttributes.VARIABLE_NAME,
                                 jobVariable.getName(),
                                 XMLAttributes.VARIABLE_VALUE,
                                 StringEscapeUtils.escapeXml11(encryptionNeeded ? PropertyDecrypter.encryptData(jobVariable.getValue())
                                                                                : jobVariable.getValue()),
                                 XMLAttributes.VARIABLE_MODEL,
                                 jobVariable.getModel());
        } else {
            return String.format(FOUR_SPACES_INDENT + TAG_WITH_TWO_ATTRIBUTES,

                                 XMLTags.VARIABLE.getXMLName(),
                                 XMLAttributes.VARIABLE_NAME,
                                 jobVariable.getName(),
                                 XMLAttributes.VARIABLE_VALUE,
                                 StringEscapeUtils.escapeXml11(jobVariable.getValue()));
        }
    }

}
