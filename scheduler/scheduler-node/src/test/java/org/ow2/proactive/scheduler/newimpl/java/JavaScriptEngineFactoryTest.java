package org.ow2.proactive.scheduler.newimpl.java;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.examples.WaitAndPrint;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;


public class JavaScriptEngineFactoryTest {

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
    public void replication_index() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("java");

        StringWriter output = new StringWriter();
        StringWriter error = new StringWriter();
        engine.getContext().setWriter(output);
        engine.getContext().setErrorWriter(new PrintWriter(error));

        engine.getContext().setAttribute("variables",
                singletonMap(SchedulerVars.JAVAENV_TASK_REPLICATION.toString(), 42),
                ScriptContext.ENGINE_SCOPE);

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