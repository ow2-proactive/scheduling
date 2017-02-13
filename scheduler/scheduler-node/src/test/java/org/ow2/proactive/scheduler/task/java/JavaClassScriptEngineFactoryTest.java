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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scheduler.task.utils.VariablesMap;


public class JavaClassScriptEngineFactoryTest {

    @Test
    public void executable_with_output_and_result() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("java");

        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();
        engine.getContext().setWriter(output);
        engine.getContext().setErrorWriter(new PrintWriter(error));

        Object result = engine.eval(WaitAndPrint.class.getName());

        assertNotEquals("", output.toString());
        assertNotEquals("", error.toString());
        assertNotEquals("", result);
        assertNotEquals("", engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).get("result"));
    }

    @Test
    public void executable_with_localspace() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("java");

        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();
        engine.getContext().setWriter(output);
        engine.getContext().setErrorWriter(new PrintWriter(error));

        String localSpacePath = new File(".").getAbsolutePath();

        engine.getContext().setAttribute(SchedulerConstants.DS_SCRATCH_BINDING_NAME,
                                         localSpacePath,
                                         ScriptContext.ENGINE_SCOPE);

        String result = (String) engine.eval(ReturnLocalSpace.class.getName());

        assertEquals(result, localSpacePath);
    }

    public static class ReturnLocalSpace extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return getLocalSpace();
        }
    }

    @Test
    public void replication_index() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("java");

        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();
        engine.getContext().setWriter(output);
        engine.getContext().setErrorWriter(new PrintWriter(error));

        VariablesMap variables = new VariablesMap();
        variables.getInheritedMap().put(SchedulerVars.PA_TASK_REPLICATION.toString(), 42);
        engine.getContext().setAttribute("variables", variables, ScriptContext.ENGINE_SCOPE);

        Object result = engine.eval(ReturnReplicationIndex.class.getName());

        assertEquals(42, result);
    }

    public static class ReturnReplicationIndex extends JavaExecutable {

        @Override
        public Serializable execute(TaskResult... results) throws Throwable {
            return getReplicationIndex();
        }
    }
}
