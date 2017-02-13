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
package org.ow2.proactive.scripting;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.Reader;

import javax.script.Bindings;

import org.junit.Test;


public class ScriptTest {

    @Test
    public void testScriptReturnsId() throws InvalidScriptException {
        String scriptString = "script";
        ScriptForTests script = new ScriptForTests(scriptString, "something");

        assertThat(script.getId(), is(scriptString));
    }

    @Test
    public void testScriptCanBeCreatedWithoutScriptEngines_Script_Inlined() throws Exception {
        ScriptForTests script = new ScriptForTests("println toto", "mySuperNonExistingScriptEngine");

        assertNull(script.createScriptEngine());

        ScriptResult<Object> result = script.execute();

        assertNotNull(result.getException());
    }

    @Test
    public void testScriptCanBeCreatedWithoutScriptEngines_Script_File() throws Exception {
        ScriptForTests script = new ScriptForTests(File.createTempFile("script", ".blahblah"));

        assertNull(script.createScriptEngine());

        ScriptResult<Object> result = script.execute();

        assertNotNull(result.getException());
    }

    @Test
    public void testJavaScriptEngine_Script_Inlined() throws Exception {
        SimpleScript script = new SimpleScript("1+1", "javascript");

        assertNotNull(script.createScriptEngine());

        ScriptResult<Object> result = script.execute();

        assertNull(result.getException());
    }

    @Test
    public void testJavaScriptEngine_Script_File() throws Exception {
        SimpleScript script = new SimpleScript(File.createTempFile("script", ".js"), new String[0]);

        assertNotNull(script.createScriptEngine());

        ScriptResult<Object> result = script.execute();

        assertNull(result.getException());
    }

    class ScriptForTests extends Script<Object> {

        @Override
        protected String getDefaultScriptName() {
            return "TestScript";
        }

        public ScriptForTests(String script, String engineName) throws InvalidScriptException {
            super(script, engineName);
        }

        public ScriptForTests(File file) throws InvalidScriptException {
            super(file);
        }

        @Override
        protected Reader getReader() {
            return null;
        }

        @Override
        protected void prepareSpecialBindings(Bindings bindings) {

        }

        @Override
        protected ScriptResult<Object> getResult(Object evalResult, Bindings bindings) {
            return null;
        }
    }
}
