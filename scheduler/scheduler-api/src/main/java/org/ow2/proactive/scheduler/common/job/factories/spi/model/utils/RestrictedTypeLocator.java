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
package org.ow2.proactive.scheduler.common.job.factories.spi.model.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.util.ClassUtils;

import com.google.common.collect.ImmutableSet;


/**
 * @author ActiveEon Team
 * @since 2019-06-07
 */
public class RestrictedTypeLocator extends StandardTypeLocator {

    // A set of authorized types to prevent security breaches i.e executing maliciously code on the server
    private final Set<String> authorizedTypes = ImmutableSet.of("String",
                                                                "java.lang.String",
                                                                "Integer",
                                                                "java.lang.Integer",
                                                                "Boolean",
                                                                "java.lang.Boolean",
                                                                "Double",
                                                                "java.lang.Double",
                                                                "Long",
                                                                "java.lang.Long",
                                                                "Float",
                                                                "java.lang.Float",
                                                                "Math",
                                                                "java.lang.Math",
                                                                "org.codehaus.jackson.map.ObjectMapper",
                                                                "ObjectMapper",
                                                                "javax.xml.parsers.DocumentBuilderFactory",
                                                                "DocumentBuilderFactory",
                                                                "java.io.StringReader",
                                                                "StringReader",
                                                                "org.xml.sax.InputSource",
                                                                "InputSource",
                                                                "org.json.simple.parser.JSONParser",
                                                                "JSONParser",
                                                                "Date",
                                                                "java.util.Date",
                                                                "ImmutableSet",
                                                                "com.google.common.collect.ImmutableSet",
                                                                "ImmutableMap",
                                                                "com.google.common.collect.ImmutableMap",
                                                                "ImmutableList",
                                                                "com.google.common.collect.ImmutableList");

    private final ClassLoader classLoader;

    private final List<String> knownPackagePrefixes = new LinkedList<String>();

    public RestrictedTypeLocator() {
        this(ClassUtils.getDefaultClassLoader());
    }

    public RestrictedTypeLocator(ClassLoader classLoader) {
        this.classLoader = classLoader;
        registerImport("java.lang");
        registerImport("com.google.common.collect");
        registerImport("org.codehaus.jackson.map");
        registerImport("javax.xml.parsers");
        registerImport("org.xml.sax.map");
        registerImport("java.io");
        registerImport("org.json.simple.parser");
    }

    /**
     * Find a (possibly unqualified) type reference among a list of authorized types - first using the type name as-is,
     * then trying any registered prefixes if the type name cannot be found.
     * @param typeName the type to locate
     * @return the class object for the type
     * @throws EvaluationException if the type cannot be found
     */
    @Override
    public Class<?> findType(String typeName) throws EvaluationException {
        if (!authorizedTypes.contains(typeName)) {
            throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
        }
        return super.findType(typeName);
    }
}
