/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.newimpl.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;


public class JavaScriptEngineFactory implements ScriptEngineFactory {

    private static final Map<String, Object> PARAMETERS = new HashMap<String, Object>();

    static {
        PARAMETERS.put(ScriptEngine.NAME, "java");
        PARAMETERS.put(ScriptEngine.ENGINE, "java");
        PARAMETERS.put(ScriptEngine.ENGINE_VERSION, "1.0");
        PARAMETERS.put(ScriptEngine.LANGUAGE, "java");
        PARAMETERS.put(ScriptEngine.LANGUAGE_VERSION, "1.7");
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
        return Collections.singletonList("java");
    }

    @Override
    public String getLanguageName() {
        return "java";
    }

    @Override
    public String getLanguageVersion() {
        return "1.7";
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
        return new JavaScriptEngine();
    }
}
