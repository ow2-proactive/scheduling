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
package org.ow2.proactive.scheduler.task.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;


public class JavaClassScriptEngineFactory implements ScriptEngineFactory {

    public static final String JAVA_CLASS_SCRIPT_ENGINE_NAME = "java";

    private static final Map<String, Object> PARAMETERS = new HashMap<>();

    static {
        PARAMETERS.put(ScriptEngine.NAME, JAVA_CLASS_SCRIPT_ENGINE_NAME);
        PARAMETERS.put(ScriptEngine.ENGINE, JAVA_CLASS_SCRIPT_ENGINE_NAME);
        PARAMETERS.put(ScriptEngine.ENGINE_VERSION, "1.0");
        PARAMETERS.put(ScriptEngine.LANGUAGE, "Java");
        PARAMETERS.put(ScriptEngine.LANGUAGE_VERSION, System.getProperty("java.version"));
    }

    @Override
    public String getEngineName() {
        return "java";
    }

    @Override
    public String getEngineVersion() {
        return "1.0";
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList(".java");
    }

    @Override
    public List<String> getMimeTypes() {
        return Collections.singletonList("application/x-java");
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList(JAVA_CLASS_SCRIPT_ENGINE_NAME);
    }

    @Override
    public String getLanguageName() {
        return "Java";
    }

    @Override
    public String getLanguageVersion() {
        return (String) PARAMETERS.get(ScriptEngine.LANGUAGE_VERSION);
    }

    @Override
    public Object getParameter(String key) {
        return PARAMETERS.get(key);
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return null;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "System.out.println(" + toDisplay + ");";
    }

    @Override
    public String getProgram(String... statements) {
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new JavaClassScriptEngine();
    }

}
